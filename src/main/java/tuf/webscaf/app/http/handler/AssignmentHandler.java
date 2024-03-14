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
import tuf.webscaf.app.dbContext.master.dto.AssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.master.dto.DocumentAttachmentDto;
import tuf.webscaf.app.dbContext.master.entity.AssignmentDocumentEntity;
import tuf.webscaf.app.dbContext.master.entity.AssignmentEntity;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentAttemptRepository;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentDocumentRepository;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAssignmentDocumentRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAssignmentRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "assignmentHandler")
@Component
public class AssignmentHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    AssignmentRepository assignmentRepository;

    @Autowired
    AssignmentAttemptRepository assignmentAttemptRepository;

    @Autowired
    AssignmentDocumentRepository assignmentDocumentRepository;

    @Autowired
    SlaveAssignmentDocumentRepository slaveAssignmentDocumentRepository;

    @Autowired
    SlaveAssignmentRepository slaveAssignmentRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.zone}")
    private String zone;

    @Value("${server.erp_drive_module.uri}")
    private String driveUri;

    @Value("${server.erp_academic_module.uri}")
    private String academicUri;

    @Value("${server.erp_auth_module.uri}")
    private String authUri;

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_index")
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String teacherUUID = serverRequest.queryParam("teacherUUID").map(String::toString).orElse("").trim();

        String courseSubjectUUID = serverRequest.queryParam("courseSubjectUUID").map(String::toString).orElse("").trim();

        String academicSessionUUID = serverRequest.queryParam("academicSessionUUID").map(String::toString).orElse("").trim();


        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        // return assignment based on teacher, courseSubject, academic-session and status
        if (!status.isEmpty() && !teacherUUID.isEmpty() && !courseSubjectUUID.isEmpty() && !academicSessionUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithStatus(UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID), Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
        // return assignment based on teacher, courseSubject and academic-session
        else if (!teacherUUID.isEmpty() && !courseSubjectUUID.isEmpty() && !academicSessionUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithoutStatus(UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndAcademicSessionUUIDAndDeletedAtIsNull
                                    (searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID), searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), UUID.fromString(academicSessionUUID))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })


                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//        return assignment based on teacher, courseSubject and status
        else if (!status.isEmpty() && !teacherUUID.isEmpty() && !courseSubjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherAndCourseSubjectWithStatus(UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countByNameContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndTeacherUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })


                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
        //        return assignment based on academic-session, courseSubject and status
        else if (!status.isEmpty() && !academicSessionUUID.isEmpty() && !courseSubjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstAcademicSessionAndCourseSubjectWithStatus(UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countByNameContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndStatusAndDeletedAtIsNull
                                    (searchKeyWord, UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID), Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })


                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
        //        return assignment based on teacher and courseSubject
        else if (!academicSessionUUID.isEmpty() && !courseSubjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstAcademicSessionAndCourseSubjectWithoutStatus(UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countByNameContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndDeletedAtIsNullOrInstructionContainingIgnoreCaseAndAcademicSessionUUIDAndCourseSubjectUUIDAndDeletedAtIsNull
                                    (searchKeyWord, UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID), searchKeyWord, UUID.fromString(academicSessionUUID), UUID.fromString(courseSubjectUUID))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })


                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignment based on teacher, courseSubject
        else if (!teacherUUID.isEmpty() && !courseSubjectUUID.isEmpty()) {

            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherAndCourseSubjectWithoutStatus(UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstTeacherAndCourseSubjectWithoutStatus(UUID.fromString(teacherUUID), UUID.fromString(courseSubjectUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignment based on courseSubject and status
        else if (!status.isEmpty() && !courseSubjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstCourseSubjectWithStatus(UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstCourseSubjectWithStatus(UUID.fromString(courseSubjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
        //      return assignment based on academic-session and status
        else if (!status.isEmpty() && !academicSessionUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstAcademicSessionWithStatus(UUID.fromString(academicSessionUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstAcademicSessionWithStatus(UUID.fromString(academicSessionUUID), searchKeyWord, searchKeyWord, Boolean.valueOf(status))
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }

        //      return assignment based on teacher and status
        else if (!status.isEmpty() && !teacherUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherWithStatus(UUID.fromString(teacherUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstTeacherWithStatus(UUID.fromString(teacherUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignment based on courseSubject
        else if (!courseSubjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstCourseSubjectWithoutStatus(UUID.fromString(courseSubjectUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstCourseSubjectWithoutStatus(UUID.fromString(courseSubjectUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }

        //      return assignment based on academic-Session
        else if (!academicSessionUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstCourseSubjectWithoutStatus(UUID.fromString(academicSessionUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstAcademicSessionWithoutStatus(UUID.fromString(academicSessionUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }

//      return assignment based on teacher
        else if (!teacherUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstTeacherWithoutStatus(UUID.fromString(teacherUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstTeacherWithoutStatus(UUID.fromString(teacherUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignment based on status
        else if (!status.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexWithStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsWithStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignments
        else {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexWithoutStatus(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsWithoutStatus(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_attempted-students_marks_facade_index")
    public Mono<ServerResponse> indexEnrolledStudentAssignmentAttemptAndMarks(ServerRequest serverRequest) {
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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");
//
        String assignmentUUID = serverRequest.queryParam("assignmentUUID").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));


        Flux<SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> slaveAssignmentFlux = slaveAssignmentRepository
                .indexAllRecords(UUID.fromString(assignmentUUID), searchKeyWord, searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());
        return slaveAssignmentFlux
                .collectList()
                .flatMap(assignmentEntity -> slaveAssignmentRepository
                        .countAllEnrolledStudentsRecordAssignmentAttemptMarksFacadeCount(UUID.fromString(assignmentUUID),searchKeyWord, searchKeyWord)
                        .flatMap(count -> {
                            if (assignmentEntity.isEmpty()) {
                                return responseIndexInfoMsg("Record does not exist", count);
                            } else {
                                return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                            }
                        })
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }


    @AuthHasPermission(value = "lms_api_v1_teacher_subjects_assignments_index")
    public Mono<ServerResponse> indexAssignmentsAgainstSubject(ServerRequest serverRequest) {

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String subjectUUID = serverRequest.queryParam("subjectUUID").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

//        return assignment based on teacher, courseSubject and status
        if (!status.isEmpty() && !subjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstSubjectWithStatus(UUID.fromString(subjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstSubjectWithStatus(UUID.fromString(subjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
//      return assignments
        else if (!subjectUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAgainstSubjectWithoutStatus(UUID.fromString(subjectUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstSubjectWithoutStatus(UUID.fromString(subjectUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexWithStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsWithStatus(Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexWithoutStatus(searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsWithoutStatus(searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    //    This Route return assignments against student, course and subject
    @AuthHasPermission(value = "lms_api_v1_student_assignments_student_course_subject_index")
    public Mono<ServerResponse> showAssignmentsAgainstStudentCourseSubject(ServerRequest serverRequest) {

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

        String directionProperty = serverRequest.queryParam("dp").map(String::toString).orElse("created_at");

        String status = serverRequest.queryParam("status").map(String::toString).orElse("").trim();

        String studentUUID = serverRequest.queryParam("studentUUID").map(String::toString).orElse("").trim();

        String courseUUID = serverRequest.queryParam("courseUUID").map(String::toString).orElse("").trim();

        String subjectUUID = serverRequest.queryParam("subjectUUID").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty() && !subjectUUID.isEmpty() && !courseUUID.isEmpty() && !studentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAssignmentsAgainstStudentCourseSubjectWithStatus(UUID.fromString(studentUUID), UUID.fromString(courseUUID), UUID.fromString(subjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstStudentCourseSubjectWithStatus(UUID.fromString(studentUUID), UUID.fromString(courseUUID), UUID.fromString(subjectUUID), Boolean.valueOf(status), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveAssignmentAttachmentDto> slaveAssignmentFlux = slaveAssignmentRepository
                    .indexAssignmentsAgainstStudentCourseSubjectWithoutStatus(UUID.fromString(studentUUID), UUID.fromString(courseUUID), UUID.fromString(subjectUUID), searchKeyWord, searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentFlux
                    .collectList()
                    .flatMap(assignmentEntity -> slaveAssignmentRepository
                            .countAssignmentsAgainstStudentCourseSubjectWithoutStatus(UUID.fromString(studentUUID), UUID.fromString(courseUUID), UUID.fromString(subjectUUID), searchKeyWord, searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentEntity.stream().distinct(), count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    //Check Document UUID In Drive Module in Delete Function to Check Existence
    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_documents_show")
    public Mono<ServerResponse> getDocumentUUID(ServerRequest serverRequest) {
        final UUID documentUUID = UUID.fromString(serverRequest.pathVariable("documentUUID"));

        return serverRequest.formData()
                .flatMap(value -> slaveAssignmentDocumentRepository.findFirstByDocumentUUIDAndDeletedAtIsNull(documentUUID)
                        .flatMap(value1 -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
                ).switchIfEmpty(responseErrorMsg("Record Does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record Does not exist.Please Contact Developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID assignmentUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveAssignmentRepository.showAssignmentRecordAgainstUUID(assignmentUUID)
                .collectList()
                .flatMap(assignmentDocumentDto -> responseSuccessMsg("Record Fetched Successfully", assignmentDocumentDto.stream().distinct()))
                .switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."));
    }

    public Mono<AssignmentAttachmentDto> storeAssignmentAttachmentsDto(AssignmentEntity assignmentEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttachmentDto assignmentDto = AssignmentAttachmentDto.builder()
                .documentDtoList(documentAttachmentDto)
                .id(assignmentEntity.getId())
                .version(assignmentEntity.getVersion())
                .uuid(assignmentEntity.getUuid())
                .name(assignmentEntity.getName())
                .instruction(assignmentEntity.getInstruction())
                .toDate(assignmentEntity.getToDate())
                .fromDate(assignmentEntity.getFromDate())
                .extendedDate(assignmentEntity.getExtendedDate())
                .totalMark(assignmentEntity.getTotalMark())
                .courseSubjectUUID(assignmentEntity.getCourseSubjectUUID())
                .teacherUUID(assignmentEntity.getTeacherUUID())
                .academicSessionUUID(assignmentEntity.getAcademicSessionUUID())
                .status(assignmentEntity.getStatus())
                .createdAt(assignmentEntity.getCreatedAt())
                .createdBy(assignmentEntity.getCreatedBy())
                .reqCompanyUUID(assignmentEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentEntity.getReqBranchUUID())
                .reqCreatedIP(assignmentEntity.getReqCreatedIP())
                .reqCreatedPort(assignmentEntity.getReqCreatedPort())
                .reqCreatedBrowser(assignmentEntity.getReqCreatedBrowser())
                .reqCreatedOS(assignmentEntity.getReqCreatedOS())
                .reqCreatedDevice(assignmentEntity.getReqCreatedDevice())
                .reqCreatedReferer(assignmentEntity.getReqCreatedReferer())
                .build();

        return Mono.just(assignmentDto);
    }

    public Mono<List<DocumentAttachmentDto>> storeDocument(AssignmentEntity assignmentEntity, List<String> docListFromFront) {

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
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", assignmentEntity.getCreatedBy().toString(), assignmentEntity.getReqCompanyUUID().toString(), assignmentEntity.getReqBranchUUID().toString())
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

                    return assignmentDocumentRepository.findAllByAssignmentUUIDAndDocumentUUIDInAndDeletedAtIsNull(assignmentEntity.getUuid(), l_list)
                            .collectList()
                            .flatMap(removeList -> {

                                for (AssignmentDocumentEntity pvtEntity : removeList) {
                                    l_list.remove(pvtEntity.getDocumentUUID());
                                    documentMap.remove(pvtEntity.getDocumentUUID().toString());
                                }


                                //List of Document ids to Store in Assignment Document Pvt Table
                                List<AssignmentDocumentEntity> listPvt = new ArrayList<AssignmentDocumentEntity>();

                                //iterating Over the Map Key "Doc Id" and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {

                                    AssignmentDocumentEntity assignmentDocsPvtEntity = AssignmentDocumentEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .assignmentUUID(assignmentEntity.getUuid())
                                            .createdBy(assignmentEntity.getCreatedBy())
                                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCompanyUUID(assignmentEntity.getReqCompanyUUID())
                                            .reqBranchUUID(assignmentEntity.getReqBranchUUID())
                                            .reqCreatedIP(assignmentEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentEntity.getReqCreatedReferer())
                                            .build();

                                    listPvt.add(assignmentDocsPvtEntity);
                                }

                                //Saving all Pvt Entries in Assignment Document Pvt Table
                                return assignmentRepository.save(assignmentEntity)
                                        .then(assignmentDocumentRepository.saveAll(listPvt)
                                                .collectList()
                                                .flatMap(assignmentDocPvt -> {
                                                    //Creating Final Document List to Update the Status
                                                    List<UUID> finalDocumentList = new ArrayList<>();

                                                    for (AssignmentDocumentEntity pvtData : assignmentDocPvt) {
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

                                                    return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", assignmentEntity.getCreatedBy().toString(), assignmentEntity.getReqCompanyUUID().toString(), assignmentEntity.getReqBranchUUID().toString())
                                                            .flatMap(document -> Mono.just(responseAttachments));
                                                }).flatMap(test -> Mono.just(responseAttachments)));
                            });
                });
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_store")
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

                    LocalDateTime extendedDate = null;
                    if (value.getFirst("extendedDate") != null && !Objects.equals(value.getFirst("extendedDate"), "")) {
                        extendedDate = LocalDateTime.parse(value.getFirst("extendedDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    }

                    LocalDateTime start_date = LocalDateTime.parse(value.getFirst("fromDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    LocalDateTime end_date = LocalDateTime.parse(value.getFirst("toDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));


                    if (extendedDate != null) {
                        if (end_date.isAfter(extendedDate)) {
                            return responseInfoMsg("End/To Date should not be after the Extended Date");
                        }
                    }

                    if (start_date.isAfter(end_date)) {
                        return responseInfoMsg("From/Start Date should not be after the End/To Date");
                    }

                    if (end_date.isBefore(start_date)) {
                        return responseInfoMsg("To/End Date should not be before the Start/From Date");
                    }

                    double totalMarks = 0.0;
                    if (value.getFirst("totalMark") != null && !Objects.equals(value.getFirst("totalMark"), "")) {
                        totalMarks = Double.parseDouble(value.getFirst("totalMark"));
                    }

                    AssignmentEntity assignmentEntity = AssignmentEntity
                            .builder()
                            .uuid(UUID.randomUUID())
                            .name(value.getFirst("name").trim())
                            .instruction(value.getFirst("instruction").trim())
                            .fromDate(start_date)
                            .toDate(end_date)
                            .extendedDate(extendedDate)
                            .totalMark(totalMarks)
                            .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
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


                    return apiCallService.getDataWithUUID(authUri + "api/v1/users/show-user-type/", assignmentEntity.getCreatedBy())
                            .flatMap(userJson -> {
                                        if (Objects.equals(apiCallService.getAccessLevelSlug(userJson), "employee")) {
                                            return apiCallService.getUserTypeUUID(userJson)
                                                    .flatMap(userType -> {
                                                        assignmentEntity.setTeacherUUID(userType);

                                                        return assignmentRepository.checkIfEnteredCourseSubjectExistsInSubjectOffered(assignmentEntity.getAcademicSessionUUID(), assignmentEntity.getCourseSubjectUUID())
                                                                .flatMap(assignmentExists -> {
                                                                            if (assignmentExists) {
                                                                                return assignmentRepository.checkIfEnteredTeacherAndCourseSubjectExists(assignmentEntity.getTeacherUUID(), assignmentEntity.getCourseSubjectUUID())
                                                                                        .flatMap(teacherSubjectExists -> {
                                                                                                    if (teacherSubjectExists) {
                                                                                                        return apiCallService.getDataWithUUID(academicUri + "api/v1/teachers/show/", assignmentEntity.getTeacherUUID())
                                                                                                                .flatMap(teacherJson -> apiCallService.getUUID(teacherJson)
                                                                                                                        //check Course Subject exists in course subjects table
                                                                                                                        .flatMap(checkCourseSubject -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", assignmentEntity.getCourseSubjectUUID())
                                                                                                                                .flatMap(courseSubjectJson -> apiCallService.getUUID(courseSubjectJson)
                                                                                                                                        //check if Academic Session exists in academic session table
                                                                                                                                        .flatMap(courseSubject -> apiCallService.getDataWithUUID(academicUri + "api/v1/academic-sessions/show/", assignmentEntity.getAcademicSessionUUID())
                                                                                                                                                .flatMap(academicSessionJson -> apiCallService.getUUID(academicSessionJson)
                                                                                                                                                        .flatMap(academicSessionUUID -> {
                                                                                                                                                                    List<String> documentList = value.get("documentUUID");

                                                                                                                                                                    documentList.removeIf(s -> s.equals(""));

                                                                                                                                                                    if (documentList.isEmpty()) {
                                                                                                                                                                        return responseInfoMsg("Please Upload Assignment Documents!");
                                                                                                                                                                    } else {
                                                                                                                                                                        return storeDocument(assignmentEntity, documentList)
                                                                                                                                                                                .flatMap(docs -> storeAssignmentAttachmentsDto(assignmentEntity, docs)
                                                                                                                                                                                        .flatMap(responseDto -> responseSuccessMsg("Record Stored Successfully", responseDto)))
                                                                                                                                                                                .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                                                                                                                                .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                                                                                                                                    }

                                                                                                                                                                }
                                                                                                                                                        )).switchIfEmpty(responseInfoMsg("Academic Session Does not exist"))
                                                                                                                                                .onErrorResume(ex -> responseErrorMsg("Academic Session Does not exist.Please Contact Developer."))
                                                                                                                                        ).switchIfEmpty(responseInfoMsg("Course Subject Does not exist"))
                                                                                                                                        .onErrorResume(ex -> responseErrorMsg("Course Subject Does not exist.Please Contact Developer."))
                                                                                                                                ))
                                                                                                                ).switchIfEmpty(responseInfoMsg("Teacher Does not exist"))
                                                                                                                .onErrorResume(ex -> responseErrorMsg("Teacher Does not exist.Please Contact Developer."));
                                                                                                    } else {
                                                                                                        return responseInfoMsg("The Entered Teacher is not mapped with this course subject.");
                                                                                                    }
                                                                                                }
//
                                                                                        )
                                                                                        .switchIfEmpty(responseInfoMsg("The Entered Teacher is not mapped with this course subject."))
                                                                                        .onErrorResume(ex -> responseErrorMsg("The Entered Teacher is not mapped with this course subject.Please Contact Developer."));
                                                                            } else {
                                                                                return responseInfoMsg("The Entered Course Subject does not offer against this academic Session.");
                                                                            }
                                                                        }
                                                                )
                                                                .switchIfEmpty(responseInfoMsg("The Entered Course Subject does not offer against this academic Session."))
                                                                .onErrorResume(ex -> responseErrorMsg("The Entered Course Subject does not offer against this academic Session.Please Contact Developer."));
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

    public Mono<List<DocumentAttachmentDto>> updateDocument(AssignmentEntity previousAssignmentEntity, AssignmentEntity assignmentEntity, List<String> docListFromFront, String userId) {

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
        return apiCallService.postDataList(formData, driveUri + "api/v1/documents/show/map", userId, assignmentEntity.getReqCompanyUUID().toString(), assignmentEntity.getReqBranchUUID().toString())
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

                    return assignmentDocumentRepository.findAllByAssignmentUUIDAndDeletedAtIsNull(assignmentEntity.getUuid())
                            .collectList()
                            .flatMap(removeListOfOldPvt -> {

                                for (AssignmentDocumentEntity previousAssignmentDocEntity : removeListOfOldPvt) {
                                    previousAssignmentDocEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousAssignmentDocEntity.setDeletedBy(UUID.fromString(userId));
                                    previousAssignmentDocEntity.setReqDeletedIP(assignmentEntity.getReqUpdatedIP());
                                    previousAssignmentDocEntity.setReqDeletedPort(assignmentEntity.getReqUpdatedPort());
                                    previousAssignmentDocEntity.setReqDeletedBrowser(assignmentEntity.getReqUpdatedBrowser());
                                    previousAssignmentDocEntity.setReqDeletedOS(assignmentEntity.getReqUpdatedOS());
                                    previousAssignmentDocEntity.setReqDeletedDevice(assignmentEntity.getReqUpdatedDevice());
                                    previousAssignmentDocEntity.setReqDeletedReferer(assignmentEntity.getReqUpdatedReferer());
                                }

                                //List of Document ids to Store in Transaction Document Pvt Table
                                List<AssignmentDocumentEntity> listPvt = new ArrayList<AssignmentDocumentEntity>();

                                //iterating Over the Map Key Document Ids and getting values against key
                                for (String documentIdsListData : documentMap.keySet()) {

                                    AssignmentDocumentEntity transactionDocumentPvtEntity = AssignmentDocumentEntity
                                            .builder()
                                            .bucketUUID(UUID.fromString(documentMap.get(documentIdsListData).replaceAll("\"", "")))
                                            .documentUUID(UUID.fromString(documentIdsListData.replaceAll("\"", "")))
                                            .assignmentUUID(assignmentEntity.getUuid())
                                            .createdBy(assignmentEntity.getCreatedBy())
                                            .createdAt(assignmentEntity.getCreatedAt())
                                            .updatedBy(UUID.fromString(userId))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .reqCreatedIP(assignmentEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentEntity.getReqCreatedReferer())
                                            .reqCompanyUUID(assignmentEntity.getReqCompanyUUID())
                                            .reqBranchUUID(assignmentEntity.getReqBranchUUID())
                                            .reqCreatedIP(assignmentEntity.getReqCreatedIP())
                                            .reqCreatedPort(assignmentEntity.getReqCreatedPort())
                                            .reqCreatedBrowser(assignmentEntity.getReqCreatedBrowser())
                                            .reqCreatedOS(assignmentEntity.getReqCreatedOS())
                                            .reqCreatedDevice(assignmentEntity.getReqCreatedDevice())
                                            .reqCreatedReferer(assignmentEntity.getReqCreatedReferer())
                                            .reqUpdatedIP(assignmentEntity.getReqUpdatedIP())
                                            .reqUpdatedPort(assignmentEntity.getReqUpdatedPort())
                                            .reqUpdatedBrowser(assignmentEntity.getReqUpdatedBrowser())
                                            .reqUpdatedOS(assignmentEntity.getReqUpdatedOS())
                                            .reqUpdatedDevice(assignmentEntity.getReqUpdatedDevice())
                                            .reqUpdatedReferer(assignmentEntity.getReqUpdatedReferer())
                                            .build();

                                    listPvt.add(transactionDocumentPvtEntity);
                                }


                                //Saving all Pvt Entries in Transaction Document Pvt Table
                                return assignmentRepository.save(previousAssignmentEntity)
                                        .then(assignmentRepository.save(assignmentEntity))
                                        .then(assignmentDocumentRepository.saveAll(removeListOfOldPvt)
                                                .collectList())
                                        .then(assignmentDocumentRepository.saveAll(listPvt)
                                                .collectList())
                                        .flatMap(assignmentDocPvt -> {

                                            //Creating Final Document List to Update the Status
                                            List<UUID> finalDocumentList = new ArrayList<>();

                                            for (AssignmentDocumentEntity pvtData : assignmentDocPvt) {
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
                                            return apiCallService.updateDataList(sendFormData, driveUri + "api/v1/documents/submitted/update", userId, assignmentEntity.getReqCompanyUUID().toString(), assignmentEntity.getReqBranchUUID().toString())
                                                    .flatMap(document -> Mono.just(responseAttachments));
                                        })
                                        .flatMap(test -> Mono.just(responseAttachments));
                            });
                });
    }

    public Mono<AssignmentAttachmentDto> updateAssignmentAttachmentsDto(AssignmentEntity previousAssignment, AssignmentEntity assignmentEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttachmentDto assignmentDto = AssignmentAttachmentDto
                .builder()
                .documentDtoList(documentAttachmentDto)
                .id(previousAssignment.getId())
                .version(assignmentEntity.getVersion())
                .uuid(previousAssignment.getUuid())
                .name(assignmentEntity.getName())
                .instruction(assignmentEntity.getInstruction())
                .toDate(assignmentEntity.getToDate())
                .fromDate(assignmentEntity.getFromDate())
                .extendedDate(assignmentEntity.getExtendedDate())
                .totalMark(assignmentEntity.getTotalMark())
                .courseSubjectUUID(assignmentEntity.getCourseSubjectUUID())
                .teacherUUID(assignmentEntity.getTeacherUUID())
                .academicSessionUUID(assignmentEntity.getAcademicSessionUUID())
                .status(assignmentEntity.getStatus())
                .createdAt(previousAssignment.getCreatedAt())
                .createdBy(previousAssignment.getCreatedBy())
                .reqCreatedIP(previousAssignment.getReqCreatedIP())
                .reqCreatedPort(previousAssignment.getReqCreatedPort())
                .reqCreatedBrowser(previousAssignment.getReqCreatedBrowser())
                .reqCreatedOS(previousAssignment.getReqCreatedOS())
                .reqCreatedDevice(previousAssignment.getReqCreatedDevice())
                .reqCreatedReferer(previousAssignment.getReqCreatedReferer())
                .updatedBy(assignmentEntity.getUpdatedBy())
                .updatedAt(assignmentEntity.getUpdatedAt())
                .reqCompanyUUID(assignmentEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentEntity.getReqBranchUUID())
                .reqUpdatedIP(assignmentEntity.getReqUpdatedIP())
                .reqUpdatedPort(assignmentEntity.getReqUpdatedPort())
                .reqUpdatedBrowser(assignmentEntity.getReqUpdatedBrowser())
                .reqUpdatedOS(assignmentEntity.getReqUpdatedOS())
                .reqUpdatedDevice(assignmentEntity.getReqUpdatedDevice())
                .reqUpdatedReferer(assignmentEntity.getReqUpdatedReferer())
                .build();

        return Mono.just(assignmentDto);
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID assignmentUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentUUID)
                                .flatMap(previousEntity -> {

                                    LocalDateTime extendedDate = null;
                                    if (value.getFirst("extendedDate") != null && !Objects.equals(value.getFirst("extendedDate"), "")) {
                                        extendedDate = LocalDateTime.parse(value.getFirst("extendedDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                    }

                                    LocalDateTime start_date = LocalDateTime.parse(value.getFirst("fromDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                                    LocalDateTime end_date = LocalDateTime.parse(value.getFirst("toDate"), DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                                    if (extendedDate != null) {
                                        if (end_date.isAfter(extendedDate)) {
                                            return responseInfoMsg("End/To Date should not be after the Extended Date");
                                        }
                                    }

                                    if (start_date.isAfter(end_date)) {
                                        return responseInfoMsg("From/Start Date should not be after the End/To Date");
                                    }

                                    if (end_date.isBefore(start_date)) {
                                        return responseInfoMsg("To/End Date should not be before the Start/From Date");
                                    }

                                    Double totalMarks = 0.0;
                                    if (value.getFirst("totalMark") != null && !Objects.equals(value.getFirst("totalMark"), "")) {
                                        totalMarks = Double.valueOf(value.getFirst("totalMark"));
                                    }

                                    AssignmentEntity updatedAssignmentEntity = AssignmentEntity
                                            .builder()
                                            .uuid(previousEntity.getUuid())
                                            .name(value.getFirst("name").trim())
                                            .instruction(value.getFirst("instruction").trim())
                                            .fromDate(start_date)
                                            .toDate(end_date)
                                            .extendedDate(extendedDate)
                                            .totalMark(totalMarks)
                                            .courseSubjectUUID(UUID.fromString(value.getFirst("courseSubjectUUID").trim()))
                                            .academicSessionUUID(UUID.fromString(value.getFirst("academicSessionUUID").trim()))
                                            .status(Boolean.valueOf(value.getFirst("status")))
                                            .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                            .updatedBy(UUID.fromString(userUUID))
                                            .createdAt(previousEntity.getCreatedAt())
                                            .createdBy(previousEntity.getCreatedBy())
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

                                    // update status
                                    previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                    previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                    previousEntity.setReqDeletedIP(reqIp);
                                    previousEntity.setReqDeletedPort(reqPort);
                                    previousEntity.setReqDeletedBrowser(reqBrowser);
                                    previousEntity.setReqDeletedOS(reqOs);
                                    previousEntity.setReqDeletedDevice(reqDevice);
                                    previousEntity.setReqDeletedReferer(reqReferer);

                                    return apiCallService.getDataWithUUID(authUri + "api/v1/users/show-user-type/", updatedAssignmentEntity.getUpdatedBy())
                                            .flatMap(userJson -> {

                                                        if (Objects.equals(apiCallService.getAccessLevelSlug(userJson), "employee")) {
                                                            return apiCallService.getUserTypeUUID(userJson)
                                                                    .flatMap(userType -> {

                                                                        updatedAssignmentEntity.setTeacherUUID(userType);

                                                                        return assignmentRepository.checkIfEnteredCourseSubjectExistsInSubjectOffered(updatedAssignmentEntity.getAcademicSessionUUID(), updatedAssignmentEntity.getCourseSubjectUUID())
                                                                                .flatMap(assignmentExists -> {
                                                                                            if (assignmentExists) {
                                                                                                return assignmentRepository.checkIfEnteredTeacherAndCourseSubjectExists(updatedAssignmentEntity.getTeacherUUID(), updatedAssignmentEntity.getCourseSubjectUUID())
                                                                                                        .flatMap(teacherSubjectExists -> {
                                                                                                                    if (teacherSubjectExists) {
                                                                                                                        return apiCallService.getDataWithUUID(academicUri + "api/v1/teachers/show/", updatedAssignmentEntity.getTeacherUUID())
                                                                                                                                .flatMap(teacherJson -> apiCallService.getUUID(teacherJson)
                                                                                                                                        //check Course Subject exists in course subjects table
                                                                                                                                        .flatMap(checkCourseSubject -> apiCallService.getDataWithUUID(academicUri + "api/v1/course-subjects/show/", updatedAssignmentEntity.getCourseSubjectUUID())
                                                                                                                                                .flatMap(courseSubjectJson -> apiCallService.getUUID(courseSubjectJson)
                                                                                                                                                        //check if Academic Session exists in academic session table
                                                                                                                                                        .flatMap(courseSubject -> apiCallService.getDataWithUUID(academicUri + "api/v1/academic-sessions/show/", updatedAssignmentEntity.getAcademicSessionUUID())
                                                                                                                                                                .flatMap(academicSessionJson -> apiCallService.getUUID(academicSessionJson)
                                                                                                                                                                        .flatMap(academicSessionUUID -> {
                                                                                                                                                                                    List<String> documentList = value.get("documentUUID");

                                                                                                                                                                                    documentList.removeIf(s -> s.equals(""));

                                                                                                                                                                                    if (documentList.isEmpty()) {
                                                                                                                                                                                        return responseInfoMsg("Please Upload Assignment Documents!");
                                                                                                                                                                                    } else {
                                                                                                                                                                                        return updateDocument(previousEntity, updatedAssignmentEntity, documentList, updatedAssignmentEntity.getCreatedBy().toString())
                                                                                                                                                                                                .flatMap(docsAttachedDto -> updateAssignmentAttachmentsDto(previousEntity, updatedAssignmentEntity, docsAttachedDto)
                                                                                                                                                                                                        .flatMap(updateAssignmentAttachmentDto -> responseSuccessMsg("Record Updated Successfully!", updateAssignmentAttachmentDto))
                                                                                                                                                                                                        .switchIfEmpty(responseInfoMsg("Unable to Update Record there is something wrong please try again."))
                                                                                                                                                                                                        .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."))
                                                                                                                                                                                                ).switchIfEmpty(responseInfoMsg("Unable to Update Record there is something wrong please try again."))
                                                                                                                                                                                                .onErrorResume(ex -> responseErrorMsg("Unable to Update Record.Please Contact Developer."));
                                                                                                                                                                                    }
                                                                                                                                                                                }
                                                                                                                                                                        )).switchIfEmpty(responseInfoMsg("Academic Session Does not exist"))
                                                                                                                                                                .onErrorResume(ex -> responseErrorMsg("Academic Session Does not exist.Please Contact Developer."))
                                                                                                                                                        ).switchIfEmpty(responseInfoMsg("Course Subject Does not exist"))
                                                                                                                                                        .onErrorResume(ex -> responseErrorMsg("Course Subject Does not exist.Please Contact Developer."))
                                                                                                                                                ))
                                                                                                                                ).switchIfEmpty(responseInfoMsg("Teacher Does not exist"))
                                                                                                                                .onErrorResume(ex -> responseErrorMsg("Teacher Does not exist.Please Contact Developer."));
                                                                                                                    } else {
                                                                                                                        return responseInfoMsg("The Entered Teacher is not mapped with this course subject.");
                                                                                                                    }
                                                                                                                }
//
                                                                                                        )
                                                                                                        .switchIfEmpty(responseInfoMsg("The Entered Teacher is not mapped with this course subject."))
                                                                                                        .onErrorResume(ex -> responseErrorMsg("The Entered Teacher is not mapped with this course subject.Please Contact Developer."));
                                                                                            } else {
                                                                                                return responseInfoMsg("The Entered Course Subject does not offer against this academic Session.");
                                                                                            }
                                                                                        }
                                                                                )
                                                                                .switchIfEmpty(responseInfoMsg("The Entered Course Subject does not offer against this academic Session."))
                                                                                .onErrorResume(ex -> responseErrorMsg("The Entered Course Subject does not offer against this academic Session.Please Contact Developer."));
                                                                    });
                                                        } else {
                                                            return responseInfoMsg("The Entered User is not allowed to Create Assignment As he/she is not employee.");
                                                        }
                                                    }
                                            ).switchIfEmpty(responseInfoMsg("Unable to fetch user Record"))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to fetch user Record.Please Contact Developer."));
                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID assignmentUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                    return assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentUUID)
                            .flatMap(previousAssignmentEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousAssignmentEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                AssignmentEntity updatedAssignmentEntity = AssignmentEntity
                                        .builder()
                                        .uuid(previousAssignmentEntity.getUuid())
                                        .name(previousAssignmentEntity.getName())
                                        .instruction(previousAssignmentEntity.getInstruction())
                                        .toDate(previousAssignmentEntity.getToDate())
                                        .fromDate(previousAssignmentEntity.getFromDate())
                                        .extendedDate(previousAssignmentEntity.getExtendedDate())
                                        .totalMark(previousAssignmentEntity.getTotalMark())
                                        .courseSubjectUUID(previousAssignmentEntity.getCourseSubjectUUID())
                                        .teacherUUID(previousAssignmentEntity.getTeacherUUID())
                                        .academicSessionUUID(previousAssignmentEntity.getAcademicSessionUUID())
                                        .status(status == true ? true : false)
                                        .updatedAt(LocalDateTime.now(ZoneId.of(zone)))
                                        .updatedBy(UUID.fromString(userUUID))
                                        .createdAt(previousAssignmentEntity.getCreatedAt())
                                        .createdBy(previousAssignmentEntity.getCreatedBy())
                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                        .reqCreatedIP(previousAssignmentEntity.getReqCreatedIP())
                                        .reqCreatedPort(previousAssignmentEntity.getReqCreatedPort())
                                        .reqCreatedBrowser(previousAssignmentEntity.getReqCreatedBrowser())
                                        .reqCreatedOS(previousAssignmentEntity.getReqCreatedOS())
                                        .reqCreatedDevice(previousAssignmentEntity.getReqCreatedDevice())
                                        .reqCreatedReferer(previousAssignmentEntity.getReqCreatedReferer())
                                        .reqUpdatedIP(reqIp)
                                        .reqUpdatedPort(reqPort)
                                        .reqUpdatedBrowser(reqBrowser)
                                        .reqUpdatedOS(reqOs)
                                        .reqUpdatedDevice(reqDevice)
                                        .reqUpdatedReferer(reqReferer)
                                        .build();

                                // update status
                                previousAssignmentEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousAssignmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousAssignmentEntity.setReqDeletedIP(reqIp);
                                previousAssignmentEntity.setReqDeletedPort(reqPort);
                                previousAssignmentEntity.setReqDeletedBrowser(reqBrowser);
                                previousAssignmentEntity.setReqDeletedOS(reqOs);
                                previousAssignmentEntity.setReqDeletedDevice(reqDevice);
                                previousAssignmentEntity.setReqDeletedReferer(reqReferer);

                                return assignmentRepository.save(previousAssignmentEntity)
                                        .then(assignmentRepository.save(updatedAssignmentEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    public Mono<AssignmentAttachmentDto> deleteAssignmentAttachmentsDto(AssignmentEntity assignmentEntity, List<DocumentAttachmentDto> documentAttachmentDto) {

        AssignmentAttachmentDto assignmentDto = AssignmentAttachmentDto
                .builder()
                .documentDtoList(documentAttachmentDto)
                .id(assignmentEntity.getId())
                .version(assignmentEntity.getVersion())
                .uuid(assignmentEntity.getUuid())
                .name(assignmentEntity.getName())
                .instruction(assignmentEntity.getInstruction())
                .toDate(assignmentEntity.getToDate())
                .fromDate(assignmentEntity.getFromDate())
                .extendedDate(assignmentEntity.getExtendedDate())
                .totalMark(assignmentEntity.getTotalMark())
                .courseSubjectUUID(assignmentEntity.getCourseSubjectUUID())
                .teacherUUID(assignmentEntity.getTeacherUUID())
                .academicSessionUUID(assignmentEntity.getAcademicSessionUUID())
                .status(assignmentEntity.getStatus())
                .createdAt(assignmentEntity.getCreatedAt())
                .createdBy(assignmentEntity.getCreatedBy())
                .reqCreatedIP(assignmentEntity.getReqCreatedIP())
                .reqCreatedPort(assignmentEntity.getReqCreatedPort())
                .reqCreatedBrowser(assignmentEntity.getReqCreatedBrowser())
                .reqCreatedOS(assignmentEntity.getReqCreatedOS())
                .reqCreatedDevice(assignmentEntity.getReqCreatedDevice())
                .reqCreatedReferer(assignmentEntity.getReqCreatedReferer())
                .updatedBy(assignmentEntity.getUpdatedBy())
                .updatedAt(assignmentEntity.getUpdatedAt())
                .reqCompanyUUID(assignmentEntity.getReqCompanyUUID())
                .reqBranchUUID(assignmentEntity.getReqBranchUUID())
                .reqUpdatedIP(assignmentEntity.getReqUpdatedIP())
                .reqUpdatedPort(assignmentEntity.getReqUpdatedPort())
                .reqUpdatedBrowser(assignmentEntity.getReqUpdatedBrowser())
                .reqUpdatedOS(assignmentEntity.getReqUpdatedOS())
                .reqUpdatedDevice(assignmentEntity.getReqUpdatedDevice())
                .reqUpdatedReferer(assignmentEntity.getReqUpdatedReferer())
                .deletedBy(assignmentEntity.getDeletedBy())
                .deletedAt(assignmentEntity.getDeletedAt())
                .reqDeletedDevice(assignmentEntity.getReqDeletedDevice())
                .reqDeletedIP(assignmentEntity.getReqDeletedIP())
                .reqDeletedBrowser(assignmentEntity.getReqDeletedBrowser())
                .reqDeletedPort(assignmentEntity.getReqDeletedPort())
                .reqDeletedReferer(assignmentEntity.getReqDeletedReferer())
                .reqDeletedOS(assignmentEntity.getReqDeletedOS())
                .build();

        return Mono.just(assignmentDto);
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID assignmentUUID = UUID.fromString(serverRequest.pathVariable("assignmentUUID"));
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

        return assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentUUID)
                .flatMap(assignmentEntity -> assignmentDocumentRepository
                        .findAllByAssignmentUUIDAndDeletedAtIsNull(assignmentUUID)
                        .collectList()
                        .flatMap(assignmentDocEntity -> {
                            List<AssignmentDocumentEntity> pvtEntity = new ArrayList<>();

                            List<DocumentAttachmentDto> documentDtos = new ArrayList<>();

                            assignmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            assignmentEntity.setDeletedBy(UUID.fromString(userId));
                            assignmentEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                            assignmentEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                            assignmentEntity.setReqDeletedIP(reqIp);
                            assignmentEntity.setReqDeletedPort(reqPort);
                            assignmentEntity.setReqDeletedBrowser(reqBrowser);
                            assignmentEntity.setReqDeletedOS(reqOs);
                            assignmentEntity.setReqDeletedDevice(reqDevice);
                            assignmentEntity.setReqDeletedReferer(reqReferer);

                            for (AssignmentDocumentEntity assignDocs : assignmentDocEntity) {
                                assignDocs.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                assignDocs.setDeletedBy(UUID.fromString(userId));
                                assignDocs.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                assignDocs.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                assignDocs.setReqDeletedIP(reqIp);
                                assignDocs.setReqDeletedPort(reqPort);
                                assignDocs.setReqDeletedBrowser(reqBrowser);
                                assignDocs.setReqDeletedOS(reqOs);
                                assignDocs.setReqDeletedDevice(reqDevice);
                                assignDocs.setReqDeletedReferer(reqReferer);

                                pvtEntity.add(assignDocs);

                                DocumentAttachmentDto documentAttachmentDto = DocumentAttachmentDto
                                        .builder()
                                        .doc_bucket_uuid(assignDocs.getBucketUUID())
                                        .doc_id(assignDocs.getDocumentUUID())
                                        .build();

                                documentDtos.add(documentAttachmentDto);
                            }

                            return assignmentRepository.save(assignmentEntity)
                                    .then(assignmentDocumentRepository.saveAll(pvtEntity)
                                            .collectList())
                                    .flatMap(deleteEntity -> deleteAssignmentAttachmentsDto(assignmentEntity, documentDtos)
                                            .flatMap(delDto -> responseSuccessMsg("Record Deleted Successfully!", delDto))
                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."))
                                    );
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Assignment does not exist"))
                .onErrorResume(err -> responseErrorMsg("Assignment does not exist.Please Contact Developer."));
    }

//    @AuthHasPermission(value = "lms_api_v1_teacher_assignments_delete")
//    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
//        UUID assignmentUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
//        return assignmentRepository.findByUuidAndDeletedAtIsNull(assignmentUUID)
//                //check if Assignment Exists in Assignment Attempts table
//                .flatMap(assignmentEntity -> assignmentAttemptRepository.findFirstByAssignmentUUIDAndDeletedAtIsNull(assignmentEntity.getUuid())
//                        .flatMap(checkMsg -> responseInfoMsg("Unable to Delete Record as the Reference Exists."))
//                        .switchIfEmpty(Mono.defer(() -> {
//
//                            assignmentEntity.setDeletedBy(UUID.fromString(userUUID));
//                            assignmentEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
//                            assignmentEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
//                            assignmentEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
//                            assignmentEntity.setReqDeletedIP(reqIp);
//                            assignmentEntity.setReqDeletedPort(reqPort);
//                            assignmentEntity.setReqDeletedBrowser(reqBrowser);
//                            assignmentEntity.setReqDeletedOS(reqOs);
//                            assignmentEntity.setReqDeletedDevice(reqDevice);
//                            assignmentEntity.setReqDeletedReferer(reqReferer);
//
//                            return assignmentRepository.save(assignmentEntity)
//                                    .flatMap(entity -> responseSuccessMsg("Record Deleted Successfully", entity))
//                                    .switchIfEmpty(responseInfoMsg("Unable to delete record. There is something wrong please try again."))
//                                    .onErrorResume(ex -> responseErrorMsg("Unable to delete record. Please contact developer."));
//                        }))
//                ).switchIfEmpty(responseInfoMsg("Requested record does not exist"))
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
