'use strict';

/* Controllers */

app.angularApp.controller('SettingsCtrl', ['$scope', '$location', '$timeout', 'GlobalService', 'AccountService', 'StudyService',
    function ($scope, $location, $timeout, GlobalService, AccountService, StudyService) {
        $scope.groupMemberEmails = undefined;
        $scope.availableStudies = [];
        $scope.forms = {};
        $scope.LOADING = 0;
        $scope.SETTINGS = 1;
        $scope.PWCHANGE = 2;
        $scope.FAILEDTOINITIALIZE = 3;
        $scope.mode = $scope.LOADING;
        $scope.passwordChange = {password1: "", password2: ""};
        $scope.message = {};

        $scope.userInfo = jQuery.extend(true, {}, GlobalService.getUserInfo());
        GlobalService.setUserInfoChangeListener(function (userInfo) {
            $scope.userInfo = userInfo;
        });

        $scope.saveLabel = 'Save';
        if (!$scope.userInfo.account.settingsConfirmed) {
            $scope.saveLabel = 'Continue';
        }

        var markInUseStudies = function () {
            _.each($scope.availableStudies, function (study) {
                _.each($scope.userInfo.account.studyKeyPublicAccepts, function (acceptedStudyKey) {
                    if (study.key == acceptedStudyKey) {
                        study.subscribed = true;
                    }
                });
            });
        };

        if ($scope.userInfo == undefined) {
            $location.path('/home');
        } else {
            //StudyService.resetLoad();
            StudyService.loadEntities({accountKey: $scope.userInfo.account.key, loadPublicStudies: true}, $scope.message, function (studies) {
                $scope.loading = false;
                $scope.availableStudies = studies;
                markInUseStudies();
                $scope.mode = $scope.SETTINGS;
            });
        }

        $scope.onStudyCheckboxChange = function (study) {
            if (study.subscribed) {
                $scope.userInfo.account.studyKeyPublicAccepts.push(study.key);
            } else {
                var index = jQuery.inArray(study.key, $scope.userInfo.account.studyKeyPublicAccepts);
                if (index > -1) {
                    $scope.userInfo.account.studyKeyPublicAccepts.splice(index, 1);
                }
            }
        };

        $scope.save = function () {
            $scope.message = undefined;
            if (!$scope.userInfo.account.name) {
                $scope.message = GlobalService.danger('Please enter your first name.');
            } else {
                var response = AccountService.saveSettings($scope.userInfo.account, function () {
                    $scope.message = response.message;
                    if (response.operationSuccessful) {
                        $scope.userInfo.account.settingsConfirmed = true;
                        GlobalService.setUserInfo($scope.userInfo);
                        GlobalService.showLoggedInHomePage();
                    }
                    GlobalService.setLocation(response.location);
                }, function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                    $scope.mode = $scope.FAILEDTOINITIALIZE;
                });
            }
        };

        $scope.cancel = function () {
            $scope.userInfo = jQuery.extend(true, {}, GlobalService.getUserInfo());
            markInUseStudies();
            GlobalService.showLoggedInHomePage();
        };

        $scope.displayPwChange = function () {
            $scope.passwordChange.password1 = "";
            $scope.passwordChange.password2 = "";
            $scope.mode = $scope.PWCHANGE;
            GlobalService.setFocus("#password1");
        };

        $scope.cancelPwChange = function () {
            $scope.mode = $scope.SETTINGS;
            $scope.message = undefined;
        };

        $scope.savePassword = function () {
            $scope.message = undefined;
            if ($scope.passwordChange.password1 == "" || $scope.passwordChange.password1.length < 6) {
                $scope.message = GlobalService.danger('Please enter a password that is at least 6 characters long.');
            } else if ($scope.passwordChange.password2 == "") {
                $scope.message = GlobalService.danger('Please enter your password a second time.');
            } else if ($scope.passwordChange.password1 != $scope.passwordChange.password2) {
                $scope.message = GlobalService.danger('The two passwords are not the same.');
            } else {
                var response = AccountService.changePassword($scope.passwordChange, function () {
                    $scope.message = response.message;
                    if (response.operationSuccessful) {
                        $scope.cancelPwChange();
                    }
                    GlobalService.setLocation(response.location);
                }, function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                    $scope.mode = $scope.FAILEDTOINITIALIZE;
                });
            }
        };

        // Services will set message tryAgain() functions upon failure.  The page can call it if set
        $scope.tryAgain = function () {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };


    }]);
