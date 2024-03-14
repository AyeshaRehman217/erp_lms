package tuf.webscaf.app.dbContext.slave.repositry.custom.contract;


import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttachmentDto;

import java.util.UUID;

// This interface wil extends in Slave Assignment Repository
public interface SlaveCustomAssignmentRepository {

    /**
     * Fetch Records With Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstSubjectWithoutStatus(UUID subjectUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstSubjectWithStatus(UUID subjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexWithoutStatus(String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexWithStatus(Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Teacher and Course Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectWithoutStatus(UUID teacherUUID, UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectWithStatus(UUID teacherUUID, UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Teacher and Course Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionAndCourseSubjectWithoutStatus(UUID academicSessionUUID, UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionAndCourseSubjectWithStatus(UUID academicSessionUUID, UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);


    /**
     * Fetch Records With Teacher, Course Subject , Academic Session And Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithoutStatus(UUID teacherUUID, UUID courseSubjectUUID, UUID academicSessionUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherAndCourseSubjectAndAcademicSessionWithStatus(UUID teacherUUID, UUID courseSubjectUUID, UUID academicSessionUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);


    /**
     * Fetch Records With Course Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstCourseSubjectWithoutStatus(UUID courseSubjectUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstCourseSubjectWithStatus(UUID courseSubjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Course Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionWithoutStatus(UUID academicSessionUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstAcademicSessionWithStatus(UUID academicSessionUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);


    /**
     * Fetch Records With Teacher and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherWithoutStatus(UUID teacherUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAgainstTeacherWithStatus(UUID teacherUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);

    /**
     * Fetch Records With Student, Course, Subject and Status (With and Without) filter
     **/
    Flux<SlaveAssignmentAttachmentDto> indexAssignmentsAgainstStudentCourseSubjectWithoutStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, String name, String instruction, String dp, String d, Integer size, Long page);

    Flux<SlaveAssignmentAttachmentDto> indexAssignmentsAgainstStudentCourseSubjectWithStatus(UUID studentUUID, UUID courseUUID, UUID subjectUUID, Boolean status, String name, String instruction, String dp, String d, Integer size, Long page);


    /**
     * Show Assignment Record Against UUID
     **/
    Flux<SlaveAssignmentAttachmentDto> showAssignmentRecordAgainstUUID(UUID assignmentUUID);
}
