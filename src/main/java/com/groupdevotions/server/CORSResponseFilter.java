package com.groupdevotions.server;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MultivaluedMap;
public class CORSResponseFilter implements ContainerResponseFilter {
    @Override
    public ContainerResponse filter(ContainerRequest containerRequest, ContainerResponse containerResponse) {
        MultivaluedMap<String, Object> headers = containerResponse.getHttpHeaders();

        String base = containerRequest.getHeaderValue("Origin");
        headers.add("Access-Control-Allow-Origin", base);
        headers.add("Access-Control-Allow-Methods", "OPTIONS, GET, POST, DELETE, PUT");
        headers.add("Access-Control-Allow-Headers", "Content-Type, Accept");
        headers.add("Access-Control-Allow-Credentials", "true");

        return containerResponse;
    }
}
