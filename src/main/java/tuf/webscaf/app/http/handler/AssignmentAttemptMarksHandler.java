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
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptMarksEntity;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentAttemptMarksRepository;
import tuf.webscaf.app.dbContext.master.repositry.AssignmentAttemptRepository;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptMarksDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptMarksEntity;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveAssignmentAttemptMarksRepository;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Tag(name = "assignmentAttemptMarksHandler")
@Component
public class AssignmentAttemptMarksHandler {
    @Autowired
    CustomResponse appresponse;

    @Autowired
    AssignmentAttemptRepository assignmentAttemptRepository;

    @Autowired
    AssignmentAttemptMarksRepository assignmentAttemptMarksRepository;

    @Autowired
    SlaveAssignmentAttemptMarksRepository slaveAssignmentAttemptMarksRepository;

    @Value("${server.zone}")
    private String zone;

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_index")
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

        String studentUUID = serverRequest.queryParam("studentUUID").map(String::toString).orElse("").trim();

        String assignmentAttemptUUID = serverRequest.queryParam("assignmentAttemptUUID").map(String::toString).orElse("").trim();

        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, directionProperty));

        if (!status.isEmpty() && !studentUUID.isEmpty() && !assignmentAttemptUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstStudentAndAssignmentAttemptWithStatus(UUID.fromString(studentUUID), UUID.fromString(assignmentAttemptUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstStudentAndAttemptWithStatusFilter(Boolean.valueOf(status), UUID.fromString(studentUUID), UUID.fromString(assignmentAttemptUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!studentUUID.isEmpty() && !assignmentAttemptUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstStudentAndAssignmentAttemptWithoutStatus(UUID.fromString(studentUUID), UUID.fromString(assignmentAttemptUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstStudentAndAttemptWithoutStatusFilter(UUID.fromString(studentUUID), UUID.fromString(assignmentAttemptUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty() && !studentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstStudentWithStatus(UUID.fromString(studentUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstStudentWithStatusFilter(Boolean.valueOf(status), UUID.fromString(studentUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty() && !assignmentAttemptUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstAssignmentAttemptWithStatus(UUID.fromString(assignmentAttemptUUID), Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstAssignmentAttemptWithStatusFilter(Boolean.valueOf(status), UUID.fromString(assignmentAttemptUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!assignmentAttemptUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstAssignmentAttemptWithoutStatus(UUID.fromString(assignmentAttemptUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstAssignmentAttemptWithoutStatusFilter(UUID.fromString(assignmentAttemptUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!studentUUID.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexAgainstStudentWithoutStatus(UUID.fromString(studentUUID), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countMarksAgainstStudentWithoutStatusFilter(UUID.fromString(studentUUID), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else if (!status.isEmpty()) {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexWithStatus(Boolean.valueOf(status), searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countAllMarksWithStatusFilter(Boolean.valueOf(status), searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        } else {
            Flux<SlaveAssignmentAttemptMarksDto> slaveAssignmentAttemptMarksEntityFlux = slaveAssignmentAttemptMarksRepository
                    .indexWithoutStatus(searchKeyWord, directionProperty, d, pageable.getPageSize(), pageable.getOffset());

            return slaveAssignmentAttemptMarksEntityFlux
                    .collectList()
                    .flatMap(assignmentAttemptMarksEntity -> slaveAssignmentAttemptMarksRepository
                            .countAllMarks(searchKeyWord)
                            .flatMap(count -> {
                                if (assignmentAttemptMarksEntity.isEmpty()) {
                                    return responseIndexInfoMsg("Record does not exist", count);
                                } else {
                                    return responseIndexSuccessMsg("All Records Fetched Successfully", assignmentAttemptMarksEntity, count);
                                }
                            })
                    ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                    .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
        }
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_show")
    public Mono<ServerResponse> show(ServerRequest serverRequest) {
        UUID assignmentAttemptMarksUUID = UUID.fromString((serverRequest.pathVariable("uuid")));

        return slaveAssignmentAttemptMarksRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptMarksUUID)
                .flatMap(assignmentAttemptMarksEntity -> responseSuccessMsg("Record Fetched Successfully", assignmentAttemptMarksEntity))
                .switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                .onErrorResume(ex -> responseErrorMsg("Requested record does not exist. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_store")
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

                    double obtainedMarks = 0.0;
                    if (value.getFirst("obtainedMarks") != null && !Objects.equals(value.getFirst("obtainedMarks"), "")) {
                        obtainedMarks = Double.parseDouble(value.getFirst("obtainedMarks"));
                    }


                    AssignmentAttemptMarksEntity assignmentAttemptMarksEntity = AssignmentAttemptMarksEntity.builder()
                            .uuid(UUID.randomUUID())
                            .obtainedMarks(obtainedMarks)
                            .assignmentAttemptUUID(UUID.fromString(value.getFirst("assignmentAttemptUUID").trim()))
                            .comments(value.getFirst("comments").trim())
                            .status(Boolean.valueOf(value.getFirst("status")))
                            .createdBy(UUID.fromString(userUUID))
                            .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                            .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                            .reqBranchUUID(UUID.fromString(reqBranchUUID))
                            .reqCreatedIP(reqIp)
                            .reqCreatedPort(reqPort)
                            .reqCreatedBrowser(reqBrowser)
                            .reqCreatedOS(reqOs)
                            .reqCreatedDevice(reqDevice)
                            .reqCreatedReferer(reqReferer)
                            .build();

                    return assignmentAttemptMarksRepository.findFirstByAssignmentAttemptUUIDAndDeletedAtIsNull(assignmentAttemptMarksEntity.getAssignmentAttemptUUID())
                            .flatMap(recordAlreadyExist -> responseInfoMsg("Marks already Entered Against this Assignment"))
                            .switchIfEmpty(Mono.defer(() -> assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptMarksEntity.getAssignmentAttemptUUID())
                                    .flatMap(assignmentAttemptEntity -> assignmentAttemptMarksRepository.save(assignmentAttemptMarksEntity)
                                            .flatMap(saveAssignmentAttemptMarksEntity -> responseSuccessMsg("Record Stored Successfully", saveAssignmentAttemptMarksEntity))
                                            .switchIfEmpty(responseInfoMsg("Unable to Store record. There is something wrong please try again."))
                                            .onErrorResume(ex -> responseErrorMsg("Unable to Store record. Please contact developer."))
                                    ).switchIfEmpty(responseInfoMsg("Assignment Attempt Record does not Exist."))
                                    .onErrorResume(ex -> responseErrorMsg("Assignment Attempt Record does not Exist. Please Contact Developer."))
                            ));
                }).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_update")
    public Mono<ServerResponse> update(ServerRequest serverRequest) {
        UUID assignmentAttemptMarksUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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
                .flatMap(value -> assignmentAttemptMarksRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptMarksUUID)
                        .flatMap(previousEntity -> {

                            Double obtainedMarks = 0.0;
                            if (value.getFirst("obtainedMarks") != null && !Objects.equals(value.getFirst("obtainedMarks"), "")) {
                                obtainedMarks = Double.valueOf(value.getFirst("obtainedMarks"));
                            }

                            AssignmentAttemptMarksEntity updatedEntity = AssignmentAttemptMarksEntity.builder()
                                    .uuid(previousEntity.getUuid())
                                    .comments(value.getFirst("comments").trim())
                                    .obtainedMarks(obtainedMarks)
                                    .assignmentAttemptUUID(previousEntity.getAssignmentAttemptUUID())
                                    .status(Boolean.valueOf(value.getFirst("status")))
                                    .createdAt(previousEntity.getCreatedAt())
                                    .createdBy(previousEntity.getCreatedBy())
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

                            previousEntity.setDeletedBy(UUID.fromString(userUUID));
                            previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                            previousEntity.setReqDeletedIP(reqIp);
                            previousEntity.setReqDeletedPort(reqPort);
                            previousEntity.setReqDeletedBrowser(reqBrowser);
                            previousEntity.setReqDeletedOS(reqOs);
                            previousEntity.setReqDeletedDevice(reqDevice);
                            previousEntity.setReqDeletedReferer(reqReferer);

                            return assignmentAttemptMarksRepository.findFirstByAssignmentAttemptUUIDAndDeletedAtIsNullAndUuidIsNot(updatedEntity.getAssignmentAttemptUUID(), assignmentAttemptMarksUUID)
                                    .flatMap(recordAlreadyExist -> responseInfoMsg("Marks already Entered Against this Assignment"))
                                    .switchIfEmpty(Mono.defer(() -> assignmentAttemptRepository.findByUuidAndDeletedAtIsNull(updatedEntity.getAssignmentAttemptUUID())
                                            .flatMap(assignmentAttemptMarksType -> assignmentAttemptMarksRepository.save(previousEntity)
                                                    .then(assignmentAttemptMarksRepository.save(updatedEntity))
                                                    .flatMap(assignmentAttemptMarksEntity -> responseSuccessMsg("Record Updated Successfully", assignmentAttemptMarksEntity))
                                                    .switchIfEmpty(responseInfoMsg("Unable to update record. There is something wrong please try again."))
                                                    .onErrorResume(ex -> responseErrorMsg("Unable to update record. Please contact developer."))
                                            ).switchIfEmpty(responseInfoMsg("Assignment Attempt Record does not Exist."))
                                            .onErrorResume(ex -> responseErrorMsg("Assignment Attempt Record does not Exist.Please Contact Developer."))
                                    ));
                        }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                        .onErrorResume(ex -> responseErrorMsg("Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read Request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read Request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_status_update")
    public Mono<ServerResponse> status(ServerRequest serverRequest) {
        UUID assignmentAttemptMarksUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

                    return assignmentAttemptMarksRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptMarksUUID)
                            .flatMap(previousEntity -> {
                                // If status is not Boolean value
                                if (status != false && status != true) {
                                    return responseInfoMsg("Status must be Active or InActive");
                                }

                                // If already same status exist in database.
                                if (((previousEntity.getStatus() ? true : false) == status)) {
                                    return responseWarningMsg("Record already exist with same status");
                                }

                                AssignmentAttemptMarksEntity assignmentAttemptMarksEntity = AssignmentAttemptMarksEntity.builder()
                                        .uuid(previousEntity.getUuid())
                                        .obtainedMarks(previousEntity.getObtainedMarks())
                                        .comments(previousEntity.getComments())
                                        .assignmentAttemptUUID(previousEntity.getAssignmentAttemptUUID())
                                        .status(status == true ? true : false)
                                        .createdAt(previousEntity.getCreatedAt())
                                        .createdBy(previousEntity.getCreatedBy())
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

                                // update status
                                previousEntity.setDeletedBy(UUID.fromString(userUUID));
                                previousEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                                previousEntity.setReqDeletedIP(reqIp);
                                previousEntity.setReqDeletedPort(reqPort);
                                previousEntity.setReqDeletedBrowser(reqBrowser);
                                previousEntity.setReqDeletedOS(reqOs);
                                previousEntity.setReqDeletedDevice(reqDevice);
                                previousEntity.setReqDeletedReferer(reqReferer);

                                return assignmentAttemptMarksRepository.save(previousEntity)
                                        .then(assignmentAttemptMarksRepository.save(assignmentAttemptMarksEntity))
                                        .flatMap(statusUpdate -> responseSuccessMsg("Status Updated Successfully", statusUpdate))
                                        .switchIfEmpty(responseInfoMsg("Unable to update the status. There is something wrong please try again."))
                                        .onErrorResume(err -> responseErrorMsg("Unable to update the status. Please contact developer."));
                            }).switchIfEmpty(responseInfoMsg("Requested Record does not exist"))
                            .onErrorResume(err -> responseErrorMsg("Requested Record does not exist. Please contact developer."));
                }).switchIfEmpty(responseInfoMsg("Unable to read the request."))
                .onErrorResume(err -> responseErrorMsg("Unable to read the request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_teacher_assignment-attempt-marks_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        UUID assignmentAttemptMarksUUID = UUID.fromString((serverRequest.pathVariable("uuid")));
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

        return assignmentAttemptMarksRepository.findByUuidAndDeletedAtIsNull(assignmentAttemptMarksUUID)
                .flatMap(assignmentAttemptMarksEntity -> {

                    assignmentAttemptMarksEntity.setDeletedBy(UUID.fromString(userUUID));
                    assignmentAttemptMarksEntity.setDeletedAt(LocalDateTime.now(ZoneId.of(zone)));
                    assignmentAttemptMarksEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                    assignmentAttemptMarksEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                    assignmentAttemptMarksEntity.setReqDeletedIP(reqIp);
                    assignmentAttemptMarksEntity.setReqDeletedPort(reqPort);
                    assignmentAttemptMarksEntity.setReqDeletedBrowser(reqBrowser);
                    assignmentAttemptMarksEntity.setReqDeletedOS(reqOs);
                    assignmentAttemptMarksEntity.setReqDeletedDevice(reqDevice);
                    assignmentAttemptMarksEntity.setReqDeletedReferer(reqReferer);

                    return assignmentAttemptMarksRepository.save(assignmentAttemptMarksEntity)
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
