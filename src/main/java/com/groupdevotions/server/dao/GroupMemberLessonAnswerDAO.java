package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.GroupMemberLessonAnswer;

import java.util.List;

public class GroupMemberLessonAnswerDAO extends DAO<GroupMemberLessonAnswer> {
	@Inject
	public GroupMemberLessonAnswerDAO() {
		super(GroupMemberLessonAnswer.class);
	}
	
	public GroupMemberLessonAnswer readByAllKeys(final ObjectDatastore datastore, String accountKey, String groupKey, String studyLessonKey) {
		List<GroupMemberLessonAnswer> groupMemberLessonAnswers = datastore.find().type(GroupMemberLessonAnswer.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.addFilter("studyLessonKey", FilterOperator.EQUAL, studyLessonKey)
				.returnAll()
				.now();

		if (0 == groupMemberLessonAnswers.size()) {
			return null;
		}

		if (groupMemberLessonAnswers.size() > 1) {
			logger.severe("There is more than one entity for GroupMemberLessonAnswer with accountKey="
					+ accountKey + " groupKey=" + groupKey + " studyLessonKey=" + studyLessonKey);
		}
		
		executeReadEntityCallbacks(datastore, groupMemberLessonAnswers.get(0));
		return groupMemberLessonAnswers.get(0);
	}	

	public void deleteAllForGroupKey(final ObjectDatastore datastore, String groupKey, String accountKey) {
		List<GroupMemberLessonAnswer> groupMemberLessonAnswers = datastore.find().type(GroupMemberLessonAnswer.class)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.returnAll()
				.now();

		for(GroupMemberLessonAnswer answer : groupMemberLessonAnswers) {
			delete(datastore, getKey(datastore, answer));
		}
	}	
}
