app.angularApp.controller('ContactUsCtrl', ['$scope', '$uibModalInstance', 'AccountService', 'GlobalService',
    function ($scope, $uibModalInstance, AccountService, GlobalService) {
        $scope.message = {};
        $scope.contact = {};

        $scope.send = function () {
            $scope.isDisabled = true;
            AccountService.contactUs($scope.contact,
                function (response) {
                    $scope.isDisabled = false;
                    $scope.message = response.message;
                    if (response.operationSuccessful) {
                        $scope.cancel();
                    }
                },
                function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                    $scope.isDisabled = false;
                });
        };

        $scope.cancel = function () {
            $uibModalInstance.dismiss('cancel');
        };
    }]);