package com.groupdevotions.mock.model;

import com.groupdevotions.shared.model.Account;

import java.util.Date;

public class MockAccountFactory {
	static public Account buildVirginGoogleAccount() {
		Account account = new Account();
		account.userId = "hpx3";
		account.email = "someone@gmail.com";
		account.siteAdmin = false;

		account.signUpDate = new Date();
		account.lastLoginDate = new Date();
		account.confirmed = false;
		account.disabled = false;
		return account;
	}
}
