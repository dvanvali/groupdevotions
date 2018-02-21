package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.GroupMemberBlog;

import java.util.List;

public class GroupMemberBlogDAO extends DAO<GroupMemberBlog> {
	@Inject
	public GroupMemberBlogDAO() {
		super(GroupMemberBlog.class);
	}
	
	public GroupMemberBlog readForGroupMember(final ObjectDatastore datastore, String groupMemberKey, String lessonKey) {
		List<GroupMemberBlog> results = datastore.find().type(GroupMemberBlog.class)
				.addFilter("lessonKey", FilterOperator.EQUAL, lessonKey)
				.addFilter("groupMemberKey", FilterOperator.EQUAL, groupMemberKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		if (results.size() > 1) {
			logger.warning("GroupMemberBlogDAO.readByKeys returned too many entities for groupMemberKey=" + groupMemberKey + " lessonKey=" + lessonKey);
		}
		return results.isEmpty() ? null : results.get(0); 
	}

	public List<GroupMemberBlog> readForLesson(final ObjectDatastore datastore, String lessonKey) {
		List<GroupMemberBlog> results = datastore.find().type(GroupMemberBlog.class)
				.addFilter("lessonKey", FilterOperator.EQUAL, lessonKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		return results; 
	}

	public List<GroupMemberBlog> readForGroupMember(final ObjectDatastore datastore, String groupMemberKey) {
		List<GroupMemberBlog> results = datastore.find().type(GroupMemberBlog.class)
				.addFilter("groupMemberKey", FilterOperator.EQUAL, groupMemberKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		return results;
	}
}
