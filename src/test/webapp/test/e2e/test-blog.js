'use strict';

/* http://docs.angularjs.org/guide/dev_guide.e2e-testing */

describe('GroupDevotions blog', function() {
/*
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

        it('should reset data', function() {
            browser.get(indexPage + '#/resetTestData');
            expect(element(by.id('testDataLink')).getText()).toMatch(/has been reset/);
        });

        it('should login', function() {
            browser.get(indexPage + '#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
        });

        it('devotion page blog button navigates to blog', function() {
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');
        });

        it('should display heading', function() {
            expect(element(by.css('.lessonTitle')).getText()).toBe('Sample Group');
        });

        it('should display eight days', function() {
            expect(element.all(by.repeater('groupBlog in groupBlogs')).count()).toBe(8);
        });

        it('should display today', function() {
            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);
            });
        });

        it('should display heading 03/08/2015 and content', function() {
            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[1].element(by.className('pageSubheading')).getText())
                    .toMatch(/03\/08\/2015/);

                blogs[1].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntry) {
                     expect(element(by.binding('blogEntry.htmlContent')).getText())
                         .toMatch(/This is a post for 3\/8\/2015/);
                });
            });
        });

        it('should cancel a new comment', function() {
            expect(element(by.id("saveCommentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeFalsy();

            element(by.id("commentButton")).click();
            expect(element(by.id("commentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("commentTextArea")).getAttribute('value')).toBe('');

            element(by.model('comment.value')).sendKeys('This is a comment for today.');
            expect(element(by.id("commentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeTruthy();

            element(by.id("cancelCommentButton")).click();
            expect(element(by.id("commentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeFalsy();

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(0);
            });

            element(by.id("commentButton")).click();
            expect(element(by.id("commentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("commentTextArea")).getAttribute('value')).toBe('');

            element(by.id("cancelCommentButton")).click();
        });

        it('should save a comment', function() {
            expect(element(by.id("commentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeFalsy();

            element(by.id("commentButton")).click();
            expect(element(by.id("commentButton")).isPresent()).toBeFalsy()
            expect(element(by.id("saveCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeTruthy();

            element(by.model('comment.value')).sendKeys('This is a comment for today.\n\nOn a new line.');
            expect(element(by.id("commentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeTruthy();

            element(by.id("saveCommentButton")).click();
            expect(element(by.id("commentButton")).isPresent()).toBeTruthy();
            expect(element(by.id("saveCommentButton")).isPresent()).toBeFalsy();
            expect(element(by.id("cancelCommentButton")).isPresent()).toBeFalsy();

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(1);
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    expect(blogEntrys[0].element(by.css('.blogPanel')).getText()).toMatch(/This is a comment for today.\n\nOn a new line./)
                });
            });
        });

        it('saved today entry is are still there after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('blogButton')).isPresent()).toBeTruthy();
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(1);
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    expect(blogEntrys[0].element(by.css('.blogPanel')).getText()).toMatch(/This is a comment for today.\n\nOn a new line./)
                });
            });
        });

        it('should edit entry for today and save it', function() {
            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(1);
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    blogEntrys[0].element(by.id('blogEntryEdit')).click();
                });
            });

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    blogEntrys[0].element(by.id('blogEditTextArea')).sendKeys('more text');
                    blogEntrys[0].element(by.id('saveBlogEditButton')).click();
                });
            });
        });

        it('edited today entry is are still there after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('blogButton')).isPresent()).toBeTruthy();
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(1);
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    expect(blogEntrys[0].getText()).toMatch(/This is a comment for today.\n\nOn a new line./)
                    expect(blogEntrys[0].getText()).toMatch(/more text/)
                });
            });
        });

        it('should delete entry for today and save it', function() {
            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(1);
                blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).then(function(blogEntrys) {
                    blogEntrys[0].element(by.id('blogEntryDelete')).click();
                });
            });

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(0);
            });
        });

        it('deleted today entry is still gone after reload', function() {
            element(by.id('devotionButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');

            expect(element(by.id('blogButton')).isPresent()).toBeTruthy();
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');

            element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                expect(blogs[0].element(by.className('pageSubheading')).getText())
                    .toMatch(/Today/);

                expect(blogs[0].all(by.repeater('blogEntry in groupBlog.blogEntries')).count()).toBe(0);
            });
        });

        it('scrolling loads another 7 days', function() {
            browser.executeScript('window.scrollTo(0,10000);').then(function () {
                element.all(by.repeater('groupBlog in groupBlogs')).then(function(blogs) {
                    expect(blogs[13].element(by.className('pageSubheading')).getText())
                        .toMatch(/02\/18\/2015/);
                });
            })
        });

        it('displays 3 group member activities', function() {
            expect(element.all(by.repeater('activity in activities')).count()).toBe(3);
        });

        it('displays group member modal on click', function() {
            browser.waitForAngular();
            browser.executeScript('window.scrollTo(0,0);');
            browser.executeScript("$('.modal').removeClass('fade');");
            element.all(by.repeater('activity in activities')).then(function(activities) {
                activities[0].click().then(function () {
                    expect(element(by.css('.modal-content')).isDisplayed()).toBeTruthy();
                    element(by.css('.close')).click();
                });
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
*/
    describe('group admin', function() {
        it('members button is present', function() {
            browser.get('/#/home');
            element(by.id('loginButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/login');
            element(by.model('form.signInEmail')).sendKeys('dvanvali@gmail.com');
            element(by.model('form.signInPassword')).sendKeys('xxxxxx');
            element(by.buttonText('Sign In')).click();
            expect(browser.getLocationAbsUrl()).toBe('/devotion');
            element(by.id('blogButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/blog');

            expect(element(by.id('membersButton')).isPresent()).toBeTruthy();
        });

        it('members display', function() {
            element(by.id('membersButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/members');
            expect(element.all(by.repeater("member in members")).count()).toBe(5);
        });

        it('edits a member', function() {
            element.all(by.repeater("member in members")).then(function(members) {
                    members[0].element(by.tagName('a')).click().then(function () {
                        expect(element(by.model('currentMember.name')).getAttribute('value')).toBe('Dan G');
                        element(by.model('currentMember.name')).clear().then(function() {
                            element(by.model('currentMember.name')).sendKeys('Dan Grabowski');
                        });
                    });
                }
            );
            expect(element(by.id('saveButton')).isPresent()).toBeTruthy();
            element(by.id('saveButton')).click();
            browser.waitForAngular();
            expect(element(by.id('viewGroup')).isPresent()).toBeTruthy();
            element(by.id('viewGroup')).click();
            browser.waitForAngular();
        });

        it('member change was saved', function() {
            element(by.id('membersButton')).click();
            expect(browser.getLocationAbsUrl()).toBe('/members');
            element.all(by.repeater("member in members")).then(function (members) {
                members[0].element(by.tagName('a')).click().then(function () {
                    expect(element(by.model('currentMember.name')).getAttribute('value')).toBe('Dan Grabowski');
                });
            });
        });

        it('puts member data back to original', function() {
            element(by.model('currentMember.name')).clear().then(function () {
                element(by.model('currentMember.name')).sendKeys('Dan G');
            });
            expect(element(by.id('saveButton')).isPresent()).toBeTruthy();
            element(by.id('saveButton')).click();
            browser.waitForAngular();
            expect(element(by.id('viewGroup')).isPresent()).toBeTruthy();
            element(by.id('viewGroup')).click();
            browser.waitForAngular();
        });
    });

    describe('logout', function() {
        it('redirects to home', function() {
            expect(element(by.id('logoutButton')).isPresent()).toBeTruthy();
            element(by.id('logoutButton')).click();
            browser.waitForAngular();
            expect(browser.getLocationAbsUrl()).toBe('/home');
        });
    });
});