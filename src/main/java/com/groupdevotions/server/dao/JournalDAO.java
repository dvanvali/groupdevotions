package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Journal;

import java.util.Date;
import java.util.List;

public class JournalDAO extends DAO<Journal> {
	@Inject
	public JournalDAO() {
		super(Journal.class);
	}
	
	public Journal readForDate(final ObjectDatastore datastore, String accountKey, Date forDay) {
		List<Journal> results = datastore.find().type(Journal.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.addFilter("forDay", FilterOperator.EQUAL, forDay)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		if (results.size() > 1) {
			logger.warning("JournalDAO.readForDate returned too many entities for accountKey=" + accountKey + " forDay=" + forDay);
		}
		return results.isEmpty() ? null : results.get(0); 
	}

	public List<Journal> readForDates(final ObjectDatastore datastore, String accountKey, Date startDay, Date endDay) {
		List<Journal> results = datastore.find().type(Journal.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.addFilter("forDay", FilterOperator.GREATER_THAN_OR_EQUAL, startDay)
				.addFilter("forDay", FilterOperator.LESS_THAN_OR_EQUAL, endDay)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);
		
		return results; 
	}

	public List<Journal> readFirst(final ObjectDatastore datastore, String accountKey, Date olderThan) {
		List<Journal> results = datastore.find().type(Journal.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.addFilter("forDay", FilterOperator.LESS_THAN, olderThan)
				.addSort("forDay", Query.SortDirection.DESCENDING)
				.fetchMaximum(7)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, results);

		return results;
	}
}
