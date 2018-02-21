package com.groupdevotions.server.rest;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.repackaged.com.google.common.base.Strings;
import com.google.appengine.repackaged.com.google.common.collect.Lists;
import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.AnnotationObjectDatastoreProvider;
import com.groupdevotions.server.ServerUtils;
import com.groupdevotions.server.dao.AccountDAO;
import com.groupdevotions.server.dao.GroupDAO;
import com.groupdevotions.server.dao.GroupMemberDAO;
import com.groupdevotions.server.service.AccountService;
import com.groupdevotions.server.service.ConfigService;
import com.groupdevotions.server.service.ContactService;
import com.groupdevotions.server.service.SecurityService;
import com.groupdevotions.shared.model.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.logging.Logger;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path("account")
public class AccountResource {
	protected static final Logger logger = Logger
			.getLogger(AccountResource.class.getName());

	private static final String COOKIE_NAME_USERID = "KEEP";
	private static final String COOKIE_NAME_TOKEN = "KEEPTOKEN";
	private static final String COOKIE_DELETED = "DELETED";

	private final AccountService accountService;
	private final ContactService contactService;
	private final SecurityService securityService;
	private final AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider;
	private final AccountDAO accountDAO;
	private final ConfigService configService;
	private final GroupDAO groupDAO;
	private final GroupMemberDAO groupMemberDAO;

	// If you redo the constructor, don't forget the config initialization
	@Inject
	public AccountResource(AccountService accountService, ContactService contactService, SecurityService securityService, AnnotationObjectDatastoreProvider annotationObjectDatastoreProvider, AccountDAO accountDAO, ConfigService configService, GroupDAO groupDAO, GroupMemberDAO groupMemberDAO) {
		this.accountService = accountService;
		this.contactService = contactService;
		this.securityService = securityService;
		this.annotationObjectDatastoreProvider = annotationObjectDatastoreProvider;
		this.accountDAO = accountDAO;
		this.configService = configService;
		this.groupDAO = groupDAO;
		this.groupMemberDAO = groupMemberDAO;

		// Initialize dev environments during startup
		configService.getApplicationConfig();
	}

	static class AccountJson {
		public String email;
		public String password;
		public String password2;
		public String name;
		public String url;
		public boolean stayLoggedIn;
		public String recaptcha;
		public String token;
		public String deviceUuid;
	}


	@GET
	@Path("/{key}")
	@Produces(APPLICATION_JSON)
	public Response<Account> get(@Context HttpServletRequest request, @PathParam("key") String accountKey) throws IOException {
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Account>  response;
		Response<Object> checkResponse = securityService.checkSiteAdmin(userInfo);
		if (checkResponse != null) {
			response = new Response<Account> (checkResponse);
		} else {
			ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
			Account account = accountDAO.read(datastore, accountKey);
			response = new Response(account);
		}
		return response;
	}


	@GET
	@Path("/")
	@Produces(APPLICATION_JSON)
	public Response<Collection<Account>> query(@Context HttpServletRequest request, @QueryParam("organizationId") String organizationId, @QueryParam("email") String email) throws IOException {
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Object> checkResponseSiteAdmin = securityService.checkOrganizationAdmin(userInfo, organizationId);
		if (checkResponseSiteAdmin != null) {
			return new Response(checkResponseSiteAdmin);
		}

		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		Collection<Account> accounts = Lists.newArrayList();
		if (organizationId != null) {
			accounts = accountDAO.readByOrganizationId(datastore, organizationId);
		} else if (email != null) {
			Account account = accountDAO.readByEmail(datastore, email);
			if (account != null) {
				accounts.add(account);
			}
		}
		return new Response(accounts);
	}

	@POST
	@Path("/")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Account> createAccountForOrganization(@Context HttpServletRequest request, @Context HttpServletResponse response,
														   Account newAccountInfo) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Object> checkResponseSiteAdmin = securityService.checkOrganizationAdmin(userInfo, newAccountInfo.adminOrganizationKey);
		if (checkResponseSiteAdmin != null) {
			return new Response(checkResponseSiteAdmin);
		}
		String errorMessage =  accountService.validateCreateAccountForOrganization(datastore, newAccountInfo);
		if (errorMessage != null) {
			return new Response(errorMessage);
		}

		Account account = accountService.createAccountForOrganization(datastore, newAccountInfo);
		return new Response(account);
	}

	@POST
	@Path("/{key}")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Organization> updateAccountForOrganization(@Context HttpServletRequest request, @PathParam("key") String accountKey, Account account) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Object> checkResponseSiteAdmin = securityService.checkOrganizationAdmin(userInfo, account.adminOrganizationKey);
		if (checkResponseSiteAdmin != null) {
			return new Response(checkResponseSiteAdmin);
		}
		String errorMessage =  accountService.validateUpdateAccountForOrganization(datastore, account);
		if (errorMessage != null) {
			return new Response(errorMessage);
		}

		Account updatedAccount = accountService.updateAccountForOrganization(datastore, account);
		return new Response(updatedAccount);
	}

	// TODO Delete - if we want to delete everything under an account.  Or particularly a dead/unused account.

	@POST
	@Path("localLogin")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<UserInfo> login(@Context HttpServletRequest request, @Context HttpServletResponse response, AccountJson loginData) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		Account account = accountService.findAccountByEmail(datastore, loginData.email);
		UserInfo userInfo = null;
		String errorMessage = accountService.loginNonGoogleAccount(datastore, account, loginData.password, loginData.url);

		if (errorMessage == null && account != null) {
			userInfo = accountService.buildLoggedInUserInfo(datastore, account);
		}

		HttpSession session = request.getSession(true);
		session.setAttribute("userInfo", userInfo);
		if (userInfo != null && userInfo.account != null && userInfo.isSignedIn) {
			if (loginData.stayLoggedIn) {
				setKeepMeLoggedInCookies(response, datastore, userInfo.account);
				accountDAO.update(datastore, userInfo.account, KeyFactory.stringToKey(userInfo.account.key));
			} else {
				response.addCookie(createPersistentCookie(COOKIE_NAME_USERID, COOKIE_DELETED, 1));
				response.addCookie(createPersistentCookie(COOKIE_NAME_TOKEN, COOKIE_DELETED, 1));
			}
		}
		if (errorMessage == null) {
			Response.LocationType forwardTo = accountService.determineLoginSuccessRedirectLocation(account);
			return new Response(true, userInfo, null, null, forwardTo);
		}
		return new Response(errorMessage);
	}

	private void setKeepMeLoggedInCookies(HttpServletResponse response, ObjectDatastore datastore, Account account) {
		int daysToKeepLoggedIn = 7;
		String token = String.valueOf(new Random().nextLong());
		response.addCookie(createPersistentCookie(COOKIE_NAME_USERID, account.userId, daysToKeepLoggedIn));
		Cookie tokenCookie = createPersistentCookie(COOKIE_NAME_TOKEN, token, daysToKeepLoggedIn);
		response.addCookie(tokenCookie);
		account.keepMeLoggedInTokens.add(token);
		account.keepMeLoggedInExpirations.add(ServerUtils.dateAddDays(new Date(), daysToKeepLoggedIn));
	}

	private Cookie createPersistentCookie(String name, String value, int days) {
		Cookie cookie = new Cookie(name, value);
		int sevenDaysInSeconds = 60*60*24*days;
		cookie.setMaxAge(sevenDaysInSeconds);
		return cookie;
	}

	@GET
	@Path("logout")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<UserInfo> logout(@Context HttpServletRequest request, @Context HttpServletResponse response) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		HttpSession httpSession = request.getSession(true);
		UserInfo userInfo = new UserInfo();
		httpSession.setAttribute("userInfo", userInfo);
		response.addCookie(createPersistentCookie(COOKIE_NAME_USERID, COOKIE_DELETED, 1));
		response.addCookie(createPersistentCookie(COOKIE_NAME_TOKEN, COOKIE_DELETED, 1));
		Account account = getAccountFromCookieAndConsumeTheCookie(datastore, request.getCookies());
		if (account != null) {
			accountDAO.update(datastore, account, KeyFactory.stringToKey(account.key));
		}
		return new Response(userInfo);
	}

	@GET
	@Path("checkLoggedIn")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<UserInfo> checkLoggedIn(@Context HttpServletRequest request, @Context HttpServletResponse response, @QueryParam("url") String requestingUrl) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		HttpSession session = request.getSession();
		UserService userService = UserServiceFactory.getUserService();
		UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");

		// See if they have a logged in session remaining and they are still logged into google if appropriate
		boolean loggedIn = accountService.refreshLogin(datastore, userInfo);
		if (!loggedIn) {
			 if (userService.isUserLoggedIn()) {
				// Full refresh was not possible.  Check to see if google user is logged in (maybe they were logged
				// in before coming to this site)
				userInfo = new UserInfo();
				userInfo.account = accountService.googleLogin(datastore, userService.getCurrentUser());
				loggedIn = true;
			} else {
				userInfo = new UserInfo();
				userInfo.account = getAccountFromCookieAndConsumeTheCookie(datastore, request.getCookies());
				if (userInfo.account != null) {
					setKeepMeLoggedInCookies(response, datastore, userInfo.account);
					loggedIn = true;
				}
			}
		}
		// Check mobile nonce if still not logged in
		if (!loggedIn && requestingUrl != null && requestingUrl.indexOf("mobileNonce") > 0) {
			Account account = accountService.getMobileAccount(datastore, requestingUrl);
			String errorMessage = accountService.verifyNonce(datastore, account, requestingUrl);
			if (errorMessage != null) {
				return new Response(errorMessage);
			}
			userInfo = accountService.loginUsingMobileNonce(datastore, account);
			loggedIn = userInfo.isSignedIn;
		}
		if (userInfo != null && userInfo.account != null && loggedIn) {
			// somehow they are logged in, so prep their session
			userInfo.account.lastLoginDate = new Date();
			accountDAO.update(datastore, userInfo.account, KeyFactory.stringToKey(userInfo.account.key));
			userInfo = accountService.buildLoggedInUserInfo(datastore, userInfo.account);
			if (userInfo.account.isGoogleAccount()) {
				userInfo.googleSignOutUrl = userService.createLogoutURL(configService.getApplicationConfig().siteUrl);
			}
			session.setAttribute("userInfo", userInfo);
			Response.LocationType forwardTo = accountService.determineLoginSuccessRedirectLocation(userInfo.account);
			return new Response(true, userInfo, null, null, forwardTo);
		}

      	return accountService.checkRequestingUrl(requestingUrl);
	}

	private Account getAccountFromCookieAndConsumeTheCookie(ObjectDatastore datastore, Cookie[] cookies) {
		String userId = accountIdFromCookie(cookies);
		String token = tokenFromCookie(cookies);

		if (userId != null && token != null) {
			Account account = accountDAO.readByUserId(datastore, userId);
			if (account != null && !account.isGoogleAccount() && account.keepMeLoggedInTokens.contains(token)) {
				int index = -1;
				for (String tokenToRemove : account.keepMeLoggedInTokens) {
					index++;
					if (tokenToRemove.equals(token)) {
						account.keepMeLoggedInTokens.remove(index);
						account.keepMeLoggedInExpirations.remove(index);
						break;
					}
				}
				return account;
			}
		}
		return null;
	}

	private String accountIdFromCookie(Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(COOKIE_NAME_USERID)) {
					return cookie.getValue().equals(COOKIE_DELETED) ? null : cookie.getValue();
				}
			}
		}
		return null;
	}

	private String tokenFromCookie(Cookie[] cookies) {
		if (cookies != null) {
			for (Cookie cookie : cookies) {
				if (cookie.getName().equals(COOKIE_NAME_TOKEN)) {
					return cookie.getValue().equals(COOKIE_DELETED) ? null : cookie.getValue();
				}
			}
		}
		return null;
	}

	@POST
	@Path("forgotYourPassword")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Object> forgotYourPassword(@Context HttpServletRequest request, Account loginData) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		Account account = accountService.findAccountByEmail(datastore, loginData.email);
		String errorMessage = accountService.forgotYourPassword(datastore, account);
		if (errorMessage == null) {
			return new Response(true, null, Response.MessageType.info, "A reset email has been sent.", null);
		}
		return new Response(errorMessage);
	}

	@POST
	@Path("resetPassword")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Object> resetPassword(AccountJson resetPassword) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		Account account = null;
		if (resetPassword != null && !Strings.isNullOrEmpty(resetPassword.email)) {
			account = accountService.findAccountByEmail(datastore, resetPassword.email);
		}
		String errorMessage = accountService.resetPassword(datastore, account, resetPassword.password, resetPassword.password2, resetPassword.url);
		if (errorMessage == null) {
			return new Response(null, "Your password was set.  Please login with your new password.");
		}
		return new Response(errorMessage);
	}

	@POST
	@Path("createAccount")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<UserInfo> createAccountForPublicUser(@Context HttpServletRequest request, @Context HttpServletResponse response, AccountJson newAccountInfo) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		String errorMessage =  accountService.validateCreateAccountForPublicUser(datastore, newAccountInfo.email, newAccountInfo.password, newAccountInfo.password2, newAccountInfo.name, newAccountInfo.url, newAccountInfo.recaptcha);
		if (errorMessage != null) {
			return new Response(errorMessage);
		}
		Account account = accountService.createAccountForPublicUser(datastore, newAccountInfo.email, newAccountInfo.password, newAccountInfo.name, newAccountInfo.url);
		if (account.confirmedByEmail) {
			errorMessage = accountService.loginNonGoogleAccount(datastore, account, newAccountInfo.password, null);
			if (errorMessage != null) {
				return new Response(errorMessage);
			}

			UserInfo userInfo = accountService.buildLoggedInUserInfo(datastore, account);
            HttpSession session = request.getSession(true);
            session.setAttribute("userInfo", userInfo);
			Response.LocationType forwardTo = accountService.determineLoginSuccessRedirectLocation(account);
			return new Response(true, userInfo, null, null, forwardTo);
		} else {
			return new Response(true, null, Response.MessageType.info, "A confirmation email has been sent to your email account.  Please click on the link in the email to finish registration of your account.", null);
		}
	}

	@POST
	@Path("createMobileAccount")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<String> createAccountForMobileUser(@Context HttpServletRequest request, @Context HttpServletResponse response, AccountJson newAccountInfo) throws IOException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		String errorMessage =  accountService.validateCreateAccountForMobileUser(newAccountInfo.email, newAccountInfo.deviceUuid);
		if (errorMessage != null) {
			return new Response(errorMessage);
		}
		accountService.createAccountForMobileUser(datastore, newAccountInfo.email, newAccountInfo.deviceUuid);
    	return new Response(true, null, Response.MessageType.info, "A confirmation email has been sent to your email account containing a token.  Please enter that token.", null);
	}

	@POST
	@Path("getMobileToken")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<String> getMobileToken(@Context HttpServletRequest request, @Context HttpServletResponse response, AccountJson newAccountInfo) throws IOException,  InterruptedException {
		ObjectDatastore datastore = annotationObjectDatastoreProvider.get();
		Account account = accountDAO.readByEmail(datastore, newAccountInfo.email);
		if (account == null) {
			Thread.sleep(5000);
			return new Response("Account not found.");
		}
		if (!newAccountInfo.token.equals(account.mobileToken)) {
			Thread.sleep(5000);
			return new Response("Incorrect token.");
		}
		if (!newAccountInfo.deviceUuid.equals(account.mobileDeviceUuid)) {
			Thread.sleep(5000);
			return new Response("Incorrect device.  You may only use your account on one mobile device.");
		}
		account.confirmedByEmail = true;
		account.mobileNonce = Long.toHexString(new Random().nextLong());
		account.mobileNonceExpirationDate = ServerUtils.dateAddMinutes(new Date(), 2);
		accountDAO.update(datastore, account);
		return new Response(true, account.mobileNonce);
	}

	@POST
	@Path("saveSettings")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Object> saveSettings(@Context HttpServletRequest request, Account settings) throws IOException {
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Object> response = securityService.check(userInfo);
		if (response == null) {
			ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

			Group group = null;
			if (userInfo.account.groupMemberKey != null) {
				GroupMember groupMember = groupMemberDAO.read(datastore, settings.groupMemberKey);
				 group = groupDAO.read(datastore, groupMember.groupKey);
			}
			String message = accountService.validateSettings(datastore, settings, group);
			if (message != null) {
				return new Response(message);
			}
			Account updatedAccount = accountService.saveSettings(datastore, userInfo.account.key, settings, userInfo, group);
			userInfo.account = updatedAccount;
			request.getSession().setAttribute("userInfo", userInfo);
			response = new Response(new Object());
		}
		return response;
	}

	static class PasswordChange {
		String password1;
		String password2;
	}

	@POST
	@Path("changePassword")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Object> changePassword(@Context HttpServletRequest request, PasswordChange passwordChange) throws IOException {
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		Response<Object> response = securityService.check(userInfo);
		if (response == null) {
			ObjectDatastore datastore = annotationObjectDatastoreProvider.get();

			String validationError = accountService.changePassword(datastore, userInfo.account.key, passwordChange.password1, passwordChange.password2);
			if (validationError != null) {
				response = new Response(validationError);
			} else {
				response = new Response(new Object());
			}
		}
		return response;
	}

	static class ContactUs {
		public String email;
		public String name;
		public String phone;
		public String request;
	}

	@POST
	@Path("contactUs")
	@Produces(APPLICATION_JSON)
	@Consumes(APPLICATION_JSON)
	public Response<Object> contactUs(@Context HttpServletRequest request, ContactUs contactUs) throws IOException {
		UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");
		String validationError = contactService.validate(contactUs.email, contactUs.name, contactUs.phone, contactUs.request);
		if (validationError != null) {
			return new Response(validationError);
		}

		contactService.sendContactEmail(userInfo, contactUs.email, contactUs.name, contactUs.phone, contactUs.request);
		return new Response<Object>((Object) null);
	}
}
