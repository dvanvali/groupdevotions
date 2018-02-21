'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('New Account Flow ', function() {

    describe('Create Account', function() {
        beforeEach(function() {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.id('newAccountLink')).click();
            expect(element(by.id('createHeading')).getText()).toMatch(/Create/)
        });

        it('requires email', function() {
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/enter your email/);
        });

        it('does not allow existing account', function() {
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/have an account/);
        });

        it('requires name', function() {
            element(by.model('form.signInEmail')).sendKeys('a@a.com');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/enter your name/);
        });

        it('requires password', function() {
            element(by.model('form.signInEmail')).sendKeys('a@a.com');
            element(by.model('form.name')).sendKeys('a');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/enter your new password/);

            element(by.model('form.signInPassword')).sendKeys('aaaaaa');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/enter your new password/);
        });

        it('requires passwords to match', function() {
            element(by.model('form.signInEmail')).sendKeys('a@a.com');
            element(by.model('form.name')).sendKeys('a');
            element(by.model('form.signInPassword')).sendKeys('aaaaaa');
            element(by.model('form.signInPassword2')).sendKeys('aaaaa');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/two passwords must/);
        });

        it('requires passwords to match', function() {
            element(by.model('form.signInEmail')).sendKeys('a@a.com');
            element(by.model('form.name')).sendKeys('a');
            element(by.model('form.signInPassword')).sendKeys('aaaaaa');
            element(by.model('form.signInPassword2')).sendKeys('aaaaa');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/two passwords must/);
        });

        it('sends email upon success', function() {
            element(by.model('form.signInEmail')).sendKeys('newaccount@gmail.com');
            element(by.model('form.name')).sendKeys('Newbie');
            element(by.model('form.signInPassword')).sendKeys('aaaaaa');
            element(by.model('form.signInPassword2')).sendKeys('aaaaaa');
            element(by.id('createButton')).click();

            expect(element(by.id('messageCreate')).getText()).toMatch(/email has been sent/);
        });
    });

    describe('newaccount email', function() {

        it('displays message and logs in', function() {
            browser.get('/#/resetTestData?sleep');
            browser.waitForAngular();
            browser.get('/#/resetTestData?emailconfirmlink');
            element(by.id('testDataLink')).getAttribute('href').then(function(href) {
                browser.get(href);
                expect(element(by.id('message')).getText()).toMatch(/finish the registration/);

                element(by.model('form.signInEmail')).sendKeys('newaccount@gmail.com');
                element(by.model('form.signInPassword')).sendKeys('aaaaaa');
                element(by.buttonText('Sign In')).click();

                expect(browser.getLocationAbsUrl()).toMatch(/terms/);
                element(by.id('logoutButton')).click();
                expect(browser.getLocationAbsUrl()).toMatch(/home/);

            });
        });

    });

    describe('invite email', function() {
        // Test data already has an invite setup for invite@gmail.com
        it('address must match created account email address', function () {
            browser.get('/#/home');
            expect(browser.getLocationAbsUrl()).toBe('/home');
            browser.get('/#/resetTestData?invitelink');
            element(by.id('testDataLink')).getAttribute('href').then(function (href) {
                browser.get(href);
                expect(element(by.id('messageCreate')).getText()).toMatch(/accept your group invitation/);

                element(by.model('form.signInEmail')).sendKeys('wrong@gmail.com');
                element(by.model('form.name')).sendKeys('Invite');
                element(by.model('form.signInPassword')).sendKeys('bbbbbb');
                element(by.model('form.signInPassword2')).sendKeys('bbbbbb');
                element(by.id('createButton')).click();

                expect(element(by.id('messageCreate')).getText()).toMatch(/does not match the invitation email/);
            });
        });

        it('group member address key bad', function () {
            browser.get('/#/home');
            expect(browser.getLocationAbsUrl()).toBe('/home');
            browser.get('/#/resetTestData?invitelink');
            element(by.id('testDataLink')).getAttribute('href').then(function (href) {
                browser.get(href.replace('Invite=', 'Invite=badkey'));
                expect(element(by.id('messageCreate')).getText()).toMatch(/accept your group invitation/);

                element(by.model('form.signInEmail')).sendKeys('invite@gmail.com');
                element(by.model('form.name')).sendKeys('Invite');
                element(by.model('form.signInPassword')).sendKeys('bbbbbb');
                element(by.model('form.signInPassword2')).sendKeys('bbbbbb');
                element(by.id('createButton')).click();

                expect(element(by.id('messageCreate')).getText()).toMatch(/invitation is no longer valid/);
            });
        });

        it('registers upon login', function () {
            browser.get('/#/home');
            expect(browser.getLocationAbsUrl()).toBe('/home');
            browser.get('/#/resetTestData?invitelink');
            element(by.id('testDataLink')).getAttribute('href').then(function (href) {
                browser.get(href);
                expect(element(by.id('messageCreate')).getText()).toMatch(/accept your group invitation/);

                element(by.model('form.signInEmail')).sendKeys('invite@gmail.com');
                element(by.model('form.name')).sendKeys('Invite');
                element(by.model('form.signInPassword')).sendKeys('bbbbbb');
                element(by.model('form.signInPassword2')).sendKeys('bbbbbb');
                element(by.id('createButton')).click();

                expect(browser.getLocationAbsUrl()).toMatch(/terms/);
                element(by.id('logoutButton')).click();
                expect(browser.getLocationAbsUrl()).toMatch(/home/);

            });
        });

        it('already used up', function () {
            browser.get('/#/home');
            expect(browser.getLocationAbsUrl()).toBe('/home');
            browser.get('/#/resetTestData?invitelink');
            element(by.id('testDataLink')).getAttribute('href').then(function (href) {
                browser.get(href);
                expect(element(by.id('messageCreate')).getText()).toMatch(/accept your group invitation/);

                element(by.model('form.signInEmail')).sendKeys('invite@gmail.com');
                element(by.model('form.name')).sendKeys('Invite');
                element(by.model('form.signInPassword')).sendKeys('bbbbbb');
                element(by.model('form.signInPassword2')).sendKeys('bbbbbb');
                element(by.id('createButton')).click();

                expect(element(by.id('messageCreate')).getText()).toMatch(/already have an account/);
            });
        });

        it('resets data', function() {
            browser.get('/#/resetTestData');
        });
    });
});