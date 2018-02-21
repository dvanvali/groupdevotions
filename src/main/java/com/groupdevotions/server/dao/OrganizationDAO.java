package com.groupdevotions.server.dao;

import com.google.code.twig.ObjectDatastore;
import com.google.inject.Inject;
import com.groupdevotions.shared.model.Organization;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by DanV on 7/19/2016.
 */
public class OrganizationDAO extends DAO<Organization> {
    @Inject
    public OrganizationDAO() {
        super(Organization.class);
    }

    public List<Organization> readAll(final ObjectDatastore datastore) {
        List<Organization> results = datastore.find().type(Organization.class)
                .returnAll()
                .now();
        executeReadEntityCallbacksMultipleEntities(datastore, results);

        Collections.sort(results, new Comparator<Organization>() {
            @Override
            public int compare(Organization arg0, Organization arg1) {
                return Collator.getInstance().compare(arg0.name, arg1.name);
            }
        });

        return results;
    }
}
