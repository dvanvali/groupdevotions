app.angularApp.controller('ResetPasswordCtrl', ['$scope', '$location', '$window', 'AccountService', 'GlobalService',
    function ($scope, $location, $window, AccountService, GlobalService) {
        $scope.form = {signInEmail: '', signInPassword: '', signInPassword2: ''};
        $scope.orgAdminRequest = GlobalService.getFirstUrl() ? GlobalService.getFirstUrl().includes('admin&') : false;

        $scope.resetYourPassword = function () {
            $scope.message = GlobalService.info('Please wait...');
            $scope.isDisabled = true;
            var loginResult = AccountService.resetPassword([], {
                    email: $scope.form.signInEmail,
                    password: $scope.form.signInPassword,
                    password2: $scope.form.signInPassword2,
                    url: GlobalService.getFirstUrl()
                },
                function () {
                    $scope.isDisabled = false;
                    $scope.message = loginResult.message;
                    if (loginResult.operationSuccessful) {
                        GlobalService.clearFirstUrl();
                        $scope.form.signInEmail = '';
                        $scope.form.signInPassword = '';
                        $scope.form.signInPassword2 = '';
                        $scope.resetSuccessful = true;
                        GlobalService.setUserInfo(loginResult.data);
                        if (loginResult.location) {
                            $location.path('/' + loginResult.location);
                        } else if (loginResult.data && loginResult.data.isSignedIn) {
                            GlobalService.showLoggedInHomePage();
                        }
                    }
                },
                function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                    $scope.isDisabled = false;
                });
        };

        $scope.google = function() {
            $scope.message = GlobalService.info('Redirecting to Google...');
            $scope.isDisabled = true;
            var userInfo = GlobalService.getUserInfo();
            $window.location.href = userInfo.googleSignInUrl;
        };

        $scope.login = function () {
            $location.path('/login');
        }
    }]);
