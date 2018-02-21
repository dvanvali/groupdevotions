package com.groupdevotions.server;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.servlet.GuiceServletContextListener;
import com.google.inject.servlet.ServletModule;
import com.sun.jersey.guice.spi.container.servlet.GuiceContainer;

import java.util.HashMap;
import java.util.Map;

public class MainGuiceServletContextListener extends
		GuiceServletContextListener {

	@Override
	protected Injector getInjector() {
		return Guice.createInjector(
				new GuiceModule(),
				new ServletModule() {
			@Override
			protected void configureServlets() {
				final Map<String, String> params = new HashMap<String, String>();
				params.put("javax.ws.rs.Application", "com.groupdevotions.server.MainJerseyApplication");
				params.put("com.sun.jersey.spi.container.ContainerResponseFilters", "com.groupdevotions.server.CORSResponseFilter");
				serve("/rest/*").with(GuiceContainer.class, params);
			}
		});
	}
}
