var util = require(__dirname + '/pages/util.js')();
var organizationsPage = require(__dirname + '/pages/organizations.page.js')();
//var groupsPage = require(__dirname + '/pages/groups.page.js')();
var maintainOrganizationPage = require(__dirname + '/pages/maintainOrganization.page.js')();
var maintainOrganizationLookupAccountPage = require(__dirname + '/pages/maintainOrganizationLookupAccount.page.js')();

describe('Organizations', function() {
    var orgName = 'Mt Hope ' + new Date().getTime();
    var accountEmail = new Date().getTime() + '@example.com';

    it('loads organization page', function() {
        util.loginGoogle('testb@example.com');
        organizationsPage.get();
        browser.waitForAngular();
        expect(element(organizationsPage.title).getText()).toContain('Maintain Organizations');
    });

    it('adds an organization', function() {
        element(organizationsPage.addButton).click();
        expect(element(organizationsPage.title).getText()).toContain('Organization Administration');
        element(maintainOrganizationPage.name).sendKeys(orgName);
        element(maintainOrganizationPage.saveButton).click();
        browser.waitForAngular();
        expect(element(organizationsPage.title).getText()).toContain('Maintain Organizations');
    });

    it('after add, displays new organization', function() {
        util.findRowContaining(organizationsPage.organizationsRepeater, orgName, function(rowElement) {
            expect(rowElement.getText()).toContain(orgName);
        });
    });

    it('edit goes to maintain organization', function() {
        util.findRowContaining(organizationsPage.organizationsRepeater, orgName, function(rowElement) {
            rowElement.element(organizationsPage.editButton).click();
            browser.waitForAngular();
            expect(element(organizationsPage.title).getText()).toContain('Organization Administration');
        });
    });

    it('maintain organization saves name change', function() {
        element(maintainOrganizationPage.name).clear();
        orgName = orgName + 'x';
        element(maintainOrganizationPage.name).sendKeys(orgName);
        element(maintainOrganizationPage.saveButton).click();
        browser.waitForAngular();
        expect(element(organizationsPage.title).getText()).toContain('Maintain Organizations');
    });

    it('organization change is retained between sessions', function() {
        util.logout();
        util.loginGoogle('testb@example.com');
        organizationsPage.get();
        expect(element(organizationsPage.title).getText()).toContain('Maintain Organizations');
        util.findRowContaining(organizationsPage.organizationsRepeater, orgName, function(rowElement) {
            expect(rowElement.getText()).toContain(orgName);
        });
    });

    it('adds a new admin account', function () {
        util.findRowContaining(organizationsPage.organizationsRepeater, orgName, function(rowElement) {
            rowElement.element(organizationsPage.editButton).click();
            browser.waitForAngular();
        });
        element(maintainOrganizationPage.addAccountButton).click();
        element(maintainOrganizationLookupAccountPage.name).sendKeys('Jim');
        element(maintainOrganizationLookupAccountPage.email).sendKeys(accountEmail);
        element(maintainOrganizationLookupAccountPage.saveButton).click();
        browser.waitForAngular();
        util.findRowContaining(maintainOrganizationPage.accountsRepeater, accountEmail, function(rowElement) {
            expect(rowElement.getText()).toContain(accountEmail);
        });
    });

    it('detects a new admin account that already exists', function () {
        element(maintainOrganizationPage.addAccountButton).click();
        element(maintainOrganizationLookupAccountPage.name).sendKeys('Jim');
        element(maintainOrganizationLookupAccountPage.email).sendKeys(accountEmail);
        element(maintainOrganizationLookupAccountPage.saveButton).click();
        browser.waitForAngular();
        util.expectMessageContains('already an administrator for your organization');
    });

    it('lookup account can cancel', function () {
        element(maintainOrganizationLookupAccountPage.cancelButton).click();
        util.findRowContaining(maintainOrganizationPage.accountsRepeater, accountEmail, function(rowElement) {
            expect(rowElement.getText()).toContain(accountEmail);
        });
    });

    it('adds admin to an existing account', function () {
        var existingAccountEmail = 'dvanvali@gmail.com';
        element(maintainOrganizationPage.addAccountButton).click();
        element(maintainOrganizationLookupAccountPage.email).sendKeys(existingAccountEmail);
        element(maintainOrganizationLookupAccountPage.saveButton).click();
        browser.waitForAngular();
        util.findRowContaining(maintainOrganizationPage.accountsRepeater, existingAccountEmail, function(rowElement) {
            expect(rowElement.getText()).toContain(existingAccountEmail);
        });
    });

    it('detects if an account is admin for another org', function () {
        var existingAccountEmail = 'org@example.com';
        element(maintainOrganizationPage.addAccountButton).click();
        element(maintainOrganizationLookupAccountPage.email).sendKeys(existingAccountEmail);
        element(maintainOrganizationLookupAccountPage.saveButton).click();
        browser.waitForAngular();
        util.expectMessageContains('already an administrator for another organization');
        element(maintainOrganizationLookupAccountPage.cancelButton).click();
    });

    it('logout', function() {
        util.logout();
        util.resetTestData();
    });
});