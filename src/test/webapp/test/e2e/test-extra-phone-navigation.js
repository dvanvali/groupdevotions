'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions temp: ', function() {
    describe('extra phone navigation', function() {

        it('should login to the devotion page', function() {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('nongoogle@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });

        it('devotion page has group button that navigates to group', function() {
            expect(element(by.id('groupButtonBottom')).isPresent()).toBeTruthy();
            element(by.id('groupButtonBottom')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');
        });

        it('group page has journal button that navigates to journal', function() {
            expect(element(by.id('journalButtonBottom')).isPresent()).toBeTruthy();
            element(by.id('journalButtonBottom')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');
        });

        it('Logout', function() {
            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });

        it('should login to the desktop page', function() {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('desktop@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/desktop');
        });

        it('desktop page should not have group button', function() {
            expect(element(by.id('groupButtonBottom')).isPresent()).toBeFalsy();
        });

        it('desktop page should not have journal button', function() {
            expect(element(by.id('journalButtonBottom')).isPresent()).toBeFalsy();
        });
    });

    describe('logout', function() {
        it('redirects to home', function() {
            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });
    });
});