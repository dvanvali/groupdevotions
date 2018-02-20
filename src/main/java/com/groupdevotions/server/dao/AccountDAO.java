package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Account;

import java.util.Collection;
import java.util.List;

public class AccountDAO extends DAO<Account> {
	@Inject
	public AccountDAO() {
		super(Account.class);
	}
	
	public Account readByUserId(final ObjectDatastore datastore, String userId, String email) {
		List<Account> accounts = datastore.find().type(Account.class)
				.addFilter("userId", FilterOperator.EQUAL, userId)
				.addFilter("email", FilterOperator.EQUAL, email.toLowerCase().trim())
				.returnAll()
				.now();
		if (0 == accounts.size()) {
			return null;
		}

		if (1 < accounts.size()) {
			logger.severe("There is more than one entity for Account with id="
					+ userId + " email " + email);
		}
		executeReadEntityCallbacks(datastore, accounts.get(0));
		return accounts.get(0);
	}

	public Account readByUserId(final ObjectDatastore datastore, String userId) {
		List<Account> accounts = datastore.find().type(Account.class)
				.addFilter("userId", FilterOperator.EQUAL, userId)
				.returnAll()
				.now();
		if (0 == accounts.size()) {
			return null;
		}

		if (1 < accounts.size()) {
			logger.severe("There is more than one entity for Account with id="
					+ userId);
		}
		// I believe a query could get stale objects, but getting by key gets the up to date entity
		return read(datastore, getKey(datastore, accounts.get(0)));
	}

	public Account readByEmail(final ObjectDatastore datastore, String email) {
		List<Account> accounts = datastore.find().type(Account.class)
				.addFilter("email", FilterOperator.EQUAL, email.toLowerCase().trim())
				.returnAll()
				.now();
		if (0 == accounts.size()) {
			return null;
		}

		if (1 < accounts.size()) {
			logger.severe("There is more than one entity for Account with email " + email);
		}
		return read(datastore, getKey(datastore, accounts.get(0)));
	}

	public Collection<Account> readByOrganizationId(final ObjectDatastore datastore, String organizationId) {
		List<Account> accounts = datastore.find().type(Account.class)
				.addFilter("adminOrganizationKey", FilterOperator.EQUAL, organizationId)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, accounts);
		return accounts;
	}
}
