package com.groupdevotions.server.dao;

import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.GroupMember;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GroupMemberDAO extends DAO<GroupMember> {
	@Inject
	public GroupMemberDAO() {
		super(GroupMember.class);
	}
	
	public List<GroupMember> readMyGroupMembers(final ObjectDatastore datastore, String accountKey) {
		List<GroupMember> myGroupMembers = datastore.find().type(GroupMember.class)
				.addFilter("accountKey", FilterOperator.EQUAL, accountKey)
				.returnAll()
				.now();

		executeReadEntityCallbacksMultipleEntities(datastore, myGroupMembers);
		return myGroupMembers;
	}

	public List<GroupMember> readInvitations(final ObjectDatastore datastore, String email) {
		List<GroupMember> myInvites = new ArrayList<GroupMember>();
		
		List<GroupMember> groupMembershipsWithMyEmail = datastore.find().type(GroupMember.class)
				.addFilter("email", FilterOperator.EQUAL, email.toLowerCase().trim())
				.returnAll()
				.now();
		
		for(GroupMember groupMember : groupMembershipsWithMyEmail) {
			if (groupMember.accountKey == null) {
				myInvites.add(groupMember);
			}
		}

		executeReadEntityCallbacksMultipleEntities(datastore, myInvites);
		return myInvites;
	}

	public List<GroupMember> readByEmail(final ObjectDatastore datastore, String email) {
		List<GroupMember> groupMembershipsWithMyEmail = datastore.find().type(GroupMember.class)
				.addFilter("email", FilterOperator.EQUAL, email.toLowerCase().trim())
				.returnAll()
				.now();

		executeReadEntityCallbacksMultipleEntities(datastore, groupMembershipsWithMyEmail);
		return groupMembershipsWithMyEmail;
	}

	public List<GroupMember> readByGroupKey(final ObjectDatastore datastore, String groupKey) {
		List<GroupMember> groupMembers = datastore.find().type(GroupMember.class)
				.addFilter("groupKey", FilterOperator.EQUAL, groupKey)
				.returnAll()
				.now();

		executeReadEntityCallbacksMultipleEntities(datastore, groupMembers);
		
		Collections.sort(groupMembers, new Comparator<GroupMember>() {
			@Override
			public int compare(GroupMember arg0, GroupMember arg1) {
				return Collator.getInstance().compare(arg0.name, arg1.name);
			}
		});

		return groupMembers;
	}
}
