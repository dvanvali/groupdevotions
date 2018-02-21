app.angularApp.controller('StudiesCtrl', ['$scope', '$location', 'GlobalService', 'StudyService', 'LessonService',
    function ($scope, $location, GlobalService, StudyService, LessonService) {

        $scope.userInfo = GlobalService.getUserInfo();
        $scope.message = {};
        $scope.loading = true;
        $scope.settings = GlobalService.getSettings();
        if ($scope.settings['studyFilter'] === undefined) {
            $scope.settings.studyFilter = true;
        }

        var removeTimeFromDate = function (date) {
            return new Date(date.getYear(), date.getMonth(), date.getDate());
        };

        var today = removeTimeFromDate(new Date());
        var todayGetTime = today.getTime();
        var daysToView = 14;
        var filterEndGetTime = new Date(todayGetTime + (daysToView - 1) * 24 * 60 * 60 * 1000).getTime();

        var projectedLessonDateNeverInPast = function (item) {
            var date = new Date(today.getFullYear(), item.month - 1, item.day);
            if (date.getTime() < todayGetTime) {
                date = new Date(today.getFullYear() + 1, item.month - 1, item.day);
            }
            return date;
        };

        $scope.filterRecentItems = function (item) {
            if (!$scope.settings.studyFilter) {
                return true;
            }

            if (item.month) {
                var projectedLessonGetTime = projectedLessonDateNeverInPast(item).getTime();
                //console.log("Item day: " + item.day + " month: " + item.month + " projected: " + new Date(projectedLessonGetTime).getMonth()+1 + "/" + new Date(projectedLessonGetTime).getDate() + "/" + new Date(projectedLessonGetTime).getFullYear() );
                return projectedLessonGetTime >= todayGetTime && projectedLessonGetTime <= filterEndGetTime;
            } else {
                return true;
            }
        };

        var userInfo = GlobalService.getUserInfo();
        StudyService.resetLoad();
        StudyService.loadEntities({accountKey: userInfo.account.key}, $scope.message, function (studies) {
            $scope.loading = false;
            $scope.studies = studies;
        });

        $scope.editStudy = function (study) {
            StudyService.setEntityToEdit(study);
            $location.path('/maintainStudy');
        };

        $scope.editLessonInfo = function (lessonInfo) {
            LessonService.loadEntityToEdit(lessonInfo.studyLessonKey, function () {
                    $location.path('/maintainLesson');
                },
                $scope.message);
        };

        $scope.editAccountability = function (study) {
            if (study.accountabilityLessonKey) {
                LessonService.loadEntityToEdit(study.accountabilityLessonKey, function () {
                        $location.path('/maintainLesson');
                    },
                    $scope.message);
            } else {
                LessonService.setEntityToEdit({
                    key: undefined, studyKey: study.key, studySections: [],
                    title: 'Accountability Questions', accountabilityLesson: true
                });
                $location.path('/maintainLesson');
            }
        };

        $scope.addLessonInfo = function (study) {
            LessonService.setEntityToEdit({key: undefined, studyKey: study.key, studySections: []});
            $location.path('/maintainLesson');
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function () {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };

    }]);
