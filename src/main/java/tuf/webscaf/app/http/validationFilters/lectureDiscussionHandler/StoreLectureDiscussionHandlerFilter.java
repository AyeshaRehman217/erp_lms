package tuf.webscaf.app.http.validationFilters.lectureDiscussionHandler;

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

public class StoreLectureDiscussionHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {
                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.containsKey("name")) {
                        if (value.getFirst("name").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Name Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("name").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Name"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Name Field Required"
                                )
                        );
                    }

                    if (value.containsKey("description")) {
                        if (!value.getFirst("description").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Description"
                                    )
                            );
                        }
                    }


                    if (value.containsKey("startDate")) {
                        if (value.getFirst("startDate").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Start Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("startDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Start Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Start Date Field Required"
                                )
                        );
                    }

                    if (value.containsKey("endDate")) {
                        if (value.getFirst("endDate").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "End Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("endDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "End Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        " End Date Field Required"
                                )
                        );
                    }


                    if (value.containsKey("courseSubjectUUID")) {
                        if (value.getFirst("courseSubjectUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Course Subject Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("courseSubjectUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Course Subject"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Course Subject Field Required"
                                )
                        );
                    }

                    if (value.containsKey("attachmentUUID") && !value.getFirst("attachmentUUID").isEmpty()) {
                        if (!value.getFirst("attachmentUUID").matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Document"
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
