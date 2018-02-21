package com.groupdevotions.shared.model;

import com.google.appengine.api.datastore.Text;
import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.code.twig.annotation.Type;
import com.groupdevotions.server.util.SharedUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Group implements Serializable, KeyMirror {
	private static final long serialVersionUID = -817000755969400905L;
	@Store(false) public String key;
	@Index public String studyKey;
	// Don't support backup studies anymore, should just stay on previous lesson
	@Deprecated
	@Index public String backupDailyStudyKey;
	@Type(Text.class) public String description;
	@Type(Text.class) public String blogInstructions;
	public String inviteEmailSubject;
	@Type(Text.class) public String inviteEmailBody;
	@Embedded public List<GroupMemberActivity> groupMemberActivities = new ArrayList<GroupMemberActivity>();
	@Index
	public String ownerOrganizationKey;
	public boolean memberPrivacyForOrganization = true;
	public String defaultOrgAccountabilityEmail;
	
	public Group() {
	}

	@Override
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public String getKey() {
		return key;
	}

	public void updateMaintainFields(Group group) {
		description = group.description;
		studyKey = SharedUtils.valueOrNullForEmpty(group.studyKey);
		backupDailyStudyKey = SharedUtils.valueOrNullForEmpty(group.backupDailyStudyKey);
		inviteEmailSubject = group.inviteEmailSubject;
		inviteEmailBody = group.inviteEmailBody;
		blogInstructions = group.blogInstructions;
		if (ownerOrganizationKey != null) {
			defaultOrgAccountabilityEmail = group.defaultOrgAccountabilityEmail;
		}
	}
	
	public void updateGroupMemberActivities(String groupMemberKey, String name) {
		GroupMemberActivity groupMemberActivity = new GroupMemberActivity();
		groupMemberActivity.groupMemberKey = groupMemberKey;
		groupMemberActivity.lastDevotionDate = new Date();
		groupMemberActivity.name = name;
		
		for(GroupMemberActivity activity : groupMemberActivities) {
			if (SharedUtils.safeEquals(groupMemberKey, activity.groupMemberKey)) {
				groupMemberActivities.remove(activity);
				break;
			}
		}
		groupMemberActivities.add(groupMemberActivity);
	}

	public boolean isPrivacyAvailable() {
		return ownerOrganizationKey != null && memberPrivacyForOrganization;
	}
}
