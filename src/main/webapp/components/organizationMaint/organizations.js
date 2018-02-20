app.angularApp.controller('OrganizationsCtrl', ['$scope', '$location', 'GlobalService', 'OrganizationService',
    function($scope, $location, GlobalService, OrganizationService) {

        $scope.userInfo = GlobalService.getUserInfo();
        $scope.message = {};
        $scope.loading = true;

        OrganizationService.loadEntities({}, $scope.message, function(result) {
            $scope.loading = false;
            $scope.organizations = result;
        });

        $scope.edit = function(organization) {
            OrganizationService.setEntityToEdit(organization);
            $location.path('/maintainOrganization');
        };

        $scope.editOrganizationAccounts = function(organization) {
            OrganizationService.setEntityToEdit(organization);
            OrganizationMemberService.resetLoad();
            $location.path('/maintainAccounts');
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function() {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };
    }]);