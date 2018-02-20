package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.*;
import com.groupdevotions.server.logic.StudyLogic;
import com.groupdevotions.server.logic.StudyLogicFactory;
import com.groupdevotions.server.rest.Response;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DevotionService {
    private Logger logger = Logger.getLogger(DevotionService.class.getName());
    private final ConfigDAO configDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final GroupMemberBlogDAO groupMemberBlogDAO;
    private final GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO;
    private final StudyDAO studyDAO;
    private final StudyLessonDAO studyLessonDAO;
    private final StudyLogicFactory studyLogicFactory;

    @Inject
    public DevotionService(ConfigDAO configDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO, GroupMemberBlogDAO groupMemberBlogDAO, GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO, StudyDAO studyDAO, StudyLessonDAO studyLessonDAO, StudyLogicFactory studyLogicFactory) {
        this.configDAO = configDAO;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.groupMemberBlogDAO = groupMemberBlogDAO;
        this.groupMemberLessonAnswerDAO = groupMemberLessonAnswerDAO;
        this.studyDAO = studyDAO;
        this.studyLessonDAO = studyLessonDAO;
        this.studyLogicFactory = studyLogicFactory;
    }

    public Response<DevotionData> getDevotions(ObjectDatastore datastore, UserInfo userInfo, StudyLessonNavigation navigation, int relativeIndex) {
        Response<DevotionData> response;
        Account account = userInfo.account;
        if (account.groupMemberKey != null) {
            boolean onlyLoadCoreStudy = false;
            List<StudyLesson> studyLessons = new ArrayList<StudyLesson>();
            GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
            Group group = groupDAO.read(datastore, groupMember.groupKey);
            String coreStudyKey = group.studyKey;
            if (coreStudyKey == null) {
                if (userInfo.account.studyKeyPublicAccepts.isEmpty()) {
                    return new Response("Please select a study to use.", Response.LocationType.settings);
                }
                coreStudyKey = userInfo.account.studyKeyPublicAccepts.iterator().next();
            }
            if (!account.settingsConfirmed) {
                return new Response("Please review your initial settings.", Response.LocationType.settings);
            }
            Study studyForGroup = studyDAO.read(datastore, coreStudyKey);
            StudyLogic groupStudyLogic = studyLogicFactory.getInstance(studyForGroup);

            if (navigation.equals(StudyLessonNavigation.TODAY)) {
                userInfo.lessonIndexRelativeToToday = 0;
            } else if (relativeIndex < 0) {
                userInfo.lessonIndexRelativeToToday = relativeIndex;
            }

            // Load core study --------------------------------------------------------------------------
            if (!navigation.equals(StudyLessonNavigation.TODAY) && !navigation.equals(StudyLessonNavigation.INDEX)) {
                if (navigation.equals(StudyLessonNavigation.NEXT) && userInfo.lessonIndexRelativeToToday == 0) {
                    if (groupStudyLogic.allowFuture(groupMember)) {
                        // fake out last completed by setting it in the past to allow moving forward
                        groupMember.lastCompletedDateAsString = ServerUtils.yesterdayDateAsString();
                        userInfo.lessonIndexRelativeToToday += 1;
                        onlyLoadCoreStudy = true;
                    }
                } else {
                    userInfo.lessonIndexRelativeToToday += navigation.getDirection();
                }
            }

            StudyLesson studyLesson = null;
            if (navigation.equals(StudyLessonNavigation.INDEX)) {
                // navigation direction here is the absolute index
                if (relativeIndex < studyForGroup.studyLessonInfos.size() && relativeIndex >= 0) {
                    studyLesson = groupStudyLogic.readIndex(datastore, groupMember, relativeIndex);
                    addStudyLesson(studyLessons, studyLesson);
                    onlyLoadCoreStudy = true;
                }
            } else {
                studyLesson = groupStudyLogic.readLesson(datastore, groupMember, userInfo.lessonIndexRelativeToToday);
                if (userInfo.lessonIndexRelativeToToday != 0 || (studyLesson != null && !account.studyKeyGroupDeclines.contains(studyLesson.studyKey))) {
                    addStudyLesson(studyLessons, studyLesson);
                }
            }

            // Unable to find anything else, use last core lesson
            if (studyLesson == null && group.backupDailyStudyKey == null && studyForGroup.studyLessonInfos.size() > 0) {
                studyLesson = groupStudyLogic.readIndex(datastore, groupMember, studyForGroup.studyLessonInfos.size() - 1);
                if (!account.studyKeyGroupDeclines.contains(studyLesson.studyKey)) {
                    addStudyLesson(studyLessons, studyLesson);
                }
            }

            // Read answers and lesson blogs for core study
            List<GroupMemberBlog> lessonBlogs = new ArrayList<GroupMemberBlog>();
            userInfo.currentStudyLessonKey = null;
            if (studyLesson != null && !SharedUtils.isEmpty(studyLesson.key)) {
                // Should I move this to the study logic?
                // yes you should!  not implemented yet in angular
                // accountability does not save or display answers
                GroupMemberLessonAnswer groupMemberLessonAnswer = groupMemberLessonAnswerDAO.readByAllKeys(datastore,
                        account.key, groupMember.groupKey, studyLesson.key);
                if (groupMemberLessonAnswer != null) {
                    studyLesson.populateStudyLessonAnswers(groupMemberLessonAnswer);
                }
                lessonBlogs = groupMemberBlogDAO.readForLesson(datastore, studyLesson.key);
                userInfo.currentStudyLessonKey = studyLesson.key;
            }
            HashMap<String, List<BlogEntry>> lessonBlogMap = transformGroupMemberBlogList(lessonBlogs);

            // the current study is used for saving answers
            // ------------------------------------------------ done loading the core study lesson

            // Load any other lessons subscribed to
            if (!onlyLoadCoreStudy) {
                Study backupStudy = null;
                //if (!SharedUtils.isEmpty(group.backupDailyStudyKey) && !account.studyKeyGroupDeclines.contains(group.backupDailyStudyKey)) {
                //    backupStudy = studyDAO.read(datastore, group.backupDailyStudyKey);
                //    StudyLogic otherStudyLogic = studyLogicFactory.getInstance(backupStudy);
                //    studyLesson = otherStudyLogic.readLesson(datastore, groupMember, userInfo.lessonIndexRelativeToToday);
                //    addStudyLesson(studyLessons, studyLesson);
                //}

                for (String otherPublicStudyKey : account.studyKeyPublicAccepts) {
                    Study otherStudy = studyDAO.read(datastore, otherPublicStudyKey);
                    // Don't add if we already are using or have checked it already
                    if (otherStudy != null && !otherStudy.key.equals(studyForGroup.key) &&
                            (backupStudy == null || !otherStudy.key.equals(backupStudy.key))) {
                        StudyLogic otherStudyLogic = studyLogicFactory.getInstance(otherStudy);
                        studyLesson = otherStudyLogic.readLesson(datastore, groupMember, userInfo.lessonIndexRelativeToToday);
                        addStudyLesson(studyLessons, studyLesson);
                    }
                }

                // load the accountability unto the end
                boolean accountabilityForTodayAlreadySent = ServerUtils.todayDateAsString().equals(groupMember.lastAccountabilityDateAsString);
                if (!SharedUtils.isEmpty(studyForGroup.accountabilityLessonKey) &&
                        (!accountabilityForTodayAlreadySent || userInfo.lessonIndexRelativeToToday == 0)) {
                    StudyLesson accountabilityLesson = studyLessonDAO.read(datastore, studyForGroup.accountabilityLessonKey);
                    if (accountabilityLesson != null && studyLessons.size() > 0) {
                        accountabilityLesson.accountabilityLesson = true;
                        addStudyLesson(studyLessons, accountabilityLesson);
                    }
                }
            }

            // navigation for core study which goes on the last lesson
            boolean firstLesson = true;
            if (studyLessons.size() > 0) {
                List<StudyLessonInfo> studyLessonInfos = buildStudyLessonInfos(studyForGroup, groupMember.lastCompletedStudyLessonKey);
                studyLessons.get(studyLessons.size() - 1).navigationStudyLessonInfos = studyLessonInfos;
                studyLessons.get(studyLessons.size() - 1).navigationCaption = studyForGroup.title;
                firstLesson = studyLessonInfos.size() + userInfo.lessonIndexRelativeToToday <= 0;
            }

            // Get the group and register the member with the group
            group.updateGroupMemberActivities(groupMember.key, groupMember.name);
            groupDAO.update(datastore, group, KeyFactory.stringToKey(group.key));

            groupMemberDAO.update(datastore, groupMember, KeyFactory.stringToKey(groupMember.key));
            boolean allowViewTomorrowsLesson = groupStudyLogic.allowFuture(groupMember) &&
                    userInfo.lessonIndexRelativeToToday >= 0 && !onlyLoadCoreStudy;

            if (navigation.equals(StudyLessonNavigation.INDEX)) {
                firstLesson = true;
                allowViewTomorrowsLesson = false;
            }

            // don't stop user anymore
            if (!onlyLoadCoreStudy) {
                firstLesson = false;
            }

            DevotionData devotionData = new DevotionData(studyLessons, lessonBlogMap, firstLesson);
            devotionData.accountabilityConfigured = !groupMember.accountabilityEmails.isEmpty();
            response = new Response<>(devotionData);
        } else {
            response = new Response<>("You are not yet a member of a group.");
        }
        return response;
    }

    private List<StudyLessonInfo> buildStudyLessonInfos(Study study, String lastCompletedLesson) {
        List<StudyLessonInfo> result = new ArrayList<StudyLessonInfo>();
        if (!SharedUtils.isEmpty(lastCompletedLesson)) {
            for (StudyLessonInfo info : study.studyLessonInfos) {
                result.add(info);
                if (SharedUtils.safeEquals(info.studyLessonKey, lastCompletedLesson)) {
                    break;
                }
            }
        }

        return result;
    }

    private HashMap<String, List<BlogEntry>> transformGroupMemberBlogList(List<GroupMemberBlog> blogList) {
        HashMap<String, List<BlogEntry>> sectionMap = new HashMap<String, List<BlogEntry>>();

        // Put them in the map by section
        for (GroupMemberBlog groupMemberBlog : blogList) {
            for (BlogEntry blogEntry : groupMemberBlog.blogEntries) {
                List<BlogEntry> sectionBlogs = sectionMap.get(blogEntry.studySectionCreationTimestamp);
                if (sectionBlogs == null) {
                    sectionBlogs = new ArrayList<BlogEntry>();
                    sectionMap.put(blogEntry.studySectionCreationTimestamp, sectionBlogs);
                }
                blogEntry.name = groupMemberBlog.name;
                blogEntry.groupMemberKey = groupMemberBlog.groupMemberKey;
                blogEntry.formattedPostedOn = ServerUtils.formatDatetimeForDisplay(blogEntry.postedOn);
                blogEntry.postedOnFullDateTime = ServerUtils.formatFullDateTime(blogEntry.postedOn);
                sectionBlogs.add(blogEntry);
            }
        }

        Comparator<BlogEntry> c = new Comparator<BlogEntry>() {
            @Override
            public int compare(BlogEntry arg0, BlogEntry arg1) {
                return arg0.postedOn.compareTo(arg1.postedOn);
            }
        };

        // sort each section
        for (String studySectionCreationTimestamp : sectionMap.keySet()) {
            List<BlogEntry> sectionBlogs = sectionMap.get(studySectionCreationTimestamp);
            Collections.sort(sectionBlogs, c);
        }
        return sectionMap;
    }

    private void addStudyLesson(List<StudyLesson> studyLessons, StudyLesson studyLesson) {
        if (studyLesson != null) {
            studyLessons.add(studyLesson);
        }
    }

    public Response.Message saveAnswersForGroupStudyAndSendAccountabilityEmail(ObjectDatastore datastore, UserInfo userInfo, DevotionData devotionData) {
        Account account = userInfo.account;
        GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        Study study = studyDAO.read(datastore, group.studyKey);
        StudyLesson groupStudyLesson = null;
        String error;
        boolean updateGroupMember = false;
        Response.Message message = null;

        for (StudyLesson studyLesson : devotionData.studyLessons) {
            if (study.key.equals(studyLesson.studyKey) && studyLesson.key != null) {
                StudyLesson pristineStudyLesson = studyLessonDAO.read(datastore, studyLesson.key);
                pristineStudyLesson.populateStudyLessonAnswers(studyLesson);
                if (study.validStudyLessonKey(studyLesson.key) && !studyLesson.key.equals(study.accountabilityLessonKey)) {
                    if (study.validStudyLessonKey(studyLesson.key)) {
                        storeAnswersForStudyLesson(datastore, account, groupMember, group, pristineStudyLesson);
                        if (studyLesson.key.equals(userInfo.currentStudyLessonKey)) {
                            updateGroupMember |= updateGroupMemberStudyProgress(datastore, userInfo, groupMember, study, pristineStudyLesson);
                        }
                        groupStudyLesson = pristineStudyLesson;
                    }
                }
                if (studyLesson.key.equals(study.accountabilityLessonKey)) {
                    error = sendAccountabilityEmail(datastore, groupMember, study, groupStudyLesson, pristineStudyLesson);
                    if (error != null) {
                        message = new Response.Message(Response.MessageType.warning, error);
                    }
                }
            }
        }
        if (updateGroupMember) {
            groupMemberDAO.update(datastore, groupMember, KeyFactory.stringToKey(groupMember.key));
        }
        return message;
    }

    public void readingComplete(ObjectDatastore datastore, UserInfo userInfo, StudyLesson studyLesson) {
        Account account = userInfo.account;
        GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);

        groupMember.lastCompletedBibleReadingIndex = String.valueOf(studyLesson.bibleReadingIndex);
        groupMemberDAO.update(datastore, groupMember, KeyFactory.stringToKey(groupMember.key));
    }

    private void storeAnswersForStudyLesson(ObjectDatastore datastore, Account account, GroupMember groupMember, Group group, StudyLesson studyLesson) {
        List<EnhancedString> enhancedStringAnswers = studyLesson.getEnhancedStringAnswerList();
        if (!enhancedStringAnswers.isEmpty()) {
            GroupMemberLessonAnswer groupMemberLessonAnswer = groupMemberLessonAnswerDAO.readByAllKeys(datastore, account.key, groupMember.groupKey, studyLesson.key);
            if (groupMemberLessonAnswer == null) {
                groupMemberLessonAnswer = new GroupMemberLessonAnswer(account.key, group.key, studyLesson.key, enhancedStringAnswers);
                groupMemberLessonAnswerDAO.create(datastore, groupMemberLessonAnswer);
            } else {
                groupMemberLessonAnswer.answers = enhancedStringAnswers;
                groupMemberLessonAnswerDAO.update(datastore, groupMemberLessonAnswer, groupMemberLessonAnswerDAO.getKey(datastore, groupMemberLessonAnswer));
            }
        }
    }

    private boolean missingAnswers(StudyLesson lesson) {
        boolean missingAnswers = false;
        for(StudySection section : lesson.studySections) {
            if (section.isAccountabilityQuestion()) {
                missingAnswers |= SharedUtils.isEmpty(section.answer);
            }
        }
        return missingAnswers;
    }

    private boolean updateGroupMemberStudyProgress(ObjectDatastore datastore, UserInfo userInfo, GroupMember groupMember, Study studyForGroup, StudyLesson lesson) {
        boolean updateGroupMember = false;
        if (!missingAnswers(lesson) && studyForGroup.studyType.equals(StudyType.SERIES) &&
                userInfo.lessonIndexRelativeToToday == 0) {  // backup study should not be a move forward situation
            boolean foundPrevious = false;
            //int lessonCount = 0;
            // Make sure this lesson is after the previous one
            for(StudyLessonInfo info : studyForGroup.studyLessonInfos) {
                //lessonCount++;
                if (SharedUtils.safeEquals(info.studyLessonKey, groupMember.lastCompletedStudyLessonKey) || SharedUtils.isEmpty(groupMember.lastCompletedStudyLessonKey)) {
                    foundPrevious = true;
                }
                if (foundPrevious && SharedUtils.safeEquals(info.studyLessonKey, lesson.key)) {
                    groupMember.lastCompletedStudyLessonKey = lesson.key;
                    groupMember.lastCompletedDateAsString = ServerUtils.todayDateAsString();
                    updateGroupMember = true;
                    break;
                }
            }
        }
        return updateGroupMember;
    }

    private String sendAccountabilityEmail(ObjectDatastore datastore, GroupMember groupMember, Study studyForGroup, StudyLesson groupStudyLesson, StudyLesson accountabilityStudyLesson) {
        if (!groupMember.accountabilityEmails.isEmpty()) {
            Config config = configDAO.readInstance(datastore);
            String subject = studyForGroup.title + ": " + groupMember.name + "'s responses";
            StringBuilder emailBody = new StringBuilder();

            emailBody.append("The study group member \"" + groupMember.name + "\" is doing a daily Bible study called \""
                    + studyForGroup.title + "\" at " + config.siteUrl + ".  ");
            emailBody.append(groupMember.name + " has chosen you to receive these daily emails containing answers to private questions.  ");
            emailBody.append("Please provide support and encouragement as a fellow Christian to " + groupMember.name + " as you read these responses.\n\n");
            if (groupStudyLesson != null) {
                emailBody.append("Lesson: " + groupStudyLesson.title + "\n\n");
            }

            int index = 0;
            if (groupStudyLesson != null) {
                index = addSectionAnswers(emailBody, groupStudyLesson.studySections, 0);
            }
            if (accountabilityStudyLesson != null) {
                addSectionAnswers(emailBody, accountabilityStudyLesson.studySections, index);
                groupMember.lastAccountabilityDateAsString = ServerUtils.todayDateAsString();
            }
            return sendEmail(config, groupMember, subject, emailBody.toString());
        }
        return null;
    }

    private int addSectionAnswers(StringBuilder emailBody, List<StudySection> sections, int index) {
        for(StudySection section : sections) {
            if (section.isAccountabilityQuestion()) {
                emailBody.append("Question " + (index+1) + ": " + section.content + "\n");
                String questionAnswer = section.answer;
                if (questionAnswer == null || SharedUtils.isEmpty(questionAnswer)) {
                    questionAnswer = "No Answer.";
                }
                emailBody.append("Answer: " + questionAnswer + "\n\n");
                index++;
            }
        }
        return index;
    }

    private String sendEmail(Config config, GroupMember groupMember, String subject, String body) {
        String error = "Your answers have been saved, but something went wrong and we are unable to send out the email.";
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            for(String oneToAddress : groupMember.accountabilityEmails) {
                InternetAddress toAddress[] = InternetAddress.parse(oneToAddress);
                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(config.fromNoReplyEmailAddr, config.fromNoReplyEmailAddrDesc));
                message.addRecipient(Message.RecipientType.TO, toAddress[0]);
                if (groupMember.sendAccountabilityEmailsToMe) {
                    message.addRecipients(Message.RecipientType.CC,  InternetAddress.parse(groupMember.email));
                }
                message.setSubject(subject);
                message.setText(body);
                logger.log(Level.INFO, "Body: " + body);
                Transport.send(message);
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Unable to send email for " + groupMember.key, e);
            return error;
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send email for " + groupMember.key, e);
            return error;
        }
        return null;
    }

}
