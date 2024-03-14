package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPollOptionVoteEntity;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPollOptionRepository;
import tuf.webscaf.app.dbContext.master.repositry.ForumPollOptionVoteRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPollOptionVoteEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveForumPollOptionVoteRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Tag(name = "lectureDiscussionPollOptionVoteHandler")
@Component
public class LectureDiscussionPollOptionVoteHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    ForumPollOptionVoteRepository forumPollOptionVoteRepository;

    @Autowired
    SlaveForumPollOptionVoteRepository slaveForumPollOptionVoteRepository;

    @Autowired
    LectureDiscussionPollOptionRepository lectureDiscussionPollOptionRepository;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
        if (size > 100) {
            size = 100;
        }
        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
        int page = pageRequest - 1;

        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
        Sort.Direction direction;
        switch (d.toLowerCase()) {
            case "asc":
                direction = Sort.Direction.ASC;
                break;
            case "desc":
                direction = Sort.Direction.DESC;
                break;
            default:
                direction = Sort.Direction.ASC;
        }

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();


        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty()) {

            Flux<SlaveLectureDiscussionPollOptionVoteEntity> slaveForumPollOptionVoteFlux = slaveForumPollOptionVoteRepository
                    .findAllByDeletedAtIsNullAndStatus(pageable, Boolean.valueOf(status));

            return slaveForumPollOptionVoteFlux
                    .collectList()
                    .flatMap(slaveForumPollOptionVoteEntities -> slaveForumPollOptionVoteRepository
                            .countByDeletedAtIsNullAndStatus(Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveForumPollOptionVoteEntities.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveForumPollOptionVoteEntities, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveLectureDiscussionPollOptionVoteEntity> slaveForumPollOptionVoteFlux = slaveForumPollOptionVoteRepository
                    .findAllByDeletedAtIsNull(pageable);
            return slaveForumPollOptionVoteFlux
                    .collectList()
                    .flatMap(slaveForumPollOptionVoteEntities -> slaveForumPollOptionVoteRepository
                            .countByDeletedAtIsNull()
                            .flatMap(count -> {
                                if (slaveForumPollOptionVoteEntities.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveForumPollOptionVoteEntities, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID forumPollOptionVoteUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveForumPollOptionVoteRepository.findByUuidAndDeletedAtIsNull(forumPollOptionVoteUUID)
                .flatMap(slaveForumPollOptionVoteEntities -> responseSuccessMsg("Record Fetched Successfully", slaveForumPollOptionVoteEntities))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {
        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User");
            }
        }

        return serverRequest.formData()
                .flatMap(value -> {

                    LectureDiscussionPollOptionVoteEntity entity = LectureDiscussionPollOptionVoteEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .pollOptionUUID(UUID.fromString(value.getFirst("pollOptionUUID")))
                            .votedByUserUUID(UUID.fromString(value.getFirst("votedByUserUUID")))
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .createdBy(UUID.fromString(userUUID))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    // check forum poll option exist
                    return lectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(entity.getPollOptionUUID())
                            .flatMap(forumPollOptionEntity -> forumPollOptionVoteRepository.save(entity)
                                            .flatMap(saveLectureDiscussionPollOptionVoteEntity -> responseSuccessMsg("Record Stored Successfully", saveLectureDiscussionPollOptionVoteEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
                            ).switchIfEmpty(responseInfoMsg("Forum Poll option record does not exist."))
                            .onErrorResume(err -> responseErrorMsg("Forum Poll option record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID forumPollOptionVoteUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User");
            }
        }

        return serverRequest.formData()
                .flatMap(value -> forumPollOptionVoteRepository.findByUuidAndDeletedAtIsNull(forumPollOptionVoteUUID)
                        .flatMap(previousEntity -> {

                            LectureDiscussionPollOptionVoteEntity updatedEntity = LectureDiscussionPollOptionVoteEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .pollOptionUUID(UUID.fromString(value.getFirst("pollOptionUUID")))
                                    .votedByUserUUID(UUID.fromString(value.getFirst("votedByUserUUID")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousEntity.getCreatedAt())
                                    .createdBy(previousEntity.getCreatedBy())
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                    .updatedBy(UUID.fromString(userUUID))
                                    .reqCreatedIP(previousEntity.getReqCreatedIP())
                                    .reqCreatedPort(previousEntity.getReqCreatedPort())
                                    .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
                                    .reqCreatedOS(previousEntity.getReqCreatedOS())
                                    .reqCreatedDevice(previousEntity.getReqCreatedDevice())
                                    .reqCreatedReferer(previousEntity.getReqCreatedReferer())
                                    .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                    .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                    .reqUpdatedIP(reqIp)
                                    .reqUpdatedPort(reqPort)
                                    .reqUpdatedBrowser(reqBrowser)
                                    .reqUpdatedOS(reqOs)
                                    .reqUpdatedDevice(reqDevice)
                                    .reqUpdatedReferer(reqReferer)
                                    .build();

                            previousEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousEntity.setReqDeletedIP(reqIp);
                            previousEntity.setReqDeletedPort(reqPort);
                            previousEntity.setReqDeletedBrowser(reqBrowser);
                            previousEntity.setReqDeletedOS(reqOs);
                            previousEntity.setReqDeletedDevice(reqDevice);
                            previousEntity.setReqDeletedReferer(reqReferer);

                            // check forum poll option exist
                            return lectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(updatedEntity.getPollOptionUUID())
                                    .flatMap(forumPollOptionEntity -> forumPollOptionVoteRepository.save(previousEntity)
                                            .then(forumPollOptionVoteRepository.save(updatedEntity))
                                            .flatMap(slaveForumPollOptionVoteEntities -> responseSuccessMsg("Record Updated Successfully", slaveForumPollOptionVoteEntities))
                                            .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
                                    ).switchIfEmpty(responseInfoMsg("Forum Poll option record does not exist."))
                                    .onErrorResume(err -> responseErrorMsg("Forum Poll option record does not exist. Please contact developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID forumPollOptionVoteUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User");
            }
        }
        return serverRequest.formData()
                .flatMap(value -> {

                    boolean status = Boolean.parseBoolean(value.getFirst("status"));

                    return forumPollOptionVoteRepository.findByUuidAndDeletedAtIsNull(forumPollOptionVoteUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                LectureDiscussionPollOptionVoteEntity lectureDiscussionPollOptionVoteEntity = LectureDiscussionPollOptionVoteEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .pollOptionUUID(previousEntity.getPollOptionUUID())
                                        .votedByUserUUID(previousEntity.getVotedByUserUUID())
                                        .status(status == true ? true : false)
                                        .createdAt(previousEntity.getCreatedAt())
                                        .createdBy(previousEntity.getCreatedBy())
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userUUID))
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                // update status
                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);

                                return forumPollOptionVoteRepository.save(previousEntity)
                                        .then(forumPollOptionVoteRepository.save(lectureDiscussionPollOptionVoteEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-option-votes_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID forumPollOptionVoteUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
        String userUUID = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userUUID == null) {
            return responseWarningMsg("Unknown User");
        } else {
            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                return responseWarningMsg("Unknown User");
            }
        }

        return forumPollOptionVoteRepository.findByUuidAndDeletedAtIsNull(forumPollOptionVoteUUID)
                .flatMap(lectureDiscussionPollOptionVoteEntity -> {

                    lectureDiscussionPollOptionVoteEntity.setDeletedBy(UUID.fromString(userUUID));
                    lectureDiscussionPollOptionVoteEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    lectureDiscussionPollOptionVoteEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    lectureDiscussionPollOptionVoteEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedIP(reqIp);
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedPort(reqPort);
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedBrowser(reqBrowser);
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedOS(reqOs);
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedDevice(reqDevice);
                    lectureDiscussionPollOptionVoteEntity.setReqDeletedReferer(reqReferer);

                    return forumPollOptionVoteRepository.save(lectureDiscussionPollOptionVoteEntity)
                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }


    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }
}
