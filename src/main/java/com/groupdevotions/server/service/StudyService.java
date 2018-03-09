package com.groupdevotions.server.service;

import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.GroupDAO;
import com.groupdevotions.server.dao.StudyContributorDAO;
import com.groupdevotions.server.dao.StudyDAO;
import com.groupdevotions.server.dao.StudyLessonDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

import java.text.Collator;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by DanV on 8/29/2015.
 */
public class StudyService {
    private Logger logger = Logger.getLogger(StudyService.class.getName());
    private final StudyDAO studyDAO;
    private final StudyLessonDAO studyLessonDAO;
    private final StudyContributorDAO studyContributorDAO;
    private final GroupDAO groupDAO;


    @Inject
    public StudyService(StudyDAO studyDAO, StudyLessonDAO studyLessonDAO, StudyContributorDAO studyContributorDAO, GroupDAO groupDAO) {
        this.studyDAO = studyDAO;
        this.studyLessonDAO = studyLessonDAO;
        this.studyContributorDAO = studyContributorDAO;
        this.groupDAO = groupDAO;
    }

    public Study read(ObjectDatastore datastore, String key) {
        return studyDAO.read(datastore, key);
    }

    public Collection<Study> readStudiesForAccount(ObjectDatastore datastore, UserInfo userInfo, boolean loadPublicStudies) {
        if (userInfo.account.siteAdmin) {
            return studyDAO.readAll(datastore);
        } else {
            Set<Study> unqiueStudies = new HashSet();
            if (loadPublicStudies) {
                unqiueStudies.addAll(studyDAO.readPublicDailyStudies(datastore));
            }
            String organizationKey = userInfo.account.adminOrganizationKey;
            if (organizationKey == null && userInfo.groupMember != null) {
                Group group = groupDAO.read(datastore, userInfo.groupMember.groupKey);
                organizationKey = group.ownerOrganizationKey;
            }
            if (organizationKey != null) {
                unqiueStudies.addAll(studyDAO.readForOrganization(datastore, organizationKey));
            } else {
                unqiueStudies.addAll(studyDAO.read(datastore, userInfo.account.studyContributors));
            }

            List<Study> sortedStudies = Lists.newArrayList(unqiueStudies);
            Collections.sort(sortedStudies, new Comparator<Study>() {
                @Override
                public int compare(Study arg0, Study arg1) {
                    String string0 = (arg0.ownerOrganizationKey != null ? "A" : "B") + arg0.title;
                    String string1 = (arg1.ownerOrganizationKey != null ? "A" : "B") + arg1.title;
                    return Collator.getInstance().compare(string0, string1);
                }
            });

            return sortedStudies;
        }
    }

    public Collection<Study> readPublicStudies(ObjectDatastore datastore) {
        Collection<Study> studies = studyDAO.readPublicDailyStudies(datastore);
        return studies;
    }

    public String addStudy(ObjectDatastore datastore, Study study, String accountKey) {
        String message = validateStudy(study);
        if (message == null) {
            studyDAO.create(datastore, study);
            StudyContributor studyContributor = new StudyContributor();
            studyContributor.studyAdmin = true;
            studyContributor.studyKey = study.key;
            studyContributor.accountKey = accountKey;
            studyContributorDAO.create(datastore, studyContributor);
        }
        return message;
    }

    public String save(ObjectDatastore datastore, Study updatedStudy, Account account) {
        String message = validateStudy(updatedStudy);
        if (message == null && !isAccountStudyAdmin(datastore, updatedStudy.key, account)) {
            message = "You are not authorized to edit this study.";
        }
        if (message == null) {
            Study study = studyDAO.read(datastore, updatedStudy.key);
            study.updateMaintainFields(updatedStudy);
            reIndexLessonsForDailyStudy(datastore, study);
            studyDAO.update(datastore, study, studyDAO.getKey(datastore, study));
        }
        return message;
    }

    private void reIndexLessonsForDailyStudy(ObjectDatastore datastore, Study study) {
        if (StudyType.DAILY.equals(study.studyType)) {
            List<StudyLesson> lessons = studyLessonDAO.readByStudyKey(datastore, study.key);
            study.studyLessonInfos = new ArrayList<StudyLessonInfo>();

            for (StudyLesson studyLesson : lessons) {
                if (!studyLesson.accountabilityLesson && !SharedUtils.safeEquals(studyLesson.key, study.accountabilityLessonKey)) {
                    StudyLessonInfo studyLessonInfo = new StudyLessonInfo();
                    studyLessonInfo.studyLessonKey = studyLesson.key;
                    studyLessonInfo.day = studyLesson.day;
                    studyLessonInfo.month = studyLesson.month;
                    studyLessonInfo.title = studyLesson.title;
                    study.studyLessonInfos.add(studyLessonInfo);
                }
            }

            study.sortStudyLessonInfoForDaily();
        }
    }

    public boolean isAccountStudyAdmin(ObjectDatastore datastore, String studyKey, Account account) {
        return isAccountStudyContributor(datastore, studyKey, account, true);
    }

    public boolean isAccountStudyContributor(ObjectDatastore datastore, String studyKey, Account account) {
        return isAccountStudyContributor(datastore, studyKey, account, false);
    }

    private boolean isAccountStudyContributor(ObjectDatastore datastore, String studyKey, Account account, boolean studyAdmin) {
        if (account.siteAdmin) {
            return true;
        } else {
            if (account.adminOrganizationKey != null) {
                Study study = studyDAO.read(datastore, studyKey);
                if (SharedUtils.safeEquals(study.ownerOrganizationKey, account.adminOrganizationKey)) {
                    return true;
                }
            }
            List<StudyContributor> accountStudyContributors = studyContributorDAO.readByAccountKey(datastore, account.key);
            for (StudyContributor studyContributor : accountStudyContributors) {
                // todo Do we want a way to validate for lessons but make that different from update to the study?
                if (studyContributor.studyKey.equals(studyKey) && (!studyAdmin || studyContributor.studyAdmin)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String validateStudy(Study study) {
        if (Strings.isNullOrEmpty(study.title)) {
            return "Title is required.";
        } else if (Strings.isNullOrEmpty(study.purpose)) {
            return "Purpose is required.";
        } else if (study.studyType == null) {
            return "Please select a type of study.";
        } else if (StudyType.BIBLE.equals(study.studyType) && !SharedUtils.isEmpty(study.dailyReadingStartingMonthDay) && study.dailyReadingStartsEachMonth) {
            return "You can set \"Starting month/day\" or select \"starts new each month\" but not both.";
        } else if (StudyType.BIBLE.equals(study.studyType) && !SharedUtils.isEmpty(study.dailyReadingStartingMonthDay)) {
            if (study.dailyReadingStartingMonthDay.length() != 5 || !study.dailyReadingStartingMonthDay.matches("\\d\\d/\\d\\d")) {
                return "Please enter a the start month and day in the format MM/DD";
            }
            int month = Integer.valueOf(study.dailyReadingStartingMonthDay.substring(0, 2));
            int day = Integer.valueOf(study.dailyReadingStartingMonthDay.substring(3));
            if (!SharedUtils.isValidDayForMonth(month, day)) {
                return "The starting month/day is not a valid month or day.";
            }
        }
        return null;
    }
}
