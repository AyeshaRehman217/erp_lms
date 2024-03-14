package tuf.webscaf.app.http.validationFilters.assignmentHandler;

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

public class StoreAssignmentHandlerFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {
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

                    if (value.containsKey("instruction")) {
                        if (!value.getFirst("instruction").matches("^[\\sa-zA-Z0-9*.,!@#$&()_-]*$")) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Invalid Instruction"
                                    )
                            );
                        }
                    }

                    if (value.containsKey("fromDate")) {
                        if (value.getFirst("fromDate").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "From Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("fromDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "From Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "From Date Field Required"
                                )
                        );
                    }

                    if (value.containsKey("toDate")) {
                        if (value.getFirst("toDate").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "To Date Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("toDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "To Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "To Date Field Required"
                                )
                        );
                    }

                    if (value.containsKey("extendedDate")) {
                        if (!value.getFirst("extendedDate").isEmpty()) {
                            if (!value.getFirst("extendedDate").matches("^(0[1-9]|[1-2][0-9]|3[0-1])-(0[1-9]|1[0-2])-[0-9]{4}\\s(2[0-3]|[01][0-9]):[0-5][0-9]:[0-5][0-9]$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Extended Date must be in dd-MM-yyyy HH:mm:ss format"
                                        )
                                );
                            }
                        }
                    }

                    List<String> listOfDocuments = value.get("documentUUID");
                    boolean notValid = false;

                    if (value.containsKey("documentUUID")) {
                        listOfDocuments.removeIf(s -> s.equals(""));
                        if (listOfDocuments.isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Select Documents First"
                                    )
                            );
                        } else {
                            for (String val : listOfDocuments) {
                                if (!val.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                    notValid = true;
                                    break;
                                }
                            }
                            if (notValid) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Document must be valid"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Select Documents First"
                                )
                        );
                    }

                    if (value.containsKey("totalMark")) {
                        if (value.getFirst("totalMark").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Total Marks Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("totalMark").matches("^[+-]?([0-9]+([.][0-9]*)?|[.][0-9]+)$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Total Marks"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Total Marks Field Required"
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


                    if (value.containsKey("academicSessionUUID")) {
                        if (value.getFirst("academicSessionUUID").isEmpty()) {
                            messages.add(
                                    new AppResponseMessage(
                                            AppResponse.Response.ERROR,
                                            "Academic Session Field Required"
                                    )
                            );
                        } else {
                            if (!value.getFirst("academicSessionUUID").matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
                                messages.add(
                                        new AppResponseMessage(
                                                AppResponse.Response.ERROR,
                                                "Invalid Academic Session"
                                        )
                                );
                            }
                        }
                    } else {
                        messages.add(
                                new AppResponseMessage(
                                        AppResponse.Response.ERROR,
                                        "Academic Session Field Required"
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
