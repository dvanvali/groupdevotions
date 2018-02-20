package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.groupdevotions.server.ServerUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GroupMember implements Serializable, KeyMirror, PostLoad, PreSave {
	private static final long serialVersionUID = -4335323849715772998L;
	@Store(false) public String key;
	@Index public String accountKey;
	@Index public String groupKey;
	@Index public String email;
	// Joined list of accountabilityEmails for storage/compatibility
	private String accountabilityEmail;
	@Store(false) public Collection<String> accountabilityEmails = Lists.newArrayList();
	public String name;
	public GroupMemberStatus status = GroupMemberStatus.NONE;
	public boolean sendAccountabilityEmailsToMe = false;
	// no longer in use
	// public Date firstLessonDate;
	public String lastCompletedStudyLessonKey;
	public String lastCompletedDateAsString;
	public String lastCompletedBibleReadingIndex;
	public String bibleReadingVersion = "nasb";
	public String lastAccountabilityDateAsString;
	public boolean groupAdmin = false;
	
	public GroupMember() {
	}
	
	//@Embedded 
	public List<String> lessonHistoryKeys = new ArrayList<String>();

	public String inviteUrl() {
		return 	"#groupInvite=" + key +	"&email=" + ServerUtils.urlEncode(email);
	}

	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void postLoad() {
		if (!Strings.isNullOrEmpty(accountabilityEmail)) {
			accountabilityEmails = Lists.newArrayList(accountabilityEmail.split(","));
		}
	}

	public void preSave() {
		accountabilityEmail = Joiner.on(",").skipNulls().join(accountabilityEmails);
	}


	public void updateSettingsFields(GroupMember clientGroupMember) {
		accountabilityEmails = clientGroupMember.accountabilityEmails;
		sendAccountabilityEmailsToMe = clientGroupMember.sendAccountabilityEmailsToMe;
		lastCompletedBibleReadingIndex = clientGroupMember.lastCompletedBibleReadingIndex;
		bibleReadingVersion = clientGroupMember.bibleReadingVersion;
	}

	public void updateGroupMemberMaintenanceFieldsAsAdmin(GroupMember clientGroupMember) {
		if (!status.equals(GroupMemberStatus.JOINED)) {
			email = clientGroupMember.email;
		}
		name = clientGroupMember.name;
		groupAdmin = clientGroupMember.groupAdmin;
		updateSettingsFields(clientGroupMember);
	}
}
