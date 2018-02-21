package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Group;
import com.groupdevotions.shared.model.GroupMember;

import java.util.ArrayList;
import java.util.List;

public class GroupDAO extends DAO<Group> {
	@Inject
	public GroupDAO() {
		super(Group.class);
	}
	
	public List<Group> readByOrganizationKey(final ObjectDatastore datastore, String organizationKey) {
		List<Group> groups = datastore.find().type(Group.class)
				.addFilter("ownerOrganizationKey", FilterOperator.EQUAL, organizationKey)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, groups);
		return groups;
	}

	public List<Group> read(final ObjectDatastore datastore, List<GroupMember> groupMemberships) {
		List<Group> groups = new ArrayList<Group>();
		
		for(GroupMember groupMember : groupMemberships) {
			Group group = read(datastore, groupMember.groupKey);
			if (group != null) {
				groups.add(group);
			} else {
				groupMemberships.remove(groupMember);
				logger.warning("Removing group membership: " + groupMember.key + " because unable to find group " + groupMember.groupKey);
				return read(datastore, groupMemberships);
			}
		}

		return groups;
	}

	public List<Group> readAll(final ObjectDatastore datastore) {
		List<Group> groups = datastore.find().type(Group.class)
				.returnAll()
				.now();
		executeReadEntityCallbacksMultipleEntities(datastore, groups);
		return groups;
	}
}
