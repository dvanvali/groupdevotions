app.angularApp.controller('LoginCtrl', ['$scope', '$location', '$window', '$timeout', 'AccountService', 'GlobalService',
    function ($scope, $location, $window, $timeout, AccountService, GlobalService) {
        var getCookie = function(cname) {
            var name = cname + "=";
            var ca = $window.document.cookie.split(';');
            for(var i=0; i<ca.length; i++) {
                var c = ca[i];
                while (c.charAt(0)==' ') c = c.substring(1);
                if (c.indexOf(name) == 0) return c.substring(name.length,c.length);
            }
            return "";
        };
        var renderRecaptcha = function () {
            if (!window.GDRecaptLoaded) {
                $timeout(renderRecaptcha, 300);
            } else {
                if ($location.host() === 'localhost') {
                    // Googles test key
                    grecaptcha.render("g-recaptcha", {sitekey: "6LeIxAcTAAAAAJcZVRqyHh71UMIEGNQ_MXjiZKhI"});
                } else {
                    grecaptcha.render("g-recaptcha", {sitekey: "6LdJHRYTAAAAAMVa2u8NqvzdQRQfHso2kA3caylk"});
                }
            }
        };

        var setCookie = function(cname, cvalue, exdays) {
            var d = new Date();
            d.setTime(d.getTime() + (exdays*24*60*60*1000));
            var expires = "expires="+d.toUTCString();
            $window.document.cookie = cname + "=" + cvalue + "; " + expires;
        };

        var emptyForm = function () {
            return {signInEmail: '', signInPassword: '', signInPassword2: '', name: '', stayLoggedIn: false, rememberMe: false}
        };

        $scope.form = emptyForm();
        var emailCookie = getCookie('signInEmail');
        if (emailCookie) {
            $scope.form.signInEmail = emailCookie;
            $scope.form.rememberMe = true;
            GlobalService.setFocus('#loginPassword');
        }

        $scope.message = undefined;
        $scope.isDisabled = false;

        var requestingUrl = GlobalService.getFirstUrl();
        $scope.groupInvite = requestingUrl && (requestingUrl.indexOf('groupInvite') > 0);
        if ($scope.groupInvite) {
            $scope.form.signInEmail = decodeURIComponent(requestingUrl.substr(requestingUrl.indexOf('email=')+6));
        }
        var emailConfirmation = requestingUrl && (requestingUrl.indexOf('newaccount') > 0);
        if (!$scope.groupInvite) {
            $timeout(renderRecaptcha, 0);
        }
        $scope.showLogin = !$scope.groupInvite;
        $scope.showForgot = false;
        $scope.showCreate = $scope.groupInvite;
        if ($scope.groupInvite) {
            $scope.message = GlobalService.info('To accept your group invitation, please create an account.');
        } else if (emailConfirmation) {
            $scope.message = GlobalService.info('Please login to finish the registration process.');
        }

        $scope.login = function () {
            $scope.message = GlobalService.info('Please wait...');
            $scope.isDisabled = true;
            var loginResult = AccountService.login([], {
                    email: $scope.form.signInEmail,
                    password: $scope.form.signInPassword,
                    url: GlobalService.getFirstUrl(),
                    stayLoggedIn: $scope.form.stayLoggedIn
                },
                function () {
                    $scope.isDisabled = false;
                    $scope.message = loginResult.message;
                    if (loginResult.operationSuccessful) {
                        if ($scope.form.rememberMe) {
                            setCookie('signInEmail', $scope.form.signInEmail, 9999);
                        }
                        $scope.form = emptyForm();
                        GlobalService.setUserInfo(loginResult.data);
                        if (loginResult.location) {
                            GlobalService.setLocation(loginResult.location);
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

        $scope.googleLogin = function () {
            $scope.message = GlobalService.info('Redirecting to Google...');
            $scope.isDisabled = true;
            var userInfo = GlobalService.getUserInfo();
            $window.location.href = userInfo.googleSignInUrl;
        };

        $scope.displayForgot = function () {
            $scope.showLogin = false;
            $scope.showForgot = true;
            $scope.form.signInPassword = '';
        };

        $scope.cancelForgot = function () {
            $scope.showLogin = true;
            $scope.showForgot = false;
        };

        $scope.displayCreate = function () {
            $scope.showLogin = false;
            $scope.showCreate = true;
            $scope.form.signInPassword = '';
            $scope.form.signInPassword2 = '';
            $scope.form.name = '';
        };

        $scope.cancelCreate = function () {
            $scope.showLogin = true;
            $scope.showCreate = false;
            $scope.form.signInPassword = '';
            $scope.form.signInPassword2 = '';
            $scope.form.name = '';
        };

        $scope.forgot = function () {
            $scope.message = GlobalService.info('Please wait...');
            $scope.isDisabled = true;
            var loginResult = AccountService.forgotYourPassword([], {email: $scope.form.signInEmail},
                function () {
                    $scope.isDisabled = false;
                    $scope.message = loginResult.message;
                    if (loginResult.operationSuccessful) {
                        $scope.form = emptyForm();
                        GlobalService.setUserInfo(loginResult.data);
                        if (loginResult.location) {
                            GlobalService.setLocation(loginResult.location);
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

        $scope.createGoogleNew = function () {
            $window.location.href = "https://www.google.com/accounts/NewAccount";
        };

        $scope.create = function () {
            $scope.message = GlobalService.info('Please wait...');
            $scope.isDisabled = true;
            var loginResult = AccountService.createAccount([], {
                    email: $scope.form.signInEmail,
                    password: $scope.form.signInPassword,
                    password2: $scope.form.signInPassword2,
                    name: $scope.form.name,
                    url: GlobalService.getFirstUrl(),
                    recaptcha: (angular.isUndefined(grecaptcha) || $scope.groupInvite? '' : grecaptcha.getResponse())
                },
                function () {
                    $scope.isDisabled = false;
                    $scope.message = loginResult.message;
                    if (!$scope.groupInvite) {
                        grecaptcha.reset();
                    }
                    if (loginResult.operationSuccessful) {
                        $scope.form = emptyForm();
                        GlobalService.clearFirstUrl();
                        GlobalService.setUserInfo(loginResult.data);
                        if (loginResult.location) {
                            GlobalService.setLocation(loginResult.location);
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

        $scope.rememberMeChanged = function() {
            if (!$scope.form.rememberMe) {
                setCookie('signInEmail', '', -1);
                $scope.form.stayLoggedIn = false;
            }
        };

    }]);
