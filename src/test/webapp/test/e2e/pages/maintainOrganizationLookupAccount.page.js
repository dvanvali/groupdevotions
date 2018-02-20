var maintainOrganizationLookupAccountPage = function () {
    var name = by.model('account.name');
    var email = by.model('account.email');
    var saveButton= by.id('saveAddAccount');
    var cancelButton = by.id('cancelAddAccount');

    return {
        name: name,
        email: email,
        saveButton: saveButton,
        cancelButton: cancelButton
    };
};


module.exports = maintainOrganizationLookupAccountPage;