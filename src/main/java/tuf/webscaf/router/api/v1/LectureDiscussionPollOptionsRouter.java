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
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPollOptionEntity;
import tuf.webscaf.app.dbContext.slave.entity.SlaveLectureDiscussionPollOptionEntity;
import tuf.webscaf.app.http.handler.LectureDiscussionPollOptionHandler;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionPollOptionHandler.IndexLectureDiscussionPollOptionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionPollOptionHandler.ShowLectureDiscussionPollOptionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionPollOptionHandler.StoreLectureDiscussionPollOptionHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionPollOptionHandler.UpdateLectureDiscussionPollOptionHandlerFilter;
import tuf.webscaf.springDocImpl.StatusDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
public class LectureDiscussionPollOptionsRouter {
    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/forum-poll-options/index",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "index",
                            operation = @Operation(
                                    operationId = "index",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
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
                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with title"),
                                            @Parameter(in = ParameterIn.QUERY, name = "status")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/forum-poll-options/show/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "show",
                            operation = @Operation(
                                    operationId = "show",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
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
                            path = "/lms/api/v1/forum-poll-options/store",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    parameters = {
                                    },
                                    description = "Store the Record",
                                    requestBody = @RequestBody(
                                            description = "Create Quiz Categories",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LectureDiscussionPollOptionEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/forum-poll-options/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "update",
                            operation = @Operation(
                                    operationId = "update",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
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
                                            description = "Update Quiz Categories",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = LectureDiscussionPollOptionEntity.class)
                                            ))
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/forum-poll-options/status/update/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.PUT,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "status",
                            operation = @Operation(
                                    operationId = "status",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
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
                            path = "/lms/api/v1/forum-poll-options/delete/{uuid}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = LectureDiscussionPollOptionHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = SlaveLectureDiscussionPollOptionEntity.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete the Record for given uuid",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
                                    }
                            )
                    )
            }
    )
    public RouterFunction<ServerResponse> lectureDiscussionPollOptionRoutes(LectureDiscussionPollOptionHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/forum-poll-options/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexLectureDiscussionPollOptionHandlerFilter())
                .and(RouterFunctions.route(GET("lms/api/v1/forum-poll-options/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowLectureDiscussionPollOptionHandlerFilter()))
                .and(RouterFunctions.route(POST("lms/api/v1/forum-poll-options/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreLectureDiscussionPollOptionHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/forum-poll-options/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateLectureDiscussionPollOptionHandlerFilter()))
                .and(RouterFunctions.route(PUT("lms/api/v1/forum-poll-options/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowLectureDiscussionPollOptionHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/forum-poll-options/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowLectureDiscussionPollOptionHandlerFilter()));
    }
}
