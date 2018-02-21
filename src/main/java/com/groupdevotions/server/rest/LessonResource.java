package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.dao.ConfigDAO;
import com.groupdevotions.server.service.LessonService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.server.service.StudyService;
import com.groupdevotions.shared.model.StudyLesson;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by DanV on 8/29/2015.
 */
@Path("lesson")
public class LessonResource {
    protected static final Logger logger = Logger
            .getLogger(LessonResource.class.getName());

    private final SecurityService securityService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final LessonService lessonService;
    private final StudyService studyService;
    private final ConfigDAO configDAO;

    @Inject
    public LessonResource(SecurityService securityService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, LessonService lessonService, StudyService studyService, ConfigDAO configDAO) {
        this.securityService = securityService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.lessonService = lessonService;
        this.studyService = studyService;
        this.configDAO = configDAO;
    }

/*    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    public Response<Collection<Study>> query(@Context HttpServletRequest request, @QueryParam("accountKey") String accountKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<Study>>  response;
        Response<Object> checkResponse = securityService.checkAndAccountKey(userInfo, accountKey);
        if (checkResponse != null) {
            response = new Response<Collection<Study>> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            Collection<Study> studies = studyService.readStudiesForAccount(datastore, userInfo.account);
            response = new Response<Collection<Study>>(studies);
        }
        return response;
    }*/

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    public Response<StudyLesson> get(@Context HttpServletRequest request, @PathParam("key") String studyLessonKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<StudyLesson>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            StudyLesson studyLesson = lessonService.read(datastore, studyLessonKey);
            if (!studyService.isAccountStudyContributor(datastore, studyLesson.studyKey, userInfo.account)) {
                response = new Response("You are not authorized to get this lesson.");
            } else {
                response = new Response(studyLesson);
            }
        }
        return response;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<StudyLesson> add(@Context HttpServletRequest request, StudyLesson lesson) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<StudyLesson>  response;
        Response<Object> checkResponse = securityService.check(userInfo);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            if (!studyService.isAccountStudyContributor(datastore, lesson.studyKey, userInfo.account)) {
                response = new Response("You are not authorized to add this lesson.");
            } else {
                String validationError = lessonService.addLesson(datastore, lesson);
                if (validationError != null) {
                    response = new Response(validationError);
                } else {
                    response = new Response(lesson);
                }
            }
        }
        return response;
    }

    @POST
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<StudyLesson> update(@Context HttpServletRequest request, @PathParam("key") String studyLessonKey, StudyLesson lesson) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<StudyLesson>  response;
        Response<Object> checkResponse = securityService.check(userInfo);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            if (!studyService.isAccountStudyContributor(datastore, lesson.studyKey, userInfo.account)) {
                response = new Response("You are not authorized to edit this lesson.");
            } else {
                String validationError = lessonService.save(datastore, lesson);
                if (validationError != null) {
                    response = new Response(validationError);
                } else {
                    response = new Response(lesson);
                }
            }
        }
        return response;
    }

    @DELETE
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<StudyLesson> delete(@Context HttpServletRequest request, @PathParam("key") String studyLessonKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<StudyLesson>  response;
        Response<Object> checkResponse = securityService.check(userInfo);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            String validationError = lessonService.delete(datastore, studyLessonKey, userInfo.account);
            if (validationError != null) {
                response = new Response(validationError);
            } else {
                response = new Response((StudyLesson) null);
            }
        }
        return response;
    }
}
