package com.groupdevotions.server.rest;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.service.BlogData;
import com.groupdevotions.server.service.BlogService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.BlogEntry;
import com.groupdevotions.shared.model.GroupBlog;
import com.groupdevotions.shared.model.UserInfo;
import org.joda.time.DateMidnight;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("blog")
public class BlogResource {
    protected static final Logger logger = Logger.getLogger(DevotionResource.class.getName());
    private final BlogService blogService;
    private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
    private final SecurityService securityService;

    @Inject
    public BlogResource(BlogService blogService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, SecurityService securityService) {
        this.blogService = blogService;
        this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
        this.securityService = securityService;
    }

    @GET
    @Path("/query")
    @Produces(APPLICATION_JSON)
    public Response<Collection<GroupBlog>> today(@Context HttpServletRequest request, @QueryParam("since") String since) {
        Date olderThan = null;
        if (since != null) {
            olderThan = DateMidnight.parse(since).toDate();
        }
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Collection<GroupBlog>>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response<Collection<GroupBlog>> (checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            Collection<GroupBlog> groupBlogs = blogService.read(datastore, userInfo.account.groupMemberKey, olderThan);
            response = new Response<Collection<GroupBlog>>(groupBlogs);
        }
        return response;
    }

    @POST
    @Path("/save")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<BlogEntry> save(@Context HttpServletRequest request, BlogEntry blogEntry) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object>  checkResponse = securityService.check(userInfo, blogEntry.groupMemberKey, "The entry you are editing does not belong to you.");
        Response<BlogEntry> response;
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            BlogEntry blobEntry = blogService.save(datastore, userInfo.account, blogEntry);
            response = new Response(blobEntry);
        }
        return response;
    }

    @POST
    @Path("/delete")
    @Produces(APPLICATION_JSON)
    @Consumes(APPLICATION_JSON)
    public Response<Object> delete(@Context HttpServletRequest request, BlogEntry blogEntry) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Object>  response = securityService.check(userInfo, blogEntry.groupMemberKey, "The entry you are deleting does not belong to you.");
        if (response == null) {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            GroupBlog updatedGroupBlog = blogService.delete(datastore, userInfo.account, blogEntry);
            if (updatedGroupBlog != null) {
                response = new Response(updatedGroupBlog);
            } else {
                response = new Response("Unable to delete this posting.");
            }
        }
        return response;
    }

    @GET
    @Path("/activities")
    @Produces(APPLICATION_JSON)
    public Response<BlogData> today(@Context HttpServletRequest request) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<BlogData> response;
        if (userInfo == null || userInfo.account == null || userInfo.account.groupMemberKey == null) {
            response = new Response<BlogData>(new BlogData());
            return response;
        }
        Response<Object> checkResponse = securityService.check(userInfo, userInfo.account.groupMemberKey, "You do not have access to this group.");
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            BlogData blogData = blogService.readBlogData(datastore, userInfo.account);
            response = new Response(blogData);
        }
        return response;
    }

    @GET
    @Path("/groupMember")
    @Produces(APPLICATION_JSON)
    public Response<Account> readGroupMember(@Context HttpServletRequest request, @QueryParam("groupMemberKey") String groupMemberKey) {
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
        Response<Account>  response;
        Response<Object> checkResponse = securityService.check(userInfo);
        if (checkResponse != null) {
            response = new Response(checkResponse);
        } else {
            ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

            Account groupMemberAccount = blogService.readAccountForAnotherGroupMember(datastore, userInfo.account.groupMemberKey, groupMemberKey);
            response = new Response(groupMemberAccount);
        }
        return response;
    }
}
