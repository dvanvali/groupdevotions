package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GroupMemberLessonAnswer implements Serializable {
	private static final long serialVersionUID = 6851001338857864351L;

	public GroupMemberLessonAnswer() {

	}

	public GroupMemberLessonAnswer(String accountKey, String groupKey, String studyLessonKey, List<EnhancedString> answers) {
		this.accountKey = accountKey;
		this.groupKey = groupKey;
		this.studyLessonKey = studyLessonKey;
		this.answers = answers;
		this.postedOn = new Date();
	}

	@Index public String accountKey;
	@Index public String groupKey;
	@Index public String studyLessonKey;
	@Embedded public List<EnhancedString> answers = new ArrayList<EnhancedString>(); 
    public Date postedOn;
}
