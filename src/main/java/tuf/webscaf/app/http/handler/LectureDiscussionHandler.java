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
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionEntity;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLectureDiscussionRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "lectureDiscussionHandler")
@Component
public class LectureDiscussionHandler {

    @Autowired
    CustomResponse appresponse;

    @Autowired
    LectureDiscussionRepository lectureDiscussionRepository;

    @Autowired
    SlaveLectureDiscussionRepository slaveLectureDiscussionRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

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
            Flux<SlaveLectureDiscussionEntity> slaveLectureDiscussionGradingTypeFlux = slaveLectureDiscussionRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable, searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveLectureDiscussionGradingTypeFlux
                    .collectList()
                    .flatMap(LectureDiscussionEntity -> slaveLectureDiscussionRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (LectureDiscussionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", LectureDiscussionEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveLectureDiscussionEntity> slaveLectureDiscussionGradingTypeFlux = slaveLectureDiscussionRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveLectureDiscussionGradingTypeFlux
                    .collectList()
                    .flatMap(LectureDiscussionEntity -> slaveLectureDiscussionRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (LectureDiscussionEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", LectureDiscussionEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID LectureDiscussionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveLectureDiscussionRepository.findByUuidAndDeletedAtIsNull(LectureDiscussionUUID)
                .flatMap(categoryEntity -> responseSuccessMsg("Record Fetched Successfully", categoryEntity))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_store")
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

                    LectureDiscussionEntity lectureDiscussionEntity = LectureDiscussionEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
                            .startDate(LocalDateTime.parse(value.getFirst("startDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                            .endDate(LocalDateTime.parse(value.getFirst("endDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
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

                    return lectureDiscussionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(lectureDiscussionEntity.getName())
                            .flatMap(checkName -> responseInfoMsg("Name Already Exist"))
                            .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", lectureDiscussionEntity.getCourseSubjectUUID())
                                    .flatMap(courseSubjectJson -> apiCallService.getUUID(courseSubjectJson)
                                            .flatMap(courseSubject -> lectureDiscussionRepository.save(lectureDiscussionEntity)
                                                    .flatMap(saveEntity -> responseSuccessMsg("Record Stored Successfully!", saveEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to Store Record, There is something wrong please try again!"))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Store Record.Please Contact Developer."))
                                            ))
                                    .switchIfEmpty(responseInfoMsg("Course Subject Does not Exist"))
                                    .onErrorResume(ex -> responseErrorMsg("Course Subject Does not Exist.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID LectureDiscussionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> lectureDiscussionRepository.findByUuidAndDeletedAtIsNull(LectureDiscussionUUID)
                        .flatMap(previousEntity -> {

                            LectureDiscussionEntity updatedLectureDiscussionEntity = LectureDiscussionEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .name(value.getFirst("name").trim())
                                    .description(value.getFirst("description").trim())
                                    .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
                                    .startDate(LocalDateTime.parse(value.getFirst("startDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                                    .endDate(LocalDateTime.parse(value.getFirst("endDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss")))
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdBy(previousEntity.getCreatedBy())
                                    .createdAt(previousEntity.getCreatedAt())
                                    .updatedBy(UUID.fromString(userUUID))
                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
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

                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousEntity.setReqDeletedIP(reqIp);
                            previousEntity.setReqDeletedPort(reqPort);
                            previousEntity.setReqDeletedBrowser(reqBrowser);
                            previousEntity.setReqDeletedOS(reqOs);
                            previousEntity.setReqDeletedDevice(reqDevice);
                            previousEntity.setReqDeletedReferer(reqReferer);


                            return lectureDiscussionRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedLectureDiscussionEntity.getName(), LectureDiscussionUUID)
                                    .flatMap(checkName -> responseInfoMsg("Name Already Exist"))
                                    .switchIfEmpty(Mono.defer(() -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", updatedLectureDiscussionEntity.getCourseSubjectUUID())
                                            .flatMap(courseSubjectJson -> apiCallService.getUUID(courseSubjectJson)
                                                    .flatMap(courseSubject -> lectureDiscussionRepository.save(previousEntity)
                                                            .then(lectureDiscussionRepository.save(updatedLectureDiscussionEntity))
                                                            .flatMap(saveEntity -> responseSuccessMsg("Record Updated Successfully!", saveEntity))
                                                            .switchIfEmpty(responseInfoMsg("Unable to Update Record, There is something wrong please try again!"))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer.")))).switchIfEmpty(responseInfoMsg("Course Subject Does not Exist"))
                                            .onErrorResume(ex -> responseErrorMsg("Course Subject Does not Exist.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID LectureDiscussionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return lectureDiscussionRepository.findByUuidAndDeletedAtIsNull(LectureDiscussionUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                LectureDiscussionEntity updatedLectureDiscussionEntity = LectureDiscussionEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
                                        .courseSubjectUUID(previousEntity.getCourseSubjectUUID())
                                        .startDate(previousEntity.getStartDate())
                                        .endDate(previousEntity.getEndDate())
                                        .status(status == true ? true : false)
                                        .createdBy(previousEntity.getCreatedBy())
                                        .createdAt(previousEntity.getCreatedAt())
                                        .updatedBy(UUID.fromString(userUUID))
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
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

                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);

                                return lectureDiscussionRepository.save(previousEntity)
                                        .then(lectureDiscussionRepository.save(updatedLectureDiscussionEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussions_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID LectureDiscussionUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return lectureDiscussionRepository.findByUuidAndDeletedAtIsNull(LectureDiscussionUUID)
                .flatMap(lectureDiscussionEntity -> {

                            lectureDiscussionEntity.setDeletedBy(UUID.fromString(userUUID));
                            lectureDiscussionEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            lectureDiscussionEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            lectureDiscussionEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            lectureDiscussionEntity.setReqDeletedIP(reqIp);
                            lectureDiscussionEntity.setReqDeletedPort(reqPort);
                            lectureDiscussionEntity.setReqDeletedBrowser(reqBrowser);
                            lectureDiscussionEntity.setReqDeletedOS(reqOs);
                            lectureDiscussionEntity.setReqDeletedDevice(reqDevice);
                            lectureDiscussionEntity.setReqDeletedReferer(reqReferer);

                            return lectureDiscussionRepository.save(lectureDiscussionEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                        }
                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
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
