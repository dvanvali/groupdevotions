app.angularApp.controller('MaintainStudyCtrl', ['$scope', '$location', 'StudyService', function($scope, $location, StudyService) {
    $scope.message = {};
    $scope.study = StudyService.getEntityToEdit();

    var onSuccessReturnToStudies = function() {
        StudyService.setEntityToEdit(undefined);
        $location.path('/studies');
    };

    $scope.save = function() {
        StudyService.saveEntity($scope.study, onSuccessReturnToStudies, $scope.message);
    };

    $scope.cancel = function() {
        StudyService.cancelEdit($scope.study, onSuccessReturnToStudies, $scope.message);
    };

    $scope.tryAgain = function() {
        if ($scope.message.text && $scope.message.tryAgain) {
            $scope.message.tryAgain();
        }
    };
}]);


