package com.groupdevotions.server.service;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.groupdevotions.mock.model.MockAccountFactory;
import com.groupdevotions.server.dao.AccountDAO;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.Config;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class AccountServiceTest {
	final private LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
	public Mockery context;
	private AccountService accountService;
	private Account mockAccount;
	private AccountDAO accountDAO;
	private ConfigService configService;

	@Before
	public void setUp() throws Exception {
		helper.setUp();
		context = new Mockery() {{
			setImposteriser(ClassImposteriser.INSTANCE);
		}};
		accountDAO = context.mock(AccountDAO.class);
		configService = context.mock(ConfigService.class);
		mockAccount = MockAccountFactory.buildVirginGoogleAccount();
		context.checking(new Expectations(){{
			oneOf(configService).getApplicationConfig();
			will(returnValue(Config.getInstance()));
		}});
		accountService = new AccountService(null, null, null, null, null, null, configService, null, null);
	}

	@Test
	public void testGoogleLogin_returnAccountFound() {
//		context.checking(new Expectations(){{
//			oneOf(accountDAO).readByEmail(null, "dvanvalin@gmail.com");
//			will(returnValue(mockAccount));
//		}});
//
//		Account account = accountService.googleLogin("dvanvalin@gmail.com");
//		assertEquals(mockAccount, account);
	}

	@Test
	public void testGoogleLogin_createAccountWhenNotFound() {
//		final Key key= KeyFactory.createKey("MyKey", "MyKeyPath");
//		context.checking(new Expectations(){{
//			oneOf(accountDAO).readByEmail(null, "new@gmail.com");
//			will(returnValue(null));
//			oneOf(configService).getApplicationConfig();
//			will(returnValue(new Config()));
//			oneOf(accountDAO).create(with((ObjectDatastore) null), with(any(Account.class)));
//			will(returnValue(key));
//		}});
//
//		Account account = accountService.googleLogin("new@gmail.com");
//		assertEquals(account.email, "new@gmail.com");
	}

	@Test
	public void testGetToken_middleToken() {
		String token = accountService.getToken("groupInvite", "blah?groupInvite=abc&email=cde");
		Assert.assertEquals("abc", token);
		token = accountService.getToken("email", "blah?groupInvite=abc&email=cde");
		Assert.assertEquals("cde", token);
	}
}
