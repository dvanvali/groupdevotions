app.angularApp.controller('ExportStudyCtrl', ['$scope', '$location', 'LessonService', 'StudyService', function($scope, $location, LessonService, StudyService) {
    $scope.message = {};
    $scope.study = StudyService.getEntityToEdit();
    $scope.loading = true;
    $scope.studyLessons = [];

    var loadLesson = function(index) {
        LessonService.loadEntityToEdit($scope.study.studyLessonInfos[index].studyLessonKey, function() {
            $scope.studyLessons.push(LessonService.getEntityToEdit());
            if (index+1 === $scope.study.studyLessonInfos.length) {
                $scope.loading = false;
            } else {
                loadLesson(++index);
            }
        }, $scope.message);
    };

    var loadLessons = function() {
        $scope.message = {};
        loadLesson(0);
    };

    loadLessons();
}]);


