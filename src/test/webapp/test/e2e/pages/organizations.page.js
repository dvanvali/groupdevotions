var organizationsPage = function () {
    var title = by.tagName('h1');
    var editButton= by.tagName('a');
    var addButton = by.id('addButton');
    var organizationsRepeater = by.repeater('organization in organizations');

    return {
        title: title,
        editButton: editButton,
        addButton: addButton,
        organizationsRepeater: organizationsRepeater,

        get: function () {
            browser.setLocation('/organizations');
        },

        add: function () {
            element(addButton).click();
            browser.waitForAngular();
        }
    };
};


module.exports = organizationsPage;
