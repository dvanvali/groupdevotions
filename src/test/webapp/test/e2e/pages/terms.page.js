var termsPage = function () {
    var that = this;

    var title = by.css(".lessonTitle");
    var agreeButton = by.id("agreeButton");
    var disagreeButton = by.id('disagreeButton');

    return {
        title: title,
        agreeButton: agreeButton,
        disagreeButton: disagreeButton
        };
};

module.exports = termsPage;
