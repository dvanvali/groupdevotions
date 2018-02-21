package com.groupdevotions.server.service;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.groupdevotions.server.dao.OrganizationDAO;
import com.groupdevotions.shared.model.Organization;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by DanV on 7/20/2016.
 */
public class OrganizationServiceTest {
    final private LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
    public Mockery context;
    private OrganizationService organizationService;
    private OrganizationDAO organizationDAO;

    @Before
    public void setUp() throws Exception {
        helper.setUp();
        context = new Mockery() {{
            setImposteriser(ClassImposteriser.INSTANCE);
        }};
        organizationDAO = context.mock(OrganizationDAO.class);
        organizationService = new OrganizationService(organizationDAO);
    }

    @Test
    public void testValidation_nameRequired() {
        String error = organizationService.add(null, new Organization());
        Assert.assertTrue("Validation should return error message", error != null);
        Assert.assertTrue("Message should require name", error.contains("Name") && error.contains("required"));
    }

}
