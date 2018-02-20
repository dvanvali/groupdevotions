app.angularApp.controller('RefreshDevotionalCtrl', ['$scope', '$uibModalInstance', 'AccountService', 'GlobalService',
    function ($scope, $uibModalInstance, AccountService, GlobalService) {
        $scope.refreshDevotional = function () {
            $uibModalInstance.close('refresh');
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);
