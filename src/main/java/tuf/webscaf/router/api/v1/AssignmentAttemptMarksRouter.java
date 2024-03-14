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
import tuf.webscaf.app.dbContext.master.entity.AssignmentAttemptMarksEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveAssignmentAttemptMarksEntity;
import tuf.webscaf.app.http.handler.AssignmentAttemptMarksHandler;
import tuf.webscaf.app.http.validationFilters.assignmentAttemptMarksHandler.IndexAssignmentAttemptMarksHandlerFilter;
import tuf.webscaf.app.http.validationFilters.assignmentAttemptMarksHandler.ShowAssignmentAttemptMarksHandlerFilter;
import tuf.webscaf.app.http.validationFilters.assignmentAttemptMarksHandler.StoreAssignmentAttemptMarksHandlerFilter;
import tuf.webscaf.app.http.validationFilters.assignmentAttemptMarksHandler.UpdateAssignmentAttemptMarksHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;
import tuf.webscaf.springDocImpl.UpdateAssignmentAttemptMarksDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class AssignmentAttemptMarksRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "index",
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status"),
                                            @Parameter(in = ParameterIn.QUERY, name = "studentUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "assignmentAttemptUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/documents/show/{documentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "getDocumentUUID",
                            operation = @Operation(
                                    operationId = "getDocumentUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "Successfully Operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                                                    schema = @Schema(type = "object", implementation = AssignmentAttemptMarksEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                                                    schema = @Schema(type = "object", implementation = UpdateAssignmentAttemptMarksDocImpl.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/status/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "status",
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
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
                            path = "/lms/api/v1/teacher/assignment-attempt-marks/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = AssignmentAttemptMarksHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveAssignmentAttemptMarksEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> assignmentAttemptMarksRoutes(AssignmentAttemptMarksHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/teacher/assignment-attempt-marks/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexAssignmentAttemptMarksHandlerFilter())
                .and(RouterFunctions.route(GET("lms/api/v1/teacher/assignment-attempt-marks/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowAssignmentAttemptMarksHandlerFilter()))
                .and(RouterFunctions.route(POST("lms/api/v1/teacher/assignment-attempt-marks/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreAssignmentAttemptMarksHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/teacher/assignment-attempt-marks/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateAssignmentAttemptMarksHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/teacher/assignment-attempt-marks/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowAssignmentAttemptMarksHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/teacher/assignment-attempt-marks/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowAssignmentAttemptMarksHandlerFilter()));
    }
}
