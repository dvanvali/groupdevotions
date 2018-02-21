app.angularApp.controller('HomeCtrl', ['$scope', 'StudyService', '$location', '$timeout',
    function ($scope, StudyService, $location, $timeout) {
        $scope.studies = undefined;
        $scope.loading = false;

        $scope.loadStudies = function () {
            $scope.loading = true;
            StudyService.loadEntities({}, undefined, function (entities) {
                $scope.loading = false;
                $scope.studies = entities;
            });
        };

        $scope.login = function () {
            $location.path('/login');
        };
    }]);