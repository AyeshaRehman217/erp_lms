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
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPollOptionEntity;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPollOptionRepository;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPostRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPollOptionEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLectureDiscussionPollOptionRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Tag(name = "lectureDiscussionPollOptionHandler")
@Component
public class LectureDiscussionPollOptionHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    LectureDiscussionPollOptionRepository lectureDiscussionPollOptionRepository;

    @Autowired
    LectureDiscussionPostRepository lectureDiscussionPostRepository;

    @Autowired
    SlaveLectureDiscussionPollOptionRepository slaveLectureDiscussionPollOptionRepository;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_index")
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

            Flux<SlaveLectureDiscussionPollOptionEntity> slaveLectureDiscussionPollOptionFlux = slaveLectureDiscussionPollOptionRepository
                    .findAllByTitleContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status));

            return slaveLectureDiscussionPollOptionFlux
                    .collectList()
                    .flatMap(slaveLectureDiscussionPollOptionEntities -> slaveLectureDiscussionPollOptionRepository
                            .countByTitleContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (slaveLectureDiscussionPollOptionEntities.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveLectureDiscussionPollOptionEntities, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveLectureDiscussionPollOptionEntity> slaveLectureDiscussionPollOptionFlux = slaveLectureDiscussionPollOptionRepository
                    .findAllByTitleContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord);
            return slaveLectureDiscussionPollOptionFlux
                    .collectList()
                    .flatMap(slaveLectureDiscussionPollOptionEntities -> slaveLectureDiscussionPollOptionRepository
                            .countByTitleContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord)
                            .flatMap(count -> {
                                if (slaveLectureDiscussionPollOptionEntities.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", slaveLectureDiscussionPollOptionEntities, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-options_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID lectureDiscussionPollOptionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveLectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPollOptionUUID)
                .flatMap(slaveLectureDiscussionPollOptionEntities -> responseSuccessMsg("Record Fetched Successfully", slaveLectureDiscussionPollOptionEntities))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-options_store")
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

                    LectureDiscussionPollOptionEntity entity = LectureDiscussionPollOptionEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .title(value.getFirst("title").trim())
                            .lectureDiscussionPostUUID(UUID.fromString(value.getFirst("lectureDiscussionPostUUID")))
                            .date(LocalDateTime.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
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

                    return lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(entity.getLectureDiscussionPostUUID())
                            //  check title is unique
                            .flatMap(lectureDiscussionPostEntity -> lectureDiscussionPollOptionRepository.findFirstByTitleIgnoreCaseAndDeletedAtIsNull(entity.getTitle())
                                    .flatMap(lectureDiscussionPollOptionEntity -> responseInfoMsg("Title already exist"))
                                    .switchIfEmpty(Mono.defer(() -> lectureDiscussionPollOptionRepository.save(entity)
                                            .flatMap(saveLectureDiscussionPollOptionEntity -> responseSuccessMsg("Record Stored Successfully", saveLectureDiscussionPollOptionEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
                                    ))
                            ).switchIfEmpty(responseInfoMsg("LectureDiscussion Post record does not exist."))
                            .onErrorResume(err -> responseErrorMsg("LectureDiscussion Post record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-options_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID lectureDiscussionPollOptionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> lectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPollOptionUUID)
                        .flatMap(previousEntity -> {

                            LectureDiscussionPollOptionEntity updatedEntity = LectureDiscussionPollOptionEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .title(value.getFirst("title").trim())
                                    .lectureDiscussionPostUUID(UUID.fromString(value.getFirst("lectureDiscussionPostUUID")))
                                    .date(LocalDateTime.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
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


                            return lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(updatedEntity.getLectureDiscussionPostUUID())
                                    //  check title is unique
                                    .flatMap(lectureDiscussionPostEntity -> lectureDiscussionPollOptionRepository.findFirstByTitleIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getTitle(), lectureDiscussionPollOptionUUID)
                                            .flatMap(titleExists -> responseInfoMsg("Title Already Exists"))
                                            .switchIfEmpty(Mono.defer(() -> lectureDiscussionPollOptionRepository.save(previousEntity)
                                                    .then(lectureDiscussionPollOptionRepository.save(updatedEntity))
                                                    .flatMap(slaveLectureDiscussionPollOptionEntities -> responseSuccessMsg("Record Updated Successfully", slaveLectureDiscussionPollOptionEntities))
                                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("LectureDiscussion Post record does not exist."))
                                    .onErrorResume(err -> responseErrorMsg("LectureDiscussion Post record does not exist. Please contact developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-options_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID lectureDiscussionPollOptionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return lectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPollOptionUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                LectureDiscussionPollOptionEntity lectureDiscussionPollOptionEntity = LectureDiscussionPollOptionEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .title(previousEntity.getTitle())
                                        .lectureDiscussionPostUUID(previousEntity.getLectureDiscussionPostUUID())
                                        .date(previousEntity.getDate())
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

                                return lectureDiscussionPollOptionRepository.save(previousEntity)
                                        .then(lectureDiscussionPollOptionRepository.save(lectureDiscussionPollOptionEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-poll-options_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID lectureDiscussionPollOptionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return lectureDiscussionPollOptionRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPollOptionUUID)
                .flatMap(lectureDiscussionPollOptionEntity -> {

                    lectureDiscussionPollOptionEntity.setDeletedBy(UUID.fromString(userUUID));
                    lectureDiscussionPollOptionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    lectureDiscussionPollOptionEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    lectureDiscussionPollOptionEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    lectureDiscussionPollOptionEntity.setReqDeletedIP(reqIp);
                    lectureDiscussionPollOptionEntity.setReqDeletedPort(reqPort);
                    lectureDiscussionPollOptionEntity.setReqDeletedBrowser(reqBrowser);
                    lectureDiscussionPollOptionEntity.setReqDeletedOS(reqOs);
                    lectureDiscussionPollOptionEntity.setReqDeletedDevice(reqDevice);
                    lectureDiscussionPollOptionEntity.setReqDeletedReferer(reqReferer);

                    return lectureDiscussionPollOptionRepository.save(lectureDiscussionPollOptionEntity)
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
