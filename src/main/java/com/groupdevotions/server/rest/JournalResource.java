package com.groupdevotions.server.rest;

import com.google.appengine.repackaged.org.joda.time.DateMidnight;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.service.JournalService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.shared.model.Journal;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("journal")
public class JournalResource {
    protected static final Logger logger = Logger.getLogger(DevotionResource.class.getName());
    private final JournalService journalService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final SecurityService securityService;

    @Inject
    public JournalResource(JournalService journalService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, SecurityService securityService) {
        this.journalService = journalService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.securityService = securityService;
    }

    @GET
    @Path("/instructions")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<String> today(@Context HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<String>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<String>(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            String instructions = journalService.readInstructions(datastore, userInfo.account);
            response = new Response<String>(instructions, (String) null);
        }
        return response;
    }

    @GET
    @Path("/query")
    @Produces(APPLICATION_JSON)
//    @Consumes(APPLICATION_JSON)
    public Response<Collection<Journal>> today(@Context HttpServletRequest request, @QueryParam("since") String since) {
        Date olderThan = null;
        if (since != null) {
            olderThan = DateMidnight.parse(since).toDate();
        }
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<Journal>>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<Collection<Journal>> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            Collection<Journal> journals = journalService.read(datastore, userInfo.account, olderThan);
            response = new Response<Collection<Journal>>(journals);
        }
        return response;
    }

    @POST
    @Path("/save")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Object> save(@Context HttpServletRequest request, Journal journal) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object>  response = securityService.check(userInfo);
        if (response == null) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            journalService.save(datastore, userInfo.account, journal);
            response = new Response<Object>(Boolean.TRUE);
        }
        return response;
    }
}