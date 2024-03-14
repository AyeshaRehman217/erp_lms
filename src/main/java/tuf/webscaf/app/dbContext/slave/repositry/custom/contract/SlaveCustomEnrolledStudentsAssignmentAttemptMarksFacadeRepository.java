package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;


import reactor.core.publisher.Flux;
import tuf.webscaf.app.dbContext.slave.dto.SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;

import java.util.UUID;

// This interface wil extends in Slave Assignment Repository
public interface SlaveCustomEnrolledStudentsAssignmentAttemptMarksFacadeRepository {

    /**
     * Fetch Records of students that are registered and enrolled in the course subject and that attempted the assignments
     **/
    Flux<SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> indexAllRecords(UUID assignmentUUID, String stdId, String firstName, String assignmentName, String dp, String d, Integer size, Long page);

//    /**
//     * Fetch Records of students that are registered and enrolled in the course subject and that attempted the assignments of Subject
//     **/
//    Flux<SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto> indexAllRecordsWithSubject(UUID subjectUUID, String stdId, String firstName, String assignmentName, String dp, String d, Integer size, Long page);

}
