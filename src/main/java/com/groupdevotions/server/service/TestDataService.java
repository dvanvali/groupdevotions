package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.org.joda.time.DateMidnight;
import com.google.code.twig.ObjectDatastore;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.*;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.*;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class TestDataService {
    protected static final Logger logger = Logger
            .getLogger(TestDataService.class.getName());
    // todo refactor out - this is not threadsafe, but not a big deal since it really need not be threadsafe as a test only service
    private final ObjectDatastore datastore;
    private final AccountDAO accountDAO;
    private final StudyLessonDAO studyLessonDAO;
    private final StudyDAO studyDAO;
    private final GroupDAO groupDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO;
    private final JournalDAO journalDAO;
    private final GroupBlogDAO groupBlogDAO;
    private final StudyContributorDAO studyContributorDAO;
    private final OrganizationDAO organizationDAO;

    @Inject
    public TestDataService(ObjectDatastore datastore, AccountDAO accountDAO, StudyLessonDAO studyLessonDAO, StudyDAO studyDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO, GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO, JournalDAO journalDAO, GroupBlogDAO groupBlogDAO, StudyContributorDAO studyContributorDAO, OrganizationDAO organizationDAO) {
        this.datastore = datastore;
        this.accountDAO = accountDAO;
        this.studyLessonDAO = studyLessonDAO;
        this.studyDAO = studyDAO;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.groupMemberLessonAnswerDAO = groupMemberLessonAnswerDAO;
        this.journalDAO = journalDAO;
        this.groupBlogDAO = groupBlogDAO;
        this.studyContributorDAO = studyContributorDAO;
        this.organizationDAO = organizationDAO;
    }

    public void buildTestData() {
        // Bad to be running tests against a live site - upping their stats
        //Study rssStudy = createRssStudy();
        //Account nonGoogleAccount = createNonGoogleAccount("dvanvali@gmail.com", "Dan V", "xxxxxx", rssStudy.key);
        Organization organization = new Organization();
        organization.name = "Dunamai";
        organizationDAO.create(datastore, organization);
        Account orgAdminAccount = createGoogle("org@example.com", "Dan O");
        orgAdminAccount.adminOrganizationKey = organization.key;
        accountDAO.update(datastore, orgAdminAccount);

        Study extraStudy = createStudy("Extra", true);
        Account nonGoogleAccount = createNonGoogleAccount("nongoogle@gmail.com", "Dan V", "xxxxxx", null, false);
        createLockedAccount("locked@gmail.com", "xxxxxx");
        Account groupOwnerAccount = createNonGoogleAccount("groupowner@gmail.com", "Dan G", "xxxxxx", null, false);
        createUnconfirmedAccount("unconfirmed@gmail.com", "xxxxxx");
        Study study = createStudy("Dan", false);
        Group group = createGroup(study, groupOwnerAccount);
        Date startDate = DateMidnight.parse("2015-03-01").toDate();
        createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, 0));
        createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, -1));
        createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, -2));
        createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, -6));
        createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, -7));
        for (int i=0; i<25; i++) {
            createJournal(nonGoogleAccount, ServerUtils.dateAddDays(startDate, -13-i));
        }

        GroupMember groupMember = createGroupMember(nonGoogleAccount, "Dan V", group);
        createGroupMemberLessonAnswerForStudy(nonGoogleAccount, group, study);
        nonGoogleAccount.groupMemberKey = groupMember.key;
        accountDAO.update(datastore, nonGoogleAccount, KeyFactory.stringToKey(nonGoogleAccount.key));

        Account googleAccount = createGoogle("test@example.com", "Dan G");
        GroupMember googleGroupMember = createGroupMember(googleAccount, "Dan G", group);
        googleAccount.groupMemberKey = googleGroupMember.key;
        accountDAO.update(datastore, googleAccount, KeyFactory.stringToKey(googleAccount.key));
        createStudyContributor(study, googleAccount);

        Account localAccountTermsNotAgreed = createNonGoogleAccount("terms@gmail.com", "No Terms", "yyyyyy", null, false);
        localAccountTermsNotAgreed.agreedToTermsOfUse = null;
        localAccountTermsNotAgreed.postingNotification = PostingNotification.EMAIL;
        groupMember = createGroupMember(localAccountTermsNotAgreed, "No Terms", group);
        localAccountTermsNotAgreed.groupMemberKey = groupMember.key;
        accountDAO.update(datastore, localAccountTermsNotAgreed, KeyFactory.stringToKey(localAccountTermsNotAgreed.key));
        buildGroupBlogs(groupMember, googleGroupMember);
        addActivityToGroup(group, localAccountTermsNotAgreed, googleAccount, nonGoogleAccount);

        createGroupMemberInvite("invite@gmail.com", "Invite", group);

        Account desktopAccount = createNonGoogleAccount("desktop@gmail.com", "Desktop", "xxxxxx", null, false);
        desktopAccount.screenFormat = ScreenFormat.MONITOR;
        GroupMember desktopGroupMember = createGroupMember(desktopAccount, desktopAccount.name, group);
        desktopAccount.groupMemberKey = desktopGroupMember.key;
        accountDAO.update(datastore, desktopAccount, KeyFactory.stringToKey(desktopAccount.key));

        // setup bible reading plan
        Account bibleAccount = createGoogle("testb@example.com", "Bible Dan");
        bibleAccount.siteAdmin = true;
        Study bibleStudy = createBibleStudy(false);
        Group bibleGroup = createGroup(bibleStudy, bibleAccount);
        GroupMember bibleMember = createGroupMember(bibleAccount, "Bible Dan", bibleGroup);
        bibleAccount.groupMemberKey = bibleMember.key;
        accountDAO.update(datastore, bibleAccount, KeyFactory.stringToKey(bibleAccount.key));
        createStudyContributor(bibleStudy, bibleAccount);

        // Add bible study which is not public to another account
        googleAccount.studyKeyPublicAccepts.add(bibleStudy.key);
        accountDAO.update(datastore, googleAccount, accountDAO.getKey(datastore, googleAccount));

        logger.info("Test data committed.");
    }

    private void createStudyContributor(Study study, Account googleAccount) {
        StudyContributor studyContributor = new StudyContributor();
        studyContributor.accountKey = googleAccount.key;
        studyContributor.studyAdmin = true;
        studyContributor.studyKey = study.key;
        studyContributorDAO.create(datastore, studyContributor);
    }

    public void resetTestData(){
        // reset terms of service
        Account localAccountTermsNotAgreed = accountDAO.readByEmail(datastore, "terms@gmail.com");
        localAccountTermsNotAgreed.agreedToTermsOfUse = null;
        accountDAO.update(datastore, localAccountTermsNotAgreed, KeyFactory.stringToKey(localAccountTermsNotAgreed.key));

        // Remove todays blog
        Account nonGoogleAccount = accountDAO.readByEmail(datastore, "nongoogle@gmail.com");
        GroupMember groupMember = groupMemberDAO.read(datastore, nonGoogleAccount.groupMemberKey);
        Date startDate = DateMidnight.parse("2015-03-08").toDate();
        Date today = ServerUtils.removeTime(new Date());
        Collection<GroupBlog> groupBlogs = groupBlogDAO.readForDates(datastore, groupMember.groupKey, startDate, today);
        for (GroupBlog groupBlog : groupBlogs) {
            groupBlogDAO.delete(datastore, groupBlogDAO.getKey(datastore, groupBlog));
        }
        // reset reset password token and value
        nonGoogleAccount.resetToken = "83980169970545";
        nonGoogleAccount.resetExpiration = ServerUtils.dateAddMinutes(new Date(), 60);
        nonGoogleAccount.password = ServerUtils.hashPassword(nonGoogleAccount.userId, "xxxxxx");
        // Remove org admin
        nonGoogleAccount.adminOrganizationKey = null;
        accountDAO.update(datastore, nonGoogleAccount, KeyFactory.stringToKey(nonGoogleAccount.key));

        // give group member activities different dates
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        for (GroupMemberActivity activity : group.groupMemberActivities) {
            if (activity.name.equals("No Terms")) {
                activity.lastDevotionDate = ServerUtils.dateAddDays(new Date(), -2);
            }
            if (activity.name.equals("Dan G")) {
                activity.lastDevotionDate = ServerUtils.dateAddDays(new Date(), -3);
            }
        }
        groupDAO.update(datastore, group, groupDAO.getKey(datastore, group));

        // Remove account/acceptance from invite
        List<GroupMember> inviteGroupMembers = groupMemberDAO.readByEmail(datastore, "invite@gmail.com");
        GroupMember inviteGroupMember = Iterables.getOnlyElement(inviteGroupMembers);
        if (inviteGroupMember != null) {
            if (inviteGroupMember.accountKey != null) {
                accountDAO.delete(datastore, KeyFactory.stringToKey(inviteGroupMember.accountKey));
            }
            inviteGroupMember.accountKey = null;
            inviteGroupMember.status = GroupMemberStatus.EMAILED;
            groupMemberDAO.update(datastore, inviteGroupMember, groupMemberDAO.getKey(datastore, inviteGroupMember));
        } else {
            throw new IllegalStateException("Unable to find invite@gmail.com");
        }

        // Remove newaccount
        Account newAccount = accountDAO.readByEmail(datastore, "newaccount@gmail.com");
        if (newAccount != null) {
            accountDAO.delete(datastore, KeyFactory.stringToKey(newAccount.key));
        }
    }

    private void addActivity(Group group, Account account, int days) {
        GroupMemberActivity activity = new GroupMemberActivity();
        activity.groupMemberKey = account.groupMemberKey;
        activity.name = account.name;
        activity.lastDevotionDate = ServerUtils.dateAddDays(new Date(), days);
        group.groupMemberActivities.add(activity);
    }

    private Account createNonGoogleAccount(String email, String name, String plainTextPassword, String extraPublicStudyKey, boolean createStudies) {
        Account account = new Account();
        account.userId = "apx" + email;
        account.email = email;
        account.siteAdmin = false;
        account.settingsConfirmed = true;

        account.screenFormat = ScreenFormat.PHONE;
        account.signUpDate = new Date();
        account.lastLoginDate = new Date();
        account.confirmed = true;
        account.confirmedByEmail = true;
        account.disabled = false;
        account.password = ServerUtils.hashPassword(account.userId, plainTextPassword);
        account.agreedToTermsOfUse = new Date();
        if (extraPublicStudyKey != null) {
            account.studyKeyPublicAccepts.add(extraPublicStudyKey);
        }
        account.phone = "517-444-1111";
        account.acceptTexts = true;
        account.name = name;
        accountDAO.create(datastore, account);
        return account;
    }

    private Account createLockedAccount(String email, String plainTextPassword) {
        Account account = new Account();
        account.userId = "apx" + email;
        account.email = email;
        account.siteAdmin = false;
        account.settingsConfirmed = true;

        account.signUpDate = new Date();
        account.lastLoginDate = new Date();
        account.confirmed = true;
        account.confirmedByEmail = true;
        account.disabled = false;
        account.password = ServerUtils.hashPassword(account.userId, plainTextPassword);
        account.consecutiveFailedLogins = 2;
        accountDAO.create(datastore, account);
        return account;
    }

    private Account createUnconfirmedAccount(String email, String plainTextPassword) {
        Account account = new Account();
        account.userId = "apx" + email;
        account.email = email;
        account.siteAdmin = false;
        account.settingsConfirmed = true;

        account.screenFormat = ScreenFormat.PHONE;
        account.signUpDate = new Date();
        account.lastLoginDate = new Date();
        account.confirmed = true;
        account.confirmedByEmail = false;
        account.disabled = false;
        account.password = ServerUtils.hashPassword(account.userId, plainTextPassword);
        account.consecutiveFailedLogins = 10;
        accountDAO.create(datastore, account);
        return account;
    }

    private Account createGoogle(String email, String name) {
        Account account = new Account();
        account.userId = email.equals("test@example.com") ? "18580476422013912411" : "12381579238718592130";
        account.email = email;
        account.name = name;
        account.siteAdmin = false;
        account.settingsConfirmed = true;

        account.screenFormat = ScreenFormat.PHONE;
        account.signUpDate = new Date();
        account.lastLoginDate = new Date();
        account.confirmed = true;
        account.confirmedByEmail = false;
        account.disabled = false;
        account.consecutiveFailedLogins = 0;
        account.agreedToTermsOfUse = new Date();
        accountDAO.create(datastore, account);
        return account;
    }

    private Study createStudy(String name, boolean availableToPublic) {
        Study study = new Study();
        study.studyType = StudyType.DAILY;
        study.author = name + " Van Valin";
        study.copyright = "@2015 by GroupDevotions.com";
        study.title = (availableToPublic ? "Public" : "Private") + " Study By " + name;
        study.purpose = "Daily thoughtful stuff " + ((int) Math.random()*100);
        study.publicStudy = availableToPublic;
        studyDAO.create(datastore, study);
        createStudyLessonForToday(study, 1, 1);
        for (int day = 1; day < 31; day++ ) {
            createStudyLessonForToday(study, 9, day);
        }
        for (int day = 1; day < 32; day++ ) {
            createStudyLessonForToday(study, 10, day);
        }
        createAccountabilityLesson(study);
        return study;
    }

    private Study createBibleStudy(boolean availableToPublic) {
        Study study = new Study();
        study.studyType = StudyType.BIBLE;
        //study.author = name + " Van Valin";
        study.copyright = "@2015 by GroupDevotions.com";
        study.title = "Bible Reading Plan";
        study.purpose = "Daily Reading for Charis";
        study.publicStudy = availableToPublic;
        study.studyByDate = false;
        study.dailyReadingList = "1 John 1; Romans 1\n1 John 2; Romans 2\n1 John 3; Romans 3";
        study.dailyReadingStartingMonthDay = ServerUtils.formatDate(ServerUtils.dateAddDays(ServerUtils.removeTime(new Date()), 0),"MM/dd");

        studyDAO.create(datastore, study);

        return study;
    }

    private Study createRssStudy() {
        Study study = new Study();
        study.studyType = StudyType.RSS;
        study.author = "Pastor Kevin Berry";
        study.copyright = "Provided via the <a target=\"_blank\" href=\"http://www.mounthopechurch.org/SecretPlace.aspx\">Secret Garden RSS Feed";
        study.title = "Secret Place";
        study.purpose = "Daily Devotional from Mt Hope Church Lansing";
        study.rssUrl = "http://www.mounthopechurch.org/DesktopModules/LiveBlog/Handlers/Syndication.ashx?mid=762&PortalId=1&tid=172&ItemCount=20";
        study.publicStudy = true;
        studyDAO.create(datastore, study);
        //createStudyLessonForToday(study);
        createAccountabilityLesson(study);
        return study;
    }

    private StudyLesson createStudyLessonForToday(Study study, int month, int day) {
        StudyLesson studyLesson = new StudyLesson();
        studyLesson.studyKey = study.key;
        studyLesson.day = day;
        studyLesson.month = month;
        studyLesson.devotionPageTagLine = "Now Is The Time";
        studyLesson.title = "Title for " + studyLesson.month + "/" + studyLesson.day;

        StudySection studySection = new StudySection();
        studySection.type = SectionType.SCRIPTURE;
        studySection.content = "Galatians 6:1-3 (NLT)\n" +
                "\n" +
                "6 Dear brothers and sisters, if another believer is overcome by some sin, you who are godly should gently and humbly help that person back onto the right path. And be careful not to fall into the same temptation yourself. 2 Share each other’s burdens, and in this way obey the law of Christ. 3 If you think you are too important to help someone, you are only fooling yourself. You are not that important.";
        studySection.rawHtml = false;
        studyLesson.studySections.add(0, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.DIALOG;
        studySection.content = "In my opinion, the best movie about Vietnam is 'We Were Soldiers' starring Mel Gibson. " +
                "\n" +
                "\n" +
                "He plays lieutenant general Hal Moore who commanded the first major battle of that war known as the Battle of Ia Drang.";
        studySection.rawHtml = false;
        studyLesson.studySections.add(1, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.QUOTE;
        studySection.content = "He knew their strategies and the angles they would take to attack. " +
                "\n" +
                "\n" +
                "This enabled him to position his forces in places that gave the greatest chance of victory.";
        studySection.rawHtml = false;
        studyLesson.studySections.add(2, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.YESNO_QUESTION;
        studySection.content = "Do you like this devotional?";
        studySection.rawHtml = false;
        studyLesson.studySections.add(3, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.TEXT_QUESTION;
        studySection.content = "Personal observations about the devotional?";
        studySection.rawHtml = false;
        studyLesson.studySections.add(4, studySection);

        studyLessonDAO.create(datastore, studyLesson);
        study.addStudyLessonInfo(studyLesson);
        studyDAO.update(datastore, study, KeyFactory.stringToKey(study.key));
        return studyLesson;
    }

    private StudyLesson createAccountabilityLesson(Study study) {
        StudyLesson studyLesson = new StudyLesson();
        studyLesson.studyKey = study.key;
        studyLesson.title = "Accountability (displays at the end of each lesson)";

        StudySection studySection = new StudySection();
        studySection.type = SectionType.QUOTE;
        studySection.content = "These questions are sent to your accountability partner's email address and your email address (if you have accountability configured).";
        studySection.rawHtml = false;
        studyLesson.studySections.add(0, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.DIALOG;
        studySection.content = "Please be honest in your answers.  This is for you.";
        studySection.rawHtml = false;
        studyLesson.studySections.add(1, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.YESNO_QUESTION;
        studySection.content = "Have you done your devotions today?";
        studySection.rawHtml = false;
        studyLesson.studySections.add(2, studySection);

        studySection = new StudySection();
        studySection.type = SectionType.TEXT_QUESTION;
        studySection.content = "What have you done to connect to the Lord today?";
        studySection.rawHtml = false;
        studyLesson.studySections.add(3, studySection);

        studyLessonDAO.create(datastore, studyLesson);
        study.accountabilityLessonKey = studyLesson.key;
        studyDAO.update(datastore, study, KeyFactory.stringToKey(study.key));
        return studyLesson;
    }

    private GroupMemberLessonAnswer createGroupMemberLessonAnswerForStudy(Account account, Group group, Study study) {
        GroupMemberLessonAnswer groupMemberLessonAnswer = new GroupMemberLessonAnswer();
        groupMemberLessonAnswer.accountKey = account.key;
        groupMemberLessonAnswer.groupKey = group.key;
        groupMemberLessonAnswer.studyLessonKey = study.studyLessonInfos.get(0).studyLessonKey;
        groupMemberLessonAnswer.postedOn = new Date();
        groupMemberLessonAnswer.answers.add(new EnhancedString("Y"));
        groupMemberLessonAnswer.answers.add(new EnhancedString("I like it."));
        groupMemberLessonAnswerDAO.create(datastore, groupMemberLessonAnswer);
        return groupMemberLessonAnswer;
    }

    private Group createGroup(Study study, Account ownerAccount) {
        Group group = new Group();
        group.blogInstructions = "Please share how you are doing today.  Concerns, praises and encouragement are also helpful to group members.";
        group.description = "Sample Group";
        group.inviteEmailBody = "Dan has invited you to join him in a daily online devotional and private blog. Click on this link to create an account and join him at www.groupdevotions.com <link>";
        group.studyKey = study.key;
        groupDAO.create(datastore, group);
        return group;
    }

    private void addActivityToGroup(Group group, Account noTerms, Account google, Account local) {
        addActivity(group, noTerms, -3);
        addActivity(group, google, -5);
        addActivity(group, local, -1);

        groupDAO.update(datastore, group, groupDAO.getKey(datastore, group));
    }

    private GroupMember createGroupMember(Account account, String name, Group group) {
        GroupMember groupMember = new GroupMember();
        groupMember.groupKey = group.key;
        groupMember.accountKey = account.key;
        groupMember.accountabilityEmails = Lists.newArrayList("groupmember@yahoo.com");
        groupMember.email = account.email;
        groupMember.name = name;
        groupMember.status = GroupMemberStatus.JOINED;
        groupMember.groupAdmin = true;
        groupMember.lastAccountabilityDateAsString = "20150127";
        groupMember.lastCompletedDateAsString = "20130418";
        groupMember.sendAccountabilityEmailsToMe = false;
        groupMemberDAO.create(datastore, groupMember);
        return groupMember;
    }

    private GroupMember createGroupMemberInvite(String email, String name, Group group) {
        GroupMember groupMember = new GroupMember();
        groupMember.groupKey = group.key;
        groupMember.accountKey = null;
        groupMember.accountabilityEmails = Lists.newArrayList("groupmember@yahoo.com");
        groupMember.email = email;
        groupMember.name = name;
        groupMember.status = GroupMemberStatus.EMAILED;
        groupMember.groupAdmin = false;
        groupMember.sendAccountabilityEmailsToMe = false;
        groupMemberDAO.create(datastore, groupMember);
        return groupMember;
    }

    private Journal createJournal(Account account, Date date) {
        Journal journal = new Journal();
        journal.accountKey = account.key;
        journal.content = "This is a journal with content for date " + date + " and all the cool things that happened.\n\n" +
                "  * A happened.\n" +
                "  * B happnened.\n" +
                " And so on.";
        journal.forDay = date;
        journalDAO.create(datastore, journal);
        return journal;
    }

    private void buildGroupBlogs(GroupMember groupMember, GroupMember groupMember2) {
        GroupBlog groupBlog = createGroupBlog(groupMember, "20150301082301000-0400", "This is a post for 3/1/2015 at 8:23:01am EST and it is not too long.\n\nAnd this is a line that goes below.");
        addBlogEntry(groupMember2, groupBlog, "20150301082301000-0400", "This is a second post on 3/1/2015.");
        groupBlogDAO.create(datastore, groupBlog);

        groupBlog = createGroupBlog(groupMember, "20150303082301000-0400", "This is a post for 3/3/2015 at 8:23:01am EST and it is not too long.");
        groupBlogDAO.create(datastore, groupBlog);

        groupBlog = createGroupBlog(groupMember, "20150305082301000-0400", "This is a post for 3/5/2015 at 8:23:01am EST and it is not too long.");
        addBlogEntry(groupMember2, groupBlog, "20150305082301000-0400", "This is a second post on 3/5/2015.");
        groupBlogDAO.create(datastore, groupBlog);

        groupBlog = createGroupBlog(groupMember2, "20150308082301000-0400", "This is a post for 3/8/2015 at 8:23:01am EST and it is not too long.");
        groupBlogDAO.create(datastore, groupBlog);

        Date may2015 = ServerUtils.parseFullDateTime("20150308082301000-0400");
        for (int i=10; i<25; i++) {
            groupBlog = createGroupBlog(groupMember2, ServerUtils.formatFullDateTime(ServerUtils.dateAddDays(may2015, -i)), "This is a post for 2/" + i + "/2015 at 8:23:01am EST and it is not too long.");
            groupBlogDAO.create(datastore, groupBlog);
        }
    }

    private GroupBlog createGroupBlog(GroupMember groupMember, String stringDate, String content) {
        Date date = ServerUtils.parseFullDateTime(stringDate);  //yyyyMMddHHmmssSSSZ
        GroupBlog groupBlog = new GroupBlog();
        groupBlog.groupKey = groupMember.groupKey;
        groupBlog.blogDate = ServerUtils.removeTime(date);
        addBlogEntry(groupMember, groupBlog, stringDate, content);
        return groupBlog;
    }

    private void addBlogEntry(GroupMember groupMember, GroupBlog groupBlog, String stringDate, String content) {
        BlogEntry blogEntry = new BlogEntry();
        blogEntry.content = content;
        blogEntry.groupMemberKey = groupMember.key;
        blogEntry.name = groupMember.name;
        blogEntry.postedOn = ServerUtils.parseFullDateTime(stringDate);
        groupBlog.blogEntries.add(blogEntry);
    }
}
