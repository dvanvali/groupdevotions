'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

xdescribe('GroupDevotions with external dependencies', function() {
    var host = 'http://localhost:8080';
    var indexPage = host + '/index.html';
    describe('Login', function() {

        it('should login', function() {
            browser.get(indexPage + '#/home');
            element(by.model('form.signInEmail')).sendKeys('nongoogle@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });

        it('should display rss copyright', function() {
            expect(element.all(by.binding('studyLesson.copyright')).get(1).getText()).toBe('Provided via the Secret Garden RSS Feed');
        });

    });
});