app.angularApp.controller('LookupAccountCtrl', ['$scope', '$uibModalInstance', 'AdminAccountService', 'GlobalService', 'organizationKey',
    function ($scope, $uibModalInstance, AdminAccountService, GlobalService, organizationKey) {
    $scope.message = {};
    $scope.account = {adminOrganizationKey: organizationKey};

    $scope.save = function () {
        if (!$scope.account.email) {
            $scope.message = GlobalService.danger('Please enter an email address.');
        }

        AdminAccountService.resetLoad();
        AdminAccountService.loadEntities({email: $scope.account.email}, $scope.message, function (resultingAccounts) {
            var accountToSave;
            if (resultingAccounts && resultingAccounts.length > 0) {
                if (resultingAccounts[0].adminOrganizationKey === organizationKey) {
                    $scope.message = GlobalService.info('This user is already an administrator for your organization.');
                    return;
                } else if (resultingAccounts[0].adminOrganizationKey) {
                    $scope.message = GlobalService.info('This user is already an administrator for another organization.  ' +
                        'A user can not be an administrator for two organizations.');
                    return;
                }
                accountToSave = resultingAccounts[0];
                accountToSave.adminOrganizationKey = organizationKey;
            } else {
                if (!$scope.account.name) {
                    $scope.message = GlobalService.danger('Please enter a name.');
                    return;
                }
                accountToSave = $scope.account;
            }

            AdminAccountService.saveEntity(accountToSave, function(account) {
                $uibModalInstance.close(account);
            }, $scope.message);
        });

    };

    $scope.cancel = function () {
        $uibModalInstance.dismiss('cancel');
    };
}]);