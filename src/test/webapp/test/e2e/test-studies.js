'use strict';
var util = require(__dirname + '/pages/util.js')();

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions studies: ', function() {

    describe('Login', function() {

        it('should login to the devotion page', function() {
            util.loginGoogle();
            expect(browser.getLocationAbsUrl()).toMatch(/devotion/);
        });

        it('devotion page studies button navigates to studies', function() {
            element(by.id('dropdown')).click();
            element(by.id('studiesMenuItem')).click();
            expect(browser.getLocationAbsUrl()).toMatch(/studies/);
        });

    });

    describe('logout', function() {
        it('redirects to home', function() {
            util.logout();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });
    });
});