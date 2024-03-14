package tuf.webscaf.app.http.validationFilters.assignmentAttemptMarksHandler;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.util.ArrayList;
import java.util.List;

public class UpdateAssignmentAttemptMarksHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (!request.pathVariable("uuid").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Invalid UUID"
                                )
                        );
                    }

//                    if (value.containsKey("assignmentAttemptUUID")) {
//                        if (value.getFirst("assignmentAttemptUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "Assignment Attempt Marks Field Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("assignmentAttemptUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid Assignment Attempt Marks"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Assignment Attempt Marks Field Required"
//                                )
//                        );
//                    }

                    if (value.containsKey("obtainedMarks")) {
                        if (value.getFirst("obtainedMarks").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Obtained Marks Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("obtainedMarks").matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Obtained Marks"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Obtained Marks Field Required"
                                )
                        );
                    }

                    if (value.containsKey("comment") && !value.getFirst("comment").isEmpty()) {
                        if (!value.getFirst("comment").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Comment"
                                    )
                            );
                        }
                    }


                    if (messages.isEmpty() != true) {
                        CustomResponse appresponse = new CustomResponse();
                        return appresponse.set(
                                HttpStatus.BAD_REQUEST.value(),
                                HttpStatus.BAD_REQUEST.name(),
                                null,
                                "eng",
                                "token",
                                0L,
                                0L,
                                messages,
                                Mono.empty()
                        );
                    }

                    return next.handle(request);
                });
    }
}
