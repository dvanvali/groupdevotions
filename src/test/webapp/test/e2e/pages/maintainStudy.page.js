var maintainStudyPage = function () {
    var that = this;

    var title = by.tagName('h1');
    var studyTitle = by.id('title');
    var author = by.id('author');
    var purpose = by.id('purpose');
    var copyright = by.id('copyright');
    var studyType = by.id('studyType');
    var rssUrl = by.id('rssUrl');
    var dailyReadingStartingDate = by.id('dailyReadingStartingDate');
    var dailyReadingStartsEachMonthYes = by.id('dailyReadingStartsEachMonthYes');
    var dailyReadingStartsEachMonthNo = by.id('dailyReadingStartsEachMonthNo');
    var dailyReadingList = by.id('dailyReadingList');
    var publicStudyYes = by.id('publicStudyYes');
    var publicStudyNo = by.id('publicStudyNo');
    var saveButton = by.id('saveButton');
    var cancelButton = by.id('.cancelButton');

    return {
        title: title,
        studyTitle: studyTitle,
        author: author,
        purpose: purpose,
        copyright: copyright,
        studyType: studyType,
        rssUrl: rssUrl,
        dailyReadingStartingDate: dailyReadingStartingDate,
        dailyReadingStartsEachMonthYes: dailyReadingStartsEachMonthYes,
        dailyReadingStartsEachMonthNo: dailyReadingStartsEachMonthNo,
        dailyReadingList: dailyReadingList,
        publicStudyYes: publicStudyYes,
        publicStudyNo: publicStudyNo,
        saveButton: saveButton,
        cancelButton: cancelButton
    };
};

module.exports = maintainStudyPage;