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
//import tuf.webscaf.app.dbContext.master.entity.QuestionBankEntity;
//import tuf.webscaf.app.dbContext.master.repositry.QuestionBankRepository;
//import tuf.webscaf.app.dbContext.master.repositry.QuestionBankRepository;
//import tuf.webscaf.app.dbContext.master.repositry.QuestionRepository;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionBankEntity;
//import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuestionBankRepository;
//import tuf.webscaf.config.service.response.AppResponse;
//import tuf.webscaf.config.service.response.AppResponseMessage;
//import tuf.webscaf.config.service.response.CustomResponse;
//
//import java.time.LocalDateTime;
//import java.time.ZoneId;
//import java.util.List;
//import java.util.UUID;
//
//@Tag(name = "questionBankHandler")
//@Component
//public class QuestionBankHandler {
//    @Autowired
//    CustomResponse appresponse;
//
//    @Autowired
//    QuestionRepository questionRepository;
//
//    @Autowired
//    QuestionBankRepository questionBankRepository;
//
//    @Autowired
//    SlaveQuestionBankRepository slaveQuestionBankRepository;
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
//            Flux<SlaveQuestionBankEntity> slaveQuestionBankFlux = slaveQuestionBankRepository
//                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
//                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));
//
//            return slaveQuestionBankFlux
//                    .collectList()
//                    .flatMap(gradingMethodEntity -> slaveQuestionBankRepository
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
//            Flux<SlaveQuestionBankEntity> slaveQuestionBankFlux = slaveQuestionBankRepository
//                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
//            return slaveQuestionBankFlux
//                    .collectList()
//                    .flatMap(gradingMethodEntity -> slaveQuestionBankRepository
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
//        UUID QuestionBankUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
//
//        return slaveQuestionBankRepository.findByUuidAndDeletedAtIsNull(QuestionBankUUID)
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
//                    QuestionBankEntity entity = QuestionBankEntity
//                            .builder()
//                            .uuid(UUID.randomUUID())
//                            .questionUUID(UUID.fromString(value.getFirst("questionUUID").trim()))
//                            .name(value.getFirst("name").trim())
//                            .description(value.getFirst("description").trim())
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
//                            .createdBy(UUID.fromString(userUUID))
//                            .build();
//
//                    //Check if name is unique
//                    return questionBankRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(entity.getName())
//                            .flatMap(QuestionBankEntity -> responseInfoMsg("Name Already Exist"))
//                            //check if question exists
//                            .switchIfEmpty(Mono.defer(() -> questionRepository.findByUuidAndDeletedAtIsNull(entity.getQuestionUUID())
//                                    .flatMap(question -> questionBankRepository.save(entity)
//                                            .flatMap(saveQuestionBankEntity -> responseSuccessMsg("Record Stored Successfully", saveQuestionBankEntity))
//                                            .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
//                                            .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
//                                    )
//                                    .switchIfEmpty(responseInfoMsg("Question Does not Exist."))
//                                    .onErrorResume(ex -> responseErrorMsg("Question Does not Exist.Please Contact Developer."))
//                            ));
//                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> update(ServerRequest serverRequest) {
//        UUID QuestionBankUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//                .flatMap(value -> questionBankRepository.findByUuidAndDeletedAtIsNull(QuestionBankUUID)
//                        .flatMap(previousEntity -> {
//
//                            QuestionBankEntity entity = QuestionBankEntity
//                                    .builder()
//                                    .uuid(previousEntity.getUuid())
//                                    .questionUUID(UUID.fromString(value.getFirst("questionUUID").trim()))
//                                    .name(value.getFirst("name").trim())
//                                    .description(value.getFirst("description").trim())
//                                    .status(Boolean.valueOf(value.getFirst("status")))
//                                    .createdAt(previousEntity.getCreatedAt())
//                                    .createdBy(previousEntity.getCreatedBy())
//                                    .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                    .updatedBy(UUID.fromString(userUUID))
//                                    .build();
//
//                            previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                            //Check if name is unique
//                            return questionBankRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(entity.getName())
//                                    .flatMap(QuestionBankEntity -> responseInfoMsg("Name Already Exist"))
//                                    //check if question exists
//                                    .switchIfEmpty(Mono.defer(() -> questionRepository.findByUuidAndDeletedAtIsNull(entity.getQuestionUUID())
//                                            .flatMap(question -> questionBankRepository.save(entity)
//                                                    .flatMap(saveQuestionBankEntity -> responseSuccessMsg("Record Updated Successfully", saveQuestionBankEntity))
//                                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
//                                                    .onErrorResume(err -> responseErrorMsg("Unable to update record. Please contact developer."))
//                                            )
//                                            .switchIfEmpty(responseInfoMsg("Question Does not Exist."))
//                                            .onErrorResume(ex -> responseErrorMsg("Question Does not Exist.Please Contact Developer."))
//                                    ));
//                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
//                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }
//
//    public Mono<ServerResponse> status(ServerRequest serverRequest) {
//        UUID QuestionBankUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//                    return questionBankRepository.findByUuidAndDeletedAtIsNull(QuestionBankUUID)
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
//                                QuestionBankEntity updatedEntity = QuestionBankEntity
//                                        .builder()
//                                        .uuid(previousEntity.getUuid())
//                                        .questionUUID(previousEntity.getQuestionUUID())
//                                        .name(previousEntity.getName())
//                                        .description(previousEntity.getDescription())
//                                        .status(status == true ? true : false)
//                                        .createdAt(previousEntity.getCreatedAt())
//                                        .createdBy(previousEntity.getCreatedBy())
//                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
//                                        .updatedBy(UUID.fromString(userUUID))
//                                        .build();
//
//                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
//                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                                return questionBankRepository.save(previousEntity)
//                                        .then(questionBankRepository.save(updatedEntity))
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
//        UUID questionBankUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//        return questionBankRepository.findByUuidAndDeletedAtIsNull(questionBankUUID)
//                .flatMap(questionBankEntity -> {
//
//                            questionBankEntity.setDeletedBy(UUID.fromString(userUUID));
//                            questionBankEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//
//                            return questionBankRepository.save(questionBankEntity)
//                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
//                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                        }
//                ).switchIfEmpty(responseInfoMsg("Record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));
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
