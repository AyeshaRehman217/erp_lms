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
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveQuizEntity;
import tuf.webscaf.app.http.handler.LectureDiscussionHandler;
import tuf.webscaf.app.http.handler.QuizHandler;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionHandler.*;
import tuf.webscaf.app.http.validationFilters.quizHandler.ShowAttachmentInQuizHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class LectureDiscussionsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "index",
                            operation = @Operation(
                                    operationId = "index",
                                    security = { @SecurityRequirement(name = "bearer") },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Get the Records With Pagination",
                                    parameters = {
                                            @Parameter(in = ParameterIn.QUERY,name = "s"),
                                            @Parameter(in = ParameterIn.QUERY,name = "p"),
                                            @Parameter(in = ParameterIn.QUERY,name = "d"),
                                            @Parameter(in = ParameterIn.QUERY,name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY,name = "skw", description = "Search with name"),
                                            @Parameter(in = ParameterIn.QUERY,name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = { @SecurityRequirement(name = "bearer") },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH,name = "uuid")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/documents/show/{documentUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "getDocumentUUID",
                            operation = @Operation(
                                    operationId = "getDocumentUUID",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Check if Document Exist in Lecture Discussion Handler (Used By drive module)",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "documentUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = { @SecurityRequirement(name = "bearer") },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                    },
                                    description = "Store the Record",
                                    requestBody = @RequestBody(
                                            description = "Create Forum Discussion",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LectureDiscussionEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    security = { @SecurityRequirement(name = "bearer") },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH,name = "uuid"),
                                    },
                                    description = "Update the Record for given uuid",
                                    requestBody = @RequestBody(
                                            description = "Update Forum Discussion",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LectureDiscussionEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussions/status/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "status",
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
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
                            path = "/lms/api/v1/lecture-discussions/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = LectureDiscussionHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = { @SecurityRequirement(name = "bearer") },
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404",description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH,name = "uuid"),
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> lectureDiscussionRoutes(LectureDiscussionHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/lecture-discussions/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexLectureDiscussionHandlerFilter())
                .and(RouterFunctions.route(GET("lms/api/v1/lecture-discussions/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowLectureDiscussionHandlerFilter()))
                .and(RouterFunctions.route(POST("lms/api/v1/lecture-discussions/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreLectureDiscussionHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/lecture-discussions/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateLectureDiscussionHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/lecture-discussions/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowLectureDiscussionHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/lecture-discussions/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowLectureDiscussionHandlerFilter()));
    }
}
