package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptMarksDto;

import java.util.UUID;

// This interface wil extends in Slave Assignment Attempt Repository
public interface SlaveCustomAssignmentAttemptMarksRepository {

    /**
     * Fetch Records With and Without Status Filter
     **/
    Flux<SlaveAssignmentAttemptMarksDto> indexWithoutStatus(String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptMarksDto> indexWithStatus(Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With StudentUUID + assignmentAttemptUUID and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentAndAssignmentAttemptWithoutStatus(UUID studentUUID, UUID assignmentAttemptUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentAndAssignmentAttemptWithStatus(UUID studentUUID, UUID assignmentAttemptUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With StudentUUID and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentWithoutStatus(UUID studentUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstStudentWithStatus(UUID studentUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Assignment Attempt UUID and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstAssignmentAttemptWithoutStatus(UUID assignmentAttemptUUID, String assignmentName, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttemptMarksDto> indexAgainstAssignmentAttemptWithStatus(UUID assignmentAttemptUUID, Boolean status, String assignmentName, String dp, String d, Integer size, Long page);
}
