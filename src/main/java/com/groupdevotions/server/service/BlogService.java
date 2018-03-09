package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.*;
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

public class BlogService {
    protected static final Logger logger = Logger.getLogger(BlogService.class.getName());

    private final GroupBlogDAO groupBlogDAO;
    private final GroupMemberDAO groupMemberDAO;
    private final GroupDAO groupDAO;
    private final AccountDAO accountDAO;
    private final ConfigDAO configDAO;

    @Inject
    public BlogService(GroupBlogDAO groupBlogDAO, GroupMemberDAO groupMemberDAO, GroupDAO groupDAO, AccountDAO accountDAO, ConfigDAO configDAO) {
        this.groupBlogDAO = groupBlogDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.groupDAO = groupDAO;
        this.accountDAO = accountDAO;
        this.configDAO = configDAO;
    }

    public Collection<GroupBlog> read(ObjectDatastore datastore, String groupMemberKey, Date lastDateAlreadyFetched) {
        GroupMember groupMember = groupMemberDAO.read(datastore, groupMemberKey);
        ArrayList<GroupBlog> groupBlogs = new ArrayList<GroupBlog>();

        Date today = ServerUtils.removeTime(new Date());
        Date startAfterThisDate = lastDateAlreadyFetched;
        if (lastDateAlreadyFetched == null) {
            startAfterThisDate = ServerUtils.dateAddDays(today, 1);
        }

        groupBlogs.addAll(groupBlogDAO.readFirst(datastore, groupMember.groupKey, startAfterThisDate));
        addMissingStandardGroupBlogs(groupMember, lastDateAlreadyFetched, groupBlogs, today);
        sortGroupBlogsNewestToOldest(groupBlogs);
        if (lastDateAlreadyFetched == null) {
            setGroupMembersBlogEntriesModifiableForToday(groupMemberKey, groupBlogs.get(0));
        }
        return addNonStoredFields(groupBlogs);
    }

    private void setGroupMembersBlogEntriesModifiableForToday(String groupMemberKey, GroupBlog groupBlog) {
        for (BlogEntry blogEntry : groupBlog.blogEntries) {
            blogEntry.modifiable = (groupMemberKey.equals(blogEntry.groupMemberKey));
        }
    }

    private void sortGroupBlogsNewestToOldest(ArrayList<GroupBlog> groupBlogs) {
        Comparator<GroupBlog> c = new Comparator<GroupBlog>() {
            @Override
            public int compare(GroupBlog arg0, GroupBlog arg1) {
                return - arg0.blogDate.compareTo(arg1.blogDate);
            }
        };
        Collections.sort(groupBlogs, c);
    }

    private Collection<GroupBlog> addNonStoredFields(ArrayList<GroupBlog> groupBlogs) {
        return Collections2.transform(groupBlogs, new Function<GroupBlog, GroupBlog>() {
            @Override
            public GroupBlog apply(GroupBlog groupBlog) {
                groupBlog.populateNonStoredFields();
                return groupBlog;
            }
        });
    }

    private void addMissingStandardGroupBlogs(GroupMember groupMember, Date olderThan, ArrayList<GroupBlog> groupBlogs, Date today) {
        if (olderThan == null) {
            if (!GroupBlog.find(groupBlogs, today)) {
                addEmptyGroupBlog(groupMember, groupBlogs, today);
            }
        }
    }

    private void addEmptyGroupBlog(GroupMember groupMember, ArrayList<GroupBlog> groupBlogs, Date forDate) {
        GroupBlog groupBlogForToday = new GroupBlog();
        groupBlogForToday.blogDate = forDate;
        groupBlogForToday.groupKey = groupMember.groupKey;
        groupBlogs.add(groupBlogForToday);
    }

    public BlogEntry save(ObjectDatastore datastore, Account account, BlogEntry blogEntry) {
        boolean revised = false;
        GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
        blogEntry.groupMemberKey = account.groupMemberKey;
        blogEntry.modifiable = true;
        if (blogEntry.postedOn == null) {
            // brand new entry
            blogEntry.postedOn = new Date();
            blogEntry.name = groupMember.name;
            blogEntry.populateNonStoredFields();
        } else {
            blogEntry.postedOn = new Date();
            blogEntry.name = groupMember.name;
            revised = true;
            // leave the postedOnFullDateTime alone because it will be used for the insert/find of prev entry
        }

        GroupBlog groupBlog = groupBlogDAO.readForDate(datastore, groupMember.groupKey, ServerUtils.removeTime(blogEntry.postedOn));
        if (groupBlog == null) {
            groupBlog = new GroupBlog();
            groupBlog.groupKey = groupMember.groupKey;
            groupBlog.blogDate = ServerUtils.removeTime(blogEntry.postedOn);
            groupBlogDAO.create(datastore, groupBlog);
        }
        groupBlog.insertOrUpdate(blogEntry);
        groupBlogDAO.update(datastore, groupBlog, groupBlogDAO.getKey(datastore, groupBlog));
        blogEntry.populateNonStoredFields();
        emailBlogEntry(datastore, account, blogEntry, revised);
        return blogEntry;
    }

    public GroupBlog delete(ObjectDatastore datastore, Account account, BlogEntry blogEntry) {
        GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
        GroupBlog groupBlog = groupBlogDAO.readForDate(datastore, groupMember.groupKey, ServerUtils.removeTime(blogEntry.postedOn));
        if (groupBlog != null) {
            for (BlogEntry groupBlogEntry : groupBlog.blogEntries) {
                if (groupBlogEntry.postedOn.equals(blogEntry.postedOn) && groupBlogEntry.groupMemberKey.equals(groupMember.key)) {
                    groupBlog.blogEntries.remove(groupBlogEntry);
                    groupBlogDAO.update(datastore, groupBlog, groupBlogDAO.getKey(datastore, groupBlog));
                    groupBlog.populateNonStoredFields();
                    setGroupMembersBlogEntriesModifiableForToday(account.groupMemberKey, groupBlog);
                    break;
                }
            }
        }
        return groupBlog;
    }

    public BlogData readBlogData(ObjectDatastore datastore, Account account) {
        GroupMember groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        List<GroupMemberActivity> results = Lists.newArrayList();
        for (GroupMemberActivity activity : group.groupMemberActivities) {
            activity.populateNonStoredFields();
            if (activity.image != null) {
                results.add(activity);
            }
        }

        BlogData blogData = new BlogData();
        blogData.groupName = group.description;
        blogData.blogInstructions = group.blogInstructions;
        blogData.groupMemberActivities = results;

        return blogData;
    }

    public Account readAccountForAnotherGroupMember(ObjectDatastore datastore, String accountGroupMemberKey, String viewGroupMemberKey) {
        GroupMember groupMember = groupMemberDAO.read(datastore, accountGroupMemberKey);
        Group group = groupDAO.read(datastore, groupMember.groupKey);
        for (GroupMemberActivity activity : group.groupMemberActivities) {
            if (viewGroupMemberKey.equals(activity.groupMemberKey)) {
                GroupMember viewGroupMember = groupMemberDAO.read(datastore, viewGroupMemberKey);
                Account viewAccount = accountDAO.read(datastore, viewGroupMember.accountKey);
                return viewAccount.sanitizedForAnotherGroupMember();
            }
        }
        return null;
    }

    private void emailBlogEntry(ObjectDatastore datastore, Account userAccount, BlogEntry blogEntry, boolean revised) {
        try {
            GroupMember userGroupMember = groupMemberDAO.read(datastore, userAccount.groupMemberKey);
            Group group = groupDAO.read(datastore, userGroupMember.groupKey);
            Config config = configDAO.readInstance(datastore);
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            for(GroupMemberActivity activity : group.groupMemberActivities) {
                if (!activity.groupMemberKey.equals(blogEntry.groupMemberKey) && activity.getDaysSinceSeen() < 5) {
                    GroupMember groupMember = groupMemberDAO.read(datastore, KeyFactory.stringToKey(activity.groupMemberKey));
                    Account otherAccount = accountDAO.read(datastore, KeyFactory.stringToKey(groupMember.accountKey));
                    if (otherAccount.postingNotification.equals(PostingNotification.EMAIL)) {
                        Message message = buildBlogNotificationEmail(blogEntry, revised, config, session);
                        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(groupMember.email));
                        logger.info("Send blog post to " + groupMember.email + " from " + blogEntry.name);
                        Transport.send(message);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "Unable to set email address", e);
        } catch (MessagingException e) {
            logger.log(Level.WARNING, "Unable to send email", e);
        }
    }

    private Message buildBlogNotificationEmail(BlogEntry blogEntry, boolean revised, Config config, Session session) throws UnsupportedEncodingException, MessagingException {
        Message message = new MimeMessage(session);
        InternetAddress noreply = new InternetAddress(config.fromNoReplyEmailAddr, config.fromNoReplyEmailAddrDesc);
        message.setFrom(noreply);
        String subject = "Posting from " + blogEntry.name + " at GroupDevotions.com";
        String body = blogEntry.content + "\n  -- " + blogEntry.name + " " + blogEntry.formattedPostedOn + "\n\nDo not reply to this email.";
        if (revised) {
            subject += " (revised)";
            body = "(Posting has been revised.)\n\n" + body;
        }
        message.setSubject(subject);
        message.setText(body);
        return message;
    }
}
