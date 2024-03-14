//package tuf.webscaf.app.http.handler;
//
//import io.swagger.v3.oas.annotations.tags.Tag;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.domain.Sort;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Component;
//import org.springframework.web.reactive.function.server.ServerRequest;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//import tuf.webscaf.app.dbContext.master.entity.GradeEntity;
//import tuf.webscaf.app.dbContext.master.repositry.GradeRepository;
//import tuf.webscaf.app.dbContext.master.repositry.GradingTypeRepository;
//import tuf.webscaf.app.dbContext.master.repositry.QuizRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveGradeEntity;
//import tuf.webscaf.app.dbContext.slave.repositry.SlaveGradeRepository;
//import tuf.webscaf.app.verification.module.AuthHasPermission;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.UUID;
//
//@Tag(name = "gradeHandler")
//@Component
//public class GradeHandler {
//    @Autowired
//    CustomResponse appresponse;
//
//    @Autowired
//    GradeRepository gradeRepository;
//
//    @Autowired
//    GradingTypeRepository gradeTypeRepository;
//
//    @Autowired
//    SlaveGradeRepository slaveGradeRepository;
//
//    @Autowired
//    QuizRepository quizRepository;
//
//    @Value("${server.zone}")
//    private String zone;
//
//    @AuthHasPermission(value = "lms_api_v1_grades_index")
//    public Mono<ServerResponse> index(ServerRequest serverRequest) {
//
//        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();
//
//        int size = serverRequest.queryParam("s").map(Integer::parseInt).orElse(10);
//        if (size > 100) {
//            size = 100;
//        }
//        int pageRequest = serverRequest.queryParam("p").map(Integer::parseInt).orElse(1);
//        int page = pageRequest - 1;
//
//        String d = serverRequest.queryParam("d").map(String::toString).orElse("asc");
//        Sort.Direction direction;
//        switch (d.toLowerCase()) {
//            case "asc":
//                direction = Sort.Direction.ASC;
//                break;
//            case "desc":
//                direction = Sort.Direction.DESC;
//                break;
//            default:
//                direction = Sort.Direction.ASC;
//        }
//
//        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("createdAt");
//        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();
//
//
//        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));
//
//        if (!status.isEmpty()) {
//            Flux<SlaveGradeEntity> slaveGradeEntityFlux = slaveGradeRepository
//                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
//                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
//            return slaveGradeEntityFlux
//                    .collectList()
//                    .flatMap(gradeEntity -> slaveGradeRepository
//                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
//                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
//                            .flatMap(count -> {
//                                if (gradeEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradeEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        } else {
//            Flux<SlaveGradeEntity> slaveGradeEntityFlux = slaveGradeRepository
//                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//            return slaveGradeEntityFlux
//                    .collectList()
//                    .flatMap(gradeEntity -> slaveGradeRepository
//                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
//                            .flatMap(count -> {
//                                if (gradeEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradeEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        }
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grades_show")
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        UUID gradeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//
//        return slaveGradeRepository.findByUuidAndDeletedAtIsNull(gradeUUID)
//                .flatMap(gradeEntity -> responseSuccessMsg("Record Fetched Successfully", gradeEntity))
//                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grades_store")
//    public Mono<ServerResponse> store(ServerRequest serverRequest) {
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User");
//            }
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> {
//
//                    GradeEntity gradeEntity = GradeEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name").trim())
//                            .criteria(value.getFirst("criteria").trim())
//                            .marks(value.getFirst("marks").trim())
//                            .description(value.getFirst("description").trim())
//                            .gradingTypeUUID(UUID.fromString(value.getFirst("gradingTypeUUID").trim()))
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdBy(UUID.fromString(userUUID))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
//                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
//                            .reqCreatedIP(reqIp)
//                            .reqCreatedPort(reqPort)
//                            .reqCreatedBrowser(reqBrowser)
//                            .reqCreatedOS(reqOs)
//                            .reqCreatedDevice(reqDevice)
//                            .reqCreatedReferer(reqReferer)
//                            .build();
//
//                    // check if Name is Unique
//                    return gradeRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(gradeEntity.getName())
//                            .flatMap(nameExists -> responseInfoMsg("Name Already Exists"))
//                            //check if Grade Type Exists
//                            .switchIfEmpty(Mono.defer(() -> gradeTypeRepository.findByUuidAndDeletedAtIsNull(gradeEntity.getGradingTypeUUID())
//                                    .flatMap(gradeType -> gradeRepository.save(gradeEntity)
//                                            .flatMap(saveGradeEntity -> responseSuccessMsg("Record Stored Successfully", saveGradeEntity))
//                                            .switchIfEmpty(responseInfoMsg("Unable to Store record. There is something wrong please try again."))
//                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store record. Please contact developer.")))
//                                    .switchIfEmpty(responseInfoMsg("Grade Type Does not Exist."))
//                                    .onErrorResume(ex -> responseErrorMsg("Grade Type Does not Exist.Please Contact Developer."))
//                            ));
//                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grades_update")
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        UUID gradeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User");
//            }
//        }
//
//        return serverRequest.formData()
//                .flatMap(value -> gradeRepository.findByUuidAndDeletedAtIsNull(gradeUUID)
//                                .flatMap(previousEntity -> {
//
//                                    GradeEntity updatedEntity = GradeEntity.builder()
//                                            .uuid(previousEntity.getUuid())
//                                            .name(value.getFirst("name").trim())
//                                            .criteria(value.getFirst("criteria").trim())
//                                            .marks(value.getFirst("marks").trim())
//                                            .description(value.getFirst("description").trim())
//                                            .gradingTypeUUID(UUID.fromString(value.getFirst("gradingTypeUUID").trim()))
//                                            .status(Boolean.valueOf(value.getFirst("status")))
//                                            .createdAt(previousEntity.getCreatedAt())
//                                            .createdBy(previousEntity.getCreatedBy())
//                                            .updatedBy(UUID.fromString(userUUID))
//                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                            .reqCreatedIP(previousEntity.getReqCreatedIP())
//                                            .reqCreatedPort(previousEntity.getReqCreatedPort())
//                                            .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
//                                            .reqCreatedOS(previousEntity.getReqCreatedOS())
//                                            .reqCreatedDevice(previousEntity.getReqCreatedDevice())
//                                            .reqCreatedReferer(previousEntity.getReqCreatedReferer())
//                                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
//                                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
//                                            .reqUpdatedIP(reqIp)
//                                            .reqUpdatedPort(reqPort)
//                                            .reqUpdatedBrowser(reqBrowser)
//                                            .reqUpdatedOS(reqOs)
//                                            .reqUpdatedDevice(reqDevice)
//                                            .reqUpdatedReferer(reqReferer)
//                                            .build();
//
//                                    previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                                    previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                    previousEntity.setReqDeletedIP(reqIp);
//                                    previousEntity.setReqDeletedPort(reqPort);
//                                    previousEntity.setReqDeletedBrowser(reqBrowser);
//                                    previousEntity.setReqDeletedOS(reqOs);
//                                    previousEntity.setReqDeletedDevice(reqDevice);
//                                    previousEntity.setReqDeletedReferer(reqReferer);
//
////                         check if Name is Unique
//                                    return gradeRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getName(), gradeUUID)
//                                            .flatMap(nameExists -> responseInfoMsg("Name Already Exists"))
//                                            //check if Grade Type Exists
//                                            .switchIfEmpty(Mono.defer(() -> gradeTypeRepository.findByUuidAndDeletedAtIsNull(updatedEntity.getGradingTypeUUID())
//                                                    .flatMap(gradeType -> gradeRepository.save(previousEntity)
//                                                            .then(gradeRepository.save(updatedEntity))
//                                                            .flatMap(gradeEntity -> responseSuccessMsg("Record Updated Successfully", gradeEntity))
//                                                            .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
//                                                            .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer.")))
//                                                    .switchIfEmpty(responseInfoMsg("Grade Type Does not Exist."))
//                                                    .onErrorResume(ex -> responseErrorMsg("Grade Type Does not Exist.Please Contact Developer."))
//                                            ));
//                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grades_status_update")
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        UUID gradeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User");
//            }
//        }
//        return serverRequest.formData()
//                .flatMap(value -> {
//                    boolean status = Boolean.parseBoolean(value.getFirst("status"));
//
//                    return gradeRepository.findByUuidAndDeletedAtIsNull(gradeUUID)
//                            .flatMap(previousEntity -> {
//                                // If status is not Boolean value
//                                if (status != false && status != true) {
//                                    return responseInfoMsg("Status must be Active or InActive");
//                                }
//
//                                // If already same status exist in database.
//                                if (((previousEntity.getStatus() ? true : false) == status)) {
//                                    return responseWarningMsg("Record already exist with same status");
//                                }
//
//                                GradeEntity gradeEntity = GradeEntity.builder()
//                                        .uuid(previousEntity.getUuid())
//                                        .name(previousEntity.getName())
//                                        .criteria(previousEntity.getCriteria())
//                                        .marks(previousEntity.getMarks())
//                                        .description(previousEntity.getDescription())
//                                        .gradingTypeUUID(previousEntity.getGradingTypeUUID())
//                                        .status(status == true ? true : false)
//                                        .createdAt(previousEntity.getCreatedAt())
//                                        .createdBy(previousEntity.getCreatedBy())
//                                        .updatedBy(UUID.fromString(userUUID))
//                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
//                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
//                                        .reqCreatedIP(previousEntity.getReqCreatedIP())
//                                        .reqCreatedPort(previousEntity.getReqCreatedPort())
//                                        .reqCreatedBrowser(previousEntity.getReqCreatedBrowser())
//                                        .reqCreatedOS(previousEntity.getReqCreatedOS())
//                                        .reqCreatedDevice(previousEntity.getReqCreatedDevice())
//                                        .reqCreatedReferer(previousEntity.getReqCreatedReferer())
//                                        .reqUpdatedIP(reqIp)
//                                        .reqUpdatedPort(reqPort)
//                                        .reqUpdatedBrowser(reqBrowser)
//                                        .reqUpdatedOS(reqOs)
//                                        .reqUpdatedDevice(reqDevice)
//                                        .reqUpdatedReferer(reqReferer)
//                                        .build();
//
//                                // update status
//                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                                previousEntity.setReqDeletedIP(reqIp);
//                                previousEntity.setReqDeletedPort(reqPort);
//                                previousEntity.setReqDeletedBrowser(reqBrowser);
//                                previousEntity.setReqDeletedOS(reqOs);
//                                previousEntity.setReqDeletedDevice(reqDevice);
//                                previousEntity.setReqDeletedReferer(reqReferer);
//
//                                return gradeRepository.save(previousEntity)
//                                        .then(gradeRepository.save(gradeEntity))
//                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
//                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
//                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
//                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grades_delete")
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID gradeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//        String userUUID = serverRequest.headers().firstHeader("auid");
//
//        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
//        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
//        String reqIp = serverRequest.headers().firstHeader("reqIp");
//        String reqPort = serverRequest.headers().firstHeader("reqPort");
//        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
//        String reqOs = serverRequest.headers().firstHeader("reqOs");
//        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
//        String reqReferer = serverRequest.headers().firstHeader("reqReferer");
//
//        if (userUUID == null) {
//            return responseWarningMsg("Unknown User");
//        } else {
//            if (!userUUID.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                return responseWarningMsg("Unknown User");
//            }
//        }
//
//        return gradeRepository.findByUuidAndDeletedAtIsNull(gradeUUID)
//                //check if Grading Type exists in Grades
//                .flatMap(gradeEntity -> quizRepository.findFirstByGradeUUIDAndDeletedAtIsNull(gradeEntity.getUuid())
//                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
//                        .switchIfEmpty(Mono.defer(() -> {
//
//                            gradeEntity.setDeletedBy(UUID.fromString(userUUID));
//                            gradeEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                            gradeEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
//                            gradeEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
//                            gradeEntity.setReqDeletedIP(reqIp);
//                            gradeEntity.setReqDeletedPort(reqPort);
//                            gradeEntity.setReqDeletedBrowser(reqBrowser);
//                            gradeEntity.setReqDeletedOS(reqOs);
//                            gradeEntity.setReqDeletedDevice(reqDevice);
//                            gradeEntity.setReqDeletedReferer(reqReferer);
//
//                            return gradeRepository.save(gradeEntity)
//                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
//                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                        }))
//                ).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> responseInfoMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.INFO,
//                        msg
//                )
//        );
//
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//
//        );
//    }
//
//    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.INFO,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                totalDataRowsWithFilter,
//                0L,
//                messages,
//                Mono.empty()
//
//        );
//    }
//
//
//    public Mono<ServerResponse> responseErrorMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.ERROR,
//                        msg
//                )
//        );
//
//        return appresponse.set(
//                HttpStatus.BAD_REQUEST.value(),
//                HttpStatus.BAD_REQUEST.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//
//    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg)
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//
//    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.SUCCESS,
//                        msg)
//        );
//
//        return appresponse.set(
//                HttpStatus.OK.value(),
//                HttpStatus.OK.name(),
//                null,
//                "eng",
//                "token",
//                totalDataRowsWithFilter,
//                0L,
//                messages,
//                Mono.just(entity)
//        );
//    }
//
//    public Mono<ServerResponse> responseWarningMsg(String msg) {
//        var messages = List.of(
//                new AppResponseMessage(
//                        AppResponse.Response.WARNING,
//                        msg)
//        );
//
//
//        return appresponse.set(
//                HttpStatus.UNPROCESSABLE_ENTITY.value(),
//                HttpStatus.UNPROCESSABLE_ENTITY.name(),
//                null,
//                "eng",
//                "token",
//                0L,
//                0L,
//                messages,
//                Mono.empty()
//        );
//    }
//}
