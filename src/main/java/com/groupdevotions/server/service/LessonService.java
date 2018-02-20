package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.groupdevotions.server.dao.StudyDAO;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

import java.util.logging.Logger;

public class LessonService {
    private Logger logger = Logger.getLogger(LessonService.class.getName());
    private final StudyLessonDAO studyLessonDAO;
    private final StudyDAO studyDAO;
    private final StudyService studyService;

    @Inject
    public LessonService(StudyLessonDAO studyLessonDAO, StudyDAO studyDAO, StudyService studyService) {
        this.studyLessonDAO = studyLessonDAO;
        this.studyDAO = studyDAO;
        this.studyService = studyService;
    }

    public StudyLesson read(ObjectDatastore datastore, String studyLessonKey) {
        StudyLesson lesson = studyLessonDAO.read(datastore, studyLessonKey);
        Study study = studyDAO.read(datastore, lesson.studyKey);
        lesson.studyInfoIndex = study.findStudyLessonInfoIndex(lesson);
        lesson.accountabilityLesson = (lesson.key.equals(study.accountabilityLessonKey));
        return lesson;
    }

    public String addLesson(ObjectDatastore datastore, StudyLesson lesson) {
        Study study = studyDAO.read(datastore, lesson.studyKey);
        String message = validateLesson(lesson, study);
        if (message == null) {
            studyLessonDAO.create(datastore, lesson);
            if (lesson.accountabilityLesson) {
                if (!SharedUtils.safeEquals(study.accountabilityLessonKey, lesson.key)) {
                    if (study.accountabilityLessonKey != null) {
                        // Should never happen, but JIC don't want to orphan an instance.
                        studyLessonDAO.delete(datastore, KeyFactory.stringToKey(study.accountabilityLessonKey));
                    }
                }
                study.accountabilityLessonKey = lesson.key;
            } else {
                study.addStudyLessonInfo(lesson);
            }
            studyDAO.update(datastore, study, studyDAO.getKey(datastore, study));
            lesson.study = study;
        }
        return message;
    }

    public String save(ObjectDatastore datastore, StudyLesson updatedLesson) {
        Study study = studyDAO.read(datastore, updatedLesson.studyKey);
        String message = validateLesson(updatedLesson, study);
        if (message == null) {
            StudyLesson lesson = studyLessonDAO.read(datastore, updatedLesson.key);
            // todo I don't think this check is strong enough
            if (!lesson.studyKey.equals(updatedLesson.studyKey)) {
                message = "You are not authorized to edit this study.";
            } else {
                lesson.updateMaintainFields(updatedLesson);
                studyLessonDAO.update(datastore, lesson, studyLessonDAO.getKey(datastore, lesson));
                if (!lesson.accountabilityLesson) {
                    study.updateStudyLessonInfo(updatedLesson);
                    studyDAO.update(datastore, study, studyDAO.getKey(datastore, study));
                }
                updatedLesson.study = study;
            }
        }
        return message;
    }

    public String delete(ObjectDatastore datastore, String studyLessonKey, Account account) {
        StudyLesson studyLesson = studyLessonDAO.read(datastore, studyLessonKey);
        if (studyLesson == null) {
            return "Unable to find the lesson.";
        }
        Study study = studyDAO.read(datastore, studyLesson.studyKey);
        if (study == null) {
            return "Unable to find the parent study.";
        }
        if (!studyService.isAccountStudyContributor(datastore, study.key, account)) {
            return "You are not authorized to edit this lesson.";
        }

        studyLessonDAO.delete(datastore, KeyFactory.stringToKey(studyLessonKey));
        if (studyLessonKey.equals(study.accountabilityLessonKey)) {
            study.accountabilityLessonKey = null;
        } else {
            study.deleteStudyLessonInfo(studyLessonKey);
        }
        studyDAO.update(datastore, study, studyDAO.getKey(datastore, study));
        return null;
    }

    private String validateLesson(StudyLesson lesson, Study study) {
        int daysInMonth[] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        if (Strings.isNullOrEmpty(lesson.title)) {
            return "Title is required.";
        }
        if (!lesson.accountabilityLesson && study.studyType.equals(StudyType.DAILY)) {
            if (lesson.day == null) {
                return "Day is required because this is a daily study.";
            }
            if (lesson.month == null) {
                return "Month is required because this is a daily study.";
            }
            if (lesson.day < 1 || daysInMonth[lesson.month] < lesson.day) {
                return "The entered day does not exist in this month.";
            }
            StudyLessonInfo lessonInfoForThisDay = study.findStudyLessonInfo(lesson);
            if (lessonInfoForThisDay != null && !lessonInfoForThisDay.studyLessonKey.equals(lesson.key)) {
                return "A study lesson for this month/day already exists.";
            }
        }
        if (lesson.studySections.size() < 1) {
            return "Please add at least one paragraph of content to your lesson.";
        }
        return null;
    }
}
