package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.service.GroupService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.shared.model.Group;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("group")
public class GroupResource {
    private final GroupService groupService;
    private final SecurityService securityService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;

    @Inject
    public GroupResource(GroupService groupService, SecurityService securityService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider) {
        this.groupService = groupService;
        this.securityService = securityService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response<Collection<Group>> query(@Context HttpServletRequest request, @QueryParam("accountKey") String accountKey) {
        // todo read accounts for a user or maybe a organization...
        // For now only siteAdmin reads, and they read all
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<Group>> response;
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Response<Object> checkResponse = securityService.checkOrganizationAdmin(userInfo, userInfo.account.adminOrganizationKey);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            Collection<Group> groups;
            if (userInfo.account.siteAdmin) {
                groups = groupService.readAll(datastore);
            } else {
                groups = groupService.readForOrganization(datastore, userInfo.account.adminOrganizationKey);
            }
            response = new Response(groups);
        }
        return response;
    }

    @GET
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    public Response<Group> get(@Context HttpServletRequest request, @PathParam("key") String groupKey) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Group>  response;
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Group group = groupService.read(datastore, groupKey);
        Response<Object> checkResponse = securityService.checkGroupAdmin(userInfo, group);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            response = new Response(group);
        }
        return response;
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Group> add(@Context HttpServletRequest request, Group group) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Group>  response;
        Response<Object> checkResponse = securityService.checkOrganizationAdmin(userInfo, userInfo.account.adminOrganizationKey);
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        if (userInfo.account.adminOrganizationKey == null && Strings.isNullOrEmpty(group.studyKey)) {
            // Orgs can have groups without a study - it will prompt the user.
            return new Response("Please select a study for your group.");
        }
        if (group.studyKey != null && checkResponse != null) {
            Response<Object> checkResponseStudyPublic = securityService.checkStudyIsPublic(datastore, group.studyKey);
            // Give the first check response if public study fails
            if (checkResponseStudyPublic == null) {
                checkResponse = null;
            }
        }

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            String validationError = groupService.addGroup(datastore, group, userInfo.account);
            if (validationError != null) {
                response = new Response(validationError);
            } else {
                response = new Response(group);
            }
        }
        return response;
    }

    @POST
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Group> update(@Context HttpServletRequest request, @PathParam("key") String groupKey, Group group) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Group>  response;
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Group existingGroup = groupService.read(datastore, groupKey);
        Response<Object> checkResponse = securityService.checkGroupAdmin(userInfo, existingGroup);

        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else if (!groupKey.equals(group.key)) {
            response = new Response("You are not authorized to edit this group.");
        } else {
            String validationError = groupService.save(datastore, group, existingGroup, userInfo.account);
            if (validationError != null) {
                response = new Response(validationError);
            } else {
                response = new Response(group);
            }
        }
        return response;
    }

}
