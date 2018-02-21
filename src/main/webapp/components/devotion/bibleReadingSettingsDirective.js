app.directiveController.appBibleReadingSettings = function ($scope, $element, $attrs, $transclude, StudyService) {
    var settings = this;
    settings.loaded = false;
    settings.readingList = [];
    settings.message = {};
    StudyService.loadEntityToEdit(settings.studyKey, function() {
        var study = StudyService.getEntityToEdit();
        settings.readingList = study.dailyReadingList.split('\n');
        if (settings.groupMember.lastCompletedBibleReadingIndex == undefined) {
            settings.groupMember.lastCompletedBibleReadingIndex = "-1";
        }
        settings.dailyReadingStartsEachMonth = study.dailyReadingStartsEachMonth;
        settings.loaded = true;
    }, settings.message);
};

app.angularApp.directive("appBibleReadingSettings", ["StudyService", function (StudyService) {
        return {
            restrict: 'E',
            scope: {
                studyKey: '@',
                groupMember: '='
            },
            templateUrl: 'components/devotion/appBibleReadingSettings.html',
            controller: app.directiveController.appBibleReadingSettings,
            controllerAs: 'settings',
            bindToController: true
        };
    }]);