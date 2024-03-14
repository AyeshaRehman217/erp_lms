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
//import tuf.webscaf.app.dbContext.master.entity.QuestionCategoryEntity;
//import tuf.webscaf.app.dbContext.master.repositry.QuestionCategoryRepository;
//import tuf.webscaf.app.dbContext.master.repositry.QuestionRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionCategoryEntity;
//import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuestionCategoryRepository;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.UUID;
//
//@Tag(name = "questionCategoryHandler")
//@Component
//public class QuestionCategoryHandler {
//    @Autowired
//    CustomResponse appresponse;
//
//    @Autowired
//    QuestionRepository questionRepository;
//
//    @Autowired
//    QuestionCategoryRepository questionCategoryRepository;
//
//    @Autowired
//    SlaveQuestionCategoryRepository slaveQuestionCategoriesRepository;
//
//    @Value("${server.zone}")
//    private String zone;
//
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
//
//            Flux<SlaveQuestionCategoryEntity> slaveQuestionCategoryFlux = slaveQuestionCategoriesRepository
//                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
//                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
//
//            return slaveQuestionCategoryFlux
//                    .collectList()
//                    .flatMap(gradingMethodEntity -> slaveQuestionCategoriesRepository
//                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
//                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
//                            .flatMap(count -> {
//                                if (gradingMethodEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingMethodEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        } else {
//            Flux<SlaveQuestionCategoryEntity> slaveQuestionCategoryFlux = slaveQuestionCategoriesRepository
//                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//
//            return slaveQuestionCategoryFlux
//                    .collectList()
//                    .flatMap(gradingMethodEntity -> slaveQuestionCategoriesRepository
//                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
//                            .flatMap(count -> {
//                                if (gradingMethodEntity.isEmpty()) {
//                                    return responseIndexInfoMsg("Record does not exist", count);
//                                } else {
//                                    return responseIndexSuccessMsg("All Records Fetched Successfully", gradingMethodEntity, count);
//                                }
//                            })
//                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//        }
//    }
//
//    public Mono<ServerResponse> show(ServerRequest serverRequest) {
//        UUID questionCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//
//        return slaveQuestionCategoriesRepository.findByUuidAndDeletedAtIsNull(questionCategoryUUID)
//                .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Fetched Successfully", gradingMethodEntity))
//                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }
//
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
//                    QuestionCategoryEntity entity = QuestionCategoryEntity
//                            .builder()
//                            .uuid(UUID.randomUUID())
//                            .name(value.getFirst("name").trim())
//                            .description(value.getFirst("description").trim())
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userUUID))
//                            .build();
//
////                    check question Category name is unique
//                    return questionCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(entity.getName())
//                            .flatMap(questionCategoryEntity -> responseInfoMsg("Name Already Exist"))
//                            .switchIfEmpty(Mono.defer(() -> questionCategoryRepository.save(entity)
//                                    .flatMap(saveQuestionCategoryEntity -> responseSuccessMsg("Record Stored Successfully", saveQuestionCategoryEntity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
//                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
//                            ));
//                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        UUID questionCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//                .flatMap(value -> questionCategoryRepository.findByUuidAndDeletedAtIsNull(questionCategoryUUID)
//                                .flatMap(previousEntity -> {
//
//                                    QuestionCategoryEntity questionCategoryEntity = QuestionCategoryEntity
//                                            .builder()
//                                            .uuid(previousEntity.getUuid())
//                                            .name(value.getFirst("name").trim())
//                                            .description(value.getFirst("description").trim())
//                                            .status(Boolean.valueOf(value.getFirst("status")))
//                                            .createdAt(previousEntity.getCreatedAt())
//                                            .createdBy(previousEntity.getCreatedBy())
//                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                            .updatedBy(UUID.fromString(userUUID))
//                                            .build();
//
//                                    previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                                    previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
////                         check address type is unique
//                                    return questionCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(questionCategoryEntity.getName(), questionCategoryUUID)
//                                            .flatMap(nameExists -> responseInfoMsg("Name Already Exists"))
//                                            .switchIfEmpty(Mono.defer(() ->
//                                                    questionCategoryRepository.save(previousEntity)
//                                                            .then(questionCategoryRepository.save(questionCategoryEntity))
//                                                            .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Updated Successfully", gradingMethodEntity))
//                                                            .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
//                                                            .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
//                                            ));
//                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        UUID questionCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//
//                    boolean status = Boolean.parseBoolean(value.getFirst("status"));
//
//                    return questionCategoryRepository.findByUuidAndDeletedAtIsNull(questionCategoryUUID)
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
//                                QuestionCategoryEntity questionCategoryEntity = QuestionCategoryEntity
//                                        .builder()
//                                        .uuid(previousEntity.getUuid())
//                                        .name(previousEntity.getName())
//                                        .description(previousEntity.getDescription())
//                                        .status(status == true ? true : false)
//                                        .createdAt(previousEntity.getCreatedAt())
//                                        .createdBy(previousEntity.getCreatedBy())
//                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                        .updatedBy(UUID.fromString(userUUID))
//                                        .build();
//
//                                // update status
//                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                                return questionCategoryRepository.save(previousEntity)
//                                        .then(questionCategoryRepository.save(questionCategoryEntity))
//                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
//                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
//                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
//                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
//                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
//                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID questionCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//        return questionCategoryRepository.findByUuidAndDeletedAtIsNull(questionCategoryUUID)
//                //check if Grading Method exists in Grades
//                .flatMap(questionCategoryEntity -> {
//
//                            questionCategoryEntity.setDeletedBy(UUID.fromString(userUUID));
//                            questionCategoryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                            return questionCategoryRepository.save(questionCategoryEntity)
//                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
//                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                        }
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
