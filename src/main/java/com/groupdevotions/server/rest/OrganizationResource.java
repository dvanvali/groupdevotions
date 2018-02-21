package com.groupdevotions.server.rest;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.service.AccountService;
import com.groupdevotions.server.service.OrganizationService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.Organization;
import com.groupdevotions.shared.model.Organization;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

/**
 * Created by DanV on 7/19/2016.
 */
@Path("organization")
public class OrganizationResource {
    protected static final Logger logger = Logger
            .getLogger(OrganizationResource.class.getName());

    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final SecurityService securityService;
    private final OrganizationService organizationService;

    @Inject
    public OrganizationResource(AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, SecurityService securityService, OrganizationService organizationService) {
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.securityService = securityService;
        this.organizationService = organizationService;
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response<Collection<Organization>> query(@Context HttpServletRequest request) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<Organization>>  response;
        Response<Object> checkResponse = securityService.checkSiteAdmin(userInfo);
        if (checkResponse != null) {
            return new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            response = new Response(organizationService.readAll(datastore));
        }
        return response;
    }

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    public Response<Organization> get(@Context HttpServletRequest request, @PathParam("key") String organizationKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Organization>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<Organization> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            // Now anyone should be able to ready a organization.  Not sure this is right,
            // but seems to expensive to check through the chain...
            Organization organization = organizationService.read(datastore, organizationKey);
            response = new Response<Organization>(organization);
        }
        return response;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Organization> add(@Context HttpServletRequest request, Organization organization) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Organization>  response;
        Response<Object> checkResponse = securityService.checkSiteAdmin(userInfo);

        if (checkResponse != null) {
            response = new Response<Organization> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            String validationError = organizationService.add(datastore, organization);
            if (validationError != null) {
                response = new Response<Organization>(null, validationError);
            } else {
                response = new Response<Organization>(organization);
            }
        }
        return response;
    }

    @POST
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Organization> update(@Context HttpServletRequest request, @PathParam("key") String organizationKey, Organization organization) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Organization>  response;
        Response<Object> checkResponse = securityService.checkSiteAdmin(userInfo);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            String validationError = organizationService.save(datastore, organization);
            if (validationError != null) {
                response = new Response(validationError);
            } else {
                response = new Response(organization);
            }
        }
        return response;
    }


}
