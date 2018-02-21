'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions navigation: ', function() {
    describe('Login', function() {

        it('should login to the devotion page', function() {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });

        it('devotion page journal button navigates to journal', function() {
            element(by.id('journalButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');
        });

        it('journal page blog button navigates to blog', function() {
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');
        });

        it('blog page devotion button navigates to devotion', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });


        it('devotion page blog button navigates to blog', function() {
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');
        });

        it('blog page journal button navigates to journal', function() {
            element(by.id('journalButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');
        });

        it('journal page devotion button navigates to devotion', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });

        it('Logout', function() {
            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });
    });
});