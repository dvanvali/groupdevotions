'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions security', function() {
    describe('Security checks', function() {
        it('you must be logged in to see a devotion', function() {
            browser.get('/#/devotion');
            //expect(element(by.id('message')).getText()).toBe('Please login.');
            expect(browser.getLocationAbsUrl()).toMatch(/home/);
        });
    });
});