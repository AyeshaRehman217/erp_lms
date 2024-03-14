package tuf.webscaf.app.http.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import tuf.webscaf.app.dbContext.master.dto.DocumentDto;
import tuf.webscaf.app.dbContext.master.entity.LectureDiscussionPostDocumentPvtEntity;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPostDocumentPvtRepository;
import tuf.webscaf.app.dbContext.master.repositry.LectureDiscussionPostRepository;
import tuf.webscaf.app.dbContext.slave.repositry.SlaveLectureDiscussionPostDocumentPvtRepository;
import tuf.webscaf.app.service.ApiCallService;
import tuf.webscaf.app.verification.module.AuthHasPermission;
import tuf.webscaf.config.service.response.AppResponse;
import tuf.webscaf.config.service.response.AppResponseMessage;
import tuf.webscaf.config.service.response.CustomResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Component
@Tag(name = "lectureDiscussionPostDocumentPvtHandler")
public class LectureDiscussionPostDocumentPvtHandler {

    @Value("${server.zone}")
    private String zone;

    @Autowired
    CustomResponse appresponse;

    @Autowired
    LectureDiscussionPostRepository lectureDiscussionPostRepository;

    @Autowired
    LectureDiscussionPostDocumentPvtRepository lectureDiscussionPostDocumentPvtRepository;

    @Autowired
    SlaveLectureDiscussionPostDocumentPvtRepository slaveLectureDiscussionPostDocumentPvtRepository;

    @Autowired
    ApiCallService apiCallService;

    @Value("${server.erp_drive_module.uri}")
    private String driveModuleUri;

    //This Function Fetch Mapped Document UUID's against Lecture Discussion Post from Pvt
    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-post-documents_list_show")
    public Mono<ServerResponse> showList(ServerRequest serverRequest) {
        final UUID lectureDiscussionPostUUID = UUID.fromString(serverRequest.pathVariable("lectureDiscussionPostUUID"));

        return slaveLectureDiscussionPostDocumentPvtRepository.getAllMappedDocumentUUIDAgainstLectureDiscussionPost(lectureDiscussionPostUUID)
                .flatMap(uuids -> {
                    List<String> listOfIds = Arrays.asList(uuids.split("\\s*,\\s*"));
                    return responseSuccessMsg("Records Fetched Successfully", listOfIds);
                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                .onErrorResume(err -> responseErrorMsg("Record does not exist. Please Contact Developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-post-documents_store")
    public Mono<ServerResponse> store(ServerRequest serverRequest) {

        String userId = serverRequest.headers().firstHeader("auid");
        final UUID lectureDiscussionPostUUID = UUID.fromString(serverRequest.pathVariable("lectureDiscussionPostUUID"));

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown User");
        }

        return serverRequest.formData()
                .flatMap(value -> lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                        .flatMap(lectureDiscussionPostEntity -> {

                            List<String> listOfDocumentString = new ArrayList<>(value.get("documentUUID"));


                            listOfDocumentString.removeIf(s -> s.equals(""));

                            //getting List of Document From Front
                            List<UUID> listOfDocumentUUID = new ArrayList();

                            for (String docs : listOfDocumentString) {
                                listOfDocumentUUID.add(UUID.fromString(docs));
                            }

                            //Sending Doc ids in Form data to check if doc Ids exist
                            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>(); //getting multiple Values from form data

                            for (String listOfValues : listOfDocumentString) {
                                formData.add("docId", listOfValues);   //iterating over multiple values and then adding in list
                            }

                            if (!listOfDocumentUUID.isEmpty()) {

                                return apiCallService.postDataList(formData, driveModuleUri + "api/v1/documents/show/map")
                                        .flatMap(documentJsonNode -> {
                                            //Reading the Response "Data" Object from Json Node
                                            final JsonNode arrNode2 = documentJsonNode.get("data");

                                            List<UUID> existingDocumentUUID = new ArrayList<>();

                                            if (arrNode2.isArray()) {
                                                for (final JsonNode objNode : arrNode2) {
                                                    for (UUID documentIdData : listOfDocumentUUID) {
                                                        JsonNode key = objNode.get(String.valueOf(documentIdData));

                                                        // create document attachment dto for only
                                                        if (key != null) {
                                                            existingDocumentUUID.add(UUID.fromString(key.get("docId").toString().replaceAll("\"", "")));
                                                        }

                                                    }
                                                }
                                            }


                                            if (existingDocumentUUID.isEmpty()) {
                                                return responseInfoMsg("Document Does Not Exist");
                                            } else {
                                                return lectureDiscussionPostDocumentPvtRepository.findAllByLectureDiscussionPostUUIDAndDocumentUUIDInAndDeletedAtIsNull(lectureDiscussionPostUUID, listOfDocumentUUID)
                                                        .collectList()
                                                        .flatMap(lectureDiscussionPostPvtEntity -> {

                                                            for (LectureDiscussionPostDocumentPvtEntity pvtEntity : lectureDiscussionPostPvtEntity) {
                                                                //Removing Existing Document UUID in Document Final List to be saved that does not contain already mapped values
                                                                listOfDocumentUUID.remove(pvtEntity.getDocumentUUID());
                                                                existingDocumentUUID.remove(pvtEntity.getDocumentUUID());
                                                            }

                                                            List<LectureDiscussionPostDocumentPvtEntity> listPvt = new ArrayList<>();


                                                            // iterate Document UUIDs for given Student Guardian
                                                            for (UUID documentUUID : existingDocumentUUID) {

                                                                LectureDiscussionPostDocumentPvtEntity lectureDiscussionPostDocumentPvt = LectureDiscussionPostDocumentPvtEntity
                                                                        .builder()
                                                                        .documentUUID(documentUUID)
                                                                        .uuid(UUID.randomUUID())
                                                                        .lectureDiscussionPostUUID(lectureDiscussionPostUUID)
                                                                        .createdBy(UUID.fromString(userId))
                                                                        .createdAt(LocalDateTime.now(ZoneId.of(zone)))
                                                                        .reqCompanyUUID(UUID.fromString(reqCompanyUUID))
                                                                        .reqBranchUUID(UUID.fromString(reqBranchUUID))
                                                                        .reqCreatedIP(reqIp)
                                                                        .reqCreatedPort(reqPort)
                                                                        .reqCreatedBrowser(reqBrowser)
                                                                        .reqCreatedOS(reqOs)
                                                                        .reqCreatedDevice(reqDevice)
                                                                        .reqCreatedReferer(reqReferer)
                                                                        .build();

                                                                listPvt.add(lectureDiscussionPostDocumentPvt);
                                                            }

                                                            return lectureDiscussionPostDocumentPvtRepository.saveAll(listPvt)
                                                                    .collectList()
                                                                    .flatMap(savePvtList -> {

                                                                        //Creating Final Document List to Update the Status
                                                                        List<UUID> finalDocumentList = new ArrayList<>();

                                                                        for (LectureDiscussionPostDocumentPvtEntity pvtData : savePvtList) {
                                                                            finalDocumentList.add(pvtData.getDocumentUUID());
                                                                        }
                                                                        //Empty List for Document Ids from Json Request user Enters
                                                                        List<String> listOfDoc = new ArrayList<>();

                                                                        finalDocumentList.forEach(uuid -> {
                                                                            if (uuid != null) {
                                                                                listOfDoc.add(uuid.toString());
                                                                            }
                                                                        });


                                                                        //Sending Document ids in Form data to check if document Id's exist
                                                                        MultiValueMap<String, String> updateDocumentFormData = new LinkedMultiValueMap<>(); //getting multiple Values from form data
                                                                        for (String updatedDocs : listOfDoc) {
                                                                            updateDocumentFormData.add("docId", updatedDocs);//iterating over multiple values and then adding in list
                                                                        }

                                                                        // if records already exist then document map key set will be empty
                                                                        if (existingDocumentUUID.isEmpty()) {
                                                                            return responseSuccessMsg("Record Already Exists", existingDocumentUUID)
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                                        } else {
                                                                            return apiCallService.updateDataList(updateDocumentFormData, driveModuleUri + "api/v1/documents/submitted/update")
                                                                                    .flatMap(document -> responseSuccessMsg("Record Stored Successfully", existingDocumentUUID))
                                                                                    .switchIfEmpty(responseInfoMsg("Unable to store record. There is something wrong please contact developer."))
                                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store record. Please contact developer."));
                                                                        }
                                                                    }).switchIfEmpty(responseInfoMsg("Unable to store Record.There is something wrong please try again."))
                                                                    .onErrorResume(err -> responseErrorMsg("Unable to store Record.Please Contact Developer."));
                                                        });
                                            }

                                        }).switchIfEmpty(responseInfoMsg("Document Does not exist."))
                                        .onErrorResume(ex -> responseErrorMsg("Document Does not exist.Please Contact Developer."));
                            } else {
                                return responseInfoMsg("Select Documents First");
                            }
                        }).switchIfEmpty(responseInfoMsg("Lecture Discussion Post Record does not exist"))
                        .onErrorResume(err -> responseInfoMsg("Lecture Discussion Post Record does not exist. Please contact developer."))
                ).switchIfEmpty(responseInfoMsg("Unable to read request"))
                .onErrorResume(ex -> responseErrorMsg("Unable to read request. Please contact developer."));
    }

    @AuthHasPermission(value = "lms_api_v1_lecture-discussion-post-documents_delete")
    public Mono<ServerResponse> delete(ServerRequest serverRequest) {
        final UUID lectureDiscussionPostUUID = UUID.fromString(serverRequest.pathVariable("lectureDiscussionPostUUID"));
        UUID documentUUID = UUID.fromString(serverRequest.queryParam("documentUUID").map(String::toString).orElse(""));
        String userId = serverRequest.headers().firstHeader("auid");

        String reqCompanyUUID = serverRequest.headers().firstHeader("reqCompanyUUID");
        String reqBranchUUID = serverRequest.headers().firstHeader("reqBranchUUID");
        String reqIp = serverRequest.headers().firstHeader("reqIp");
        String reqPort = serverRequest.headers().firstHeader("reqPort");
        String reqBrowser = serverRequest.headers().firstHeader("reqBrowser");
        String reqOs = serverRequest.headers().firstHeader("reqOs");
        String reqDevice = serverRequest.headers().firstHeader("reqDevice");
        String reqReferer = serverRequest.headers().firstHeader("reqReferer");

        if (userId == null) {
            return responseWarningMsg("Unknown user");
        } else if (!userId.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$")) {
            return responseWarningMsg("Unknown User");
        }

        return lectureDiscussionPostRepository.findByUuidAndDeletedAtIsNull(lectureDiscussionPostUUID)
                .flatMap(lectureDiscussionPostEntity -> apiCallService.getDataWithUUID(driveModuleUri + "api/v1/documents/show/", documentUUID)
                                .flatMap(documentJson -> apiCallService.checkDocId(documentJson)
                                                .flatMap(language -> lectureDiscussionPostDocumentPvtRepository.findFirstByLectureDiscussionPostUUIDAndDocumentUUIDAndDeletedAtIsNull(lectureDiscussionPostUUID, documentUUID)
                                                                .flatMap(lectureDiscussionPostDocumentPvtEntity -> {

                                                                    lectureDiscussionPostDocumentPvtEntity.setDeletedAt(LocalDateTime.now());
                                                                    lectureDiscussionPostDocumentPvtEntity.setDeletedBy(UUID.fromString(userId));
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqCompanyUUID(UUID.fromString(reqCompanyUUID));
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqBranchUUID(UUID.fromString(reqBranchUUID));
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedIP(reqIp);
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedPort(reqPort);
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedBrowser(reqBrowser);
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedOS(reqOs);
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedDevice(reqDevice);
                                                                    lectureDiscussionPostDocumentPvtEntity.setReqDeletedReferer(reqReferer);

                                                                    return lectureDiscussionPostDocumentPvtRepository.save(lectureDiscussionPostDocumentPvtEntity)
                                                                            .flatMap(deleteEntity -> responseSuccessMsg("Record Deleted Successfully", documentDtoMapper(documentJson)))
                                                                            .switchIfEmpty(responseInfoMsg("Unable to delete the record.There is something wrong please try again."))
                                                                            .onErrorResume(err -> responseErrorMsg("Unable to delete the record.Please Contact Developer."));
                                                                }).switchIfEmpty(responseInfoMsg("Record does not exist"))
                                                                .onErrorResume(err -> responseErrorMsg("Record does not exist.Please Contact Developer."))
                                                )
                                ).switchIfEmpty(responseInfoMsg("Document Does not exist"))
                                .onErrorResume(ex -> responseErrorMsg("Document Does not Exist.Please Contact Developer."))
                ).switchIfEmpty(responseInfoMsg("Lecture Discussion Post Record does not exist."))
                .onErrorResume(ex -> responseErrorMsg("Lecture Discussion Post Record does not exist.Please Contact Developer."));
    }

    public DocumentDto documentDtoMapper(JsonNode jsonNode) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        final JsonNode arrNode = jsonNode.get("data");
        JsonNode objectNode = null;
        if (arrNode.isArray()) {
            for (final JsonNode objNode : arrNode) {
                objectNode = objNode;
            }
        }
        ObjectReader reader = mapper.readerFor(new TypeReference<DocumentDto>() {
        });
        DocumentDto documentDto = null;
        if (!jsonNode.get("data").isEmpty()) {
            try {
                documentDto = reader.readValue(objectNode);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return documentDto;
    }

    public Mono<ServerResponse> responseInfoMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );


        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()

        );
    }

    public Mono<ServerResponse> responseIndexInfoMsg(String msg, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.INFO,
                        msg
                )
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.empty()

        );
    }


    public Mono<ServerResponse> responseErrorMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.ERROR,
                        msg
                )
        );

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

    public Mono<ServerResponse> responseSuccessMsg(String msg, Object entity) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseIndexSuccessMsg(String msg, Object entity, Long totalDataRowsWithFilter) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.SUCCESS,
                        msg)
        );

        return appresponse.set(
                HttpStatus.OK.value(),
                HttpStatus.OK.name(),
                null,
                "eng",
                "token",
                totalDataRowsWithFilter,
                0L,
                messages,
                Mono.just(entity)
        );
    }

    public Mono<ServerResponse> responseWarningMsg(String msg) {
        var messages = List.of(
                new AppResponseMessage(
                        AppResponse.Response.WARNING,
                        msg)
        );


        return appresponse.set(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                HttpStatus.UNPROCESSABLE_ENTITY.name(),
                null,
                "eng",
                "token",
                0L,
                0L,
                messages,
                Mono.empty()
        );
    }
}
