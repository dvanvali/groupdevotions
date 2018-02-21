package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.StudyContributor;

import java.util.List;

public class StudyContributorDAO extends DAO<StudyContributor> {
	@Inject
	public StudyContributorDAO() {
		super(StudyContributor.class);
	}
	
	public List<StudyContributor> readByAccountKey(final ObjectDatastore datastore, String accountKey) {
		List<StudyContributor> contributors = datastore.find().type(StudyContributor.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, contributors);
		return contributors;
	}
}
