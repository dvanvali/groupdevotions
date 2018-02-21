package com.groupdevotions.server.logic;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.StudyLesson;

public interface StudyLogic {
	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex);
	public StudyLesson readIndex(ObjectDatastore datastore, GroupMember groupMember, int index);
	public boolean allowFuture(GroupMember groupMember);
	public void initializeGroupMember(GroupMember groupMember);
}
