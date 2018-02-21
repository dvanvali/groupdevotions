package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.dao.AccountDAO;
import com.groupdevotions.server.dao.ConfigDAO;
import com.groupdevotions.server.dao.GroupMemberDAO;
import com.groupdevotions.server.service.AccountService;
import com.groupdevotions.server.service.ConfigService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.server.service.TestDataService;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("config")
public class ConfigResource {
    protected static final Logger logger = Logger
            .getLogger(ConfigResource.class.getName());
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final ConfigDAO configDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final AccountDAO accountDAO;
    private final ConfigService configService;
    private final SecurityService securityService;
    private final AccountService accountService;
    private final TestDataService testDataService;

    @Inject
    public ConfigResource(AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, ConfigDAO configDAO, GroupMemberDAO groupMemberDAO, AccountDAO accountDAO, ConfigService configService, SecurityService securityService, AccountService accountService, TestDataService testDataService) {
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.configDAO = configDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.accountDAO = accountDAO;
        this.configService = configService;
        this.securityService = securityService;
        this.accountService = accountService;
        this.testDataService = testDataService;
    }

    @GET
    @Path("/terms")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<String> logout(@Context HttpServletRequest request) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse == null || Response.LocationType.terms.equals(checkResponse.location)) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            return new Response(configDAO.readInstance(datastore).terms, (String) null);
        } else {
            return new Response<String>(checkResponse);
        }
    }

    @POST
    @Path("/terms")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Object> terms(@Context HttpServletRequest request) throws IOException {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object> response;
        response = securityService.check(userInfo);
        if (response == null || Response.LocationType.terms.equals(response.location)) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            configService.agreeToTermsOfService(datastore, userInfo.account);
            request.getSession().setAttribute("userInfo", userInfo);
            response = new Response(true, null, null, null, accountService.determineLoginSuccessRedirectLocation(userInfo.account));
        }
        return response;
    }

    static class TestDataCommand {
        public String url;
    }

    @POST
    @Path("/resetTestData")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<String> resetTestData(TestDataCommand command) throws IOException {
        if (configService.getApplicationConfig().development) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
            if (command.url.contains("sleep")) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    // Ignore
                }
                return new Response(true, "Done");
            } else if (command.url.contains("invitelink")) {
                Collection<GroupMember> groupMembers = groupMemberDAO.readByEmail(datastore, "invite@gmail.com");
                if (groupMembers.isEmpty()) {
                    return new Response("Unable to find invite@gmail.com");
                }
                return new Response(true, Iterables.get(groupMembers, 0).inviteUrl());
            } else if (command.url.contains("adminAccountLink")) {
                String email = command.url.substring(command.url.indexOf("&email=")+7);
                Account account = accountDAO.readByEmail(datastore, email);
                if (account == null) {
                    return new Response("Unable to find " + email);
                }
                return new Response(true, "#admin&resetToken=" + account.resetToken);
            } else if (command.url.contains("emailconfirmlink")) {
                Account account = accountDAO.readByEmail(datastore, "newaccount@gmail.com");
                if (account == null) {
                    return new Response("Unable to find newaccount@gmail.com");
                }
                return new Response(true, account.newAccountUrl());
            } else {
                if (configDAO.readInstance(datastore).development) {
                    testDataService.resetTestData();
                    return new Response(true, "Test data has been reset.");
                } else {
                    return new Response(false, "Reset not allowed since this is not a development environment.");
                }
            }
        }
        return new Response("Not development");
    }
}
