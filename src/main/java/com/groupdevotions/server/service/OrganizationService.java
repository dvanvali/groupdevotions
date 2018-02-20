package com.groupdevotions.server.service;

import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.groupdevotions.server.dao.OrganizationDAO;
import com.groupdevotions.shared.model.Organization;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * Created by DanV on 7/19/2016.
 */
public class OrganizationService {
    private Logger logger = Logger.getLogger(OrganizationService.class.getName());

    private OrganizationDAO organizationDAO;

    @Inject
    public OrganizationService(OrganizationDAO organizationDAO) {
        this.organizationDAO = organizationDAO;
    }

    public Collection<Organization> readAll(ObjectDatastore datastore) {
        return organizationDAO.readAll(datastore);
    }

    public Organization read(ObjectDatastore datastore, String organizationKey) {
        return organizationDAO.read(datastore, organizationKey);
    }

    public String add(ObjectDatastore datastore, Organization organization) {
        String errorMessage = validateOrganization(organization);
        if (errorMessage != null) {
            return errorMessage;
        }

        organizationDAO.create(datastore, organization);
        return null;
    }

    private String validateOrganization(Organization organization) {
        if (Strings.isNullOrEmpty(organization.name)) {
            return "Name is required.";
        }
        return null;
    }

    public String save(ObjectDatastore datastore, Organization organization) {
        String errorMessage = validateOrganization(organization);
        if (errorMessage != null) {
            return errorMessage;
        }

        organizationDAO.update(datastore, organization);
        return null;
    }
}
