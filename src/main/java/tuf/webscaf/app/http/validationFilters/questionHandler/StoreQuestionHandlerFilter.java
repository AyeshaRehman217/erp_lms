package tuf.webscaf.app.http.validationFilters.questionHandler;

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
import java.util.Objects;

public class StoreQuestionHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
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

                    if (value.containsKey("questionText")) {
                        if (!value.getFirst("questionText").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Question text"
                                    )
                            );
                        }
                    }

                    if ((value.containsKey("attachmentUUID") && (!Objects.equals(value.getFirst("remarksAttachmentUUID"), "")))) {
                        if (!value.getFirst("attachmentUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Attachment UUID"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("questionCategoryUUID")) {
                        if (value.getFirst("questionCategoryUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Question Category Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("questionCategoryUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Question Category"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Question Category Field Required"
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
