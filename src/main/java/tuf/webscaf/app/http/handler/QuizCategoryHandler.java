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
import tuf.webscaf.app.dbContext.master.entity.QuizCategoryEntity;
import tuf.webscaf.app.dbContext.master.repositry.QuizCategoryRepository;
import tuf.webscaf.app.dbContext.master.repositry.QuizRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizCategoryEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuizCategoryRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Tag(name = "quizCategoryHandler")
@Component
public class QuizCategoryHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    QuizCategoryRepository quizCategoryRepository;

    @Autowired
    SlaveQuizCategoryRepository slaveQuizCategoriesRepository;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_index")
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

            Flux<SlaveQuizCategoryEntity> slaveQuizCategoryFlux = slaveQuizCategoriesRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveQuizCategoryFlux
                    .collectList()
                    .flatMap(gradingMethodEntity -> slaveQuizCategoriesRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
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
            Flux<SlaveQuizCategoryEntity> slaveQuizCategoryFlux = slaveQuizCategoriesRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);
            return slaveQuizCategoryFlux
                    .collectList()
                    .flatMap(gradingMethodEntity -> slaveQuizCategoriesRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
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

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID quizCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveQuizCategoriesRepository.findByUuidAndDeletedAtIsNull(quizCategoryUUID)
                .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Fetched Successfully", gradingMethodEntity))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_store")
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

                    QuizCategoryEntity entity = QuizCategoryEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description"))
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

//                    check quiz Category name is unique
                    return quizCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(entity.getName())
                            .flatMap(quizCategoryEntity -> responseInfoMsg("Name Already Exist"))
                            .switchIfEmpty(Mono.defer(() -> quizCategoryRepository.save(entity)
                                    .flatMap(saveQuizCategoryEntity -> responseSuccessMsg("Record Stored Successfully", saveQuizCategoryEntity))
                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
//                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"));
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID quizCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> quizCategoryRepository.findByUuidAndDeletedAtIsNull(quizCategoryUUID)
                                .flatMap(previousEntity -> {

                                    QuizCategoryEntity quizCategoryEntity = QuizCategoryEntity
                                            .builder()
                                            .uuid(previousEntity.getUuid())
                                            .name(value.getFirst("name").trim())
                                            .description(value.getFirst("description").trim())
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


//                         check quiz category is unique
                                    return quizCategoryRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(quizCategoryEntity.getName(), quizCategoryUUID)
                                            .flatMap(nameExists -> responseInfoMsg("Name Already Exists"))
                                            .switchIfEmpty(Mono.defer(() ->
                                                    quizCategoryRepository.save(previousEntity)
                                                            .then(quizCategoryRepository.save(quizCategoryEntity))
                                                            .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Updated Successfully", gradingMethodEntity))
                                                            .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
                                            ));
                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID quizCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return quizCategoryRepository.findByUuidAndDeletedAtIsNull(quizCategoryUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                QuizCategoryEntity quizCategoryEntity = QuizCategoryEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
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

                                return quizCategoryRepository.save(previousEntity)
                                        .then(quizCategoryRepository.save(quizCategoryEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_quiz-categories_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID quizCategoryUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return quizCategoryRepository.findByUuidAndDeletedAtIsNull(quizCategoryUUID)
                //check if Grading Method exists in Grades
                .flatMap(quizCategoryEntity -> quizRepository.findFirstByQuizCategoryUUIDAndDeletedAtIsNull(quizCategoryEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> {

                            quizCategoryEntity.setDeletedBy(UUID.fromString(userUUID));
                            quizCategoryEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            quizCategoryEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            quizCategoryEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            quizCategoryEntity.setReqDeletedIP(reqIp);
                            quizCategoryEntity.setReqDeletedPort(reqPort);
                            quizCategoryEntity.setReqDeletedBrowser(reqBrowser);
                            quizCategoryEntity.setReqDeletedOS(reqOs);
                            quizCategoryEntity.setReqDeletedDevice(reqDevice);
                            quizCategoryEntity.setReqDeletedReferer(reqReferer);

                            return quizCategoryRepository.save(quizCategoryEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                        }))
                ).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
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
