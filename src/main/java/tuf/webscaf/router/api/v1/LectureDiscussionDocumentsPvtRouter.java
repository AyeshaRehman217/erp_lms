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
import tuf.webscaf.app.dbContext.master.dto.DocumentDto;
import tuf.webscaf.app.http.handler.LectureDiscussionDocumentPvtHandler;
import tuf.webscaf.app.http.validationFilters.gradingTypeHandler.IndexGradingTypeHandlerFilter;
import tuf.webscaf.app.http.validationFilters.gradingTypeHandler.ShowGradingTypeHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionDocumentPvtHandler.DeleteLectureDiscussionDocumentPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionDocumentPvtHandler.ShowLectureDiscussionDocumentPvtHandlerFilter;
import tuf.webscaf.app.http.validationFilters.lectureDiscussionDocumentPvtHandler.StoreLectureDiscussionDocumentPvtHandlerFilter;
import tuf.webscaf.springDocImpl.DocumentDocImpl;

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.web.reactive.function.server.RequestPredicates.*;


@Configuration
public class LectureDiscussionDocumentsPvtRouter {

    @Bean
    @RouterOperations(
            {
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussion-documents/list/show/{lectureDiscussionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.GET,
                            beanClass = LectureDiscussionDocumentPvtHandler.class,
                            beanMethod = "showList",
                            operation = @Operation(
                                    operationId = "showList",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = DocumentDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Show the list of Document UUIDs that are mapped for given Lecture Discussion \n" +
                                            "This Route is used By Drive Module in Document Handler",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "lectureDiscussionUUID")
                                    }
                            )
                    ),
                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussion-documents/store/{lectureDiscussionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.POST,
                            beanClass = LectureDiscussionDocumentPvtHandler.class,
                            beanMethod = "store",
                            operation = @Operation(
                                    operationId = "store",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = DocumentDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    requestBody = @RequestBody(
                                            description = "Create Documents for a Lecture Discussion",
                                            required = true,
                                            content = @Content(
                                                    mediaType = "application/x-www-form-urlencoded",
                                                    encoding = {
                                                            @Encoding(name = "document", contentType = "application/x-www-form-urlencoded")
                                                    },
                                                    schema = @Schema(type = "object", implementation = DocumentDocImpl.class)
                                            )),
                                    description = "Store Documents Against a Given Lecture Discussion",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "lectureDiscussionUUID")
                                    }
                            )
                    ),

                    @RouterOperation(
                            path = "/lms/api/v1/lecture-discussion-documents/delete/{lectureDiscussionUUID}",
                            produces = {
                                    MediaType.APPLICATION_FORM_URLENCODED_VALUE
                            },
                            method = RequestMethod.DELETE,
                            beanClass = LectureDiscussionDocumentPvtHandler.class,
                            beanMethod = "delete",
                            operation = @Operation(
                                    operationId = "delete",
                                    security = {@SecurityRequirement(name = "bearer")},
                                    responses = {
                                            @ApiResponse(
                                                    responseCode = "200",
                                                    description = "successful operation",
                                                    content = @Content(schema = @Schema(
                                                            implementation = DocumentDto.class
                                                    ))
                                            ),
                                            @ApiResponse(responseCode = "404", description = "Records not found!",
                                                    content = @Content(schema = @Schema(hidden = true))
                                            )
                                    },
                                    description = "Delete Documents Against a Given Lecture Discussion",
                                    parameters = {
                                            @Parameter(in = ParameterIn.PATH, name = "lectureDiscussionUUID"),
                                            @Parameter(in = ParameterIn.QUERY, name = "documentUUID"),
                                    }
                            )
                    )
            }
    )

    public RouterFunction<ServerResponse> lectureDiscussionDocumentPvtRoutes(LectureDiscussionDocumentPvtHandler handle) {
        return RouterFunctions.route(GET("lms/api/v1/lecture-discussion-documents/list/show/{lectureDiscussionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::showList).filter(new ShowLectureDiscussionDocumentPvtHandlerFilter())
                .and(RouterFunctions.route(POST("lms/api/v1/lecture-discussion-documents/store/{lectureDiscussionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::store).filter(new StoreLectureDiscussionDocumentPvtHandlerFilter()))
                .and(RouterFunctions.route(DELETE("lms/api/v1/lecture-discussion-documents/delete/{lectureDiscussionUUID}").and(accept(APPLICATION_FORM_URLENCODED)), handle::delete).filter(new DeleteLectureDiscussionDocumentPvtHandlerFilter()));
    }

}
