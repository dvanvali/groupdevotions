package com.groupdevotions.server.service;

import com.google.inject.Inject;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.Config;
import com.groupdevotions.shared.model.UserInfo;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by DanV on 9/12/2016.
 */
public class ContactService {
    protected static final Logger logger = Logger
            .getLogger(AccountService.class.getName());
    private final Config config;

    @Inject
    public ContactService(ConfigService configService) {
        this.config = configService.getApplicationConfig();
    }

    public String validate(String email, String name, String phone, String request) {
        if (SharedUtils.isEmpty(email)) {
            return "Please enter your email address.";
        }
        if (!SharedUtils.validateEmails(email)) {
            return "The email address you entered is not valid.";
        }
        if (SharedUtils.isEmpty(request)) {
            return "Please enter your request.";
        }
        return null;
    }

    public void sendContactEmail(UserInfo userInfo, String email, String name, String phone, String request) {
        try {
            Properties props = new Properties();
            Session session = Session.getDefaultInstance(props, null);
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(config.fromNoReplyEmailAddr, config.fromNoReplyEmailAddrDesc));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(config.forwardContactUsEmailsTo));
            message.setSubject("Contact us request for " + config.siteUrl);
            String body = "Email: " + email + "\n";
            if (!SharedUtils.isEmpty(name)) {
                body += "Name: " + name + "\n";
            }
            if (!SharedUtils.isEmpty(phone)) {
                body += "Phone: " + phone + "\n";
            }
            body += "Request: " + request + "\n";
            if (userInfo != null && userInfo.account != null) {
                body += "Account name: " + userInfo.account.name + "\n";
                body += "Account email: " + userInfo.account.email + "\n";
                body += "Account key: " + userInfo.account.key + "\n";
            }
            message.setText(body);
            Transport.send(message);
            logger.info("------------------------\nEmail body: " + body);
        } catch (Exception e) {
            logger.warning("Unable to send email to " + email + " " + e);
        }
    }

}
