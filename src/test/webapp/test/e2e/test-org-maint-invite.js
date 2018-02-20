var util = require(__dirname + '/pages/util.js')();
var maintainOrganizationPage = require(__dirname + '/pages/maintainOrganization.page.js')();
var maintainOrganizationLookupAccountPage = require(__dirname + '/pages/maintainOrganizationLookupAccount.page.js')();
var homePage = require(__dirname + '/pages/home.page.js')();
var resetPasswordPage = require(__dirname + '/pages/resetPassword.page.js')();
var termsPage = require(__dirname + '/pages/terms.page.js')();
var groupsPage = require(__dirname + '/pages/groups.page.js')();
var maintainGroupPage = require(__dirname + '/pages/maintainGroup.page.js')();
var studiesPage = require(__dirname + '/pages/studies.page.js')();
var maintainStudyPage = require(__dirname + '/pages/maintainStudy.page.js')();
var lessonPage = require(__dirname + '/pages/lesson.page.js')();

describe('Organizations', function() {
    var orgName = 'Dunamai';
    var accountEmail = new Date().getTime() + '@example.com';
    var groupName = 'New group ' + new Date().getTime();
    var studyName = 'New study ' + new Date().getTime();

    describe('organization admin', function() {
        it('loads groups page for an org admin that is not a group member', function () {
            util.loginGoogle('org@example.com');
            expect(element(groupsPage.title).getText()).toContain('Groups');
        });

        it('can access organization administration', function () {
            homePage.getAdministrationPage();
            expect(element(maintainOrganizationPage.title).getText()).toContain('Organization Administration');
        });

        it('invites a new admin account', function () {
            homePage.getAdministrationPage();
            element(maintainOrganizationPage.addAccountButton).click();
            element(maintainOrganizationLookupAccountPage.name).sendKeys('Jim');
            element(maintainOrganizationLookupAccountPage.email).sendKeys(accountEmail);
            element(maintainOrganizationLookupAccountPage.saveButton).click();
            browser.waitForAngular();
            util.findRowContaining(maintainOrganizationPage.accountsRepeater, accountEmail, function (rowElement) {
                expect(rowElement.getText()).toContain(accountEmail);
            });
            util.logout();
        });
    });

    describe('organization admin invitation', function() {
        it('sets password when clicking invite url in email', function () {
            browser.get('/#/resetTestData?adminAccountLink&email=' + accountEmail);
            element(by.id('testDataLink')).getAttribute('href').then(function (href) {
                browser.get(href);
            });
            browser.waitForAngular();
            element(resetPasswordPage.signInEmail).sendKeys(accountEmail);
            element(resetPasswordPage.signInPassword).sendKeys('xxxxxx');
            element(resetPasswordPage.signInPassword2).sendKeys('xxxxxx');
            element(resetPasswordPage.resetButton).click();
            util.expectMessageContains('Your password was set');
        });

        it('invited admin user logs in and sees Terms of Service', function () {
            util.login(accountEmail, 'xxxxxx');
            expect(element(termsPage.title).getText()).toContain('Terms Of Service');
        });

        it('invited admin user can agree to terms of service and lands on group admin', function () {
            element(termsPage.agreeButton).click();
            browser.waitForAngular();
            util.expectSelectorContainsText(groupsPage.title, 'Groups');
        });
    });

    describe('new admin user', function() {
        it('can add group', function () {
            element(groupsPage.addGroup).click();
            browser.waitForAngular();
            expect(element(maintainGroupPage.pageTitle).getText()).toContain('Group');
        });

        it('can save new group', function () {
            element(maintainGroupPage.groupTitle).sendKeys(groupName);
            util.selectDropdownbyNum(element(maintainGroupPage.study), 1);
            element(maintainGroupPage.saveButton).click();
            browser.waitForAngular();

            util.expectSelectorContainsText(groupsPage.title, 'Maintain Members');
        });

        it('can go to studies page', function () {
            homePage.getStudiesPage();
            util.expectSelectorContainsText(studiesPage.title, 'Maintain Bible Studies');
        });

        it('can add a study', function () {
            element(studiesPage.addStudyButton).click();
            browser.waitForAngular();
            util.expectSelectorContainsText(maintainStudyPage.title, 'Maintain Study');
        });

        it('can save a study', function () {
            element(maintainStudyPage.studyTitle).sendKeys(studyName);
            element(maintainStudyPage.author).sendKeys('author');
            element(maintainStudyPage.purpose).sendKeys('purpose');
            element(maintainStudyPage.copyright).sendKeys('copyright');
            util.selectDropdownbyNum(element(maintainStudyPage.studyType), 1);
            element(maintainStudyPage.saveButton).click();
            browser.waitForAngular();

            util.expectSelectorContainsText(studiesPage.title, 'Maintain Bible Studies');
            util.findRowContaining(studiesPage.studiesRepeater, studyName, function (rowElement) {
                expect(rowElement.getText()).toContain(studyName);
            });
        });
    });

    describe('original admin user', function() {
        it('can see new group', function () {
            util.logout();
            util.loginGoogle('org@example.com');
            expect(element(groupsPage.title).getText()).toContain('Groups');
            util.findRowContaining(groupsPage.groupsRepeater, groupName, function (rowElement) {
                expect(rowElement.getText()).toContain(groupName);
            });
        });

        it('can see new study', function () {
            homePage.getStudiesPage();
            util.expectSelectorContainsText(studiesPage.title, 'Maintain Bible Studies');
            util.findRowContaining(studiesPage.studiesRepeater, studyName, function (rowElement) {
                expect(rowElement.getText()).toContain(studyName);
            });
        });

        it('can add a lesson to the new study', function() {
            util.findRowContaining(studiesPage.studiesRepeater, studyName, function (rowElement) {
                rowElement.element(studiesPage.addLessonButton).click();
                browser.waitForAngular();
                util.expectSelectorContainsText(lessonPage.pageTitle, 'Maintain Lesson');
            });
        });

        it('can save a lesson to the new study', function() {
            element(lessonPage.title).sendKeys('lesson title');
            element(lessonPage.questionTextbox).sendKeys('dialog content');
            element(lessonPage.saveButton).click();
            browser.waitForAngular();

            util.expectSelectorContainsText(studiesPage.title, 'Maintain Bible Studies');
        });

        it('can logout', function () {
            util.logout();
            util.resetTestData();
        });
    });
});