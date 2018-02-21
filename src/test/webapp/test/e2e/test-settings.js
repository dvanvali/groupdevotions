'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions settings ', function() {
    var loginAndNavToSettings = function(password) {
        if (!password) {
            password = 'xxxxxx';
        }
        browser.get('/#/home');
        element(by.id('loginButton')).click();
        expect(browser.getLocationAbsUrl()).toBe('/login');
        element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
        element(by.model('form.signInPassword')).sendKeys(password);
        element(by.buttonText('Sign In')).click();
        browser.waitForAngular();

        element(by.id('dropdown')).click();
        element(by.id('settingsMenuItem')).click();
        browser.waitForAngular();

        expect(element(by.id('title')).getText()).toBe('Settings');
    };

    var logout = function() {
        browser.waitForAngular();
        element(by.id('logoutButton')).click();
        browser.waitForAngular();
        expect(browser.getLocationAbsUrl()).toBe('/home');
    };

    describe('Settings', function() {

        it('should navigate to settings', function () {
            loginAndNavToSettings();
        });

        it('should save a name change', function () {
            element(by.model('userInfo.account.name')).clear().then(function () {
                element(by.model('userInfo.account.name')).sendKeys('Dan x');
            });
            element(by.id('settingsSaveButton')).click();
            logout();
            loginAndNavToSettings();

            expect(element(by.model('userInfo.account.name')).getAttribute('value')).toBe('Dan x');
        });

        it('should save a study addition', function () {
            element.all(by.repeater('study in availableStudies')).then(function (studies) {
                expect(studies.length).toBe(1);
                expect(studies[0].element(by.tagName('input')).isSelected()).toBeFalsy();
                studies[0].element(by.tagName('input')).click();
                browser.waitForAngular();
                expect(studies[0].element(by.tagName('input')).isSelected()).toBeTruthy();
            });

            element(by.id('settingsSaveButton')).click();
            logout();
        });

        it('should save a removed study', function () {
            loginAndNavToSettings();

            element.all(by.repeater('study in availableStudies')).then(function (studies) {
                expect(studies.length).toBe(1);
                expect(studies[0].element(by.tagName('input')).isSelected()).toBeTruthy();
                studies[0].element(by.tagName('input')).click();
                browser.waitForAngular();
                expect(studies[0].element(by.tagName('input')).isSelected()).toBeFalsy();
            });

            element(by.id('settingsSaveButton')).click();
            logout();
           loginAndNavToSettings();
            element.all(by.repeater('study in availableStudies')).then(function (studies) {
                expect(studies[0].element(by.tagName('input')).isSelected()).toBeFalsy();
            });
        });

        it('should switch to password change display', function () {
            expect(element(by.id('passwordChange')).isPresent()).toBeTruthy();
            element(by.id('passwordChange')).click();
            browser.waitForAngular();
            expect(element(by.id('password1')).isPresent()).toBeTruthy();
        });
    });

    describe('Password Change', function() {
        it('should switch to settings when canceled', function() {
            expect(element(by.id('password1')).isPresent()).toBeTruthy();
            element(by.id('cancelButton')).click();
            browser.waitForAngular();
            expect(element(by.id('title')).getText()).toBe('Settings');
        });

        it('should save a password change', function() {
            expect(element(by.id('passwordChange')).isPresent()).toBeTruthy();
            element(by.id('passwordChange')).click();
            browser.waitForAngular();
            expect(element(by.id('password1')).isPresent()).toBeTruthy();

            element(by.id('password1')).sendKeys('abcxyss');
            element(by.id('password2')).sendKeys('abcxyss');
            element(by.id('saveButton')).click();
            browser.waitForAngular();
            logout();
        });

        it('should login after the password change', function() {
            loginAndNavToSettings('abcxyss');

            // restore the password
            expect(element(by.id('passwordChange')).isPresent()).toBeTruthy();
            element(by.id('passwordChange')).click();
            browser.waitForAngular();
            expect(element(by.id('password1')).isPresent()).toBeTruthy();

            element(by.id('password1')).sendKeys('xxxxxx');
            element(by.id('password2')).sendKeys('xxxxxx');
            element(by.id('saveButton')).click();
            browser.waitForAngular();
            expect(element(by.id('title')).getText()).toBe("Settings");
            logout();
        });
    });
});