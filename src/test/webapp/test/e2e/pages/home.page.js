var homePage = function () {
    var that = this;

    var loginLink = by.id("loginButton");
    var logoutButton = by.id("logoutButton");
    var dropdownMenu = by.id('dropdown');
    var settingsMenuItem = by.id('settingsMenuItem');
    var organizationsMenuItem = by.id('organizationsMenuItem');
    var administrationMenuItem = by.id('administrationMenuItem');
    var studiesMenuItem = by.id('studiesMenuItem');

    var selectMenuItem = function (menuItem) {
        element(dropdownMenu).click();
        element(menuItem).click();
        browser.waitForAngular();
    };

    return {
        loginLink: loginLink,
        logoutButton: logoutButton,
        dropdownMenu: dropdownMenu,
        settingsMenuItem: settingsMenuItem,
        organizationsMenuItem: organizationsMenuItem,
        administrationMenuItem: administrationMenuItem,

        clickLogin: function () {
            browser.get("/");
            element(loginLink).click();
            browser.waitForAngular();
            expect(browser.getLocationAbsUrl()).toBe('/login');
        },

        getOrganizationsPage: function () {
            selectMenuItem(organizationsMenuItem);
        },

        getAdministrationPage: function () {
            selectMenuItem(administrationMenuItem);
        },

        getStudiesPage: function () {
            selectMenuItem(studiesMenuItem);
        }
    };
};

module.exports = homePage;
