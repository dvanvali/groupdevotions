package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.GroupBlog;

import java.util.Date;
import java.util.List;

public class GroupBlogDAO extends DAO<GroupBlog> {
	@Inject
	public GroupBlogDAO() {
		super(GroupBlog.class);
	}
	
	public GroupBlog readForDate(final ObjectDatastore datastore, String groupKey, Date blogDate) {
		List<GroupBlog> results = datastore.find().type(GroupBlog.class)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.addFilter("blogDate", FilterOperator.EQUAL, blogDate)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		if (results.size() > 1) {
			logger.warning("GroupBlogDAO.readForDate returned too many entities for groupKey=" + groupKey + " blogDate=" + blogDate);
		}
		return results.isEmpty() ? null : results.get(0); 
	}

	public List<GroupBlog> readForDates(final ObjectDatastore datastore, String groupKey, Date startDate, Date endDate) {
		List<GroupBlog> results = datastore.find().type(GroupBlog.class)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.addFilter("blogDate", FilterOperator.GREATER_THAN_OR_EQUAL, startDate)
				.addFilter("blogDate", FilterOperator.LESS_THAN_OR_EQUAL, endDate)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		return results; 
	}

	public List<GroupBlog> readFirst(final ObjectDatastore datastore, String groupKey, Date olderThan) {
		List<GroupBlog> results = datastore.find().type(GroupBlog.class)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.addFilter("blogDate", FilterOperator.LESS_THAN, olderThan)
				.addSort("blogDate", Query.SortDirection.DESCENDING)
				.fetchMaximum(7)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		return results;
	}
}
