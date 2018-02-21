package com.groupdevotions.server.logic;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.StudyLesson;

public class SeriesStudyLogicImpl extends BaseStudyLogicImpl {

	public SeriesStudyLogicImpl(StudyLessonDAO studyLessonDAO, Study study) {
		super(studyLessonDAO, study);
	}

	/**
	 * Only goes forward when someone "completes" the lesson.  But allows the user to move forward more than 
	 * one day at a time once completed.  That is relativeIndex=0 is always the next uncompleted lesson
	 */
	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex) {
		int lessonIndex = -1;
		String lessonKey;
		if (!SharedUtils.isEmpty(groupMember.lastCompletedStudyLessonKey)
				&& ServerUtils.todayDateAsString().equals(
						groupMember.lastCompletedDateAsString)) {
			lessonKey = groupMember.lastCompletedStudyLessonKey;
		} else {
			lessonKey = findNextUncompletedLesson(
					study, groupMember.lastCompletedStudyLessonKey);
		}
		
		// find today's lesson
		int size = study.studyLessonInfos.size();
		for(int index = 0; index < size; index++) {
			if (study.studyLessonInfos.get(index).studyLessonKey.equals(lessonKey)) {
				lessonIndex = index;
				break;
			}
		}
		
		if (lessonIndex == -1 || lessonIndex + relativeIndex < 0) {
			return null;
		} else {
			return readRelativeLesson(datastore, lessonIndex, relativeIndex);
		}
	}

	private String findNextUncompletedLesson(Study study,
			String lastCompletedStudyLessonKey) {
		int size = study.studyLessonInfos.size();
		// if nothing ever completed, return the first lesson
		if (SharedUtils.isEmpty(lastCompletedStudyLessonKey) && size > 0) {
			return study.studyLessonInfos.get(0).studyLessonKey;
		}
		// if the last lesson was completed, return null
		if (!SharedUtils.isEmpty(lastCompletedStudyLessonKey) && size > 0 && study.studyLessonInfos.get(size-1).studyLessonKey.equals(lastCompletedStudyLessonKey)) {
			return null;
		}
		for (int index = 0; index < size - 1; index++) {
			if (study.studyLessonInfos.get(index).studyLessonKey
					.equals(lastCompletedStudyLessonKey)) {
				return study.studyLessonInfos.get(index + 1).studyLessonKey;
			}
		}
		// don't recognize the lesson!
		return null;
	}
	
	public boolean allowFuture(GroupMember groupMember) {
		// Only allow future if the member is not on the last lesson
		String lessonKey = findNextUncompletedLesson(
				study, groupMember.lastCompletedStudyLessonKey);
		if (lessonKey == null) {
			return false;
		}
		
		// allow future if there is no accountability or they sent it for today and they completed their last lesson
		if (study.accountabilityLessonKey != null) {
			boolean accountabilityForTodayAlreadySent 
			    = ServerUtils.todayDateAsString().equals(groupMember.lastAccountabilityDateAsString);
			boolean lastLessonCompleted 
				= ServerUtils.todayDateAsString().equals(groupMember.lastCompletedDateAsString);
			return accountabilityForTodayAlreadySent & lastLessonCompleted;
		} else {
			return true;
		}
	}
}
