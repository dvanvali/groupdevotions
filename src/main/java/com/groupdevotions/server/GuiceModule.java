package com.groupdevotions.server;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.AbstractModule;
import com.groupdevotions.server.dao.AccountDAO;

/**
 * Created by DanV on 1/10/2015.
 */
public class GuiceModule extends AbstractModule {
    public GuiceModule() {
    }

    @Override
    protected void configure() {
        bind(ObjectDatastore.class).toProvider(AnnotationObjectDatastoreProvider.class);
        bind(AccountDAO.class);
    }
}
