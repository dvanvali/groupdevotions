package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.org.joda.time.DateMidnight;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.*;
import com.groupdevotions.server.rest.Response;
import com.groupdevotions.shared.model.*;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

public class GroupMemberService {
    protected static final Logger logger = Logger
            .getLogger(GroupMemberService.class.getName());

    private final GroupMemberDAO groupMemberDAO;
    private final GroupDAO groupDAO;
    private final AccountDAO accountDAO;
    private final GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO;
    private final JournalDAO journalDAO;
    private final GroupMemberBlogDAO groupMemberBlogDAO;
    private final ConfigService configService;
    private final Config config;

    @Inject
    public GroupMemberService(ConfigService configService, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO, AccountDAO accountDAO, GroupMemberLessonAnswerDAO groupMemberLessonAnswerDAO, JournalDAO journalDAO, GroupMemberBlogDAO groupMemberBlogDAO) {
        this.configService = configService;
        this.groupDAO = groupDAO;
        this.groupMemberDAO = groupMemberDAO;
        this.accountDAO = accountDAO;
        this.groupMemberLessonAnswerDAO = groupMemberLessonAnswerDAO;
        this.journalDAO = journalDAO;
        this.groupMemberBlogDAO = groupMemberBlogDAO;
        config = configService.getApplicationConfig();
    }

    public GroupMember readAsGroupMember(ObjectDatastore datastore, String accountGroupMemberKey, String groupMemberKeyAttemptingToView) {
        if (groupMemberKeyAttemptingToView.equals(accountGroupMemberKey)) {
            return groupMemberDAO.read(datastore, accountGroupMemberKey);
        } else {
            return null;
        }
    }

    public Collection<GroupMember> readAsGroupMemberAdmin(ObjectDatastore datastore, String adminGroupMemberKey) {
        GroupMember adminGroupMember = groupMemberDAO.read(datastore, adminGroupMemberKey);
        return groupMemberDAO.readByGroupKey(datastore, adminGroupMember.groupKey);
    }

    public Collection<GroupMember> readAsSiteAdmin(ObjectDatastore datastore, String groupKey) {
        return groupMemberDAO.readByGroupKey(datastore, groupKey);
    }

    public Response<GroupMember> updateSettingsAsGroupMember(ObjectDatastore datastore, GroupMember groupMemberSettings) {
        String error = validateGroupMemberSettings(groupMemberSettings);
        if (error != null) {
            return new Response(error);
        }
        GroupMember groupMember = groupMemberDAO.read(datastore, groupMemberSettings.key);
        boolean accountabilityOffMessage = false;
        if ((groupMemberSettings.accountabilityEmails == null || groupMemberSettings.accountabilityEmails.isEmpty()) && !groupMember.accountabilityEmails.isEmpty()) {
            accountabilityOffMessage = true;
        }
        groupMember.updateSettingsFields(groupMemberSettings);
        groupMemberDAO.update(datastore, groupMember, groupMemberDAO.getKey(datastore, groupMember));
        if (accountabilityOffMessage) {
            return new Response(true, groupMember, Response.MessageType.info, "Accountability is now off because you have no accountability email configured.", null);
        }
        return new Response(groupMember);
    }

    public GroupMember saveOrUpdateSettingsAsAdmin(ObjectDatastore datastore, String groupKey, GroupMember groupMemberData) {
        GroupMember groupMember = null;
        boolean sendInvite = false;
        if (groupMemberData.key == null) {
            groupMemberData.groupKey = groupKey;
            Key key = groupMemberDAO.create(datastore, groupMemberData);
            groupMemberData.key = KeyFactory.keyToString(key);
            groupMember = groupMemberData;
            sendInvite = !GroupMemberStatus.JOINED.equals(groupMemberData.status);
        } else {
            groupMember = groupMemberDAO.read(datastore, groupMemberData.key);
            if (groupMember != null) {
                sendInvite = !GroupMemberStatus.JOINED.equals(groupMemberData.status)
                        && !GroupMemberStatus.DECLINED.equals(groupMemberData.status);
                groupMember.updateGroupMemberMaintenanceFieldsAsAdmin(groupMemberData);
                groupMemberDAO.update(datastore, groupMember, groupMemberDAO.getKey(datastore, groupMember));
                Group group = groupDAO.read(datastore, groupMember.groupKey);
                for (GroupMemberActivity activity : group.groupMemberActivities) {
                    if (activity.groupMemberKey.equals(groupMember.key)) {
                        activity.name = groupMember.name;
                    }
                }
                groupDAO.update(datastore, group, groupDAO.getKey(datastore, group));
            }
        }
        if (sendInvite) {
            sendInvite(datastore, groupMember);
        }
        return groupMember;
    }

    private String validateGroupMemberSettings(GroupMember groupMemberSettings) {
        for(String email : groupMemberSettings.accountabilityEmails) {
            try {
                if (!email.contains("@")) {
                    return "Each email address must contain an @ symbol.";
                } else {
                    InternetAddress internetAddress = new InternetAddress(email, true);
                    internetAddress.validate();
                }
            } catch (AddressException e) {
                return "\"" +email + "\" is not a valid email address.";
            }
        }
        return null;
    }

    public String validateGroupMember(GroupMember groupMember) {
        String error = validateGroupMemberSettings(groupMember);
        if (error == null) {
            error = validateEmailAddress(groupMember);
        }
        if (error == null) {
            error = validateName(groupMember);
        }
        return error;
    }

    private String validateEmailAddress(GroupMember groupMember) {
        if (Strings.isNullOrEmpty(groupMember.email)) {
            return "Email address is required.";
        } else {
            try {
                if (!groupMember.email.contains("@")) {
                    return "Each email address must contain an @ symbol.";
                } else {
                    InternetAddress internetAddress = new InternetAddress(groupMember.email, true);
                    internetAddress.validate();
                }
            } catch (AddressException e) {
                return "\"" + groupMember.email + "\" is not a valid email address.";
            }
        }
        return null;
    }

    private String validateName(GroupMember groupMember) {
        if (Strings.isNullOrEmpty(groupMember.name)) {
            return "Name is required.";
        }
        return null;
    }

    public void sendInvite(ObjectDatastore datastore, GroupMember serverGroupMember) {
        Group group = groupDAO.read(datastore, serverGroupMember.groupKey);
        sendEmail(config, group, serverGroupMember);
        serverGroupMember.status = GroupMemberStatus.EMAILED;
        groupMemberDAO.update(datastore, serverGroupMember, KeyFactory.stringToKey(serverGroupMember.key));
    }

    private void sendEmail(Config config, Group group, GroupMember groupMember) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.fromNoReplyEmailAddr, config.fromNoReplyEmailAddrDesc));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(groupMember.email));
            message.setSubject(group.inviteEmailSubject);
            String body = group.inviteEmailBody.replaceAll("<link>", generateLink(config, groupMember));
            message.setText(body);
            Transport.send(message);
            logger.info("------------------------\nEmail body: " + body);
        } catch (Exception e) {
            logger.warning("Unable to send email invite to " + groupMember.email + " " + e);
        }
    }

    private String generateLink(Config config, GroupMember groupMember) {
        return config.siteUrl + "#groupInvite=" + ServerUtils.urlEncode(groupMember.key) +
                "&email=" + ServerUtils.urlEncode(groupMember.email);
    }

    public String delete(ObjectDatastore datastore, UserInfo userInfo, GroupMember groupMember) {
        if (userInfo.groupMember.key.equals(groupMember.key)) {
            return "You may not delete yourself.";
        }

        // delete member
        groupMemberLessonAnswerDAO.deleteAllForGroupKey(datastore, groupMember.groupKey, groupMember.accountKey);
        groupMemberDAO.delete(datastore, KeyFactory.stringToKey(groupMember.key));
        for (GroupMemberBlog blog : groupMemberBlogDAO.readForGroupMember(datastore, groupMember.key)) {
            groupMemberBlogDAO.delete(datastore, KeyFactory.stringToKey(blog.key));
        }

        // Delete the account if the account has no journals
        if (groupMember.accountKey != null) {
            List<GroupMember> groupMemberList = groupMemberDAO.readMyGroupMembers(datastore, groupMember.accountKey);

            if (groupMemberList.size() == 0 && journalDAO.readFirst(datastore, groupMember.accountKey, null).size() == 0) {
                Key key = KeyFactory.stringToKey(groupMember.accountKey);
                Account account = accountDAO.read(datastore, key);
                if (account != null) {
                    accountDAO.delete(datastore, key);
                }
            }
        }

        return null;
    }
}
