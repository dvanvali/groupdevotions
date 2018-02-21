var maintainOrganizationPage = function () {
    var title = by.tagName('h1');
    var name = by.id('name');
    var saveButton = by.id('saveButton');
    var addAccountButton = by.id('addAccountButton');
    var accountsRepeater = by.repeater("account in accounts | orderBy: 'name' track by account.key");

    return {
        title: title,
        name: name,
        saveButton: saveButton,
        addAccountButton: addAccountButton,
        accountsRepeater: accountsRepeater
    };
};


module.exports = maintainOrganizationPage;
