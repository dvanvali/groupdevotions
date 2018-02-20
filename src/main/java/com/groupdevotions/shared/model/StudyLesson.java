package com.groupdevotions.shared.model;

import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.code.twig.annotation.Embedded;
import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class StudyLesson implements Serializable, KeyMirror  {
	private static final long serialVersionUID = -8327955913962634431L;
	public static final int ACCOUNTABILITY_LESSON = -1;
	@Store(false) public String key;
	@Index public String studyKey;
    public String title;
    @Index public Integer month;
    @Index public Integer day;
    //public Date forDate;
    @Embedded public List<StudySection> studySections = new ArrayList<StudySection>();
	@Store(false) public String devotionPageTagLine;
	@Store(false) public String copyright;
	@Store(false) public String author;
	@Store(false) public String navigationCaption;
	@Store(false) public List<StudyLessonInfo> navigationStudyLessonInfos;
	@Store(false) public Boolean accountabilityLesson = false;
	// These next two are used for maintaining lessons.  Do not use for any other purpose.
	@Store(false) public Integer studyInfoIndex;
	@Store(false) public Study study;
	@Store(false) public StudyType studyType;
	@Store(false) public Integer bibleReadingIndex;
	@Store(false) public boolean bibleReadingComplete;
	@Store(false) public String bibleReadingVersion;
	@Store(false) public boolean dailyReadingStartsEachMonth;

	public void updateMaintainFields(StudyLesson updatedLesson) {
		this.title = updatedLesson.title;
		this.month = updatedLesson.month;
		this.day = updatedLesson.day;
		this.accountabilityLesson = updatedLesson.accountabilityLesson;
		studySections = updatedLesson.studySections;
	}

    public void setKey(String key) {
    	this.key = key;
    }

	@Override
	public String getKey() {
		return key;
	}

	public List<EnhancedString> getEnhancedStringAnswerList() {
		return getEnhancedStringAnswerList(studySections);
	}

	private List<EnhancedString> getEnhancedStringAnswerList(Collection<StudySection> sections) {
		List<EnhancedString> enhancedStringAnswers = Lists.newArrayList();
		if (sections != null) {
			for (StudySection studySection : sections) {
				if (studySection.type.isPrivateQuestion()) {
					enhancedStringAnswers.add(new EnhancedString(studySection.answer));
				}
			}
		}
		return enhancedStringAnswers;
	}

	public void populateStudyLessonAnswers(StudyLesson lessonFromUi) {
		int i = 0;
		for (StudySection studySection : studySections) {
			if (studySection.type.isPrivateQuestion()) {
				studySection.answer = lessonFromUi.studySections.get(i).answer;
			}
			i++;
		}
	}

	public void populateStudyLessonAnswers(GroupMemberLessonAnswer groupMemberLessonAnswer) {
		int index = 0;
		for (StudySection studySection : studySections) {
			if (studySection.isAccountabilityQuestion()) {
				if (groupMemberLessonAnswer.answers.size() > index) {
					studySection.answer = groupMemberLessonAnswer.answers.get(index).value;
					index++;
				}
			}
		}
	}
}
