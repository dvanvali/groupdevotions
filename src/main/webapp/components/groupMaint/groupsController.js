app.angularApp.controller('GroupsCtrl', ['$scope', '$location', 'GlobalService', 'GroupService', 'GroupMemberService',
    function($scope, $location, GlobalService, GroupService, GroupMemberService) {

        $scope.userInfo = GlobalService.getUserInfo();
        $scope.message = {};
        $scope.loading = true;

        GroupService.loadEntities({accountKey: $scope.userInfo.account.key}, $scope.message, function(result) {
            $scope.loading = false;
            $scope.groups = result;
        });

        $scope.editGroup = function(group) {
            GroupService.setEntityToEdit(group);
            $location.path('/maintainGroup');
        };

        $scope.editGroupMembers = function(group) {
            GroupService.setEntityToEdit(group);
            GroupMemberService.resetLoad();
            $location.path('/members');
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function() {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };
    }]);