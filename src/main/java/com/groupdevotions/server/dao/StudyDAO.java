package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.SectionType;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.StudyContributor;
import com.groupdevotions.shared.model.StudyType;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class StudyDAO extends DAO<Study> {
	@Inject
	public StudyDAO() {
		super(Study.class);
	}

	// todo remove when all types are populated (data conversion)
	public Study read(final ObjectDatastore datastore, Key key) {
		Study study = super.read(datastore, key);
		if (study != null && study.studyType == null) {
			if (study.studyByDate) {
				study.studyType = StudyType.DAILY;
			} else {
				study.studyType = StudyType.SERIES;
			}
		}
		return study;
	}
	
	public List<Study> read(final ObjectDatastore datastore, List<StudyContributor> studyContributors) {
		List<Study> studys = new ArrayList<Study>();
		
		for(StudyContributor studyContributor : studyContributors) {
			Study study = read(datastore, studyContributor.studyKey);
			if (study != null) {
				studys.add(study);
			} else {
				studyContributors.remove(studyContributor);
				logger.warning("Removing study contributor for accountKey: " + studyContributor.accountKey + " because unable to find study " + studyContributor.studyKey);
				return read(datastore, studyContributors);
			}
		}
		
		return studys;
	}
	
    public List<Study> readPublicDailyStudies(final ObjectDatastore datastore) {
		List<Study> results = datastore.find().type(Study.class)
				.addFilter("publicStudy", FilterOperator.EQUAL, true)
				.addFilter("studyType", FilterOperator.IN, Lists.newArrayList(StudyType.DAILY, StudyType.RSS, StudyType.BIBLE))
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		Collections.sort(results, new Comparator<Study>() {
			@Override
			public int compare(Study arg0, Study arg1) {
				return Collator.getInstance().compare(arg0.title, arg1.title);
			}
		});
		
		return results;
	}

	public List<Study> readForOrganization(final ObjectDatastore datastore, final String organizationKey) {
		List<Study> results = datastore.find().type(Study.class)
				.addFilter("ownerOrganizationKey", FilterOperator.EQUAL, organizationKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		Collections.sort(results, new Comparator<Study>() {
			@Override
			public int compare(Study arg0, Study arg1) {
				return Collator.getInstance().compare(arg0.title, arg1.title);
			}
		});

		return results;
	}

	public List<Study> readAll(final ObjectDatastore datastore) {
		List<Study> results = datastore.find().type(Study.class)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		Collections.sort(results, new Comparator<Study>() {
			@Override
			public int compare(Study arg0, Study arg1) {
				return Collator.getInstance().compare(arg0.title, arg1.title);
			}
		});

		return results;
	}
}
