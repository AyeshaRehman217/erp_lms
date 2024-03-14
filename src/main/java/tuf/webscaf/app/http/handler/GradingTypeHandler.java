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
//import tuf.webscaf.app.dbContext.master.entity.GradingTypeEntity;
//import tuf.webscaf.app.dbContext.master.repositry.GradeRepository;
//import tuf.webscaf.app.dbContext.master.repositry.GradingTypeRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveGradingTypeEntity;
//import tuf.webscaf.app.dbContext.slave.repositry.SlaveGradingTypeRepository;
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
//@Tag(name = "gradingTypeHandler")
//@Component
//public class GradingTypeHandler {
//    @Autowired
//    CustomResponse appresponse;
//
//    @Autowired
//    GradingTypeRepository gradingTypeRepository;
//
//    @Autowired
//    GradeRepository gradeRepository;
//
//    @Autowired
//    SlaveGradingTypeRepository slaveGradingTypeRepository;
//
//    @Value("${server.zone}")
//    private String zone;
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_index")
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
//            Flux<SlaveGradingTypeEntity> slaveGradingTypeFlux = slaveGradingTypeRepository
//                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
//                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
//            return slaveGradingTypeFlux
//                    .collectList()
//                    .flatMap(gradingTypeEntity -> slaveGradingTypeRepository
//                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
//                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
//                            .flatMap(count -> {
//                                if (gradingTypeEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingTypeEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        } else {
//            Flux<SlaveGradingTypeEntity> slaveGradingTypeFlux = slaveGradingTypeRepository
//                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//            return slaveGradingTypeFlux
//                    .collectList()
//                    .flatMap(gradingTypeEntity -> slaveGradingTypeRepository
//                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
//                            .flatMap(count -> {
//                                if (gradingTypeEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingTypeEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        }
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_show")
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        UUID gradingTypeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//
//        return slaveGradingTypeRepository.findByUuidAndDeletedAtIsNull(gradingTypeUUID)
//                .flatMap(gradingTypeEntity -> responseSuccessMsg("Record Fetched Successfully", gradingTypeEntity))
//                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_store")
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
//                    GradingTypeEntity entity = GradingTypeEntity.builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name").trim())
//                            .description(value.getFirst("description").trim())
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userUUID))
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
////                    check grading method name is unique
//                    return gradingTypeRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(entity.getName())
//                            .flatMap(gradingTypeEntity -> responseInfoMsg("Name Already Exist"))
//                            .switchIfEmpty(Mono.defer(() -> gradingTypeRepository.save(entity)
//                                    .flatMap(gradingTypeEntity -> responseSuccessMsg("Record Stored Successfully", gradingTypeEntity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
//                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
//                            ));
//                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_update")
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        UUID gradingTypeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//                .flatMap(value -> gradingTypeRepository.findByUuidAndDeletedAtIsNull(gradingTypeUUID)
//                                .flatMap(previousEntity -> {
//
//                                    GradingTypeEntity updatedEntity = GradingTypeEntity.builder()
//                                            .uuid(previousEntity.getUuid())
//                                            .name(value.getFirst("name").trim())
//                                            .description(value.getFirst("description").trim())
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
////                         check address type is unique
//                                    return gradingTypeRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getName(), gradingTypeUUID)
//                                            .flatMap(nameExists -> responseInfoMsg("Name Already Exists"))
//                                            .switchIfEmpty(Mono.defer(() ->
//                                                    gradingTypeRepository.save(previousEntity)
//                                                            .then(gradingTypeRepository.save(updatedEntity))
//                                                            .flatMap(gradingTypeEntity -> responseSuccessMsg("Record Updated Successfully", gradingTypeEntity))
//                                                            .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
//                                                            .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
//                                            ));
//                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_status_update")
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        UUID gradingTypeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//                    return gradingTypeRepository.findByUuidAndDeletedAtIsNull(gradingTypeUUID)
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
//                                GradingTypeEntity entity = GradingTypeEntity.builder()
//                                        .uuid(previousEntity.getUuid())
//                                        .name(previousEntity.getName())
//                                        .description(previousEntity.getDescription())
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
//                                return gradingTypeRepository.save(previousEntity)
//                                        .then(gradingTypeRepository.save(entity))
//                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
//                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
//                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
//                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
//    }
//
//    @AuthHasPermission(value = "lms_api_v1_grading-types_delete")
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID gradingTypeUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//        return gradingTypeRepository.findByUuidAndDeletedAtIsNull(gradingTypeUUID)
//                //check if Grading Type exists in Grades
//                .flatMap(gradingTypeEntity -> gradeRepository.findFirstByGradingTypeUUIDAndDeletedAtIsNull(gradingTypeEntity.getUuid())
//                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
//                        .switchIfEmpty(Mono.defer(() -> {
//
//                            gradingTypeEntity.setDeletedBy(UUID.fromString(userUUID));
//                            gradingTypeEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                            gradingTypeEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
//                            gradingTypeEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
//                            gradingTypeEntity.setReqDeletedIP(reqIp);
//                            gradingTypeEntity.setReqDeletedPort(reqPort);
//                            gradingTypeEntity.setReqDeletedBrowser(reqBrowser);
//                            gradingTypeEntity.setReqDeletedOS(reqOs);
//                            gradingTypeEntity.setReqDeletedDevice(reqDevice);
//                            gradingTypeEntity.setReqDeletedReferer(reqReferer);
//
//                            return gradingTypeRepository.save(gradingTypeEntity)
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
