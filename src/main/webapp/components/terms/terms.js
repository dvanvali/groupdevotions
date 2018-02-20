app.angularApp.controller('TermsCtrl', ['$scope', 'ConfigService', 'GlobalService', '$location',
    function ($scope, ConfigService, GlobalService, $location) {
        $scope.buttonsDisabled = true;
        $scope.message = undefined;
        $scope.terms = '';

        var response = ConfigService.terms({}, function () {
            $scope.message = response.message;
            if (response.operationSuccessful) {
                $scope.terms = response.data.replace(/(?:\r\n|\r|\n)/g, '<br />');
                $scope.buttonsDisabled = false;
            }
            if (response.location) {
                $location.path('/' + response.location);
            }
        }, function () {
            $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
        });

        $scope.agree = function () {
            $scope.buttonsDisabled = true;
            $scope.message = undefined;
            var response = ConfigService.agree({}, function () {
                $scope.buttonsDisabled = false;
                $scope.message = response.message;
                if (response.location) {
                    $location.path('/' + response.location);
                } else if (response.operationSuccessful) {
                    GlobalService.showLoggedInHomePage();
                }
            }, function () {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                $scope.buttonsDisabled = false;
            });
        };

        $scope.disagree = function () {
            $scope.message = GlobalService.danger('You are not allowed to use GroupDevotions unless you agree to the terms of service.');
        };
    }]);
