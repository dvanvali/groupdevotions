package com.groupdevotions.server.service;

import com.google.appengine.api.datastore.KeyFactory;
import com.google.code.twig.ObjectDatastore;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.groupdevotions.server.dao.GroupDAO;
import com.groupdevotions.server.util.SharedUtils;
import com.groupdevotions.shared.model.Account;
import com.groupdevotions.shared.model.Group;

import java.util.Collection;
import java.util.logging.Logger;

public class GroupService {
    protected static final Logger logger = Logger
            .getLogger(GroupService.class.getName());

    private final GroupDAO groupDAO;

    @Inject
    public GroupService(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
    }

    public Collection<Group> readAll(ObjectDatastore datastore) {
        return groupDAO.readAll(datastore);
    }

    public Collection<Group> readForOrganization(ObjectDatastore datastore, String organizationKey) {
        return groupDAO.readByOrganizationKey(datastore, organizationKey);
    }

    public Group read(ObjectDatastore datastore, String groupKey) {
        return groupDAO.read(datastore, KeyFactory.stringToKey(groupKey));
    }

    public String addGroup(ObjectDatastore datastore, Group group, Account account) {
        String message = validateGroup(group, account);
        if (message == null) {
            groupDAO.create(datastore, group);
        }
        return message;
    }

    public String save(ObjectDatastore datastore, Group updatedGroup, Group existingGroup, Account account) {
        String message = validateGroup(updatedGroup, account);
        if (message == null) {
            existingGroup.updateMaintainFields(updatedGroup);
            groupDAO.update(datastore, existingGroup);
        }
        return message;
    }

    private String validateGroup(Group group, Account account) {
        // Self-signup do not require description, so this is fine now.
        //if (Strings.isNullOrEmpty(group.description)) {
        //    return "Description is required.";
        //}
        if (Strings.isNullOrEmpty(group.studyKey) && account.adminOrganizationKey == null && !account.siteAdmin) {
            return "Please select a study for your group.";
        }
        if (Strings.isNullOrEmpty(group.description) && account.adminOrganizationKey != null) {
            return "Please enter a group description.";
        }
        if (Strings.isNullOrEmpty(group.inviteEmailSubject)) {
            return "Email Invitation Subject is required.";
        }
        if (Strings.isNullOrEmpty(group.inviteEmailBody)) {
            return "Email Invitation Body is required.";
        }
        if (!Strings.isNullOrEmpty(group.defaultOrgAccountabilityEmail)) {
            if (group.ownerOrganizationKey != null) {
                if (!SharedUtils.validateEmails(group.defaultOrgAccountabilityEmail)) {
                    return "Default Accountability Email is not a valid email address.";
                }
            } else {
                return "You don't have rights to update Default Accountability Email.";
            }
        }
        return null;
    }
}
