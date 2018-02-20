package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.service.DevotionData;
import com.groupdevotions.server.service.DevotionService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.server.service.StudyLessonNavigation;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("devotion")
public class DevotionResource {
    protected static final Logger logger = Logger.getLogger(DevotionResource.class.getName());
    private final DevotionService devotionService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final SecurityService securityService;

    @Inject
    public DevotionResource(DevotionService devotionService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, SecurityService securityService) {
        this.devotionService = devotionService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.securityService = securityService;
    }

    @GET
    @Path("/today")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<DevotionData> today(@Context HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<DevotionData> response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<DevotionData>(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            response = devotionService.getDevotions(datastore, userInfo, StudyLessonNavigation.TODAY, 0);
            request.getSession().setAttribute("userInfo", userInfo);
        }
        return response;
    }

    @GET
    @Path("/previous")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<DevotionData> previous(@Context HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<DevotionData> response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<DevotionData>(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            response = devotionService.getDevotions(datastore, userInfo, StudyLessonNavigation.PREVIOUS, 0);
            request.getSession().setAttribute("userInfo", userInfo);
        }
        return response;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Object> save(@Context HttpServletRequest request, DevotionData devotionData) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object> response = securityService.check(userInfo);
        if (response == null) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            if (devotionData.readingCompleteStudyLesson != null) {
                devotionService.readingComplete(datastore, userInfo, devotionData.readingCompleteStudyLesson);
                return new Response<Object>((Object) null);
            }
            Response.Message message = devotionService.saveAnswersForGroupStudyAndSendAccountabilityEmail(datastore, userInfo, devotionData);
            response = new Response<>(message);
        }
        return response;
    }
}
