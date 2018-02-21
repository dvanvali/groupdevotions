package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.dao.GroupDAO;
import com.groupdevotions.server.dao.GroupMemberDAO;
import com.groupdevotions.server.service.GroupMemberService;
import com.groupdevotions.server.service.GroupService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.shared.model.Group;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Collection;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("groupMember")
public class GroupMemberResource {
    private final GroupMemberService groupMemberService;
    private final GroupService groupService;
    private final SecurityService securityService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final GroupMemberDAO groupMemberDAO;
    private final GroupDAO groupDAO;

    @Inject
    public GroupMemberResource(GroupMemberService groupMemberService, GroupService groupService, SecurityService securityService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, GroupMemberDAO groupMemberDAO, GroupDAO groupDAO) {
        this.groupMemberService = groupMemberService;
        this.groupService = groupService;
        this.securityService = securityService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.groupMemberDAO = groupMemberDAO;
        this.groupDAO = groupDAO;
    }

    @GET
    @Produces(APPLICATION_JSON)
    public Response<Collection<GroupMember>> loadGroupMembers(@Context HttpServletRequest request, @QueryParam("groupKey") String groupKey) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<GroupMember>>  response;
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Response<Object> checkResponse;
        Group group = groupService.read(datastore, groupKey != null ? groupKey : userInfo.groupMember.groupKey);
        checkResponse = securityService.checkGroupAdmin(userInfo, group);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            Collection<GroupMember> groupMembers;
            if (groupKey == null) {
                groupMembers = groupMemberService.readAsGroupMemberAdmin(datastore, userInfo.account.groupMemberKey);
            } else {
                groupMembers = groupMemberService.readAsSiteAdmin(datastore, groupKey);
            }
            response = new Response(groupMembers);
        }
        return response;
    }

    @GET
    @Path("/{groupMemberKey}")
    @Produces(APPLICATION_JSON)
    public Response<GroupMember> get(@Context HttpServletRequest request, @PathParam("groupMemberKey") String groupMemberKey) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<GroupMember>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            GroupMember groupMember = groupMemberService.readAsGroupMember(datastore, userInfo.account.groupMemberKey, groupMemberKey);
            if (groupMember == null) {
                response = new Response<GroupMember>("You do not have access to this group member.");
            } else {
                response = new Response(groupMember);
            }
        }
        return response;
    }

    @POST
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<GroupMember> update(@Context HttpServletRequest request, @PathParam("key") String groupMemberKey, GroupMember groupMember) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        Response<Object> checkResponse;
        if (!groupMemberKey.equals(userInfo.account.groupMemberKey)) {
            checkResponse = securityService.checkGroupAdmin(userInfo, group);
            if (checkResponse != null) {
                return new Response(checkResponse);
            }
            return addOrUpdate(userInfo, groupMember);
        }

        // Updating reading or accountability as yourself.
        checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            return new Response(checkResponse);
        }
        checkResponse = securityService.check(userInfo, groupMember.key, "You are not authorized to update this group member.");
        if (checkResponse != null) {
            return new Response(checkResponse);
        }
        return groupMemberService.updateSettingsAsGroupMember(datastore, groupMember);
    }

    @DELETE
    @Path("/{key}")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Object> delete(@Context HttpServletRequest request, @PathParam("key") String groupMemberKey) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        GroupMember groupMember = groupMemberDAO.read(datastore, groupMemberKey);
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        Response<Object> checkResponse = securityService.checkGroupAdmin(userInfo, group);
        if (checkResponse != null) {
            return checkResponse;
        }
        String errorMessage = groupMemberService.delete(datastore, userInfo, groupMember);
        if (errorMessage != null) {
            return new Response<Object>(errorMessage);
        } else {
            return new Response<Object>((Object) null);
        }
    }

    @POST
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<GroupMember> add(@Context HttpServletRequest request, GroupMember groupMember) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        if (groupMember.groupKey == null) {
            // Admins for a group (not site admin and not org admin) will not set groupKey...
            groupMember.groupKey = userInfo.groupMember.groupKey;
        }
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        Response<Object> checkResponse = securityService.checkGroupAdmin(userInfo, group);
        if (checkResponse == null) {
            return addOrUpdate(userInfo, groupMember);
        } else {
            return new Response<GroupMember>(checkResponse);
        }
    }

    public Response<GroupMember> addOrUpdate(UserInfo userInfo, GroupMember groupMemberToSave) {
        ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
        Response<GroupMember> response;
        String error = groupMemberService.validateGroupMember(groupMemberToSave);
        if (error != null) {
            response = new Response(error);
        } else {
            groupMemberToSave = groupMemberService.saveOrUpdateSettingsAsAdmin(datastore, groupMemberToSave.groupKey, groupMemberToSave);
            response = new Response(groupMemberToSave);
        }
        return response;
    }
}
