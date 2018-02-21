package com.groupdevotions.server.logic;

import java.util.Calendar;

import com.google.code.twig.ObjectDatastore;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.GroupMember;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.StudyLesson;

public class DailyStudyLogicImpl extends BaseStudyLogicImpl {
	public DailyStudyLogicImpl(StudyLessonDAO studyLessonDAO, Study study) {
		super(studyLessonDAO, study);
	}

	/**
	 * Allows the user to see anything on or before today.  Can't move past "today"
	 */
	public StudyLesson readLesson(ObjectDatastore datastore, GroupMember groupMember, int relativeIndex) {
		Calendar today = ServerUtils.todayForGroup();
		int todaysMonth = today.get(Calendar.MONTH)+1;
		int todaysDay = today.get(Calendar.DAY_OF_MONTH);
		int closestIndex = -1;
		int closestDay = -1;
		int closestMonth = -1;
		int size = study.studyLessonInfos.size();
		// find today.
		for(int index = 0; index < size; index++) {
			if (SharedUtils.notEmptyMonthDay(study.studyLessonInfos.get(index).month, study.studyLessonInfos.get(index).day)) {
				if (compareMonthDay(todaysMonth, todaysDay, study.studyLessonInfos.get(index).month, study.studyLessonInfos.get(index).day) == 0) {
					closestIndex = index;
					break;
				}
				// Is today after this lesson and is the closest lesson so far before this lesson? 
				if (compareMonthDay(todaysMonth, todaysDay, study.studyLessonInfos.get(index).month, study.studyLessonInfos.get(index).day) > 0 && 
						compareMonthDay(closestMonth, closestDay, study.studyLessonInfos.get(index).month, study.studyLessonInfos.get(index).day) < 0) 
				{
					closestMonth = study.studyLessonInfos.get(index).month;
					closestDay = study.studyLessonInfos.get(index).day;
					closestIndex = index;
				}
			}
		}
		
		if (relativeIndex < 0) {
			return readRelativeLesson(datastore, closestIndex, relativeIndex);
		} else {
			return readRelativeLesson(datastore, closestIndex, 0);
		}
	}
	
	private static int compareMonthDay(int month1, int day1, int month2,
			int day2) {
		// compares 1 to 2. 1 < 2 will be negative
		// 1 > 2 will be positive
		// 1 = 2 will be 0
		if (month1 != month2) {
			return month1 - month2;
		}
		return day1 - day2;
	}
	
    protected void populateLessonDescription(StudyLesson lesson, int lessonIndex, int relativeIndex) {
    	String pageTagLine = "";
		if (lesson.day != null && lesson.month != null && lesson.day > 0 && lesson.month > 0) {
			String month[] = new String[] {"", "January","February","March","April","May","June","July","August","September","October","November","December"};
			String day[] = new String[] {"", "1st","2nd","3rd","4th","5th","6th","7th","8th","9th",
					"10th","11th","12th","13th","14th","15th","16th","17th","18th","19th",
					"20th","21st","22nd","23rd","24th","25th","26th","27th","28th","29th",
					"30th","31st"};
			pageTagLine = month[lesson.month] + " " + day[lesson.day];
		} else if (relativeIndex == 0) {
			pageTagLine = "Today's Lesson";
		} else {
			super.populateLessonDescription(lesson, lessonIndex, relativeIndex);
			return;
		}
		
		lesson.devotionPageTagLine = pageTagLine;
	}
}
