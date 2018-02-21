package com.groupdevotions.shared.model;

import java.io.Serializable;

public class UserInfo implements Serializable {
	private static final long serialVersionUID = -2434838003667521704L;
	// These first fields are controlled by login
	public boolean isSignedIn;
	public String googleSignInUrl;
	public String googleSignOutUrl;
	public int lessonIndexRelativeToToday = 0;
	// The current study is used for saving answers
	public String currentStudyLessonKey;
	public Account account;
	public GroupMember groupMember;

	public UserInfo() {
	}

	/*
	public boolean authorizedToEditGroup(String key) {
		if (currentGroupMember != null && currentGroupMember.groupAdmin && currentGroupMember.groupKey.equals(key)) {
			return true;
		}
		Group group = findGroupToEdit(key);
		return group != null;
	}

	public Group findGroupToEdit(String key) {
		if (key != null) {
			for(Group group : groupsCanEdit) {
				if (key.equals(group.key)) {
					return group;
				}
			}
		}
		return null;
	}
	
	public boolean authorizedToCreateGroup(String key) {
		Study study = findStudyForGroupCreate(key);
		return study != null;
	}

	public Study findStudyForGroupCreate(String key) {
		if (key != null) {
			for(Study study : studysWithInvitationsToCreateGroups) {
				if (key.equals(study.key)) {
					return study;
				}
			}
		}
		return null;
	}
	*/
}
