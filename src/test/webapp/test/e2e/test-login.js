'use strict';
var util = require(__dirname + '/pages/util.js')();
var homePageFactory = require(__dirname + '/pages/home.page.js');
var homePage = homePageFactory();
var loginPageFactory = require(__dirname + '/pages/login.page.js');
var loginPage = loginPageFactory();

describe('GroupDevotions login ', function() {

    describe('Login', function() {
        var log = function(message, x) {
            var seen = [];
            console.log(message + JSON.stringify(x, function (_, value) {
                if (typeof value === 'object' && value !== null) {
                    if (seen.indexOf(value) !== -1) return;
                    else seen.push(value);
                }
                return value;
            }, '\t'));
        };

        beforeEach(function() {
            homePage.clickLogin();
        });

        it('should display email, password and sign in', function() {
            expect(element(loginPage.userName).isPresent()).toBe(true);
            expect(element(loginPage.password).isDisplayed()).toBe(true);
            expect(element(loginPage.signinButton).isDisplayed()).toBe(true);
        });

        it('Displays error for bad password', function() {
            util.login('dvanvali@gmail.com', 'badxxx');
            util.expectMessageContains('Your password is incorrect.');
        });

        it('Displays error for account does not exist', function() {
            util.login('baddvanvali@gmail.com', 'xxxxxx');
            util.expectMessageContains('Unable to find your account.');
        });

        it('Displays error for locked account', function() {
            util.login('locked@gmail.com', 'xxxxxx');
            util.expectMessageContains('is locked');
        });

        it('Displays error for logging in locally for google account', function() {
            util.login('test@example.com', 'xxxxxx');
            util.expectMessageContains('Your account is linked to your Google account. Please click on the Google image below to login.');
        });

        it('Displays error when email confirmation not processed yet.', function() {
            util.login('unconfirmed@gmail.com', 'xxxxxx');
            util.expectMessageContains("You need to confirm your email address by finding the email sent to you when you signed up");
        });

        it('Can login with an existing account and redirect to devotion', function() {
            util.login('dvanvali@gmail.com', 'xxxxxx');
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
            util.logout();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });

        it('Can login with an existing google account and redirect to devotion', function() {
            util.loginGoogle();
            expect(browser.getLocationAbsUrl()).toMatch(/devotion/);
            util.logout();
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });

        it('should reset data because password changed', function() {
            util.resetTestData();
        });

        it('should display terms of service', function() {
            util.login('terms@gmail.com', 'yyyyyy');
            expect(browser.getLocationAbsUrl()).toMatch(/terms/);
            util.logout();
        });

        it('should display terms of service if trying to get to devotion', function() {
            util.login('terms@gmail.com', 'yyyyyy');
            browser.get('/#/devotion');
            expect(browser.getLocationAbsUrl()).toMatch(/terms/);
            util.logout();
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });

        it('should display error message for disagree', function() {
            util.login('terms@gmail.com', 'yyyyyy');

            expect(browser.getLocationAbsUrl()).toMatch(/terms/);
            element(by.id("disagreeButton")).click();
            expect(browser.getLocationAbsUrl()).toMatch(/terms/);
            expect(element(by.id("message")).getText()).toMatch(/unless you agree/);
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });

        it('should display devotion agree', function() {
            util.login('terms@gmail.com', 'yyyyyy');

            element(by.id("agreeButton")).click();
            expect(browser.getLocationAbsUrl()).toMatch(/devotion/);

            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });

        it('should display devotion on next login after agree to terms of service', function() {
            util.login('terms@gmail.com', 'yyyyyy');
            expect(browser.getLocationAbsUrl()).toMatch(/devotion/);

            util.logout();
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });
    });

    describe('Reset Password From Login', function() {
        beforeEach(function() {
            browser.get('/#resetToken=83980169970545');
            expect(browser.getLocationAbsUrl()).toBe('/resetPassword');
        });

        it('displays heading', function() {
            expect(element(loginPage.resetHeading).getText()).toMatch(/Set/);
        });

        it('displays unable to find account when no email', function() {
            element(by.id('resetButton')).click();
            util.expectMessageContains('Unable to find your account');
        });

        it('displays unable to find account', function() {
            element(by.model('form.signInEmail')).sendKeys('bademail');
            element(by.id('resetButton')).click();
            util.expectMessageContains('Unable to find your account');
        });

        it('displays must enter passwords', function() {
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.id('resetButton')).click();
            util.expectMessageContains('enter your new password');

            element(by.model('form.signInPassword')).sendKeys('password');
            element(by.id('resetButton')).click();
            util.expectMessageContains('enter your new password');
        });

        it('displays passwords must match', function() {
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('password');
            element(by.model('form.signInPassword2')).sendKeys('notpassword');
            element(by.id('resetButton')).click();
            util.expectMessageContains('must be the same');
        });

        it('displays passwords must be at least six characters', function() {
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('pass');
            element(by.model('form.signInPassword2')).sendKeys('pass');
            element(by.id('resetButton')).click();
            util.expectMessageContains('at least six characters');
        });

        it('resets password successfully', function() {
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('zzzzzz');
            element(by.model('form.signInPassword2')).sendKeys('zzzzzz');
            element(by.id('resetButton')).click();
            browser.waitForAngular();
            util.expectMessageContains('password was set');
        });

        it('logs in with reset password', function() {
            util.login('dvanvali@gmail.com', 'zzzzzz');
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
            util.logout();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });

        it('should reset data', function() {
            util.resetTestData();
        });
    });

    describe('Forgot Your Password', function() {
        beforeEach(function() {
            homePage.clickLogin();
        });

        it('displays when forgot your password when link is clicked', function() {
            element(loginPage.forgotLink).click();

            expect(element(loginPage.forgotHeading).getText()).toMatch(/Forgot/);
        });

        it('displays login page when cancel is clicked', function() {
            element(loginPage.forgotLink).click();
            expect(element(loginPage.forgotHeading).getText()).toMatch(/Forgot/);
            element(loginPage.forgotCancel).click();
            expect(element(by.id('loginHeading')).getText()).toMatch(/sign in/);
        });

        it('displays unable to find account when email is empty', function() {
            element(loginPage.forgotLink).click();
            element(loginPage.forgotUserName).sendKeys('bademail@gmail.com');
            element(loginPage.forgotButton).click();

            expect(element(by.id('messageForgot')).getText()).toMatch(/Unable to find your account/);
        });

        it('displays reset email sent', function() {
            element(loginPage.forgotLink).click();
            element(loginPage.forgotUserName).sendKeys('dvanvali@gmail.com');
            element(loginPage.forgotButton).click();

            expect(element(by.id('messageForgot')).getText()).toMatch(/reset email has been sent/);
        });
    });
});