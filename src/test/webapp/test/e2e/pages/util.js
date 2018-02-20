var util = function () {
    var that = this;

    var userName = by.id('email');
    var password = by.id('password');
    var signinButton = by.buttonText('Sign In');
    var googleButton = by.id('googleButton');
    var logoutButton = by.id("logoutButton");
    var message = by.id('message');

    var login = function (userNameValue, passwordValue) {
            browser.get("#/login");
            element(userName).sendKeys(userNameValue);
            element(password).sendKeys(passwordValue);
            element(signinButton).click();
            browser.waitForAngular();
        };

    var loginGoogle = function (userName) {
            browser.get("#/login");
            element(googleButton).click();
            if (userName) {
                browser.driver.findElement(By.id('email')).clear();
                browser.driver.findElement(By.id('email')).sendKeys(userName);
            }
            browser.driver.findElements(By.tagName('input')).then(function (inputElements) {
                inputElements[3].click()
            });
            browser.waitForAngular();
            //browser.driver.sleep(1000);
        };

    var logout = function () {
        element(logoutButton).click();
        browser.waitForAngular();
    };

    var expectMessageContains = function (messageSubstring) {
            expect(element(message).getText()).toContain(messageSubstring);
        };

    var dumpLogs = function() {
        // Use console.warn() in the app and then call this to display in node.
        browser.manage().logs().get('browser').then(function(browserLogs) {
            browserLogs.forEach(function(log){
                console.log(log.message);
            });
        });
    };

    var findRowContaining = function (locator, textToFind, callback, failureCallback) {
        var logMessages = 'textToFind: ' + textToFind + '\n';
        // stale element reference can happen if you do the callback too early.  Need to wait for each() to complete first!
        if (!callback) {
            expect(false).toBeTruthy();
        }
        var foundRow;
        element.all(locator).each(function(rowElement, index) {
            rowElement.getText().then(function (rowText) {
                logMessages += 'index: ' + index + ' rowText: ' + rowText + '\n';
                if (rowText.includes(textToFind)) {
                    foundRow = rowElement;
                }
            });
        }).then(function() {
            if (!foundRow) {
                if (failureCallback) {
                    failureCallback();
                } else {
                    console.log(logMessages);
                    expect('row was not found: ' + textToFind).toBeFalsy();
                }
            } else {
                callback(foundRow);
            }
        });
    };

    var selectDropdownbyNum = function ( element, optionNum ) {
        element.all(by.tagName('option'))
            .then(function(options){
                options[optionNum].click();
            });
    };

    var expectSelectorContainsText = function(selector, text) {
        expect(element(selector).getText()).toContain(text);
    };

    var resetTestData = function () {
        browser.get('/#/resetTestData');
        expect(element(by.id('testDataLink')).getText()).toMatch(/has been reset/);
        browser.waitForAngular();
        //        browser.get('/#/resetTestData?sleep');
    };


    return {
        login: login,
        loginGoogle: loginGoogle,
        logout: logout,
        expectMessageContains: expectMessageContains,
        dumpLogs: dumpLogs,
        findRowContaining: findRowContaining,
        selectDropdownbyNum: selectDropdownbyNum,
        expectSelectorContainsText: expectSelectorContainsText,
        resetTestData: resetTestData
    };
};

module.exports = util;
