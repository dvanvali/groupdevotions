app.angularApp.controller('MaintainGroupCtrl', ['$scope', '$location', 'GroupService', 'StudyService', 'GlobalService', 'AccountService',
    function($scope, $location, GroupService, StudyService, GlobalService, AccountService) {
    $scope.loadingStudies = true;
    $scope.message = {};
    $scope.group = GroupService.getEntityToEdit();

    if (!$scope.group.key) {
        $scope.group = GlobalService.buildDefaultGroup();
    }

    StudyService.loadEntities({accountKey: GlobalService.getUserInfo().account.key, loadPublicStudies: true}, $scope.message, function(studies) {
        $scope.loadingStudies = false;
        $scope.studies = studies;
    });

    var onSuccessReturnToGroups = function(group) {
        // save cleared the entity to edit, so set back for member display
        if (group) {
            GroupService.setEntityToEdit(group);
        }
        $location.path('/members');
    };

    $scope.save = function() {
        GroupService.saveEntity($scope.group, onSuccessReturnToGroups, $scope.message);
    };

    $scope.cancel = function() {
        GroupService.cancelEdit($scope.group, onSuccessReturnToGroups, $scope.message);
    };

    $scope.tryAgain = function() {
        if ($scope.message.text && $scope.message.tryAgain) {
            $scope.message.tryAgain();
        }
    };
}]);
