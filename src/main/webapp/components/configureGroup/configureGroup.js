app.angularApp.controller('ConfigureGroupCtrl', ['$scope', '$location', 'GlobalService', 'StudyService', 'GroupService', 'GroupMemberService', 'AccountService',
    function ($scope, $location, GlobalService, StudyService, GroupService, GroupMemberService, AccountService) {
        $scope.userInfo = GlobalService.getUserInfo();
        if (!$scope.userInfo) {
            // Not logged in - maybe in the middle of configuring their group
            $location.path('/');
            return;
        }

        if (GlobalService.isGroupMember()) {
            GlobalService.showLoggedInHomePage();
            return;
        }

        $scope.message = {};
        $scope.loading = true;
        $scope.group = GlobalService.buildDefaultGroup();
        $scope.accountNameMissing = !$scope.userInfo.account.name;
        $scope.invitedGroupMember = {status: 'NONE'};

         // Using null for accountKey will load public studies
        StudyService.loadEntities({accountKey: null}, $scope.message, function (studies) {
            $scope.loading = false;
            $scope.studies = studies;
        });

        var valid = function () {
            var result = true;
            if (!$scope.userInfo.account.name) {
                $scope.message = GlobalService.danger("Please enter your name to continue.");
                result = false;
            } else if (!$scope.group.studyKey) {
                $scope.message = GlobalService.danger("Please select a study to continue.");
                result = false;
            } else if ($scope.invitedGroupMember.name && !$scope.invitedGroupMember.email || !$scope.invitedGroupMember.name && $scope.invitedGroupMember.email) {
                $scope.message = GlobalService.danger("When inviting another person, you must enter both a name and email address.");
                result = false;
            } else if ($scope.invitedGroupMember.email && $scope.invitedGroupMember.email.indexOf('@') < 0) {
                $scope.message = GlobalService.danger("The email address is not valid.");
                result = false;
            } else if ($scope.invitedGroupMember.email == $scope.userInfo.account.email) {
                $scope.message = GlobalService.danger("You have entered your own email address.  The email address is intended for an invitation to another person.");
                result = false;
            }

            return result;
        };

        var saveStep4SendInvite = function () {
            if ($scope.invitedGroupMember.name && $scope.invitedGroupMember.email && $scope.group.key) {
                $scope.invitedGroupMember.groupKey = $scope.group.key;

                GroupMemberService.saveEntity($scope.invitedGroupMember, function (newGroupMember) {
                        if (!$scope.message || !$scope.message.text) {
                            $scope.message = {};
                            GlobalService.showLoggedInHomePage();
                        }
                    },
                    $scope.message);
            } else {
                GlobalService.showLoggedInHomePage();
            }
        };

        var saveStep3UpdateAccount = function(groupMember) {
            $scope.userInfo.account.groupMemberKey = groupMember.key;
            var response = AccountService.saveSettings($scope.userInfo.account, function () {
                if ($scope.message) {
                    $scope.message = response.message;
                }
                if (response.operationSuccessful) {
                    GlobalService.setUserInfo($scope.userInfo);
                    saveStep4SendInvite();
                }
            }, function () {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
            });
        };

        var saveStep2SaveGroupMember = function (updatedGroup) {
            var groupMember = {};
            groupMember.accountKey = $scope.userInfo.account.key;
            groupMember.groupKey = $scope.group.key;
            groupMember.email = $scope.userInfo.account.email;
            groupMember.name = $scope.userInfo.account.name;
            groupMember.status = 'JOINED';
            groupMember.groupAdmin = 'true';

            GroupMemberService.saveEntity(groupMember, function (updatedGroupMember) {
                if (!$scope.message || !$scope.message.text) {
                    $scope.message = {};
                    saveStep3UpdateAccount(updatedGroupMember);
                }
            },
            $scope.message);
        };

        $scope.save = function () {
            // Validates, saves the group, sends the invite upon success
            if (valid()) {
                //$scope.group.name = $scope.userInfo.account.name + "'s Group";
                //$scope.group.description = $scope.userInfo.account.name + "'s Group";

                GroupService.saveEntity($scope.group, function (updatedGroup) {
                        if (!$scope.message || !$scope.message.text) {
                            $scope.message = {};
                            $scope.group = updatedGroup;
                            saveStep2SaveGroupMember();
                        }
                },
                $scope.message)
            }
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function () {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };
    }]);
