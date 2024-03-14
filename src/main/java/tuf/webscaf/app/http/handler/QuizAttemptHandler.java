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
import tuf.webscaf.app.dbContext.master.entity.QuizAttemptEntity;
import tuf.webscaf.app.dbContext.master.repositry.QuizAttemptRepository;
import tuf.webscaf.app.dbContext.master.repositry.QuizRepository;
import tuf.webscaf.app.dbContext.master.repositry.QuizAttemptRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizAttemptEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuizAttemptRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuizAttemptRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Tag(name = "quizAttemptHandler")
@Component
public class QuizAttemptHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    @Autowired
    SlaveQuizAttemptRepository slaveQuizAttemptRepository;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Autowired
    ApiCallService apiCallService;

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_index")
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
            Flux<SlaveQuizAttemptEntity> slaveQuizAttemptFlux = slaveQuizAttemptRepository
                    .findAllByDeletedAtIsNullAndStatus(pageable, Boolean.valueOf(status));
            return slaveQuizAttemptFlux
                    .collectList()
                    .flatMap(QuizAttemptEntity -> slaveQuizAttemptRepository
                            .countByDeletedAtIsNullAndStatus(Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (QuizAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", QuizAttemptEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"));
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveQuizAttemptEntity> slaveQuizAttemptFlux = slaveQuizAttemptRepository
                    .findAllByDeletedAtIsNull(pageable);
            return slaveQuizAttemptFlux
                    .collectList()
                    .flatMap(QuizAttemptEntity -> slaveQuizAttemptRepository
                            .countByDeletedAtIsNull()
                            .flatMap(count -> {
                                if (QuizAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", QuizAttemptEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"));
//                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID QuizAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

        return slaveQuizAttemptRepository.findByUuidAndDeletedAtIsNull(QuizAttemptUUID)
                .flatMap(QuizAttemptEntity -> responseSuccessMsg("Record Fetched Successfully", QuizAttemptEntity))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_store")
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

                    QuizAttemptEntity quizAttemptEntity = QuizAttemptEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .submissionStatus(Boolean.valueOf(value.getFirst("submissionStatus")))
                            .comment(value.getFirst("comment").trim())
                            .attachmentUUID(UUID.fromString(value.getFirst("attachmentUUID").trim()))
                            .attemptedBy(UUID.fromString(userUUID))
                            .quizUUID(UUID.fromString(value.getFirst("quizUUID").trim()))
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

                    sendFormData.add("docId", String.valueOf(quizAttemptEntity.getAttachmentUUID()));

                    return quizRepository.findByUuidAndDeletedAtIsNull(quizAttemptEntity.getQuizUUID())
                            .flatMap(Quiz -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", quizAttemptEntity.getAttachmentUUID())
                                    .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                            .flatMap(documentJson1 -> apiCallService.getDocumentExtension(documentJson)
                                                    .flatMap(extension -> {
                                                        quizAttemptEntity.setDocumentExtension(extension);
                                                        return quizAttemptRepository.save(quizAttemptEntity)
                                                                .flatMap(saveQuizAttemptEntity -> apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update")
                                                                        .flatMap(documentUpload -> responseSuccessMsg("Record Stored Successfully", saveQuizAttemptEntity)))
                                                                .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
                                                                .onErrorResume(err -> responseInfoMsg("Unable to store record. Please contact developer."));
                                                    }).switchIfEmpty(responseInfoMsg("Document Extension Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("Document Extension Does not exist.Please Contact Developer,."))
                                            )).switchIfEmpty(responseInfoMsg("Document Does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Document Does not exist.Please Contact Developer."))
                            ).switchIfEmpty(responseInfoMsg("Quiz Does not exist."))
                            .onErrorResume(ex -> responseErrorMsg("Quiz Does not exist.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID quizAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> quizAttemptRepository.findByUuidAndDeletedAtIsNull(quizAttemptUUID)
                        .flatMap(previousEntity -> {

                            MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();

                            QuizAttemptEntity updatedQuizAttemptEntity = QuizAttemptEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .submissionStatus(Boolean.valueOf(value.getFirst("submissionStatus")))
                                    .comment(value.getFirst("comment").trim())
                                    .attachmentUUID(UUID.fromString(value.getFirst("attachmentUUID").trim()))
                                    .attemptedBy(UUID.fromString(userUUID))
                                    .quizUUID(UUID.fromString(value.getFirst("quizUUID").trim()))
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

                            sendFormData.add("docId", String.valueOf(updatedQuizAttemptEntity.getAttachmentUUID()));

                            return quizRepository.findByUuidAndDeletedAtIsNull(updatedQuizAttemptEntity.getQuizUUID())
                                    .flatMap(Quiz -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", updatedQuizAttemptEntity.getAttachmentUUID())
                                            .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                    .flatMap(documentJson1 -> apiCallService.getDocumentExtension(documentJson)
                                                            .flatMap(extension -> {
                                                                updatedQuizAttemptEntity.setDocumentExtension(extension);
                                                                return quizAttemptRepository.save(previousEntity)
                                                                        .then(quizAttemptRepository.save(updatedQuizAttemptEntity))
                                                                        .flatMap(saveQuizAttemptEntity -> apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update")
                                                                                .flatMap(documentUpload -> responseSuccessMsg("Record Updated Successfully", saveQuizAttemptEntity)))
                                                                        .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                        .onErrorResume(err -> responseInfoMsg("Unable to update record. Please contact developer."));
                                                            })
                                                            .switchIfEmpty(responseInfoMsg("Document Extension Does not exist."))
                                                            .onErrorResume(ex -> responseErrorMsg("Document Extension Does not exist.Please Contact Developer,."))
                                                    )
                                                    .switchIfEmpty(responseInfoMsg("Document Does not exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("Document Does not exist.Please Contact Developer."))
                                            )
                                    )
                                    .switchIfEmpty(responseInfoMsg("Quiz Does not exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Quiz Does not exist.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID quizAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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

                    return quizAttemptRepository.findByUuidAndDeletedAtIsNull(quizAttemptUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                QuizAttemptEntity updatedQuizAttemptEntity = QuizAttemptEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .status(status == true ? true : false)
                                        .submissionStatus(previousEntity.getSubmissionStatus())
                                        .comment(previousEntity.getComment())
                                        .attachmentUUID(previousEntity.getAttachmentUUID())
                                        .documentExtension(previousEntity.getDocumentExtension())
                                        .attemptedBy(previousEntity.getAttemptedBy())
                                        .quizUUID(previousEntity.getQuizUUID())
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

                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);

                                return quizAttemptRepository.save(previousEntity)
                                        .then(quizAttemptRepository.save(updatedQuizAttemptEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_student_quiz-attempts_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID quizAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));

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

        return quizAttemptRepository.findByUuidAndDeletedAtIsNull(quizAttemptUUID)
                .flatMap(quizAttemptEntity -> {

                    quizAttemptEntity.setDeletedBy(UUID.fromString(userUUID));
                    quizAttemptEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    quizAttemptEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    quizAttemptEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    quizAttemptEntity.setReqDeletedIP(reqIp);
                    quizAttemptEntity.setReqDeletedPort(reqPort);
                    quizAttemptEntity.setReqDeletedBrowser(reqBrowser);
                    quizAttemptEntity.setReqDeletedOS(reqOs);
                    quizAttemptEntity.setReqDeletedDevice(reqDevice);
                    quizAttemptEntity.setReqDeletedReferer(reqReferer);

                    return quizAttemptRepository.save(quizAttemptEntity)
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
