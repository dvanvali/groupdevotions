var maintainGroupPage = function () {
    var that = this;

    var pageTitle = by.tagName('h1');
    var groupTitle= by.id('title');
    var study = by.id('study');
    var addGroup = by.id('addGroup');
    var saveButton = by.id('saveButton');
    var cancelButton = by.id('cancelButton');

    return {
        pageTitle: pageTitle,
        groupTitle: groupTitle,
        study: study,
        addGroup: addGroup,
        saveButton: saveButton,
        cancelButton: cancelButton
    };
};

module.exports = maintainGroupPage;
