var studiesPage = function () {
    var that = this;

    var title = by.tagName('h1');
    var addStudyButton = by.id('addStudy');
    var editButton = by.css('.editButton');
    var addLessonButton = by.css('.addLesson');
    var editLessonButton = by.id('.editLesson');
    var addAccountabilityButton = by.id('.addAccountability');
    var editAccountabilityButton = by.id('.editAccountability');
    var studiesRepeater = by.repeater('study in studies');
    var lessonsRepeater = by.repeater("lessonInfo in study.studyLessonInfos | filter:filterRecentItems | orderBy:'month*31+day'");

    return {
        title: title,
        addStudyButton: addStudyButton,
        editButton: editButton,
        addLessonButton: addLessonButton,
        editLessonButton: editLessonButton,
        addAccountabilityButton: addAccountabilityButton,
        editAccountabilityButton: editAccountabilityButton,
        studiesRepeater: studiesRepeater,
        lessonsRepeater: lessonsRepeater
    };
};

module.exports = studiesPage;