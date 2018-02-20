package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Config;

import java.util.List;

public class ConfigDAO extends DAO<Config> {
	@Inject
	public ConfigDAO() {
		super(Config.class);
	}
	
	public Config readInstance(final ObjectDatastore datastore) {
		List<Config> configs = datastore.find().type(Config.class)
				.addFilter("id", FilterOperator.EQUAL, "config")
				.returnAll()
				.now();
		if (0 == configs.size()) {
			return null;
		}
		return configs.get(0);
	}	
}