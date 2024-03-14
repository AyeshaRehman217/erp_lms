//package tuf.webscaf.router.api.v1;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.Parameter;
//import io.swagger.v3.oas.annotations.enums.ParameterIn;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Encoding;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.parameters.RequestBody;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import org.springdoc.core.annotations.RouterOperation;
//import org.springdoc.core.annotations.RouterOperations;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.RequestMethod;
//import org.springframework.web.reactive.function.server.RouterFunction;
//import org.springframework.web.reactive.function.server.RouterFunctions;
//import org.springframework.web.reactive.function.server.ServerResponse;
//import tuf.webscaf.app.dbContext.master.entity.QuestionBankEntity;
//import tuf.webscaf.app.dbContext.slave.entity.SlaveQuestionBankEntity;
//import tuf.webscaf.app.http.handler.QuestionBankHandler;
//import tuf.webscaf.app.http.validationFilters.questionBankHandler.IndexQuestionBankHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.questionBankHandler.ShowQuestionBankHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.questionBankHandler.StoreQuestionBankHandlerFilter;
//import tuf.webscaf.app.http.validationFilters.questionBankHandler.UpdateQuestionBankHandlerFilter;
//import tuf.webscaf.springDocImpl.StatusDocImpl;
//
//import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
//import static org.springframework.web.reactive.function.server.RequestPredicates.*;
//
//@Configuration
//public class QuestionBanksRouter {
//    @Bean
//    @RouterOperations(
//            {
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/index",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "index",
//                            operation = @Operation(
//                                    operationId = "index",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Get the Records With Pagination",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.QUERY, name = "s"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "p"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "d"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "dp", description = "Sorting can be based on all columns. Default sort is in ascending order by createdAt"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "skw", description = "Search with name or description"),
//                                            @Parameter(in = ParameterIn.QUERY, name = "status")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/show/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.GET,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "show",
//                            operation = @Operation(
//                                    operationId = "show",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Show the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid")
//                                    }
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/store",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.POST,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "store",
//                            operation = @Operation(
//                                    operationId = "store",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    },
//                                    description = "Store the Record",
//                                    requestBody = @RequestBody(
//                                            description = "Create Grading Method",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = QuestionBankEntity.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/update/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "update",
//                            operation = @Operation(
//                                    operationId = "update",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    },
//                                    description = "Update the Record for given uuid",
//                                    requestBody = @RequestBody(
//                                            description = "Update Grading Method",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = QuestionBankEntity.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/status/update/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.PUT,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "status",
//                            operation = @Operation(
//                                    operationId = "status",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Update the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    },
//                                    requestBody = @RequestBody(
//                                            description = "Update the Status",
//                                            required = true,
//                                            content = @Content(
//                                                    mediaType = "application/x-www-form-urlencoded",
//                                                    encoding = {
//                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
//                                                    },
//                                                    schema = @Schema(type = "object", implementation = StatusDocImpl.class)
//                                            ))
//                            )
//                    ),
//                    @RouterOperation(
//                            path = "/lms/api/v1/question-banks/delete/{uuid}",
//                            produces = {
//                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
//                            },
//                            method = RequestMethod.DELETE,
//                            beanClass = QuestionBankHandler.class,
//                            beanMethod = "delete",
//                            operation = @Operation(
//                                    operationId = "delete",
//                                    security = {@SecurityRequirement(name = "bearer")},
//                                    responses = {
//                                            @ApiResponse(
//                                                    responseCode = "200",
//                                                    description = "successful operation",
//                                                    content = @Content(schema = @Schema(
//                                                            implementation = SlaveQuestionBankEntity.class
//                                                    ))
//                                            ),
//                                            @ApiResponse(responseCode = "404", description = "Record does not exist",
//                                                    content = @Content(schema = @Schema(hidden = true))
//                                            )
//                                    },
//                                    description = "Delete the Record for given uuid",
//                                    parameters = {
//                                            @Parameter(in = ParameterIn.PATH, name = "uuid"),
//                                            @Parameter(in = ParameterIn.HEADER, name = "auid")
//                                    }
//                            )
//                    )
//            }
//    )
//    public RouterFunction<ServerResponse> questionBankRoutes(QuestionBankHandler handle) {
//        return RouterFunctions.route(GET("lms/api/v1/question-banks/index").and(accept(APPLICATION_FORM_URLENCODED)), handle::index).filter(new IndexQuestionBankHandlerFilter())
//                .and(RouterFunctions.route(GET("lms/api/v1/question-banks/show/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::show).filter(new ShowQuestionBankHandlerFilter()))
//                .and(RouterFunctions.route(POST("lms/api/v1/question-banks/store").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreQuestionBankHandlerFilter()))
//                .and(RouterFunctions.route(PUT("lms/api/v1/question-banks/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::update).filter(new UpdateQuestionBankHandlerFilter()))
//                .and(RouterFunctions.route(PUT("lms/api/v1/question-banks/status/update/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::status).filter(new ShowQuestionBankHandlerFilter()))
//                .and(RouterFunctions.route(DELETE("lms/api/v1/question-banks/delete/{uuid}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new ShowQuestionBankHandlerFilter()));
//    }
//}
