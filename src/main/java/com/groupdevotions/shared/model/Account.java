package com.groupdevotions.shared.model;

import com.google.code.twig.annotation.Index;
import com.google.code.twig.annotation.Store;
import com.google.gson.annotations.Expose;
import com.groupdevotions.server.ServerUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class Account implements Serializable, KeyMirror, PostLoad {
	private static final long serialVersionUID = -8994689740553162855L;
	@Expose(serialize = false)
	@Index public String userId;
	public String name;
	@Index public String email;
	public String phone;
	public boolean acceptTexts;
	public Date signUpDate; 
	public Date lastLoginDate;
	public Date lastLoginFailureDate;
	public Boolean siteAdmin = false;
	public Boolean disabled = false;
	public Boolean confirmed = false;
	public boolean privacy = false;
	public boolean settingsConfirmed = false;
	public Date agreedToTermsOfUse;
	public ScreenFormat screenFormat = ScreenFormat.DETECT;
	public PostingNotification postingNotification = PostingNotification.NO;
	public Collection<String> studyKeyPublicAccepts = new ArrayList<String>();
	public Collection<String> studyKeyGroupDeclines = new ArrayList<String>();
	@Index public String adminOrganizationKey;
	public String mobileToken;
	public String mobileNonce;
	public String mobileDeviceUuid;
	public Date mobileNonceExpirationDate;

	// Fields only used for GroupDevotions accounts
	@Expose(serialize = false)
	public String password;
	public boolean confirmedByEmail;
	public String resetToken;
	public Date resetExpiration;
	public int consecutiveFailedLogins = 0;
	public List<String> keepMeLoggedInTokens = new ArrayList<>();
	public List<Date> keepMeLoggedInExpirations = new ArrayList<>();

	public String groupMemberKey;

	@Store(false) public String key;
	@Store(false) public boolean allowPasswordChange;
	@Store(false) public List<StudyContributor> studyContributors = new ArrayList<StudyContributor>();
	@Store(false) public boolean privacyAvailableInSettings = false;

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	public String newAccountUrl() {
		return "#newaccount=" + ServerUtils.urlEncode(userId);
	}

	public boolean isGoogleAccount() {
		return userId != null && !userId.startsWith("apx");
	}

	public Account sanitizedForAnotherGroupMember() {
		Account account = new Account();
		if (privacy) {
			account.name = name;
			account.privacy = true;
		} else {
			account.name = name;
			account.email = email;
			account.phone = phone;
			account.acceptTexts = acceptTexts;
			account.privacy = false;
		}
		return account;
	}

	@Override
	public void postLoad() {
		allowPasswordChange = !isGoogleAccount();
		email = email.toLowerCase().trim();
		removeExpiredKeepMeLoggedInTokens();
		if (siteAdmin == null) {
			siteAdmin = false;
		}
	}

	private void removeExpiredKeepMeLoggedInTokens() {
		Date now = new Date();
		boolean removedOne;
		do {
			int index = -1;
			removedOne = false;
			for (Date expirationDate : keepMeLoggedInExpirations) {
				index++;
				if (now.after(expirationDate)) {
					keepMeLoggedInTokens.remove(index);
					keepMeLoggedInExpirations.remove(index);
					removedOne = true;
					break;
				}
			}
		} while(removedOne);
	}
}
