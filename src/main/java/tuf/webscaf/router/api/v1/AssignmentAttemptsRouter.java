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
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptEntity;
import tuf.webscaf.app.dbContext.slave.dto.SlaveAssignmentAttemptDto;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptEntity;
import tuf.webscaf.app.http.handler.AssignmentAttemptHandler;
import tuf.webscaf.app.http.validationFilters.assignmentAttemptHandler.*;
import tuf.webscaf.springDocImpl.StatusDocImpl;
import tuf.webscaf.springDocImpl.StoreAssignmentAttemptDocumentDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AssignmentAttemptsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignment-attempts/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "index",
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with Assignment Name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "attemptedByUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "assignmentUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignments/mapped/assignment-attempts/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "mappedAssignmentAttemptAgainstAssignment",
                            operation = @Operation(
                                    operationId = "mappedAssignmentAttemptAgainstAssignment",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records With Pagination Against Student UUID (used to fetch Assignment Attempts against the enrolled and registered students)",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by created_at"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with assignmentName and comment"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "studentUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignment-attempts/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
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
                            path = "/lms/api/v1/student/assignment-attempts/documents/show/{documentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "getDocumentUUID",
                            operation = @Operation(
                                    operationId = "getDocumentUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "This Route Will be used By Documents in Drive Module to check if Documents exists in Assignment Attempt",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "documentUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignment-attempts/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Store the Record",
                                    requestBody = @RequestBody(
                                            description = "Create Assignment Attempt",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StoreAssignmentAttemptDocumentDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignment-attempts/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
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
                                            description = "Update Assignment Attempt",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = StoreAssignmentAttemptDocumentDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/student/assignment-attempts/status/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "status",
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
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
                            path = "/lms/api/v1/student/assignment-attempts/delete/{assignmentAttemptUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = AssignmentAttemptHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "assignmentAttemptUUID")
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> assignmentAttemptRoutes(AssignmentAttemptHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/student/assignment-attempts/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexAssignmentAttemptHandlerFilter())
                .and(RouterFunctions.route(GET("lms/api/v1/student/assignments/mapped/assignment-attempts/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::mappedAssignmentAttemptAgainstAssignment).filter(new MappedAssignmentAttemptAgainstAssignmentHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/student/assignment-attempts/documents/show/{documentUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::getDocumentUUID).filter(new ShowAttachmentInAssignmentAttemptHandlerFilter()))
                .and(RouterFunctions.route(GET("lms/api/v1/student/assignment-attempts/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowAssignmentAttemptHandlerFilter()))
                .and(RouterFunctions.route(POST("lms/api/v1/student/assignment-attempts/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreAssignmentAttemptHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/student/assignment-attempts/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateAssignmentAttemptHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/student/assignment-attempts/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowAssignmentAttemptHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/student/assignment-attempts/delete/{assignmentAttemptUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteAssignmentAttemptHandlerFilter()));
    }
}
