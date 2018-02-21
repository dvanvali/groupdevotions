package com.groupdevotions.server.logic;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.StudyLesson;


public class BaseStudyLogicImpl implements StudyLogic {
	protected StudyLessonDAO studyLessonDAO;
	protected Study study;
	
	public BaseStudyLogicImpl(StudyLessonDAO studyLessonDAO, Study study) {
		this.studyLessonDAO = studyLessonDAO;
		this.study = study;
	}
	
	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex) {
		return null;
	}
	
	public StudyLesson readIndex(ObjectDatastore datastore, GroupMember groupMember, int index) {
		StudyLesson studyLesson = readRelativeLesson(datastore, 0, index);
		populateLessonDescription(studyLesson, index, -1);
		return studyLesson;
	}

	protected StudyLesson readRelativeLesson(ObjectDatastore datastore, int todaysIndex, int relativeIndex) {
		if (todaysIndex == -1) {
			return null;
		}

		int lessonIndex = todaysIndex;
		
		// move according to relativeIndex wrapping as appropriate
		lessonIndex = lessonIndex + relativeIndex;
		if (lessonIndex >= study.studyLessonInfos.size()) {
			lessonIndex -= study.studyLessonInfos.size();
		}
		if (lessonIndex < 0) {
			lessonIndex += study.studyLessonInfos.size();
		}

		// return the lesson
		if (lessonIndex < 0 || lessonIndex >= study.studyLessonInfos.size()) {
			return null; 
		}
		
		StudyLesson studyLesson = studyLessonDAO.read(datastore, study.studyLessonInfos.get(lessonIndex).studyLessonKey);
		populateLessonDescription(studyLesson, lessonIndex, relativeIndex);
		studyLesson.author = study.author;
		studyLesson.copyright = study.copyright;
		
		return studyLesson;
	}
	
	protected void populateLessonDescription(StudyLesson lesson, int lessonIndex, int relativeIndex) {
		if (relativeIndex == 0) {
			lesson.devotionPageTagLine = "Today's Lesson";
		} else {
			lesson.devotionPageTagLine = "Lesson " + String.valueOf(lessonIndex+1);
		}
	}
	
	public boolean allowFuture(GroupMember groupMember) {
		return false;
	}

	public void initializeGroupMember(GroupMember groupMember) {

	}
}
