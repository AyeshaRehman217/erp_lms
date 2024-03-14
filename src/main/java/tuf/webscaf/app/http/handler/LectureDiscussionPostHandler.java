package tuf.webscaf.app.http.handler;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostEntity;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPostRepository;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPostEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLectureDiscussionPostRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Tag(name = "lectureDiscussionPostHandler")
@Component
public class LectureDiscussionPostHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    LectureDiscussionPostRepository lectureDiscussionPostRepository;

    @Autowired
    SlaveLectureDiscussionPostRepository slaveLectureDiscussionPostRepository;

    @Autowired
    LectureDiscussionRepository lectureDiscussionRepository;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Autowired
    ApiCallService apiCallService;

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_index")
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

            Flux<SlaveLectureDiscussionPostEntity> slaveLecturePostFlux = slaveLectureDiscussionPostRepository
                    .findAllBySubjectContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                            (pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveLecturePostFlux
                    .collectList()
                    .flatMap(gradingMethodEntity -> slaveLectureDiscussionPostRepository
                            .countBySubjectContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (gradingMethodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingMethodEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveLectureDiscussionPostEntity> slaveLecturePostFlux = slaveLectureDiscussionPostRepository
                    .findAllBySubjectContainingIgnoreCaseAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveLecturePostFlux
                    .collectList()
                    .flatMap(gradingMethodEntity -> slaveLectureDiscussionPostRepository
                            .countBySubjectContainingIgnoreCaseAndDeletedAtIsNullOrMessageContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (gradingMethodEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingMethodEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID lectureDiscussionPostUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveLectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Fetched Successfully", gradingMethodEntity))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_store")
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

                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();

                    LectureDiscussionPostEntity entity = LectureDiscussionPostEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .subject(value.getFirst("subject").trim())
                            .message(value.getFirst("message").trim())
                            .lectureDiscussionUUID(UUID.fromString(value.getFirst("lectureDiscussionUUID").trim()))
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .isPoll(Boolean.valueOf(value.getFirst("isPoll")))
                            .date(LocalDateTime.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
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

                    //check if lecture discussion record exists or not
                    return lectureDiscussionRepository.findByUuidAndDeletedAtIsNull(entity.getLectureDiscussionUUID())
                            //check if subject record exists or not
                            .flatMap(lectureDiscussionEntity -> lectureDiscussionPostRepository.findFirstBySubjectIgnoreCaseAndDeletedAtIsNull(entity.getSubject())
                                    .flatMap(lectureDiscussionPostEntity -> responseInfoMsg("Subject already exist"))
                                    .switchIfEmpty(Mono.defer(() -> lectureDiscussionPostRepository.save(entity)
                                            .flatMap(lectureDiscussionPostEntity -> responseSuccessMsg("Record Stored Successfully", lectureDiscussionPostEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store Record.There is something wrong please try again"))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                    ))
                            ).switchIfEmpty(responseInfoMsg("Lecture Discussion record does not exist"))
                            .onErrorResume(ex -> responseErrorMsg("Lecture Discussion record does not exist. Please contact developer"));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID lectureDiscussionPostUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                        .flatMap(previousEntity -> {

                            MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();

                            LectureDiscussionPostEntity updatedEntity = LectureDiscussionPostEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .subject(value.getFirst("subject").trim())
                                    .message(value.getFirst("message").trim())
                                    .lectureDiscussionUUID(UUID.fromString(value.getFirst("lectureDiscussionUUID").trim()))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .isPoll(Boolean.valueOf(value.getFirst("isPoll")))
                                    .date(LocalDateTime.parse(value.getFirst("date"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
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


                            return lectureDiscussionRepository.findByUuidAndDeletedAtIsNull(updatedEntity.getLectureDiscussionUUID())
                                    .flatMap(lectureDiscussionEntity -> lectureDiscussionPostRepository.findFirstBySubjectIgnoreCaseAndDeletedAtIsNull(updatedEntity.getSubject())
                                            .flatMap(lectureDiscussionPostEntity -> responseInfoMsg("Subject already exist"))
                                            .switchIfEmpty(Mono.defer(() -> lectureDiscussionPostRepository.save(previousEntity)
                                                    .then(lectureDiscussionPostRepository.save(updatedEntity))
                                                    .flatMap(lectureDiscussionPostEntity -> responseSuccessMsg("Record Updated Successfully", lectureDiscussionPostEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Update Record.There is something wrong please try again"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                            ))
                                    ).switchIfEmpty(responseInfoMsg("Lecture Discussion record does not exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Lecture Discussion record does not exist. Please contact developer"));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID lectureDiscussionPostUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                LectureDiscussionPostEntity lectureDiscussionPostEntity = LectureDiscussionPostEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .subject(previousEntity.getSubject())
                                        .message(previousEntity.getMessage())
                                        .lectureDiscussionUUID(previousEntity.getLectureDiscussionUUID())
                                        .isPoll(previousEntity.getIsPoll())
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

                                return lectureDiscussionPostRepository.save(previousEntity)
                                        .then(lectureDiscussionPostRepository.save(lectureDiscussionPostEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-posts_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID lectureDiscussionPostUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                //check if Grading Method exists in Grades
                .flatMap(lectureDiscussionPostEntity -> {

                    lectureDiscussionPostEntity.setDeletedBy(UUID.fromString(userUUID));
                    lectureDiscussionPostEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    lectureDiscussionPostEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    lectureDiscussionPostEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    lectureDiscussionPostEntity.setReqDeletedIP(reqIp);
                    lectureDiscussionPostEntity.setReqDeletedPort(reqPort);
                    lectureDiscussionPostEntity.setReqDeletedBrowser(reqBrowser);
                    lectureDiscussionPostEntity.setReqDeletedOS(reqOs);
                    lectureDiscussionPostEntity.setReqDeletedDevice(reqDevice);
                    lectureDiscussionPostEntity.setReqDeletedReferer(reqReferer);

                    return lectureDiscussionPostRepository.save(lectureDiscussionPostEntity)
                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));
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
