package com.groupdevotions.server.logic;

import com.google.inject.Inject;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.shared.model.Study;
import com.groupdevotions.shared.model.StudyType;

public class StudyLogicFactory {
	@Inject StudyLessonDAO studyLessonDAO;
	
	public StudyLogic getInstance(Study study) {
		if (study.studyType == StudyType.DAILY) {
			return new DailyStudyLogicImpl(studyLessonDAO, study);
		} else if (study.studyType == StudyType.SERIES) {
			return new SeriesStudyLogicImpl(studyLessonDAO, study);
		} else if (study.studyType == StudyType.BIBLE) {
			return new BibleStudyLogicImpl(studyLessonDAO, study);
		}
		return new RssStudyLogicImpl(studyLessonDAO, study);
	}
}
