package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.databind.JsonNode;
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
import tuf.webscaf.app.dbContext.master.dto.AssignmentAttemptAttachmentDto;
import tuf.webscaf.app.dbContext.master.dto.DocumentAttachmentDto;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptDocumentEntity;
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptEntity;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentAttemptDocumentRepository;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentAttemptRepository;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAssignmentAttemptDocumentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAssignmentAttemptRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Tag(name = "assignmentAttemptHandler")
@Component
public class AssignmentAttemptHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    AssignmentAttemptRepository assignmentAttemptRepository;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    SlaveAssignmentAttemptRepository slaveAssignmentAttemptRepository;

    @Autowired
    AssignmentAttemptDocumentRepository assignmentAttemptDocumentRepository;

    @Autowired
    SlaveAssignmentAttemptDocumentRepository slaveAssignmentAttemptDocumentRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_auth_module.uri}")
    private String authUri;

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_index")
    public Mono<ServerResponse> index(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String attemptedByUUID = serverRequest.queryParam("attemptedByUUID").map(String::toString).orElse("").trim();

        String assignmentUUID = serverRequest.queryParam("assignmentUUID").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();


        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty() && !attemptedByUUID.isEmpty() && !assignmentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAttemptedByAndAssignmentWithStatus(UUID.fromString(attemptedByUUID), UUID.fromString(assignmentUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAttemptedByAndAssignmentWithStatusFilter(UUID.fromString(attemptedByUUID), UUID.fromString(assignmentUUID), Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!attemptedByUUID.isEmpty() && !assignmentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAttemptedByAndAssignmentWithoutStatus(UUID.fromString(attemptedByUUID), UUID.fromString(assignmentUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAttemptedByAndAssignmentWithOutStatusFilter(UUID.fromString(attemptedByUUID), UUID.fromString(assignmentUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }else if (!status.isEmpty() && !assignmentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAssignmentWithStatus(UUID.fromString(assignmentUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAssignmentWithStatusFilter(UUID.fromString(assignmentUUID), Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty() && !attemptedByUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAttemptedByWithStatus(UUID.fromString(attemptedByUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAttemptedByWithStatusFilter(UUID.fromString(attemptedByUUID), Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!assignmentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAssignmentWithoutStatus(UUID.fromString(assignmentUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAssignmentWithoutStatusFilter(UUID.fromString(assignmentUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }else if (!attemptedByUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexAgainstAttemptedByWithoutStatus(UUID.fromString(attemptedByUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsAgainstAttemptedByWithoutStatusFilter(UUID.fromString(attemptedByUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexWithStatus(Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsWithStatusFilter(Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexWithoutStatus(searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsWithoutStatusFilter(searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignments_mapped_assignment-attempts_index")
    public Mono<ServerResponse> mappedAssignmentAttemptAgainstAssignment(ServerRequest serverRequest) {

        String searchKeyWord = serverRequest.queryParam("skw").map(String::toString).orElse("").trim();

        String studentUUID = serverRequest.queryParam("studentUUID").map(String::toString).orElse("").trim();

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();


        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty() && !studentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .mappedAssignmentAgainstAttemptedByWithStatus(UUID.fromString(studentUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countMappedRecordsAgainstAttemptedByWithStatusFilter(UUID.fromString(studentUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!studentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .mappedAssignmentAgainstAttemptedByWithoutStatus(UUID.fromString(studentUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countMappedRecordsAgainstAttemptedByWithoutStatusFilter(UUID.fromString(studentUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty()) {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexWithStatus(Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsWithStatusFilter(Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveAssignmentAttemptDto> slaveAssignmentAttemptFlux = slaveAssignmentAttemptRepository
                    .indexWithoutStatus(searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptFlux
                    .collectList()
                    .flatMap(assignmentAttemptEntity -> slaveAssignmentAttemptRepository
                            .countIndexRecordsWithoutStatusFilter(searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID assignmentAttemptUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveAssignmentAttemptRepository.showAssignmentAttemptRecordAgainstUUID(assignmentAttemptUUID)
                .collectList()
                .flatMap(assignmentAttemptDocumentDto -> responseSuccessMsg("Record Fetched Successfully", assignmentAttemptDocumentDto.stream().distinct()))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));
    }

    //    Check Document UUID In Drive Module in Delete Function to Check Existence
    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_documents_show")
    public Mono<ServerResponse> getDocumentUUID(ServerRequest serverRequest) {
        final UUID documentUUID = UUID.fromString(serverRequest.pathVariable("documentUUID"));

        return serverRequest.formData()
                .flatMap(value -> slaveAssignmentAttemptDocumentRepository.findFirstByDocumentUUIDAndDeletedAtIsNull(documentUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

//    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_store")
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
//                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>();
//
//                    AssignmentAttemptEntity assignmentAttemptEntity = AssignmentAttemptEntity
//                            .builder()
//                            .uuid(UUID.randomUUID())
//                            .status(Boolean.valueOf(value.getFirst("status")))
//                            .submissionStatus(Boolean.valueOf(value.getFirst("submissionStatus")))
//                            .comment(value.getFirst("comment").trim())
//                            .documentUUID(UUID.fromString(value.getFirst("documentUUID").trim()))
////                            .attemptedBy(UUID.fromString(value.getFirst("attemptedBy").trim()))
//                            .assignmentUUID(UUID.fromString(value.getFirst("assignmentUUID").trim()))
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
//                    sendFormData.add("docId", String.valueOf(assignmentAttemptEntity.getDocumentUUID()));
//
//                    return assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptEntity.getAssignmentUUID())
//                            .flatMap(assignment -> apiCallService.getDataWithUUID(driveUri + "api/v1/documents/show/", assignmentAttemptEntity.getDocumentUUID())
//                                    .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
//                                            //get user Type from Logged in user (user Type and set in Attempted by to check either its a student or not)
//                                            .flatMap(extension -> apiCallService.getDataWithUUID(authUri + "api/v1/users/show/", UUID.fromString(userUUID))
//                                                    .flatMap(user -> apiCallService.getUserTypeUUID(user)
//                                                            .flatMap(userType -> {
//
//                                                                assignmentAttemptEntity.setAttemptedBy(userType);
//
//                                                                return assignmentAttemptRepository.findFirstByAssignmentUUIDAndAttemptedByAndDeletedAtIsNull(assignmentAttemptEntity.getAssignmentUUID(), assignmentAttemptEntity.getAttemptedBy())
//                                                                        .flatMap(checkMsg -> responseInfoMsg("The Student Has Already Attempted the Assignment"))
//                                                                        .switchIfEmpty(Mono.defer(() -> assignmentAttemptRepository.save(assignmentAttemptEntity)
//                                                                                .flatMap(saveAssignmentAttemptEntity -> apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update")
//                                                                                        .flatMap(documentUpload -> responseSuccessMsg("Record Stored Successfully", saveAssignmentAttemptEntity)))
//                                                                                .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please try again."))
//                                                                                .onErrorResume(err -> responseInfoMsg("Unable to store record. Please contact developer."))
//                                                                        ));
//                                                            }))
//                                                    .switchIfEmpty(responseInfoMsg("The Entered User is not allowed to attempt this Assignment as the user is not student"))
//                                                    .onErrorResume(ex -> responseErrorMsg("The Entered User is not allowed to attempt this Assignment as the user is not student.Please Contact Developer."))
//                                            )).switchIfEmpty(responseInfoMsg("Document Does not exist."))
//                                    .onErrorResume(ex -> responseErrorMsg("Document Does not exist.Please Contact Developer."))
//                            ).switchIfEmpty(responseInfoMsg("Assignment Does not exist."))
//                            .onErrorResume(ex -> responseErrorMsg("Assignment Does not exist.Please Contact Developer."));
//                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
//                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
//    }


    public Mono<AssignmentAttemptAttachmentDto> storeAssignmentAttachmentsDto(AssignmentAttemptEntity assignmentAttemptEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttemptAttachmentDto assignmentDto = AssignmentAttemptAttachmentDto
                .builder()
                .documentDtoList(documentAttachmentDto)
                .id(assignmentAttemptEntity.getId())
                .version(assignmentAttemptEntity.getVersion())
                .uuid(assignmentAttemptEntity.getUuid())
                .comment(assignmentAttemptEntity.getComment())
                .status(assignmentAttemptEntity.getStatus())
                .submissionStatus(assignmentAttemptEntity.getSubmissionStatus())
                .attemptedBy(assignmentAttemptEntity.getAttemptedBy())
                .createdAt(assignmentAttemptEntity.getCreatedAt())
                .createdBy(assignmentAttemptEntity.getCreatedBy())
                .reqCompanyUUID(assignmentAttemptEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentAttemptEntity.getReqBranchUUID())
                .reqCreatedIP(assignmentAttemptEntity.getReqCreatedIP())
                .reqCreatedPort(assignmentAttemptEntity.getReqCreatedPort())
                .reqCreatedBrowser(assignmentAttemptEntity.getReqCreatedBrowser())
                .reqCreatedOS(assignmentAttemptEntity.getReqCreatedOS())
                .reqCreatedDevice(assignmentAttemptEntity.getReqCreatedDevice())
                .reqCreatedReferer(assignmentAttemptEntity.getReqCreatedReferer())
                .build();

        return Mono.just(assignmentDto);
    }

    public Mono<List<DocumentAttachmentDto>> storeDocument(AssignmentAttemptEntity assignmentAttemptEntity, List<String> docListFromFront) {

        List<UUID> l_list = new ArrayList<>();
        for (String getDocumentUUID : docListFromFront) {
            l_list.add(UUID.fromString(getDocumentUUID));
        }

        //Sending Document UUID's in Form data to check if doc UUID exist
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
        for (String listOfValues : docListFromFront) {
            formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
        }

        //posting Documents Ids in Drive Module Document Handler to get Only that document UUID's that exists
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", assignmentAttemptEntity.getCreatedBy().toString(), assignmentAttemptEntity.getReqCompanyUUID().toString(), assignmentAttemptEntity.getReqBranchUUID().toString())
                .flatMap(jsonNode2 -> {
                    //Reading the Response "Data" Object from Json Node
                    final JsonNode arrNode2 = jsonNode2.get("data");

                    Map<String, String> documentMap = new HashMap<String, String>();

                    List<DocumentAttachmentDto> responseAttachments = new LinkedList<>();

                    if (arrNode2.isArray()) {
                        for (final JsonNode objNode : arrNode2) {
                            for (UUID documentIdData : l_list) {
                                JsonNode key = objNode.get(String.valueOf(documentIdData));

                                // create document attachment dto for only
                                if (key != null) {

                                    DocumentAttachmentDto documentAttachments = DocumentAttachmentDto.builder()
                                            .doc_id(UUID.fromString(key.get("docId").toString().replaceAll("\"", "")))
                                            .doc_bucket_uuid(UUID.fromString(key.get("docBucketUUID").toString().replaceAll("\"", "")))
                                            .build();

                                    documentMap.put(documentAttachments.getDoc_id().toString(), documentAttachments.getDoc_bucket_uuid().toString());

                                    responseAttachments.add(documentAttachments);
                                }

                            }
                        }
                    }

                    return assignmentAttemptDocumentRepository.findAllByAssignmentAttemptUUIDAndDocumentUUIDInAndDeletedAtIsNull(assignmentAttemptEntity.getUuid(), l_list)
                            .collectList()
                            .flatMap(removeList -> {

                                for (AssignmentAttemptDocumentEntity pvtEntity : removeList) {
                                    l_list.remove(pvtEntity.getDocumentUUID());
                                    documentMap.remove(pvtEntity.getDocumentUUID().toString());
                                }


                                //List of Document ids to Store in Assignment Attempt Document Pvt Table
                                List<AssignmentAttemptDocumentEntity> listPvt = new ArrayList<AssignmentAttemptDocumentEntity>();

                                //iterating Over the Map Key "Doc Id" and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {

                                    AssignmentAttemptDocumentEntity assignmentDocsPvtEntity = AssignmentAttemptDocumentEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .assignmentAttemptUUID(assignmentAttemptEntity.getUuid())
                                            .createdBy(assignmentAttemptEntity.getCreatedBy())
                                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCompanyUUID(assignmentAttemptEntity.getReqCompanyUUID())
                                            .reqBranchUUID(assignmentAttemptEntity.getReqBranchUUID())
                                            .reqCreatedIP(assignmentAttemptEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentAttemptEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentAttemptEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentAttemptEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentAttemptEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentAttemptEntity.getReqCreatedReferer())
                                            .build();

                                    listPvt.add(assignmentDocsPvtEntity);
                                }

                                //Saving all Pvt Entries in Assignment Document Pvt Table
                                return assignmentAttemptRepository.save(assignmentAttemptEntity)
                                        .then(assignmentAttemptDocumentRepository.saveAll(listPvt)
                                                .collectList()
                                                .flatMap(assignmentAttemptDocPvt -> {
                                                    //Creating Final Document List to Update the Status
                                                    List<UUID> finalDocumentList = new ArrayList<>();

                                                    for (AssignmentAttemptDocumentEntity pvtData : assignmentAttemptDocPvt) {
                                                        finalDocumentList.add(pvtData.getDocumentUUID());
                                                    }

                                                    //Empty List for Document Ids from Json Request user Enters
                                                    List<String> listOfDoc = new ArrayList<>();

                                                    finalDocumentList.forEach(uuid -> {
                                                        if (uuid != null) {
                                                            listOfDoc.add(uuid.toString());
                                                        }
                                                    });


                                                    //Sending Document ids in Form data to check if document Id's exist
                                                    MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                                    for (String listOfDocumentUUID : listOfDoc) {
                                                        sendFormData.add("docId", listOfDocumentUUID);//iterating over multiple values and then adding in list
                                                    }

                                                    return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", assignmentAttemptEntity.getCreatedBy().toString(), assignmentAttemptEntity.getReqCompanyUUID().toString(), assignmentAttemptEntity.getReqBranchUUID().toString())
                                                            .flatMap(document -> Mono.just(responseAttachments));
                                                }).flatMap(test -> Mono.just(responseAttachments)));
                            });
                });
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_store")
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

                    AssignmentAttemptEntity assignmentAttemptEntity = AssignmentAttemptEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .comment(value.getFirst("comment").trim())
                            .assignmentUUID(UUID.fromString(value.getFirst("assignmentUUID").trim()))
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


                    //get user Type from Login user (user Type and set in Attempted by to check either its a student or not)
                    return apiCallService.getDataWithUUID(authUri + "api/v1/users/show-user-type/", assignmentAttemptEntity.getCreatedBy())
                            .flatMap(userJson -> {
                                        if (Objects.equals(apiCallService.getAccessLevelSlug(userJson), "student")) {
                                            return apiCallService.getUserTypeUUID(userJson)
                                                    .flatMap(userType -> {

                                                        assignmentAttemptEntity.setAttemptedBy(userType);

                                                        return assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptEntity.getAssignmentUUID())
                                                                //Check If Student Has already attempted the assignment
                                                                .flatMap(assignment -> assignmentAttemptRepository.findFirstByAssignmentUUIDAndAttemptedByAndDeletedAtIsNull(assignmentAttemptEntity.getAssignmentUUID(), assignmentAttemptEntity.getAttemptedBy())
                                                                        .flatMap(checkMsg -> responseInfoMsg("The Student Has Already Attempted the Assignment"))
                                                                        .switchIfEmpty(Mono.defer(() -> {
                                                                                    List<String> documentList = value.get("documentUUID");

                                                                                    documentList.removeIf(s -> s.equals(""));

                                                                                    if (assignment.getLock().equals(true)) {
                                                                                        return responseInfoMsg("Unable to Attempt Assignment as this Assignment is locked now!");
                                                                                    } else {
                                                                                        if (documentList.isEmpty()) {
                                                                                            return responseInfoMsg("Please Upload Assignment Attempt Documents!");
                                                                                        } else {

                                                                                            //Check if the Attachment Assignment Extended date has passed or not
                                                                                            if (assignment.getExtendedDate() != null && LocalDate.now().isAfter(assignment.getExtendedDate().toLocalDate())) {
                                                                                                return responseInfoMsg("Unable to Attempt Assignment as Due Date and Extended Date has passed.");
                                                                                            } else if (LocalDate.now().isAfter(assignment.getToDate().toLocalDate())) {
                                                                                                return responseInfoMsg("Unable to Attempt Assignment as Due Date has passed.");
                                                                                            } else {
                                                                                                //As Student has attempted the Assignment so submission status is TRUE
                                                                                                assignmentAttemptEntity.setSubmissionStatus(true);

                                                                                                return storeDocument(assignmentAttemptEntity, documentList)
                                                                                                        .flatMap(docs -> storeAssignmentAttachmentsDto(assignmentAttemptEntity, docs)
                                                                                                                .flatMap(responseDto -> responseSuccessMsg("Record Stored Successfully", responseDto)))
                                                                                                        .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                                                        .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                }
                                                                        ))
                                                                ).switchIfEmpty(responseInfoMsg("Assignment Does not exist."))
                                                                .onErrorResume(ex -> responseErrorMsg("Assignment Does not exist.Please Contact Developer."));
                                                    });
                                        } else {
                                            return responseInfoMsg("The Entered User is not allowed to Create Assignment As he/she is not employee.");
                                        }
                                    }
                            ).switchIfEmpty(responseInfoMsg("Unable to fetch user Record"))
                            .onErrorResume(ex -> responseErrorMsg("Unable to fetch user Record.Please Contact Developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    public Mono<List<DocumentAttachmentDto>> updateDocument(AssignmentAttemptEntity previousAssignmentAttemptEntity, AssignmentAttemptEntity assignmentAttemptEntity, List<String> docListFromFront, String userId) {

        List<UUID> l_list = new ArrayList<>();
        for (String getDocumentUUID : docListFromFront) {
            l_list.add(UUID.fromString(getDocumentUUID));
        }

        //Sending Doc Ids in Form data to check if doc Ids exist
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
        for (String listOfValues : docListFromFront) {
            formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
        }

        //posting Doc Ids in Drive Module Document Handler to get only those doc Ids that exists
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", userId, assignmentAttemptEntity.getReqCompanyUUID().toString(), assignmentAttemptEntity.getReqBranchUUID().toString())
                .flatMap(jsonNode2 -> {
                    //Reading the Response "Data" Object from Json Node
                    final JsonNode arrNode2 = jsonNode2.get("data");

                    Map<String, String> documentMap = new HashMap<String, String>();

                    List<DocumentAttachmentDto> responseAttachments = new LinkedList<>();

                    if (arrNode2.isArray()) {
                        for (final JsonNode objNode : arrNode2) {
                            for (UUID documentIdData : l_list) {
                                JsonNode key = objNode.get(String.valueOf(documentIdData));
                                if (key != null) {

                                    DocumentAttachmentDto documentAttachments = DocumentAttachmentDto.builder()
                                            .doc_id(UUID.fromString(key.get("docId").toString().replaceAll("\"", "")))
                                            .doc_bucket_uuid(UUID.fromString(key.get("docBucketUUID").toString().replaceAll("\"", "")))
                                            .build();

                                    documentMap.put(documentAttachments.getDoc_id().toString(), documentAttachments.getDoc_bucket_uuid().toString());

                                    responseAttachments.add(documentAttachments);
                                }
                            }
                        }
                    }

                    l_list.removeAll(Arrays.asList("", null));

                    return assignmentAttemptDocumentRepository.findAllByAssignmentAttemptUUIDAndDeletedAtIsNull(assignmentAttemptEntity.getUuid())
                            .collectList()
                            .flatMap(removeListOfOldPvt -> {

                                for (AssignmentAttemptDocumentEntity previousAssignmentAttemptDocEntity : removeListOfOldPvt) {
                                    previousAssignmentAttemptDocEntity.setDeletedAt(assignmentAttemptEntity.getUpdatedAt());
                                    previousAssignmentAttemptDocEntity.setDeletedBy(assignmentAttemptEntity.getUpdatedBy());
                                    previousAssignmentAttemptDocEntity.setReqDeletedIP(assignmentAttemptEntity.getReqUpdatedIP());
                                    previousAssignmentAttemptDocEntity.setReqDeletedPort(assignmentAttemptEntity.getReqUpdatedPort());
                                    previousAssignmentAttemptDocEntity.setReqDeletedBrowser(assignmentAttemptEntity.getReqUpdatedBrowser());
                                    previousAssignmentAttemptDocEntity.setReqDeletedOS(assignmentAttemptEntity.getReqUpdatedOS());
                                    previousAssignmentAttemptDocEntity.setReqDeletedDevice(assignmentAttemptEntity.getReqUpdatedDevice());
                                    previousAssignmentAttemptDocEntity.setReqDeletedReferer(assignmentAttemptEntity.getReqUpdatedReferer());
                                }

                                //List of Document ids to Store in Assignment Attempt Document Pvt Table
                                List<AssignmentAttemptDocumentEntity> listPvt = new ArrayList<AssignmentAttemptDocumentEntity>();

                                //iterating Over the Map Key Document Ids and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {

                                    AssignmentAttemptDocumentEntity assignmentAttemptDocumentPvtEntity = AssignmentAttemptDocumentEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .assignmentAttemptUUID(assignmentAttemptEntity.getUuid())
                                            .createdBy(assignmentAttemptEntity.getCreatedBy())
                                            .createdAt(assignmentAttemptEntity.getCreatedAt())
                                            .updatedBy(UUID.fromString(userId))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(assignmentAttemptEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentAttemptEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentAttemptEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentAttemptEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentAttemptEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentAttemptEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(assignmentAttemptEntity.getReqCompanyUUID())
                                            .reqBranchUUID(assignmentAttemptEntity.getReqBranchUUID())
                                            .reqCreatedIP(assignmentAttemptEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentAttemptEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentAttemptEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentAttemptEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentAttemptEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentAttemptEntity.getReqCreatedReferer())
                                            .reqUpdatedIP(assignmentAttemptEntity.getReqUpdatedIP())
                                            .reqUpdatedPort(assignmentAttemptEntity.getReqUpdatedPort())
                                            .reqUpdatedBrowser(assignmentAttemptEntity.getReqUpdatedBrowser())
                                            .reqUpdatedOS(assignmentAttemptEntity.getReqUpdatedOS())
                                            .reqUpdatedDevice(assignmentAttemptEntity.getReqUpdatedDevice())
                                            .reqUpdatedReferer(assignmentAttemptEntity.getReqUpdatedReferer())
                                            .build();

                                    listPvt.add(assignmentAttemptDocumentPvtEntity);
                                }


                                //Saving all Pvt Entries in Transaction Document Pvt Table
                                return assignmentAttemptRepository.save(previousAssignmentAttemptEntity)
                                        .then(assignmentAttemptDocumentRepository.saveAll(removeListOfOldPvt)
                                                .collectList())
                                        .then(assignmentAttemptRepository.save(assignmentAttemptEntity))
                                        .then(assignmentAttemptDocumentRepository.saveAll(listPvt)
                                                .collectList())
                                        .flatMap(assignmentDocPvt -> {

                                            //Creating Final Document List to Update the Status
                                            List<UUID> finalDocumentList = new ArrayList<>();

                                            for (AssignmentAttemptDocumentEntity pvtData : assignmentDocPvt) {
                                                finalDocumentList.add(pvtData.getDocumentUUID());
                                            }

                                            //Empty List for Doc Ids from Json Request user Enters
                                            List<String> listOfDoc = new ArrayList<>();

                                            finalDocumentList.forEach(uuid -> {
                                                if (uuid != null) {
                                                    listOfDoc.add(uuid.toString());
                                                }
                                            });


                                            //Sending Doc Ids in Form data to check if doc Ids exist
                                            MultiValueMap<String, String> sendFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                            for (String listOfDocumentUUID : listOfDoc) {
                                                sendFormData.add("docId", listOfDocumentUUID);   //iterating over multiple values and then adding in list
                                            }
                                            return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userId, assignmentAttemptEntity.getReqCompanyUUID().toString(), assignmentAttemptEntity.getReqBranchUUID().toString())
                                                    .flatMap(document -> Mono.just(responseAttachments));
                                        })
                                        .flatMap(test -> Mono.just(responseAttachments));
                            });
                });
    }

    public Mono<AssignmentAttemptAttachmentDto> updateAssignmentAttachmentsDto(AssignmentAttemptEntity previousAssignmentAttempt, AssignmentAttemptEntity assignmentAttemptEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttemptAttachmentDto assignmentDto = AssignmentAttemptAttachmentDto
                .builder()
                .documentDtoList(documentAttachmentDto)
                .id(previousAssignmentAttempt.getId())
                .version(assignmentAttemptEntity.getVersion())
                .uuid(previousAssignmentAttempt.getUuid())
                .submissionStatus(assignmentAttemptEntity.getSubmissionStatus())
                .assignmentUUID(assignmentAttemptEntity.getAssignmentUUID())
                .status(assignmentAttemptEntity.getStatus())
                .createdAt(previousAssignmentAttempt.getCreatedAt())
                .createdBy(previousAssignmentAttempt.getCreatedBy())
                .reqCreatedIP(previousAssignmentAttempt.getReqCreatedIP())
                .reqCreatedPort(previousAssignmentAttempt.getReqCreatedPort())
                .reqCreatedBrowser(previousAssignmentAttempt.getReqCreatedBrowser())
                .reqCreatedOS(previousAssignmentAttempt.getReqCreatedOS())
                .reqCreatedDevice(previousAssignmentAttempt.getReqCreatedDevice())
                .reqCreatedReferer(previousAssignmentAttempt.getReqCreatedReferer())
                .updatedBy(assignmentAttemptEntity.getUpdatedBy())
                .updatedAt(assignmentAttemptEntity.getUpdatedAt())
                .reqCompanyUUID(assignmentAttemptEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentAttemptEntity.getReqBranchUUID())
                .reqUpdatedIP(assignmentAttemptEntity.getReqUpdatedIP())
                .reqUpdatedPort(assignmentAttemptEntity.getReqUpdatedPort())
                .reqUpdatedBrowser(assignmentAttemptEntity.getReqUpdatedBrowser())
                .reqUpdatedOS(assignmentAttemptEntity.getReqUpdatedOS())
                .reqUpdatedDevice(assignmentAttemptEntity.getReqUpdatedDevice())
                .reqUpdatedReferer(assignmentAttemptEntity.getReqUpdatedReferer())
                .build();

        return Mono.just(assignmentDto);
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID assignmentAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                .flatMap(value -> assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptUUID)
                        .flatMap(previousEntity -> {

                            AssignmentAttemptEntity updatedAssignmentAttemptEntity = AssignmentAttemptEntity
                                    .builder()
                                    .uuid(previousEntity.getUuid())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .submissionStatus(Boolean.valueOf(value.getFirst("submissionStatus")))
                                    .comment(value.getFirst("comment").trim())
                                    .assignmentUUID(UUID.fromString(value.getFirst("assignmentUUID").trim()))
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

                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousEntity.setReqDeletedIP(reqIp);
                            previousEntity.setReqDeletedPort(reqPort);
                            previousEntity.setReqDeletedBrowser(reqBrowser);
                            previousEntity.setReqDeletedOS(reqOs);
                            previousEntity.setReqDeletedDevice(reqDevice);
                            previousEntity.setReqDeletedReferer(reqReferer);

                            //get user Type from Login user (user Type and set in Attempted by to check either its a student or not)
                            return apiCallService.getDataWithUUID(authUri + "api/v1/users/show-user-type/", updatedAssignmentAttemptEntity.getUpdatedBy())
                                    .flatMap(userJson -> {
                                                if (Objects.equals(apiCallService.getAccessLevelSlug(userJson), "student")) {
                                                    return apiCallService.getUserTypeUUID(userJson)
                                                            .flatMap(userType -> {

                                                                updatedAssignmentAttemptEntity.setAttemptedBy(userType);

                                                                return assignmentRepository.findByUuidAndDeletedAtIsNull(updatedAssignmentAttemptEntity.getAssignmentUUID())
                                                                        //Check If Student Has already attempted the assignment
                                                                        .flatMap(assignment -> assignmentAttemptRepository.findFirstByAssignmentUUIDAndAttemptedByAndDeletedAtIsNullAndUuidIsNot(updatedAssignmentAttemptEntity.getAssignmentUUID(), updatedAssignmentAttemptEntity.getAttemptedBy(), assignmentAttemptUUID)
                                                                                .flatMap(checkMsg -> responseInfoMsg("The Student Has Already Attempted the Assignment"))
                                                                                .switchIfEmpty(Mono.defer(() -> {
                                                                                            List<String> documentList = value.get("documentUUID");

                                                                                            documentList.removeIf(s -> s.equals(""));

                                                                                            if (assignment.getLock().equals(true)) {
                                                                                                return responseInfoMsg("Unable to Attempt Assignment as this Assignment is locked now!");
                                                                                            } else {
                                                                                                if (documentList.isEmpty()) {
                                                                                                    return responseInfoMsg("Please Upload Assignment Attempt Documents!");
                                                                                                } else {

                                                                                                    //Check if the Attachment Assignment Extended date has passed or not
                                                                                                    if (assignment.getExtendedDate() != null && LocalDate.now().isAfter(assignment.getExtendedDate().toLocalDate())) {
                                                                                                        return responseInfoMsg("Unable to Attempt Assignment as Due Date and Extended Date has passed.");
                                                                                                    } else if (LocalDate.now().isAfter(assignment.getToDate().toLocalDate())) {
                                                                                                        return responseInfoMsg("Unable to Attempt Assignment as Due Date has passed.");
                                                                                                    } else {
                                                                                                        //As Student has attempted the Assignment so submission status is TRUE
                                                                                                        updatedAssignmentAttemptEntity.setSubmissionStatus(true);

                                                                                                        return updateDocument(previousEntity, updatedAssignmentAttemptEntity, documentList, updatedAssignmentAttemptEntity.getCreatedBy().toString())
                                                                                                                .flatMap(docsAttachedDto -> updateAssignmentAttachmentsDto(previousEntity, updatedAssignmentAttemptEntity, docsAttachedDto)
                                                                                                                        .flatMap(updateAssignmentAttachmentDto -> responseSuccessMsg("Record Updated Successfully!", updateAssignmentAttachmentDto))
                                                                                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Record there is something wrong please try again."))
                                                                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                                                                                                ).switchIfEmpty(responseInfoMsg("Unable to Update Record there is something wrong please try again."))
                                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."));
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                ))
                                                                        ).switchIfEmpty(responseInfoMsg("Assignment Does not exist."))
                                                                        .onErrorResume(ex -> responseErrorMsg("Assignment Does not exist.Please Contact Developer."));
                                                            });
                                                } else {
                                                    return responseInfoMsg("The Entered User is not allowed to Create Assignment As he/she is not employee.");
                                                }
                                            }
                                    ).switchIfEmpty(responseInfoMsg("Unable to fetch user Record"))
                                    .onErrorResume(ex -> responseErrorMsg("Unable to fetch user Record.Please Contact Developer."));
                        }).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID assignmentAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
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
                    return assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptUUID)
                            .flatMap(previousEntity -> {

                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                AssignmentAttemptEntity updatedAssignmentAttemptEntity = AssignmentAttemptEntity
                                        .builder()
                                        .uuid(previousEntity.getUuid())
                                        .status(status == true ? true : false)
                                        .submissionStatus(previousEntity.getSubmissionStatus())
                                        .comment(previousEntity.getComment())
                                        .attemptedBy(previousEntity.getAttemptedBy())
                                        .assignmentUUID(previousEntity.getAssignmentUUID())
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

                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);

                                return assignmentAttemptRepository.save(previousEntity)
                                        .then(assignmentAttemptRepository.save(updatedAssignmentAttemptEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    public Mono<AssignmentAttemptAttachmentDto> deleteAssignmentAttachmentsDto(AssignmentAttemptEntity assignmentAttemptEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttemptAttachmentDto assignmentDto = AssignmentAttemptAttachmentDto
                .builder()
                .documentDtoList(documentAttachmentDto)
                .id(assignmentAttemptEntity.getId())
                .version(assignmentAttemptEntity.getVersion())
                .uuid(assignmentAttemptEntity.getUuid())
                .comment(assignmentAttemptEntity.getComment())
                .status(assignmentAttemptEntity.getStatus())
                .submissionStatus(assignmentAttemptEntity.getSubmissionStatus())
                .assignmentUUID(assignmentAttemptEntity.getAssignmentUUID())
                .createdAt(assignmentAttemptEntity.getCreatedAt())
                .createdBy(assignmentAttemptEntity.getCreatedBy())
                .reqCreatedIP(assignmentAttemptEntity.getReqCreatedIP())
                .reqCreatedPort(assignmentAttemptEntity.getReqCreatedPort())
                .reqCreatedBrowser(assignmentAttemptEntity.getReqCreatedBrowser())
                .reqCreatedOS(assignmentAttemptEntity.getReqCreatedOS())
                .reqCreatedDevice(assignmentAttemptEntity.getReqCreatedDevice())
                .reqCreatedReferer(assignmentAttemptEntity.getReqCreatedReferer())
                .updatedBy(assignmentAttemptEntity.getUpdatedBy())
                .updatedAt(assignmentAttemptEntity.getUpdatedAt())
                .reqCompanyUUID(assignmentAttemptEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentAttemptEntity.getReqBranchUUID())
                .reqUpdatedIP(assignmentAttemptEntity.getReqUpdatedIP())
                .reqUpdatedPort(assignmentAttemptEntity.getReqUpdatedPort())
                .reqUpdatedBrowser(assignmentAttemptEntity.getReqUpdatedBrowser())
                .reqUpdatedOS(assignmentAttemptEntity.getReqUpdatedOS())
                .reqUpdatedDevice(assignmentAttemptEntity.getReqUpdatedDevice())
                .reqUpdatedReferer(assignmentAttemptEntity.getReqUpdatedReferer())
                .deletedBy(assignmentAttemptEntity.getDeletedBy())
                .deletedAt(assignmentAttemptEntity.getDeletedAt())
                .reqDeletedDevice(assignmentAttemptEntity.getReqDeletedDevice())
                .reqDeletedIP(assignmentAttemptEntity.getReqDeletedIP())
                .reqDeletedBrowser(assignmentAttemptEntity.getReqDeletedBrowser())
                .reqDeletedPort(assignmentAttemptEntity.getReqDeletedPort())
                .reqDeletedReferer(assignmentAttemptEntity.getReqDeletedReferer())
                .reqDeletedOS(assignmentAttemptEntity.getReqDeletedOS())
                .build();

        return Mono.just(assignmentDto);
    }

    @AuthHasPermission(value = "lms_api_v1_student_assignment-attempts_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID assignmentAttemptUUID = UUID.fromString(serverRequest.pathVariable("assignmentAttemptUUID"));
        String userId = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown user");
        }

        return assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptUUID)
                .flatMap(assignmentAttemptEntity -> assignmentAttemptDocumentRepository
                        .findAllByAssignmentAttemptUUIDAndDeletedAtIsNull(assignmentAttemptEntity.getUuid())
                        .collectList()
                        .flatMap(assignmentAttemptDocEntity -> {

                            List<AssignmentAttemptDocumentEntity> pvtEntity = new ArrayList<>();

                            List<DocumentAttachmentDto> documentDtos = new ArrayList<>();

                            assignmentAttemptEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            assignmentAttemptEntity.setDeletedBy(UUID.fromString(userId));
                            assignmentAttemptEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            assignmentAttemptEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            assignmentAttemptEntity.setReqDeletedIP(reqIp);
                            assignmentAttemptEntity.setReqDeletedPort(reqPort);
                            assignmentAttemptEntity.setReqDeletedBrowser(reqBrowser);
                            assignmentAttemptEntity.setReqDeletedOS(reqOs);
                            assignmentAttemptEntity.setReqDeletedDevice(reqDevice);
                            assignmentAttemptEntity.setReqDeletedReferer(reqReferer);

                            for (AssignmentAttemptDocumentEntity assignAttemptDocs : assignmentAttemptDocEntity) {
                                assignAttemptDocs.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                assignAttemptDocs.setDeletedBy(UUID.fromString(userId));
                                assignAttemptDocs.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                assignAttemptDocs.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                assignAttemptDocs.setReqDeletedIP(reqIp);
                                assignAttemptDocs.setReqDeletedPort(reqPort);
                                assignAttemptDocs.setReqDeletedBrowser(reqBrowser);
                                assignAttemptDocs.setReqDeletedOS(reqOs);
                                assignAttemptDocs.setReqDeletedDevice(reqDevice);
                                assignAttemptDocs.setReqDeletedReferer(reqReferer);

                                pvtEntity.add(assignAttemptDocs);

                                DocumentAttachmentDto documentAttachmentDto = DocumentAttachmentDto
                                        .builder()
                                        .doc_bucket_uuid(assignAttemptDocs.getBucketUUID())
                                        .doc_id(assignAttemptDocs.getDocumentUUID())
                                        .build();

                                documentDtos.add(documentAttachmentDto);
                            }

                            return assignmentAttemptRepository.save(assignmentAttemptEntity)
                                    .then(assignmentAttemptDocumentRepository.saveAll(pvtEntity)
                                            .collectList())
                                    .flatMap(deleteEntity -> deleteAssignmentAttachmentsDto(assignmentAttemptEntity, documentDtos)
                                            .flatMap(delDto -> responseSuccessMsg("Record Deleted Successfully!", delDto))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."))
                                    );
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Assignment Attempt does not exist"))
                .onErrorResume(err -> responseErrorMsg("Assignment Attempt does not exist.Please Contact Developer."));
    }
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID assignmentAttemptUUID = UUID.fromString(serverRequest.pathVariable("uuid"));
//
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
//        return assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptUUID)
//                .flatMap(assignmentAttemptEntity -> {
//
//                    assignmentAttemptEntity.setDeletedBy(UUID.fromString(userUUID));
//                    assignmentAttemptEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                    assignmentAttemptEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
//                    assignmentAttemptEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
//                    assignmentAttemptEntity.setReqDeletedIP(reqIp);
//                    assignmentAttemptEntity.setReqDeletedPort(reqPort);
//                    assignmentAttemptEntity.setReqDeletedBrowser(reqBrowser);
//                    assignmentAttemptEntity.setReqDeletedOS(reqOs);
//                    assignmentAttemptEntity.setReqDeletedDevice(reqDevice);
//                    assignmentAttemptEntity.setReqDeletedReferer(reqReferer);
//
//                    return assignmentAttemptRepository.save(assignmentAttemptEntity)
//                            .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                            .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
//                            .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                }).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
//                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
//    }

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
