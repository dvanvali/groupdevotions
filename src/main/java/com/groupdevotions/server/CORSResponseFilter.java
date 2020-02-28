package com.groupdevotions.server;

import com.groupdevotions.server.rest.AccountResource;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MultivaluedMap;
import java.util.logging.Logger;

public class CORSResponseFilter implements ContainerResponseFilter {
    protected static final Logger logger = Logger
            .getLogger(CORSResponseFilter.class.getName());
    @Override
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {

        MultivaluedMap<String, Object> headers = containerResponse.getHttpHeaders();

        String origin = containerRequest.getHeaderValue("Origin");
//        logger.info("Origin is " + origin);

        headers.add("Access-Control-Allow-Origin", origin);
        headers.add("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Accept");
        headers.add("Access-Control-Allow-Credentials", "true");

        if (containerRequest.getPath().contains("sw.js")) {
            headers.add("Service-Worker-Allowed", "/");
        }
        return containerResponse;
    }
}
