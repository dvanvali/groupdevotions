'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('test-devotion', function() {
    describe('render', function() {
        it('should login', function () {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });
/*
        it('should display study title with pageHeading class', function () {
            expect(element(by.binding('studyLesson.title')).getText()).toBe('Private Study By Dan');
            expect(element(by.css('.lessonTitle')).getText()).toBe('Private Study By Dan');
        });

        it('should display sanitized html', function () {
            element.all(by.repeater('section in studyLesson.studySections')).then(function (rows) {
                expect(rows[2].getText()).toBe('He knew their strategies and the angles they would take to attack.\n\nThis enabled him to position his forces in places that gave the greatest chance of victory.');
            });
        });

        it('should display accountability questions', function () {
            expect(element.all(by.css('.questionTextbox')).get(1)).toBeDefined();
            expect(element.all(by.tagName('input')).get(2).getAttribute('value')).toBe('Y');
            expect(element.all(by.tagName('input')).get(3).getAttribute('value')).toBe('N');
        });

        it('should display copyright', function () {
            expect(element(by.binding('studyLesson.copyright')).getText()).toBe('@2015 by GroupDevotions.com');
        });
  */
    });
/*
    describe('questions', function() {
        it('should have text answer for study text question', function () {
            expect(element.all(by.tagName('textarea')).get(0).getAttribute('value')).toBe('I like it.');
        });

        it('should display save button', function () {
            expect(element(by.id('saveButton')).isPresent()).toBeTruthy();
        });

        it('should have some value selected for study question y/n', function () {
            var yesIndex = 0;
            var noIndex = 1;
            var yesInput = element.all(by.tagName('input')).get(yesIndex);
            var noInput = element.all(by.tagName('input')).get(noIndex);
            yesInput.isSelected().then(function (yesSelected) {
                if (yesSelected) {
                    expect(yesSelected).toBeTruthy();
                } else {
                    noInput.isSelected().then(function (noSelected) {
                        expect(noSelected).toBeTruthy();
                    });
                }
            });
        });

        it('should save answers', function () {
            var yesIndex = 0;
            var noIndex = 1;
            var yesInput = element.all(by.tagName('input')).get(yesIndex);
            yesInput.isSelected().then(function (yesSelected) {
                if (yesSelected) {
                    element.all(by.tagName('input')).get(noIndex).click();
                } else {
                    element.all(by.tagName('input')).get(yesIndex).click();
                }

                element(by.id('saveButton')).click();

                element(by.id('logoutButton')).click(); // used to be browser.get(indexPage + '#/home');
                element(by.id('loginButton')).click();
                expect(browser.getLocationAbsUrl()).toBe('/login');

                element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
                element(by.model('form.signInPassword')).sendKeys('xxxxxx');
                element(by.buttonText('Sign In')).click();
                expect(browser.getLocationAbsUrl()).toBe('/devotion');

                if (yesSelected) {
                    element.all(by.tagName('input')).get(noIndex).isSelected().then(function (noSelected) {
                        expect(noSelected).toBeTruthy();
                    });
                } else {
                    element.all(by.tagName('input')).get(yesIndex).isSelected().then(function (yesSelected) {
                        expect(yesSelected).toBeTruthy();
                    });
                }
            });
        });
    });
*/
    describe('settings', function() {
        /*
        it('button displays settings', function () {
            expect(element(by.id('accountabilitySettingsButton')).isPresent()).toBeTruthy();

            element(by.id('accountabilitySettingsButton')).click();

            expect(element(by.id('accountabilitySettingsButton')).isPresent()).toBeFalsy();
            expect(element(by.id('emailsToMeYes')).isPresent()).toBeTruthy();
            expect(element(by.id('emailsToMeNo')).isPresent()).toBeTruthy();
        });

        it('cancel hides settings', function () {
            expect(element(by.id('emailsToMeYes')).isPresent()).toBeTruthy();

            element(by.id('cancelSettingsButton')).click();

            expect(element(by.id('emailsToMeYes')).isPresent()).toBeFalsy();
        });
*/
        it('save and reload with saved value', function () {
            element(by.id('accountabilitySettingsButton')).click();
            browser.waitForAngular();
            browser.driver.sleep(2000);

            expect(element(by.id('emailsToMeNo')).isSelected()).toBeTruthy();
            element(by.id('emailsToMeYes')).click();
            element(by.id('saveSettingsButton')).click();
            browser.waitForAngular();

            expect(element(by.id('emailsToMeNo')).isPresent()).toBeFalsy();
            expect(element(by.id('accountabilitySettingsButton')).isPresent()).toBeTruthy();
/*
            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            element(by.id('accountabilitySettingsButton')).click();
            browser.executeScript('window.scrollTo(0,document.body.scrollHeight);');
            expect(element(by.id('emailsToMeYes')).isPresent()).toBeTruthy();
            expect(element(by.id('emailsToMeYes')).isSelected()).toBeTruthy();
            expect(element(by.id('emailsToMeNo')).isSelected()).toBeFalsy();

            element(by.id('emailsToMeNo')).click();
            element(by.id('saveSettingsButton')).click();
            */
        });

        it('resets the accountability setting for the next test run', function () {
            element(by.id('accountabilitySettingsButton')).click();
            browser.driver.sleep(2000);

            element(by.id('emailsToMeNo')).click();
            element(by.id('saveSettingsButton')).click();
            browser.waitForAngular();
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