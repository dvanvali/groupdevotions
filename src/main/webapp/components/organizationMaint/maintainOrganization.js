app.angularApp.controller('MaintainOrganizationCtrl', ['$scope', '$location', '$uibModal', 'OrganizationService', 'AdminAccountService', 'GlobalService',
    function($scope, $location, $uibModal, OrganizationService, AdminAccountService, GlobalService) {
        $scope.message = {};
        $scope.userInfo = GlobalService.getUserInfo();

        if ($scope.userInfo.account.siteAdmin) {
            $scope.organization = OrganizationService.getEntityToEdit();

            if (!$scope.organization.key) {
                $scope.organization = {};
            } else {
                $scope.loading = true;
                AdminAccountService.loadEntities({organizationId: $scope.organization.key}, $scope.message, function (result) {
                    $scope.accounts = result;
                    $scope.loading = false;
                });
            }
        } else {
            $scope.loading = true;
            AdminAccountService.loadEntities({organizationId: $scope.userInfo.account.adminOrganizationKey}, $scope.message, function (result) {
                $scope.accounts = result;
                $scope.loading = false;
            });
        }

        var onSuccessReturnToOrganizations = function() {
            OrganizationService.setEntityToEdit(undefined);
            $location.path('/organizations');
        };

        $scope.save = function() {
            OrganizationService.saveEntity($scope.organization, onSuccessReturnToOrganizations, $scope.message);
        };

        $scope.cancel = function() {
            OrganizationService.cancelEdit($scope.organization, onSuccessReturnToOrganizations, $scope.message);
        };

        $scope.tryAgain = function() {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };

        $scope.addAccount = function () {
            var modalInstance = $uibModal.open({
                animation: false,
                templateUrl: '/components/organizationMaint/lookupAccount.html',
                controller: 'LookupAccountCtrl',
                size: 'md',
                resolve: {
                    organizationKey: function () {
                        return $scope.organization ? $scope.organization.key : $scope.userInfo.account.adminOrganizationKey;
                    }
                }
            });

            modalInstance.result.then(function (account) {
                $scope.accounts.push(account);
            }, function () {
                // Closed
            });
        };
    }]);
