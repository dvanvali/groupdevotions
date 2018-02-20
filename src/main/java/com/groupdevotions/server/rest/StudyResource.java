package com.groupdevotions.server.rest;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.dao.ConfigDAO;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.server.service.StudyService;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by DanV on 8/29/2015.
 */
@Path("study")
public class StudyResource {
    protected static final Logger logger = Logger
            .getLogger(StudyResource.class.getName());

    private final SecurityService securityService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final StudyService studyService;
    private final ConfigDAO configDAO;

    @Inject
    public StudyResource(SecurityService securityService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, StudyService studyService, ConfigDAO configDAO) {
        this.securityService = securityService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.studyService = studyService;
        this.configDAO = configDAO;
    }

    /**
     * Read studies for an account or load public studies.
     * @param request
     * @param accountKey if null, load public studies
     * @param loadPublicStudies if true and accountKey is not null load public studies and add any missing account studies
     * @return
     * @throws IOException
     */
    @GET
    @Produces(APPLICATION_JSON)
    public Response<Collection<Study>> query(@Context HttpServletRequest request, @QueryParam("accountKey") String accountKey, @QueryParam("loadPublicStudies") boolean loadPublicStudies) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<Study>>  response;
        Response<Object> checkResponse = null;
        if (accountKey != null) {
            checkResponse = securityService.checkAndAccountKey(userInfo, accountKey);
        }
        if (checkResponse != null) {
            response = new Response<Collection<Study>> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            Collection<Study> studies;
            if (accountKey == null) {
                studies = studyService.readPublicStudies(datastore);
                if (accountKey != null) {
                    addMissingAccountStudiesToPublicStudies(datastore, studies, userInfo.account.studyKeyPublicAccepts);
                }
            } else {
                studies = studyService.readStudiesForAccount(datastore, userInfo, loadPublicStudies);
            }
            response = new Response<Collection<Study>>(studies);
        }
        return response;
    }

    private void addMissingAccountStudiesToPublicStudies(ObjectDatastore datastore, Collection<Study> publicStudies, Collection<String> publicStudyKeyAccepts) {
        Collection<Study> missingStudies = Lists.newArrayList();
        for (String accountStudyKey : publicStudyKeyAccepts) {
            boolean found = false;
            for (Study publicStudy : publicStudies) {
                if (SharedUtils.safeEquals(publicStudy.key, accountStudyKey)) {
                    found = true;
                }
            }

            if (!found) {
                missingStudies.add(studyService.read(datastore, accountStudyKey));
            }
        }

        publicStudies.addAll(missingStudies);
    }

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    public Response<Study> get(@Context HttpServletRequest request, @PathParam("key") String studyKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Study>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<Study> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            // Now anyone should be able to ready a study.  Not sure this is right,
            // but seems to expensive to check through the chain...
            Study study = studyService.read(datastore, studyKey);
            response = new Response<Study>(study);
//            Collection<Study> studies = studyService.readStudiesForAccount(datastore, userInfo.account);
//            response = new Response<Study>("You are not authorized to get this study.");
//            for(Study study: studies) {
//                if (study.key.equals(studyKey)) {
//                    response = new Response<Study>(study);
//                }
//            }
        }
        return response;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Study> add(@Context HttpServletRequest request, Study study) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Study>  response;
        Response<Object> checkResponse = securityService.checkForAddStudy(userInfo);

        if (checkResponse != null) {
            response = new Response<Study> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            study.ownerOrganizationKey = userInfo.account.adminOrganizationKey;
            String validationError = studyService.addStudy(datastore, study, userInfo.account.key);
            if (validationError != null) {
                response = new Response<Study>(null, validationError);
            } else {
                response = new Response<Study>(study);
            }
        }
        return response;
    }

    @POST
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Study> update(@Context HttpServletRequest request, @PathParam("key") String studyKey, Study study) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Study>  response;
        Response<Object> checkResponse = securityService.checkForAddStudy(userInfo);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else if (!studyKey.equals(study.key)) {
            response = new Response("You are not authorized to edit this study.");
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            String validationError = studyService.save(datastore, study, userInfo.account);
            if (validationError != null) {
                response = new Response(validationError);
            } else {
                response = new Response(study);
            }
        }
        return response;
    }
}
