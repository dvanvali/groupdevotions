app.angularApp.controller('MembersCtrl', ['$scope', '$location', '$timeout', 'GlobalService', 'GroupMemberService','GroupService',
    function ($scope, $location, $timeout, GlobalService, GroupMemberService, GroupService) {
        $scope.loading = true;
        $scope.message = {};
        $scope.userInfo = GlobalService.getUserInfo();
        GlobalService.setUserInfoChangeListener(function(userInfo) {
            $scope.userInfo = userInfo;
        });
        $scope.members = [];
        $scope.currentMember = undefined;
        // If GroupService is in the middle of editing, then load members for it.  Otherwise
        // it will load the members for the current account's groupMemberKey.  group will be null for that case.
        $scope.group = GroupService.getEntityToEdit();

        var loadMembers = function() {
            $scope.message = {};
            var parameters = {};
            if ($scope.group && $scope.group.key) {
                parameters = {groupKey:$scope.group.key};
            }
            GroupMemberService.loadEntities(parameters, $scope.message, function(members) {
                $scope.loading = false;
                $scope.members = members;
            });
        };

        loadMembers();

        $scope.returnDescription = 'Return to Group';
        if ($scope.userInfo.account.siteAdmin || $scope.userInfo.account.adminOrganizationKey) {
            $scope.returnDescription += 's';
        }

        $scope.return = function() {
            if ($scope.userInfo.account.siteAdmin || $scope.userInfo.account.adminOrganizationKey) {
                $location.path('/groups');
            } else {
                if (GlobalService.isScreenDesktop()) {
                    GlobalService.showLoggedInHomePage();
                } else {
                    $location.path('/blog');
                }
            }
        };

        var savedIndex;
        $scope.editMember  = function(member) {
            savedIndex = $scope.members.indexOf(member);
            $scope.currentMember = jQuery.extend(true, {}, member);
            GlobalService.setFocus();
        };

        var adding = false;
        $scope.addMember = function() {
            if (!adding) {
                // Need to be able to see the new row when we are done.
                $scope.filterValue = "";
                adding = true;
                $scope.currentMember = {mode: $scope.EDIT, key: undefined, name: '', email: ''};
                if ($scope.group.ownerOrganizationKey && $scope.group.defaultOrgAccountabilityEmail) {
                    $scope.currentMember['accountabilityEmails'] = [$scope.group.defaultOrgAccountabilityEmail];
                }
                // If group is set, then the siteAdmin is adding a GroupMember.
                $scope.currentMember.groupAdmin = false;
                if ($scope.group && $scope.group.key) {
                    $scope.currentMember.groupKey = $scope.group.key;
                    if (!$scope.group.groupMemberActivities || $scope.group.groupMemberActivities.length == 0) {
                        // First one added should be defaulted to be an admin
                        $scope.currentMember.groupAdmin = true;
                    }
                }
                GlobalService.setFocus();
            }
            adding = false;
        };

        $scope.cancelEditMember = function(member) {
            $scope.message = {};
            $scope.currentMember = undefined;
        };

        $scope.saving = false;
        $scope.saveMember = function() {
            if (!$scope.saving) {
                $scope.saving = true;
                $scope.message = {};
                GroupMemberService.saveEntity($scope.currentMember, function (updatedGroupMember) {
                        // since the data was loaded into the service, the new data will show up in the array automatically.
                        // See saveEntity().
                    $scope.currentMember = undefined;
                    $scope.saving = false;
                }, $scope.message,
                function () {
                    $scope.saving = false;
                });
            }
        };

        $scope.deleteMember = function () {
            if (!$scope.saving) {
                $scope.saving = true;
                var index = _.indexOf($scope.members, $scope.currentMember);
                $scope.message = {};
                GroupMemberService.deleteEntity($scope.currentMember, function () {
                        // since the data was loaded into the service, the new data will show up in the array automatically.
                        // See saveEntity().
                        $scope.currentMember = undefined;
                        $scope.saving = false;
                    }, $scope.message,
                    function () {
                        $scope.saving = false;
                    });
            }
        };

        $scope.editGroup = function() {
            if (!$scope.group) {
                GroupService.loadEntityToEdit($scope.userInfo.groupMember.groupKey, function () {
                    $location.path('/maintainGroup');
                }, $scope.message);
            } else {
                $location.path('/maintainGroup');
            }
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function () {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };
    }]);