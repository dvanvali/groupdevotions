var lessonPage = function () {
    var that = this;

    var pageTitle = by.tagName('h1');
    var title= by.id('title');
    var month = by.id('month');
    var day = by.id('day');
    var addSection = by.css('addSection');
    var deleteSection = by.css('deleteSection');
    var sectionType = by.id('sectionType');
    var questionTextbox = by.css('.questionTextbox');
    var saveButton = by.id('saveButton');
    var cancelButton = by.id('cancelButton');
    var deleteButton = by.id('deleteButton');
    var sectionRepeater = by.id('section in lesson.studySections track by $index');

    return {
        pageTitle: pageTitle,
        title: title,
        month: month,
        day: day,
        addSection: addSection,
        deleteSection: deleteSection,
        sectionType: sectionType,
        questionTextbox: questionTextbox,
        saveButton: saveButton,
        cancelButton: cancelButton,
        deleteButton: deleteButton,
        sectionRepeater: sectionRepeater
    };
};

module.exports = lessonPage;