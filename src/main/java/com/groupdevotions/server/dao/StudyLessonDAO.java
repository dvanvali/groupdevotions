package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.StudyLesson;

import java.util.List;

public class StudyLessonDAO extends DAO<StudyLesson> {
	@Inject
	public StudyLessonDAO() {
		super(StudyLesson.class);
	}

	public List<StudyLesson> readByStudyKey(final ObjectDatastore datastore, String studyKey) {
		List<StudyLesson> studyLessons = datastore.find().type(StudyLesson.class)
				.addFilter("studyKey", Query.FilterOperator.EQUAL, studyKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, studyLessons);
		return studyLessons;
	}
}
