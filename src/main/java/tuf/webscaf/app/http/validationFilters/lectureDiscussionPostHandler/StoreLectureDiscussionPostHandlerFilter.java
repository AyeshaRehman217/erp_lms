package tuf.webscaf.app.http.validationFilters.lectureDiscussionPostHandler;

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

public class StoreLectureDiscussionPostHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {

        return request.formData()
                .flatMap(value -> {

                    List<AppResponseMessage> messages = new ArrayList<>();

                    if (value.containsKey("subject")) {
                        if (value.getFirst("subject").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Subject Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("subject").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Subject"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Subject Field Required"
                                )
                        );
                    }

                    if (value.containsKey("message")) {
                        if (value.getFirst("message").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Message Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("message").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Message"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Message Field Required"
                                )
                        );
                    }

                    if (value.containsKey("lectureDiscussionUUID")) {
                        if (value.getFirst("lectureDiscussionUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Lecture Discussion Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("lectureDiscussionUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Lecture Discussion"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Lecture Discussion Field Required"
                                )
                        );
                    }

//                    if (value.containsKey("attachmentUUID")) {
//                        if (value.getFirst("attachmentUUID").isEmpty()) {
//                            messages.add(
//                                    new AppResponseMessage(
//                                            AppResponse.Response.ERROR,
//                                            "Attachment Field Required"
//                                    )
//                            );
//                        } else {
//                            if (!value.getFirst("attachmentUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
//                                messages.add(
//                                        new AppResponseMessage(
//                                                AppResponse.Response.ERROR,
//                                                "Invalid Attachment"
//                                        )
//                                );
//                            }
//                        }
//                    } else {
//                        messages.add(
//                                new AppResponseMessage(
//                                        AppResponse.Response.ERROR,
//                                        "Attachment Field Required"
//                                )
//                        );
//                    }

                    if (value.containsKey("date")) {
                        if (value.getFirst("date").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("date").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Date Field Required"
                                )
                        );
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
