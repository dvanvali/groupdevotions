package com.groupdevotions.server.service;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.server.dao.AccountDAO;
import com.groupdevotions.server.dao.ConfigDAO;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.Config;

import java.util.Date;
import java.util.logging.Logger;

public class ConfigService {
    protected static final Logger logger = Logger
            .getLogger(AccountService.class.getName());
    private final ObjectDatastore datastore;
    private final ConfigDAO configDAO;
    private final AccountDAO accountDAO;
    private final TestDataService testDataService;
    static private Config config;

    @Inject
    public ConfigService(ObjectDatastore datastore, ConfigDAO configDAO, AccountDAO accountDAO, TestDataService testDataService) {
        this.datastore = datastore;
        this.configDAO = configDAO;
        this.accountDAO = accountDAO;
        this.testDataService = testDataService;
    }

    public Config getApplicationConfig() {
        if (config == null) {
            populateConfig();
        }
        return config;
    }

    synchronized private void populateConfig() {
        config = configDAO.readInstance(datastore);
        if (config == null) {
            logger.info("Building config");
            Config newlyInitializedConfig = Config.getInstance();
            configDAO.create(datastore, newlyInitializedConfig);
            if (newlyInitializedConfig.development) {
                logger.info("Building test data");
                testDataService.buildTestData();
            }
            config = newlyInitializedConfig;
        }
    }

    public void agreeToTermsOfService(ObjectDatastore datastore, Account account) {
        account.agreedToTermsOfUse = new Date();
        accountDAO.update(datastore, account, com.google.appengine.api.datastore.KeyFactory.stringToKey(account.key));
    }
}
