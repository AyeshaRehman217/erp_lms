package tuf.webscaf.router.api.v1;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import tuf.webscaf.app.dbContext.master.dto.AssignmentAttachmentDto;
import tuf.webscaf.app.dbContext.master.entity.AssignmentEntity;
import tuf.webscaf.app.dbContext.slave.dto.SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentEntity;
import tuf.webscaf.app.http.handler.AssignmentHandler;
import tuf.webscaf.app.http.validationFilters.assignmentHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;
import tuf.webscaf.springDocImpl.StoreAssignmentDocumentDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AssignmentsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/subjects/assignments/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "indexAssignmentsAgainstSubject",
                            operation = @Operation(
                                    operationId = "indexAssignmentsAgainstSubject",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records With Pagination and Subject Filter",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or instruction"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "subjectUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignments/student/course/subject/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "showAssignmentsAgainstStudentCourseSubject",
                            operation = @Operation(
                                    operationId = "showAssignmentsAgainstStudentCourseSubject",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records Against Student, Course and Subject With Pagination and Subject Filter",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or instruction"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "subjectUUID", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "studentUUID", required = true),
                                            @Parameter(in = ParameterIn.QUERY, name = "courseUUID", required = true)
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/attempted-students/marks/facade/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "indexEnrolledStudentAssignmentAttemptAndMarks",
                            operation = @Operation(
                                    operationId = "indexEnrolledStudentAssignmentAttemptAndMarks",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveEnrolledStudentsAssignmentAndAttemptAndMarksDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the List of  Student Records that are enrolled in course subject and students who attempted the assignment and not attempted the Assignment yet (With marks) and Pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with assignmentName or studentCode"),
                                            @Parameter(in = ParameterIn.QUERY, name = "assignmentUUID", required = true)
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "index",
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records With Pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or instruction"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "teacherUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "courseSubjectUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "academicSessionUUID"),
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/documents/show/{documentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "getDocumentUUID",
                            operation = @Operation(
                                    operationId = "getDocumentUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Will be used By Documents in Drive Module to check if Documents exists in Assignment",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "documentUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store the Record",
                                    requestBody = @RequestBody(
                                            description = "Create Assignment",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StoreAssignmentDocumentDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                    },
                                    description = "Update the Record for given uuid",
                                    requestBody = @RequestBody(
                                            description = "Update Assignment",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StoreAssignmentDocumentDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/status/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "status",
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Update the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                    },
                                    requestBody = @RequestBody(
                                            description = "Update the Status",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignments/delete/{assignmentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = AssignmentHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = AssignmentAttachmentDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete All the Assignment And Document Records for given Assignment UUID",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "assignmentUUID")
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> assignmentRoutes(AssignmentHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/teacher/assignments/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexAssignmentHandlerFilter())
                .and(RouterFunctions.route(GET("lms/api/v1/teacher/subjects/assignments/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::indexAssignmentsAgainstSubject).filter(new IndexAssignmentAgainstSubjecttHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/student/assignments/student/course/subject/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::showAssignmentsAgainstStudentCourseSubject).filter(new IndexAssignmentAgainstStudentCourseSubjecttHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/teacher/assignments/attempted-students/marks/facade/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::indexEnrolledStudentAssignmentAttemptAndMarks).filter(new IndexEnrolledStudentAssignmentFacadeHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/teacher/assignments/documents/show/{documentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getDocumentUUID).filter(new ShowAttachmentInAssignmentHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/teacher/assignments/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowAssignmentHandlerFilter()))
                .and(RouterFunctions.route(POST("lms/api/v1/teacher/assignments/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreAssignmentHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/teacher/assignments/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateAssignmentHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/teacher/assignments/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowAssignmentHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/teacher/assignments/delete/{assignmentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteAssignmentHandlerFilter()));
    }
}
