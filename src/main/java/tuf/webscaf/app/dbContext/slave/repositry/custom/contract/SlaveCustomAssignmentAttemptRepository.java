package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;

import java.util.UUID;

// This interface wil extends in Slave Assignment Attempt Repository
public interface SlaveCustomAssignmentAttemptRepository {

    /**
     * Fetch Records With and Without Status Filter
     **/
    Flux<SlaveAssignmentAttemptDto> indexWithoutStatus(String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptDto> indexWithStatus(Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Attempted By and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByWithoutStatus(UUID attemptedByUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByWithStatus(UUID attemptedByUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Un-Mapped Records With Attempted By and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptDto> mappedAssignmentAgainstAttemptedByWithoutStatus(UUID attemptedByUUID, String assignmentName, String comment, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptDto> mappedAssignmentAgainstAttemptedByWithStatus(UUID attemptedByUUID, Boolean status, String assignmentName, String comment, String dp, String d, Integer size, Long page);

    /**
     * Show Assignment Record Against UUID
     **/
    Flux<SlaveAssignmentAttemptDto> showAssignmentAttemptRecordAgainstUUID(UUID assignmentAttemptUUID);

    /**
     * Fetch Records With Attempted By and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByAndAssignmentWithoutStatus(UUID attemptedByUUID,UUID assignmentUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptDto> indexAgainstAttemptedByAndAssignmentWithStatus(UUID attemptedByUUID, UUID assignmentUUID,Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Attempted By and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptDto> indexAgainstAssignmentWithoutStatus(UUID assignmentUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptDto> indexAgainstAssignmentWithStatus(UUID assignmentUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

}
