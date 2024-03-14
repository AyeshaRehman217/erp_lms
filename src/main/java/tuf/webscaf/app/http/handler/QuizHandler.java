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
import tuf.webscaf.app.dbContext.master.entity.QuizEntity;
import tuf.webscaf.app.dbContext.master.repositry.QuizAttemptRepository;
import tuf.webscaf.app.dbContext.master.repositry.QuizCategoryRepository;
import tuf.webscaf.app.dbContext.master.repositry.QuizRepository;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveQuizRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Tag(name = "quizHandler")
@Component
public class QuizHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    QuizAttemptRepository quizAttemptRepository;

    @Autowired
    QuizRepository quizRepository;

    @Autowired
    SlaveQuizRepository slaveQuizRepository;

    @Autowired
    QuizCategoryRepository quizCategoryRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_index")
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
            Flux<SlaveQuizEntity> slaveQuizFlux = slaveQuizRepository
                    .findAllByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(pageable,
                            searchKeyWord, Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status));

            return slaveQuizFlux
                    .collectList()
                    .flatMap(quizEntity -> slaveQuizRepository
                            .countByNameContainingIgnoreCaseAndStatusAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndStatusAndDeletedAtIsNull(searchKeyWord,
                                    Boolean.valueOf(status), searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (quizEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", quizEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveQuizEntity> slaveQuizFlux = slaveQuizRepository
                    .findAllByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(pageable, searchKeyWord, searchKeyWord);

            return slaveQuizFlux
                    .collectList()
                    .flatMap(quizEntity -> slaveQuizRepository
                            .countByNameContainingIgnoreCaseAndDeletedAtIsNullOrDescriptionContainingIgnoreCaseAndDeletedAtIsNull(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (quizEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", quizEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID quizUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveQuizRepository.findByUuidAndDeletedAtIsNull(quizUUID)
                .flatMap(gradingMethodEntity -> responseSuccessMsg("Record Fetched Successfully", gradingMethodEntity))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }


    //Check Document UUID In Drive Module in Delete Function to Check Existence
    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_documents_show")
    public Mono<ServerResponse> getDocumentUUID(ServerRequest serverRequest) {
        final UUID documentUUID = UUID.fromString(serverRequest.pathVariable("documentUUID"));

        return serverRequest.formData()
                .flatMap(value -> slaveQuizRepository.findFirstByDocumentUUIDAndDeletedAtIsNull(documentUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_store")
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

                    LocalTime extendedTime = null;

                    if (value.getFirst("extendedTime") != null && value.getFirst("") != null) {
                        extendedTime = LocalTime.parse(value.getFirst("extendedTime"));
                    }

                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();

                    QuizEntity quizEntity = QuizEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .startTime(LocalTime.parse(value.getFirst("startTime")))
                            .endTime(LocalTime.parse(value.getFirst("endTime")))
                            .extendedTime(extendedTime)
                            .name(value.getFirst("name").trim())
                            .description(value.getFirst("description").trim())
                            .documentUUID(UUID.fromString(value.getFirst("documentUUID").trim()))
                            .quizCategoryUUID(UUID.fromString(value.getFirst("quizCategoryUUID").trim()))
                            .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
                            .teacherUUID(UUID.fromString(value.getFirst("teacherUUID").trim()))
                            .academicSessionUUID(UUID.fromString(value.getFirst("academicSessionUUID").trim()))
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

                    sendFormData.add("docId", String.valueOf(quizEntity.getDocumentUUID()));

                    //check if Start Time is before the End time
                    if (quizEntity.getStartTime().isAfter(quizEntity.getEndTime())) {
                        return responseInfoMsg("Start Time Should be Before the End Time");
                    }

                    //check if Start Time is before the End time
                    if (quizEntity.getEndTime().isBefore(quizEntity.getStartTime())) {
                        return responseInfoMsg("End Time Should be After the Start Time");
                    }

//                    check quiz Category name is unique
                    return quizRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNull(quizEntity.getName())
                            .flatMap(QuizEntity -> responseInfoMsg("Name Already Exist"))
                            //check if quiz Category exists
                            .switchIfEmpty(Mono.defer(() -> quizCategoryRepository.findByUuidAndDeletedAtIsNull(quizEntity.getQuizCategoryUUID())
                                    //check if Document Exists in Drive Module
                                    .flatMap(quizCategoryEntity -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", quizEntity.getDocumentUUID())
                                            .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                    //check if teacher Record Exists in Academics
                                                    .flatMap(document -> apiCallService.getDataWithUUID(academicUri + "api/v1/teachers/show/", quizEntity.getTeacherUUID())
                                                            //check if Academic Session Record Exists in Academics
                                                            .flatMap(teacher -> apiCallService.getDataWithUUID(academicUri + "api/v1/academic-sessions/show/", quizEntity.getAcademicSessionUUID())
                                                                    //check if course Subject Record Exists in Academics
                                                                    .flatMap(academicSession -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", quizEntity.getCourseSubjectUUID())
                                                                            //Save Quiz Entity
                                                                            .flatMap(courseSubject -> quizRepository.save(quizEntity)
                                                                                    .flatMap(saveQuizEntity -> apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update")
                                                                                            .flatMap(documentUpload -> responseSuccessMsg("Record Stored Successfully", saveQuizEntity)))
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
                                                                                    .onErrorResume(err -> responseInfoMsg("Unable to store record. Please contact developer."))
                                                                            ).switchIfEmpty(responseInfoMsg("Course Subject Does not Exist."))
                                                                            .onErrorResume(ex -> responseErrorMsg("Course Subject Does not Exist.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Academic Session Does not Exist."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Academic Session Does not Exist.Please Contact Developer."))
                                                            ).switchIfEmpty(responseInfoMsg("Teacher Record Does not Exist."))
                                                            .onErrorResume(ex -> responseErrorMsg("Teacher Record Does not Exist.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("Unable to Upload Document."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Upload Document.Please Contact Developer."))
                                            ).switchIfEmpty(responseInfoMsg("Unable to Upload Document."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Upload Document.Please Contact Developer."))
                                    ).switchIfEmpty(responseInfoMsg("Quiz Category Does not Exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Quiz Category Does not Exist.Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID quizUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> quizRepository.findByUuidAndDeletedAtIsNull(quizUUID)
                                .flatMap(previousEntity -> {

                                    LocalTime extendedTime = null;

                                    if (value.getFirst("extendedTime") != null && value.getFirst("") != null) {
                                        extendedTime = LocalTime.parse(value.getFirst("extendedTime"));
                                    }

                                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();

                                    QuizEntity updatedQuizEntity = QuizEntity
                                            .builder()
                                            .uuid(previousEntity.getUuid())
                                            .startTime(LocalTime.parse(value.getFirst("startTime")))
                                            .endTime(LocalTime.parse(value.getFirst("endTime")))
                                            .extendedTime(extendedTime)
                                            .name(value.getFirst("name").trim())
                                            .description(value.getFirst("description").trim())
                                            .documentUUID(UUID.fromString(value.getFirst("documentUUID").trim()))
                                            .quizCategoryUUID(UUID.fromString(value.getFirst("quizCategoryUUID").trim()))
                                            .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
                                            .teacherUUID(UUID.fromString(value.getFirst("teacherUUID").trim()))
                                            .academicSessionUUID(UUID.fromString(value.getFirst("academicSessionUUID").trim()))
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

                                    sendFormData.add("docId", String.valueOf(updatedQuizEntity.getDocumentUUID()));

                                    //check if Start Time is before the End time
                                    if (updatedQuizEntity.getStartTime().isAfter(updatedQuizEntity.getEndTime())) {
                                        return responseInfoMsg("Start Time Should be Before the End Time");
                                    }

                                    //check if Start Time is before the End time
                                    if (updatedQuizEntity.getEndTime().isBefore(updatedQuizEntity.getStartTime())) {
                                        return responseInfoMsg("End Time Should be After the Start Time");
                                    }

//                                  check quiz Category name is unique
                                    return quizRepository.findFirstByNameIgnoreCaseAndDeletedAtIsNullAndUuidIsNot(updatedQuizEntity.getName(), quizUUID)
                                            .flatMap(QuizEntity -> responseInfoMsg("Name Already Exist"))
                                            //check if quiz Category exists
                                            .switchIfEmpty(Mono.defer(() -> quizCategoryRepository.findByUuidAndDeletedAtIsNull(updatedQuizEntity.getQuizCategoryUUID())
                                                    //check if Document Exists in Drive Module
                                                    .flatMap(quizCategoryEntity -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", updatedQuizEntity.getDocumentUUID())
                                                            .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                                    //check if teacher Record Exists in Academics
                                                                    .flatMap(document -> apiCallService.getDataWithUUID(academicUri + "api/v1/teachers/show/", updatedQuizEntity.getTeacherUUID())
                                                                            //check if Academic Session Record Exists in Academics
                                                                            .flatMap(teacher -> apiCallService.getDataWithUUID(academicUri + "api/v1/academic-sessions/show/", updatedQuizEntity.getAcademicSessionUUID())
                                                                                    //check if course Subject Record Exists in Academics
                                                                                    .flatMap(academicSession -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", updatedQuizEntity.getCourseSubjectUUID())
                                                                                            //Save Quiz Entity
                                                                                            .flatMap(courseSubject -> quizRepository.save(previousEntity)
                                                                                                    .then(quizRepository.save(updatedQuizEntity))
                                                                                                    .flatMap(saveQuizEntity -> apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update")
                                                                                                            .flatMap(documentUpload -> responseSuccessMsg("Record Updated Successfully", saveQuizEntity)))
                                                                                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                                                                    .onErrorResume(err -> responseInfoMsg("Unable to update record. Please contact developer."))
                                                                                            ).switchIfEmpty(responseInfoMsg("Course Subject Does not Exist."))
                                                                                            .onErrorResume(ex -> responseErrorMsg("Course Subject Does not Exist.Please Contact Developer."))
                                                                                    ).switchIfEmpty(responseInfoMsg("Academic Session Does not Exist."))
                                                                                    .onErrorResume(ex -> responseErrorMsg("Academic Session Does not Exist.Please Contact Developer."))
                                                                            ).switchIfEmpty(responseInfoMsg("Teacher Record Does not Exist."))
                                                                            .onErrorResume(ex -> responseErrorMsg("Teacher Record Does not Exist.Please Contact Developer."))
                                                                    ).switchIfEmpty(responseInfoMsg("Unable to Upload Document."))
                                                                    .onErrorResume(ex -> responseErrorMsg("Unable to Upload Document.Please Contact Developer."))
                                                            ).switchIfEmpty(responseInfoMsg("Unable to Upload Document."))
                                                            .onErrorResume(ex -> responseErrorMsg("Unable to Upload Document.Please Contact Developer."))
                                                    ).switchIfEmpty(responseInfoMsg("Quiz Category Does not Exist."))
                                                    .onErrorResume(ex -> responseErrorMsg("Quiz Category Does not Exist.Please Contact Developer."))
                                            ));
                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID quizUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return quizRepository.findByUuidAndDeletedAtIsNull(quizUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }


                                QuizEntity updatedQuizEntity = QuizEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .startTime(previousEntity.getStartTime())
                                        .endTime(previousEntity.getEndTime())
                                        .extendedTime(previousEntity.getExtendedTime())
                                        .name(previousEntity.getName())
                                        .description(previousEntity.getDescription())
                                        .documentUUID(previousEntity.getDocumentUUID())
                                        .quizCategoryUUID(previousEntity.getQuizCategoryUUID())
                                        .courseSubjectUUID(previousEntity.getCourseSubjectUUID())
                                        .teacherUUID(previousEntity.getTeacherUUID())
                                        .academicSessionUUID(previousEntity.getAcademicSessionUUID())
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

                                return quizRepository.save(previousEntity)
                                        .then(quizRepository.save(updatedQuizEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_quizzes_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID quizUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return quizRepository.findByUuidAndDeletedAtIsNull(quizUUID)
                .flatMap(QuizEntity -> quizAttemptRepository.findFirstByQuizUUIDAndDeletedAtIsNull(QuizEntity.getUuid())
                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists"))
                        .switchIfEmpty(Mono.defer(() -> {

                            QuizEntity.setDeletedBy(UUID.fromString(userUUID));
                            QuizEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            QuizEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            QuizEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            QuizEntity.setReqDeletedIP(reqIp);
                            QuizEntity.setReqDeletedPort(reqPort);
                            QuizEntity.setReqDeletedBrowser(reqBrowser);
                            QuizEntity.setReqDeletedOS(reqOs);
                            QuizEntity.setReqDeletedDevice(reqDevice);
                            QuizEntity.setReqDeletedReferer(reqReferer);

                            return quizRepository.save(QuizEntity)
                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
                        }))
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
