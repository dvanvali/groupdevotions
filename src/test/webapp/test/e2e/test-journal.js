'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions journal', function() {
    describe('page', function() {
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

        it('should login', function() {
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

        it('should display instructions', function() {
            expect(element(by.css('.instructions')).getText()).toMatch(/This is your personal journal/);
        });

        it('should display ten journals', function() {
            expect(element.all(by.repeater('journal in journals')).count()).toBe(10);
        });

        it('should display goals and reminders', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                expect(journals[0].element(by.className('blogJournalHeading')).getText())
                    .toMatch(/Goals\/Reminders/);
            });
        });

        it('should display heading 03/01/2015 and content', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                expect(journals[3].element(by.className('blogJournalHeading')).getText())
                    .toMatch(/03\/01\/2015/);

                expect(journals[3].element(by.binding('journal.htmlContent')).getText())
                    .toMatch(/content for date Sun Mar 01/);
            });
        });

        it('should display done button when edit', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('editLink')).click().then(function() {
                    expect(journals[1].element(by.id('editLink')).isPresent()).toBe(true);
                    expect(journals[0].element(by.id('editLink')).isPresent()).toBe(false);
                    expect(journals[0].element(by.id('savedText')).isPresent()).toBe(false);
                    expect(journals[0].element(by.id('savingText')).isPresent()).toBe(false);
                    expect(journals[0].element(by.id('done')).isPresent()).toBe(true);
                });
            });
        });

        it('should save changed edit goals when done is pressed', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.model('journal.content')).sendKeys("hey there");
                journals[0].element(by.id('done')).click();
            });
        });

        it('saved goals are still there after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('journalButton')).isPresent()).toBeTruthy();
            element(by.id('journalButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');

            element.all(by.repeater('journal in journals')).then(function(journals) {
                expect(journals[0].element(by.binding('journal.htmlContent')).getText())
                    .toMatch(/hey there/);
            });
        });

        it('should delete goals content and save it', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('editLink')).click().then(function() {
                    journals[0].element(by.model('journal.content')).clear();
                });
            });

            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('done')).click();
            });
        });

        it('deleted goals are still there after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('journalButton')).isPresent()).toBeTruthy();
            element(by.id('journalButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');

            element.all(by.repeater('journal in journals')).then(function(journals) {
                expect(journals[0].element(by.binding('journal.htmlContent')).getText())
                    .toBe('');
            });
        });

        it('scrolling loads another 6 days', function() {
            browser.executeScript('window.scrollTo(0,10000);').then(function () {
                element.all(by.repeater('journal in journals')).then(function(journals) {
                    expect(journals[13].element(by.className('blogJournalHeading')).getText())
                        .toMatch(/02\/11\/2015/);
                });
            })
        });

        it('should edit goals content and save it automatically after 4 seconds', function() {
            browser.executeScript('window.scrollTo(0,0);');
            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('editLink')).click().then(function() {
                    journals[0].element(by.model('journal.content')).sendKeys("this should save automatically");
                    var waitLoading = journals[0].element(by.id('savedText'));

                    browser.wait(function() {
                        return browser.isElementPresent(waitLoading);
                    }, 8000);

                    expect(journals[0].element(by.id('savedText')).isPresent()).toBe(true);
                });
            });
        });

        it('automatically saved goals are still there after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('journalButton')).isPresent()).toBeTruthy();
            element(by.id('journalButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/journal');

            element.all(by.repeater('journal in journals')).then(function(journals) {
                expect(journals[0].element(by.binding('journal.htmlContent')).getText())
                    .toMatch(/this should save automatically/);
            });
        });

        it('should delete goals content and save it after automatic save', function() {
            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('editLink')).click().then(function() {
                    journals[0].element(by.model('journal.content')).clear();
                });
            });

            element.all(by.repeater('journal in journals')).then(function(journals) {
                journals[0].element(by.id('done')).click();
            });
        });

        it('Logout redirects to home', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });
    });
});