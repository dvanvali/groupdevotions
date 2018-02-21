package com.groupdevotions.server.service;

import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.dao.GroupDAO;
import com.groupdevotions.server.dao.GroupMemberDAO;
import com.groupdevotions.server.dao.StudyDAO;
import com.groupdevotions.server.rest.Response;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

public class SecurityService {
    private GroupMemberDAO groupMemberDAO;
    private StudyDAO studyDAO;
    private GroupDAO groupDAO;

    @Inject
    public SecurityService(GroupMemberDAO groupMemberDAO, StudyDAO studyDAO, GroupDAO groupDAO) {
        this.groupMemberDAO = groupMemberDAO;
        this.studyDAO = studyDAO;
        this.groupDAO = groupDAO;
    }

    public Response<Object> check(UserInfo userInfo) {
        Response<Object> response = null;
        if (userInfo == null || !userInfo.isSignedIn || userInfo.account == null) {
            response = new Response("Please login.", Response.LocationType.checkLogin);
        } else if (userInfo.account.disabled) {
            response = new Response("Your account has been disabled.  Please contact your group administrator.", Response.LocationType.home);
        } else if (userInfo.account.agreedToTermsOfUse == null) {
            response = new Response(null, Response.LocationType.terms);
        }
        return response;
    }

    public Response<Object> checkSiteAdmin(UserInfo userInfo) {
        Response<Object> response = check(userInfo);
        if (response == null && !userInfo.account.siteAdmin) {
            response = new Response("You are not authorized to access this data.");
        }
        return response;
    }

    public Response<Object> checkOrganizationAdmin(UserInfo userInfo, String organizationKey) {
        Response<Object> checkResponseSiteAdmin = checkSiteAdmin(userInfo);
        if (checkResponseSiteAdmin != null) {
            if (userInfo.account.adminOrganizationKey == null || (organizationKey != null && !SharedUtils.safeEquals(userInfo.account.adminOrganizationKey, organizationKey))) {
                return checkResponseSiteAdmin;
            }
        }
        return null;
    }

    public Response<Object> checkGroupAdmin(UserInfo userInfo, Group group) {
        Response<Object> checkResponseSiteAdmin = checkSiteAdmin(userInfo);
        // site admins have access to all groups
        if (checkResponseSiteAdmin == null) {
            return null;
        }
        // org admins can access groups for their org
        if (group.ownerOrganizationKey != null && SharedUtils.safeEquals(userInfo.account.adminOrganizationKey, group.ownerOrganizationKey)) {
            return check(userInfo);
        }
        // group members with admin can access their group
        if (userInfo.groupMember != null && userInfo.groupMember.groupKey.equals(group.key) && userInfo.groupMember.groupAdmin) {
            return check(userInfo);
        }
        if (userInfo.account.groupMemberKey == null && userInfo.account.adminOrganizationKey == null) {
            if (group != null && group.groupMemberActivities.size() == 0 && group.ownerOrganizationKey == null) {
                // special case for when a new user creates their own group when they first signup.
                return check(userInfo);
            }
            checkResponseSiteAdmin = new Response("You are not a member of a group.", Response.LocationType.configureGroup);
        }

        return checkResponseSiteAdmin;
    }

    public Response<Object> checkAndAccountKey(UserInfo userInfo, String accountKey) {
        Response<Object> response = check(userInfo);
        if (response == null && !userInfo.account.key.equals(accountKey)) {
            response = new Response("You are not authorized to access this account.");
        }
        return response;
    }

    public Response<Object> checkForAddStudy(UserInfo userInfo) {
        Response<Object> response = check(userInfo);
        if (response == null && (!userInfo.account.siteAdmin && userInfo.account.adminOrganizationKey == null)) {
            response = new Response("You are not authorized to add studies.");
        }
        return response;
    }

    public Response<Object> check(UserInfo userInfo, String groupMemberKey, String errorMessage) {
        Response<Object> response = check(userInfo);
        if (response == null && groupMemberKey != null && !SharedUtils.safeEquals(groupMemberKey, userInfo.account.groupMemberKey)) {
            response = new Response(errorMessage);
        }
        return response;
    }

    public Response<Object> checkStudyIsPublic(ObjectDatastore datastore, String studyKey) {
        Response<Object> response = null;
        Study study = studyDAO.read(datastore, studyKey);
        if (study == null || !study.publicStudy) {
            response = new Response("Study does not exist or it is not public.", Response.LocationType.home);
        }

        return response;
    }
}
