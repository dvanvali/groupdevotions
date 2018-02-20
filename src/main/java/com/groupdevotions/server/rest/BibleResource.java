package com.groupdevotions.server.rest;

import com.google.inject.Inject;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import org.aopalliance.intercept.Invocation;


import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import javax.ws.rs.QueryParam;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("bible")
public class BibleResource {
    protected static final Logger logger = Logger
            .getLogger(BibleResource.class.getName());

    @Inject
    public BibleResource() {

    }

    static private class bible {
        String reference;
        String version;
    }

    @GET
    @Path("/")
    @Produces(APPLICATION_JSON)
    public Response<String> get( @QueryParam("reference") String reference,  @QueryParam("version") String version) {
        // Add authentication
        Client c = Client.create();
        c.setFollowRedirects(true);
        final HTTPBasicAuthFilter authFilter = new HTTPBasicAuthFilter("key from bibls org", "");
        c.addFilter(authFilter);

        WebResource r = c.resource("https://bibles.org/v2/passages.js?q[]=" + reference.replace(" ", "+") + "&version=eng-" + version.toUpperCase());
        String response = r.accept(
                MediaType.APPLICATION_JSON_TYPE)
                .get(String.class);

        // todo handle error
        String json = response.toString();
        return new Response(json, (String) null);
    }
}
