package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.code.twig.ObjectDatastore;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.*;
import com.groupdevotions.server.logic.StudyLogic;
import com.groupdevotions.server.logic.StudyLogicFactory;
import com.groupdevotions.server.rest.Response;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.server.util.VerifyRecaptcha;
import com.groupdevotions.shared.model.*;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

public class AccountService {
	protected static final Logger logger = Logger
			.getLogger(AccountService.class.getName());
	private final AccountDAO accountDAO;
	private final StudyDAO studyDAO;
	private final GroupDAO groupDAO;
	private final GroupMemberDAO groupMemberDAO;
	private final StudyContributorDAO studyContributorDAO;
	private final OrganizationDAO organizationDAO;
	private final ConfigService configService;
	private final Config config;
	private final StudyLogicFactory studyLogicFactory;
	private final StudyService studyService;

	@Inject
	public AccountService(AccountDAO accountDAO, StudyDAO studyDAO, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO,
						  StudyContributorDAO studyContributorDAO, OrganizationDAO organizationDAO,
						  ConfigService configService, StudyLogicFactory studyLogicFactory, StudyService studyService) {
		this.accountDAO = accountDAO;
		this.studyDAO = studyDAO;
		this.groupDAO = groupDAO;
		this.groupMemberDAO = groupMemberDAO;
		this.studyContributorDAO = studyContributorDAO;
		this.organizationDAO = organizationDAO;
		this.configService = configService;
		this.studyLogicFactory = studyLogicFactory;
		this.studyService = studyService;
		this.config = configService.getApplicationConfig();
	}

	public Account googleLogin(ObjectDatastore datastore, User user) {
		Account account = accountDAO.readByUserId(datastore, user.getUserId());
		
		if (account == null) {
			account = findAccountByEmail(datastore, user.getEmail().toLowerCase().trim());
			if (account == null) {
				account = createAccountForPublicUser(datastore, user.getEmail().toLowerCase().trim(), user.getUserId());
			} else {
				convertLocalAccountToGoogle(datastore, user, account);
			}
		}
		processGroupInvitationIfNotInGroup(datastore, account);

		return account;
	}

	private void convertLocalAccountToGoogle(ObjectDatastore datastore, User user, Account account) {
		account.userId = user.getUserId();
		account.password = null;
		accountDAO.update(datastore, account, KeyFactory.stringToKey(account.key));
	}

	public Account findAccountByEmail(ObjectDatastore datastore, String email) {
		return accountDAO.readByEmail(datastore, email);
	}

	public String loginNonGoogleAccount(ObjectDatastore datastore, Account account, String plainTextPassword, String requestingUrl) {
		String message = null;

		if (account == null) {
			message = "Unable to find your account.";
		} else if (account.isGoogleAccount()) {
			message = "Your account is linked to your Google account.  Please click on the Google image below to login.";
		} else {
			boolean updateAccount = false;
			Date fiveMinutesAgo = ServerUtils.dateAddMinutes(new Date(), -5);
			if (account.confirmedByEmail
					&& account.consecutiveFailedLogins >= config.failedLoginCountBeforeLocked
					&& (account.lastLoginFailureDate == null || account.lastLoginFailureDate.after(fiveMinutesAgo))) {
				message = "Your account is locked for five minutes because too many attempts to login have failed.  Please reset your password using the \"forgot\" link below or wait five minutes and try again.";
			} else if (!ServerUtils.hashPassword(account.userId, plainTextPassword)
					.equals(account.password)) {
				message = "Your password is incorrect.  Please try again or click the \"forgot\" link.";
				account.consecutiveFailedLogins++;
				account.lastLoginFailureDate = new Date();
				updateAccount = true;
			} else {
				if (account.consecutiveFailedLogins > 0) {
					account.consecutiveFailedLogins = 0;
					account.lastLoginFailureDate = null;
					updateAccount = true;
				}
				String token = getToken("newaccount", requestingUrl);
				if (token != null) {
					logger.info("Newaccount token " + token);
				}
				if (account.confirmedByEmail
						|| account.userId.equals(token)
						|| account.userId.equals(ServerUtils.urlDecode(token))) {
					// user is logged in and confirmed!
					if (!account.confirmedByEmail) {
						account.confirmedByEmail = true;
						updateAccount = true;
					}
					processGroupInvitationIfNotInGroup(datastore, account);
				} else {
					message = "You need to confirm your email address by finding the email sent to you when you signed up and clicking on the link on the email.  "
							+ "If you can't find the email, you may sign-up again and a second email will be sent to you.";
				}
			}
			if (updateAccount) {
				accountDAO.update(datastore, account,
						KeyFactory.stringToKey(account.key));
			}
		}
		return message;
	}

	public Account getMobileAccount(ObjectDatastore datastore, String requestingUrl) {
		String email = requestingUrl.substring(requestingUrl.indexOf("email=") + 6, requestingUrl.indexOf("&"));
		Account account = accountDAO.readByEmail(datastore, email);
		return account;
	}

	public String verifyNonce(ObjectDatastore datastore, Account account, String requestingUrl) {
		String mobileToken = requestingUrl.substring(requestingUrl.indexOf("mobileNonce=")+12);
		if (account == null) {
			// todo These have no way to display on the home page
			return "Your account does not exist.";
		}
		Date now = new Date();
		if (account.mobileNonceExpirationDate == null || now.after(account.mobileNonceExpirationDate)) {
			return "The token has expired.  Please try closing the application and run it again.";
		}
		if (!account.mobileNonce.equals(mobileToken)) {
			return "The token is not valid.";
		}
		return null;
	}

	public UserInfo loginUsingMobileNonce(ObjectDatastore datastore, Account account) {
		// todo no checks on locked account?
		UserInfo userInfo = new UserInfo();
		userInfo.isSignedIn = true;
		userInfo.account = account;
		processGroupInvitationIfNotInGroup(datastore, account);
		return userInfo;
	}

	private Account createAccountForPublicUser(ObjectDatastore datastore, String email, String userId) {
		logger.info("Creating account");
		Account account = new Account();
		account.userId = userId;
		account.email = email.toLowerCase().trim();
		account.siteAdmin = false;

		account.signUpDate = new Date();
		account.lastLoginDate = new Date();
		account.confirmed = false;
		account.disabled = false;
		accountDAO.create(datastore, account);
		return account;
	}

	public boolean refreshLogin(ObjectDatastore datastore, UserInfo userInfo) {
		boolean loggedIn = false;
		UserService userService = UserServiceFactory.getUserService();
		if (userInfo != null && userInfo.account != null && userInfo != null && userInfo.isSignedIn) {
			userInfo.account = accountDAO.readByUserId(datastore, userInfo.account.userId, userInfo.account.email);
			if (!userInfo.account.disabled) {
				if (userInfo.account.isGoogleAccount()) {
					if (userService.isUserLoggedIn() && userService.getCurrentUser().getUserId().equals(userInfo.account.userId)) {
						// user is still logged in as google user
						loggedIn = true;
					}
				} else {
					loggedIn = true;
				}
			}
		}

		return loggedIn;
	}

	public String forgotYourPassword(ObjectDatastore datastore, Account account) {
		String message = null;

		if (account == null) {
			message = "Unable to find your account.";
		} else if (account.isGoogleAccount()) {
			message = "Your account is a Google account.  You will need to go to your Google account to reset the password.  " +
					"One way to do this is to click on the 'Sign In with Google' button the Login page.";
		} else {
			account.resetToken = String.valueOf(Math.round(Math.random()*99999999999999d));
			account.resetExpiration = ServerUtils.dateAddMinutes(new Date(), config.resetPasswordExpirationMinutes);
			String subject = "GroupDevotions Password Reset";
			String body = "To reset your password for your GroupDevotions account, click on this link or paste it into your browser: " +
					config.siteUrl + "#resetToken=" + account.resetToken +
					"  This link will be good for one hour.";
			if (ServerUtils.sendEmail(account.email, subject, body)) {
				accountDAO.update(datastore, account, KeyFactory.stringToKey(account.key));
			} else {
				message = "Something went wrong attempting to send an email to your email account.";
			}
    		accountDAO.update(datastore, account,
						KeyFactory.stringToKey(account.key));
		}
		return message;
	}

	public String resetPassword(ObjectDatastore datastore, Account account, String password1, String password2, String requestingUrl) {
		String errorMessage = validateResetToken(account, requestingUrl);
		if (errorMessage == null) {
			errorMessage = changePassword(datastore, account.key, password1, password2);
		}
		return errorMessage;
	}

	private String validateResetToken(Account account, String requestingUrl) {
		if (account == null) {
			return "Unable to find your account.  Check your email address.";
		}
		if (requestingUrl == null || account.resetToken == null || !requestingUrl.contains(account.resetToken)) {
			return "Unable to validate your password request.  If you have a more recent email, try that one.";
		}
		if (account.resetExpiration == null || account.resetExpiration.before(new Date())) {
			return "The email has expired.  You can start over with the 'Forgot Your Password' link.  It is located on the login page.";
		}
		return null;
	}

	public Response checkRequestingUrl(String url) {
		UserInfo userInfo = new UserInfo();
		userInfo.googleSignInUrl = UserServiceFactory.getUserService().createLoginURL(url) + "?" + Math.random();
		Response response = new Response(userInfo);

		if (url != null) {
			if (url.contains("resetToken")) {
				response = new Response(true, userInfo, null, null, Response.LocationType.resetPassword);
			} else if (url.contains("groupInvite") || url.contains("newaccount")) {
				response = new Response(true, userInfo, null, null, Response.LocationType.login);
			}
		}
		return response;
	}

	private void addPrivateGroupStudiesForAccount(ObjectDatastore datastore, Account account, Collection<Study> studies) {
		Collection<GroupMember> groupMemberships = groupMemberDAO.readMyGroupMembers(datastore, account.key);
		for (GroupMember groupMember : groupMemberships) {
			Group group = groupDAO.read(datastore, groupMember.groupKey);
			Study study = studyDAO.read(datastore, group.studyKey);
			if (!studies.contains(study)) {
				studies.add(study);
			}
		}
	}

	public String validateSettings(ObjectDatastore datastore, Account settings, Group group) {
		String message = null;

		if (SharedUtils.isEmpty(settings.name)) {
			return "Please enter your name.";
		}

		if (group != null) {
			if (group.studyKey == null && (settings.studyKeyPublicAccepts == null || settings.studyKeyPublicAccepts.isEmpty())) {
				return "Please select at least one study.";
			}
		}

		boolean keepingTrackOfReading = false;
		for (String settingStudyKey : settings.studyKeyPublicAccepts) {
			Study study = studyDAO.read(datastore, settingStudyKey);
			if (StudyType.BIBLE.equals(study.studyType) && !SharedUtils.isEmpty(study.dailyReadingStartingMonthDay) && !study.dailyReadingStartsEachMonth) {
				if (keepingTrackOfReading) {
					return "You may only select one reading plan that keeps track of your progress.  Please remove one reading plan.";
				}
				keepingTrackOfReading = true;
			}
		}
		return message;
	}

	public Account saveSettings(ObjectDatastore datastore, String accountKey, Account settings, UserInfo userInfo, Group group) {
		Account account = accountDAO.read(datastore, accountKey);

		account.settingsConfirmed = true;
		account.postingNotification = settings.postingNotification;
		account.acceptTexts = settings.acceptTexts;
		account.name = settings.name;
		account.phone = settings.phone;
		account.screenFormat = settings.screenFormat;
		account.studyKeyPublicAccepts = validateStudyKeyPublicAccepts(datastore, settings, account, group);
		if (!SharedUtils.safeEquals(account.groupMemberKey, settings.groupMemberKey)) {
			// during initial signup, the account gets updated with the newly created groupMember
			GroupMember groupMember = groupMemberDAO.read(datastore, settings.groupMemberKey);
			if (SharedUtils.safeEquals(groupMember.accountKey, accountKey)) {
				account.groupMemberKey = settings.groupMemberKey;
				userInfo.groupMember = groupMember;
			}
		} else {
			if (group.isPrivacyAvailable()) {
				account.privacy = settings.privacy;
			}
		}

		accountDAO.update(datastore, account, accountDAO.getKey(datastore, account));

		return account;
	}

	public Account updateAccountForOrganization(ObjectDatastore datastore, Account settings) {
		Account account = accountDAO.read(datastore, settings.key);
		if (!account.key.equals(settings.key)) {
			throw new IllegalStateException("Account keys do not match");
		}

		// Seems like things like disabling the account might be useful.
		account.adminOrganizationKey = settings.adminOrganizationKey;
		accountDAO.update(datastore, account);

		return account;
	}

	private Collection<String> validateStudyKeyPublicAccepts(ObjectDatastore datastore, Account settings, Account account, Group group) {
		Collection<String> studyKeyPublicAccepts = Lists.newArrayList();
		Collection<Study> publicStudies = null;

		for (String settingStudyKey : settings.studyKeyPublicAccepts) {
			// If a study is already accepted, keep it because it could have been added manually if not public
			if (account.studyKeyPublicAccepts.contains(settingStudyKey)) {
				studyKeyPublicAccepts.add(settingStudyKey);
			} else {
				// If it is in the public studies, add it.
				if (publicStudies == null) {
					publicStudies = studyService.readPublicStudies(datastore);
				}
				if (group != null && group.ownerOrganizationKey != null) {
					publicStudies.addAll(studyDAO.readForOrganization(datastore, group.ownerOrganizationKey));
				}
				for (Study possibleStudy : publicStudies) {
					if (SharedUtils.safeEquals(possibleStudy.key, settingStudyKey) && !studyKeyPublicAccepts.contains(settingStudyKey)) {
						studyKeyPublicAccepts.add(settingStudyKey);
					}
				}
			}
		}
		return studyKeyPublicAccepts;
	}

	public String changePassword(ObjectDatastore datastore, String accountKey, String password1, String password2) {
		String validationError = validatePassword(password1, password2);
		if (validationError == null) {
			Account account = accountDAO.read(datastore, accountKey);
			if (!account.isGoogleAccount()) {
				account.password = ServerUtils.hashPassword(account.userId, password1);
				account.consecutiveFailedLogins = 0;
				account.confirmedByEmail = true;
				account.resetToken = null;
				accountDAO.update(datastore, account, accountDAO.getKey(datastore, account));
			} else {
				validationError = "Your account password is managed by your google account.  GroupDevotions can't access or change it.";
			}
		}
		return validationError;
	}

	private String validatePassword(String password1, String password2) {
		if (Strings.isNullOrEmpty(password1) || Strings.isNullOrEmpty(password2)) {
			return "Please enter your new password in both password fields.";
		} else if (Strings.isNullOrEmpty(password1) || Strings.isNullOrEmpty(password2) || !password1.equals(password2)) {
			return "The two passwords must be the same.";
		} else if (password1.length() < 6) {
			return "The password must be at least six characters.";
		}
		return null;
	}

	public UserInfo buildLoggedInUserInfo(ObjectDatastore datastore, Account account) {
		populateStudyContributorsForAccount(datastore, account);

		UserInfo userInfo = new UserInfo();
		userInfo.isSignedIn = true;
		userInfo.account = account;
		if (!Strings.isNullOrEmpty(account.groupMemberKey)) {
			userInfo.groupMember = groupMemberDAO.read(datastore, account.groupMemberKey);
			Group group = groupDAO.read(datastore, userInfo.groupMember.groupKey);
			account.privacyAvailableInSettings = group.isPrivacyAvailable();
		}
		return userInfo;
	}

	private void populateStudyContributorsForAccount(ObjectDatastore datastore, Account account) {
		if (account.siteAdmin) {
			// Site admins get access to all studies
			for (Study study : studyDAO.readAll(datastore)) {
				StudyContributor studyContributor = new StudyContributor();
				studyContributor.studyAdmin = true;
				studyContributor.studyKey = study.key;
				studyContributor.accountKey = account.key;
				account.studyContributors.add(studyContributor);
			}
		} else {
			Collection<StudyContributor> contributors = studyContributorDAO.readByAccountKey(datastore, account.key);
			if (contributors != null) {
				account.studyContributors.addAll(contributors);
			}
		}
	}

	public String validateUpdateAccountForOrganization(ObjectDatastore datastore, Account newAccount) {
		String errorMessage = validateAccountForOrganization(datastore, newAccount);
		if (errorMessage != null) {
			return errorMessage;
		}

		// Todo support email change?  If so, lookup by userId and then check to see if an account already exists.
		// Then reset confirmation and send another email.

		return null;
	}

	public String validateAccountForOrganization(ObjectDatastore datastore, Account newAccount) {
		if (Strings.isNullOrEmpty(newAccount.email)) {
			return "Please enter an email address.";
		}
		if (!SharedUtils.isValidEmail(newAccount.email)) {
			return "The email address you entered is not valid.";
		}
		if (Strings.isNullOrEmpty(newAccount.name)) {
			return "Please a name.";
		}
		return null;
	}

	public String validateCreateAccountForOrganization(ObjectDatastore datastore, Account newAccount) {
		String errorMessage = validateAccountForOrganization(datastore, newAccount);
		if (errorMessage != null) {
			return errorMessage;
		}
		Account account = accountDAO.readByEmail(datastore, newAccount.email);
		if (account != null) {
			if (SharedUtils.safeEquals(newAccount.adminOrganizationKey, account.adminOrganizationKey)) {
				return "This account is already an administrator for your organization.";
			}
			if (account.adminOrganizationKey != null) {
				return "This account is already an administrator for a different organization.";
			}
    		return "An account already exists for this email address.";
		}
		return null;
	}

	public String validateCreateAccountForPublicUser(ObjectDatastore datastore, String email, String password1, String password2, String name, String requestingUrl, String recaptcha) {
		if (Strings.isNullOrEmpty(email)) {
			return "Please enter your email address.";
		}
		Account account = accountDAO.readByEmail(datastore, email);
		if (account != null) {
			if (account.isGoogleAccount()) {
				return "You already have an account linked to your Google account.  Please login using the 'Sign In with Google' button.";
			}
			if (account.confirmedByEmail) {
				return "You already have an account.  Please cancel and try to login.  If you have forgotten your password, click the 'Forgot your password?' link.";
			} else {
				sendNewAccountConfirmationEmail(account);
				account.password = ServerUtils.hashPassword(account.userId, password1);
				accountDAO.update(datastore, account, accountDAO.getKey(datastore, account));
				return "You already have an account.  Another confirmation email has been sent, please click on the link in this email to finish the registration process.";
			}
		}
		String errorMessage = validateGroupInvite(datastore, email, requestingUrl);
		if (errorMessage != null) {
			return errorMessage;
		}
		if ((requestingUrl == null || !requestingUrl.contains("groupInvite=")) && Strings.isNullOrEmpty(name)) {
			return "Please enter your name.";
		}
		errorMessage = validatePassword(password1, password2);
		if (errorMessage != null) {
			return errorMessage;
		}
		if (SharedUtils.isEmpty(requestingUrl) || !requestingUrl.contains("groupInvite")) {
			if (SharedUtils.isEmpty(recaptcha) && !config.development) {
				return "Please click the recaptcha checkbox.";
			}
			try {
				boolean success = VerifyRecaptcha.verify(recaptcha, config.recaptchaSecret);
				if (!success && !config.development) {
					return "Your recaptcha response was not valid.  Please try again.";
				}
			} catch (IOException e) {
				return "Unable to communicate with the recaptcha validation service.";
			}
		}
		return null;
	}

	public String validateCreateAccountForMobileUser(String email, String deviceUuid) {
		if (Strings.isNullOrEmpty(email)) {
			return "Please enter your email address.";
		}
		if (Strings.isNullOrEmpty(deviceUuid)) {
			throw new UnsupportedOperationException();
		}
		if (!SharedUtils.isValidEmail(email)) {
			return "Your email address is not valid.  Please double check it.";
		}
		return null;
	}

	private String validateGroupInvite(ObjectDatastore datastore, String email, String requestingUrl) {
		String groupMemberEmail = getToken("email", requestingUrl);
		String decodeGroupMemberEmail = ServerUtils.urlDecode(groupMemberEmail);
		String groupMemberKey = getToken("groupInvite", requestingUrl);
		String decodeGroupMemberKey = ServerUtils.urlDecode(groupMemberKey);
		if (!Strings.isNullOrEmpty(groupMemberEmail) && !Strings.isNullOrEmpty(groupMemberKey)) {
			if (!email.equals(groupMemberEmail) && !email.equals(decodeGroupMemberEmail)) {
				return "Your new account email address does not match the invitation email address.";
			} else {
				GroupMember groupMember;
				try {
					groupMember = groupMemberDAO.read(datastore, groupMemberKey);
				} catch (IllegalArgumentException e) {
					groupMember = null;
				}
				if (groupMember == null) {
					try {
						groupMember = groupMemberDAO.read(datastore, decodeGroupMemberKey);
					} catch (IllegalArgumentException e) {
						//
					}
				}
				if (groupMember == null) {
					logger.warning("Invitation no longer valid, Email: " + groupMemberEmail + " Invite GroupMemberKey: " + groupMemberKey +
							"  decodeGroupMemberEmail " + decodeGroupMemberEmail +
							"  decodeGroupMemberKey " + decodeGroupMemberKey +
							" url " + requestingUrl);
					return "Your invitation is no longer valid.";
				}
				if (groupMember.accountKey != null) {
					// This case is not expected to happen since the account existence has already been checked
					return "Your invitation has already been used to create an account.  If you forgot your password, please press the cancel button and click on the 'Forgot your password' link.";
				}
			}
		}
		return null;
	}

	public Account createAccountForOrganization(ObjectDatastore datastore, Account newAccount) {
		Account account = new Account();
		account.email = newAccount.email.toLowerCase().trim();
		account.name = newAccount.name;
		account.userId = "apx" + newAccount.email + Math.round(Math.random()*9999999999999d);
		account.confirmedByEmail = false;
		account.adminOrganizationKey = newAccount.adminOrganizationKey;
		account.resetToken = String.valueOf(Math.round(Math.random()*99999999999999d));
		account.resetExpiration = ServerUtils.dateAddMinutes(new Date(), 60*24*7);
		accountDAO.create(datastore, account);
		sendNewAccountAdminInviteEmail(datastore, account);
		return account;
	}

	public Account createAccountForPublicUser(ObjectDatastore datastore, String email, String password1, String name, String requestingUrl) {
		Account account = new Account();
		account.email = email.toLowerCase().trim();
		account.name = name;
		account.signUpDate = new Date();
		account.userId = "apx" + email + Math.round(Math.random()*9999999999999d);
		account.password = ServerUtils.hashPassword(account.userId, password1);
		account.confirmedByEmail = false;
		accountDAO.create(datastore, account);
		String groupMemberKey = null;
		String groupMemberEmail = null;
		String decodeGroupMemberKey = null;
		String decodeGroupMemberEmail = null;
		if (!Strings.isNullOrEmpty(requestingUrl)) {
			groupMemberKey = getToken("groupInvite", requestingUrl);
			groupMemberEmail = getToken("email", requestingUrl);
			decodeGroupMemberKey = ServerUtils.urlDecode(groupMemberKey);
			decodeGroupMemberEmail = ServerUtils.urlDecode(groupMemberEmail);
		}
		if (!processGroupInvitation(datastore, account, groupMemberKey, groupMemberEmail)
				&& !processGroupInvitation(datastore, account, decodeGroupMemberKey, decodeGroupMemberEmail)) {
	    	sendNewAccountConfirmationEmail(account);
		} else {
			logger.info("Group invitation so email not sent to " + account.email + " groupMemberEmail " + groupMemberEmail);
		}
		return account;
	}

	public void createAccountForMobileUser(ObjectDatastore datastore, String email, String deviceUuid) {
		Account account = accountDAO.readByEmail(datastore, email);
		if (account == null) {
			account = new Account();
			account.email = email.toLowerCase().trim();
			account.signUpDate = new Date();
			account.userId = "apx" + email + Math.round(Math.random() * 9999999999999d);
			account.confirmedByEmail = false;
			accountDAO.create(datastore, account);
		}
		if (account.mobileToken == null || !account.confirmedByEmail) {
			account.mobileDeviceUuid = deviceUuid;
			account.mobileToken = (String.valueOf(Math.abs(new Random().nextLong()))+"000000").substring(0, 6);
			accountDAO.update(datastore, account);
		}
		sendNewAccountConfirmationEmailMobile(account);
	}

	private boolean processGroupInvitation(ObjectDatastore datastore, Account account, String groupMemberKey, String groupMemberEmail) {
		boolean accepted = false;
		if (!Strings.isNullOrEmpty(groupMemberEmail) && !Strings.isNullOrEmpty(groupMemberKey)) {
			GroupMember groupMember = null;
            try {
                groupMember = groupMemberDAO.read(datastore, groupMemberKey);
            } catch (Exception e) {
                // Ignore this because we are trying with two keys decoded in different ways.
            }
			if (groupMember != null && groupMember.email.equalsIgnoreCase(groupMemberEmail)) {
				account.confirmed = true;
				account.confirmedByEmail = true;
				if (account.name == null) {
					account.name = groupMember.name;
				} else {
					groupMember.name = account.name;
				}
				account.groupMemberKey = groupMember.key;
				groupMember.status = GroupMemberStatus.JOINED;
				groupMember.accountKey = account.key;
				initializeGroupData(datastore, groupMember, account);
				groupMemberDAO.update(datastore, groupMember, KeyFactory.stringToKey(groupMember.key));
				accountDAO.update(datastore, account, KeyFactory.stringToKey(account.key));
				accepted = true;
			} else {
				logger.warning("Invite Email: " + groupMemberEmail + " Invite GroupMemberKey: " + groupMemberKey +
						" the email did not match the group member in the invite.  groupMember " + groupMember +
						" groupMember.email: " + (groupMember == null ? "" : groupMember.email));
			}
		}
		return accepted;
	}

	private void initializeGroupData(ObjectDatastore datastore, GroupMember groupMember, Account account) {
		Group group = groupDAO.read(datastore, groupMember.groupKey);
		if (group.studyKey != null) {
			Study study = studyDAO.read(datastore, group.studyKey);
			StudyLogic studyLogic = studyLogicFactory.getInstance(study);
			studyLogic.initializeGroupMember(groupMember);
		}
		if (group.isPrivacyAvailable()) {
			account.privacyAvailableInSettings = true;
			account.privacy = true;
		}
	}

	private void processGroupInvitationIfNotInGroup(ObjectDatastore datastore, Account account) {
		Collection<GroupMember> groupMembers = groupMemberDAO.readInvitations(datastore, account.email);
		if (account.groupMemberKey == null && groupMembers != null && groupMembers.size() > 0) {
			GroupMember groupMember = Iterables.get(groupMembers, 0);
			if (account.name == null) {
				account.name = groupMember.name;
			} else {
				groupMember.name = account.name;
			}
			account.groupMemberKey = groupMember.key;
			groupMember.status = GroupMemberStatus.JOINED;
			groupMember.accountKey = account.key;
			initializeGroupData(datastore, groupMember, account);
			groupMemberDAO.update(datastore, groupMember, KeyFactory.stringToKey(groupMember.key));
			accountDAO.update(datastore, account, KeyFactory.stringToKey(account.key));
		}
	}

	private void sendNewAccountAdminInviteEmail(ObjectDatastore datastore, Account account) {
		Organization organization = organizationDAO.read(datastore, account.adminOrganizationKey);
		String subject = "New GroupDevotions Administrative Account";
		String body = "A GroupDevotions account has been created for you to maintain groups and studies for " + organization.name +
				".  Please confirm your GroupDevotions account using this email address by clicking on the link below.  You may also " +
				"copy the link into a browser like Internet Explorer or Chrome.\n\n" +
				config.siteUrl + "#admin&resetToken=" + account.resetToken;

		ServerUtils.sendEmail(account.email, subject, body);
	}

	private void sendNewAccountConfirmationEmail(Account account) {
		String subject = "New GroupDevotions Account";
		String body = "Thank you for creating an account with GroupDevotions.  Please confirm that you created a " +
				"GroupDevotions account using this email address by clicking on the link below.  You may also " +
				"copy the link into a browser like Internet Explorer or Chrome.\n\n" +
				config.siteUrl + account.newAccountUrl();
		ServerUtils.sendEmail(account.email, subject, body);
	}

	private void sendNewAccountConfirmationEmailMobile(Account account) {
		String subject = "New GroupDevotions Account";
		String body = "Thank you for creating an account with GroupDevotions.  Please confirm that you created a " +
				"GroupDevotions account by entering this code in the GroupDevotions application:\n\n" +
				account.mobileToken;
		ServerUtils.sendEmail(account.email, subject, body);
	}

	@VisibleForTesting
	protected String getToken(String prefix, String url) {
		String token = "";
		if (!Strings.isNullOrEmpty(url)) {
			int start = url.indexOf(prefix);
			if (start > -1) {
				url = url.substring(start + prefix.length() + 1);
				int end = url.indexOf("&");
				if (end == -1) {
					end = url.length();
				}
				url = url.substring(0, end);
				if (!Strings.isNullOrEmpty(url)) {
					token = url;
				}
			}
		}
		return token;
	}

	public Response.LocationType determineLoginSuccessRedirectLocation(Account account) {
		Response.LocationType forwardTo = null;
		if (account.agreedToTermsOfUse == null || configService.getApplicationConfig().timestamp.after(account.agreedToTermsOfUse)) {
			forwardTo = Response.LocationType.terms;
		} else if (account.groupMemberKey == null) {
			if (account.adminOrganizationKey != null) {
				forwardTo = Response.LocationType.groups;
			} else {
				forwardTo = Response.LocationType.configureGroup;
			}
		} else if (!account.settingsConfirmed) {
			forwardTo = Response.LocationType.settings;
		}
		return forwardTo;
	}
}
