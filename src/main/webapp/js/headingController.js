'use strict';

app.angularApp.controller('HeadingCtrl', ['$scope', '$rootScope', '$location', '$window', '$timeout', '$uibModal', '$route', 'GlobalService', 'AccountService',
    function ($scope, $rootScope, $location, $window, $timeout, $uibModal, $route, GlobalService, AccountService) {
        $scope.loginShowing = true;
        $scope.checkingLogin = true;

        GlobalService.recordFirstUrl();

        var isLoggedIn = function() {
            return $scope.userInfo && $scope.userInfo.account;
        };

        var hideNavbarToggle = function () {
            var navbar = jQuery(".navbar-collapse");
            if (navbar && navbar.hasClass('in')) {
                navbar.collapse('hide');
            }
        };

        var safeApply = function () {
            var phase = $rootScope.$$phase;
            if (phase != '$apply' && phase != '$digest') {
                $scope.$apply();
            }
        };

        var calculateViewportSettings = function () {
            $scope.viewportSize = jQuery($window).width();
            $scope.tinyViewport = $scope.viewportSize < 450;
            var userInfo = GlobalService.getUserInfo();
            if (userInfo && userInfo.isDesktop) {
                $scope.tabMinWidth = 600;
            } else {
                $scope.tabMinWidth = 425;
            }
            safeApply();
        };

        angular.element($window).bind('resize', function (e) {
            calculateViewportSettings();
            hideNavbarToggle();
        });

        calculateViewportSettings();

        var removeActive = function () {
            // Not all of these have both a button and MenuItem, but that does not hurt
            var items = ['blog', 'desktop', 'devotion', 'groups', 'journal', 'organizations', 'logout', 'studies', 'settings'];
            _.forEach(items, function(item) {
                $('#' + item + 'Button').removeClass('active');
                $('#' + item + 'MenuItem').removeClass('active');
            });
        };

        $rootScope.$on("$routeChangeStart", function (event, next, current) {
            hideNavbarToggle();
            removeActive();
            if (next.templateUrl === "components/home/home.html") {
                var loggedIn = $scope.userInfo && $scope.userInfo.isSignedIn;
                $scope.loginShowing = !loggedIn;
            } else if (next.templateUrl === "components/login/login.html") {
                $scope.loginShowing = false;
            } else if (next.templateUrl === "partials/resetPassword.html") {
                $scope.loginShowing = true;
            } else if (next.templateUrl === "components/blog/blog.html") {
                $('#blogButton').addClass('active');
                $('#blogMenuItem').addClass('active');
            } else if (next.templateUrl === "partials/desktop.html") {
                $('#desktopButton').addClass('active');
            } else if (next.templateUrl === "components/devotion/devotion.html") {
                $('#devotionButton').addClass('active');
                $('#devotionMenuItem').addClass('active');
            } else if (next.templateUrl === "components/groupMaint/groups.html") {
                $('#groupsMenuItem').addClass('active');
            } else if (next.templateUrl === "components/journal/journal.html") {
                $('#journalButton').addClass('active');
                $('#journalMenuItem').addClass('active');
            } else if (next.templateUrl === "components/organizationMaint/organizations.html") {
                $('#organizationsButton').addClass('active');
                $('#organizationsMenuItem').addClass('active');
            } else if (next.templateUrl === "components/settings/settings.html") {
                $('#settingsButton').addClass('active');
                $('#settingsMenuItem').addClass('active');
            } else if (next.templateUrl === "components/studyMaint/studies.html") {
                $('#studiesButton').addClass('active');
                $('#studiesMenuItem').addClass('active');
            }
        });

        $scope.home = function () {
            $location.path('/home');
        };

        $scope.bannerClick = function () {
            $window.location = $window.location.origin;
        };

        $scope.blog = function () {
            if ($scope.showBlogMenuItem()) {
                $location.path('/blog');
            }
        };

        $scope.desktop = function () {
            if (GlobalService.isGroupMember()) {
                $location.path('/desktop');
            }
        };

        $scope.devotion = function () {
            if ($scope.showDevotionMenuItem()) {
                $location.path('/devotion');
            }
        };

        $scope.groups = function () {
            if ($scope.showGroupsMenuItem()) {
                $location.path('/groups');
            }
        };

        $scope.journal = function () {
            $location.path('/journal');
        };

        $scope.login = function () {
            $scope.loginShowing = false;
            $location.path('/login');
        };

        $scope.logout = function () {
            GlobalService.logout();
        };

        $scope.settings = function () {
            $location.path('/settings');
        };

        $scope.studies = function () {
            if ($scope.showStudiesMenuItem()) {
                $location.path('/studies');
            }
        };

        $scope.organizations = function () {
            if ($scope.showOrganizationsMenuItem()) {
                $location.path('/organizations');
            }
        };

        $scope.administration = function () {
            if ($scope.showAdministrationMenuItem()) {
                $location.path('/maintainOrganization');
            }
        };

        var menuItems = ['devotion', 'blog', 'journal'];
        $scope.swipeMoveLeftOneMenuItem = function () {
            if (!$scope.userInfo.isDesktop) {
                var index = 0;
                _.each(menuItems, function (menuItem, iteratorIndex) {
                    if ($location.url().indexOf(menuItem) > -1) {
                        index = iteratorIndex;
                    }
                });
                if (index == 0) {
                    $location.path('/' + menuItems[menuItems.length - 1]);
                } else {
                    $location.path('/' + menuItems[index - 1]);
                }
            }
        };

        $scope.swipeMoveRightOneMenuItem = function () {
            if (!$scope.userInfo.isDesktop) {
                var index = 0;
                _.each(menuItems, function (menuItem, iteratorIndex) {
                    if ($location.url().indexOf(menuItem) > -1) {
                        index = iteratorIndex;
                    }
                });
                if (index == menuItems.length - 1) {
                    $location.path('/' + menuItems[0]);
                } else {
                    $location.path('/' + menuItems[index + 1]);
                }
            }
        };

        $scope.showDevotionMenuItem = function() {
            return isLoggedIn() && GlobalService.isGroupMember();
        };

        $scope.showBlogMenuItem = function() {
            return isLoggedIn() && GlobalService.isGroupMember();
        };

        $scope.showGroupsMenuItem = function() {
            return isLoggedIn() && ($scope.userInfo.account.siteAdmin || $scope.userInfo.account.adminOrganizationKey);
        };

        $scope.showOrganizationsMenuItem = function() {
            return isLoggedIn() && $scope.userInfo.account.siteAdmin;
        };

        $scope.showStudiesMenuItem = function() {
            return isLoggedIn() && ($scope.userInfo.account.siteAdmin || $scope.userInfo.account.adminOrganizationKey || $scope.isStudyContributor);
        };

        $scope.showAdministrationMenuItem = function() {
            return isLoggedIn() && $scope.userInfo.account.adminOrganizationKey;
        };

        $scope.tryCheckLoggedIn = function() {
            $scope.checkLoggedInFailure = false;
            GlobalService.checkLoggedIn(function (success) {
                $scope.checkingLogin = false;
                if (success) {
                    $scope.userInfo = GlobalService.getUserInfo();
                    $scope.isStudyContributor = GlobalService.isStudyContributor();
                    GlobalService.setUserInfoChangeListener(function (userInfo) {
                        $scope.userInfo = userInfo;
                        $scope.isStudyContributor = GlobalService.isStudyContributor();
                        $scope.loginShowing = (userInfo ? false : true);
                    });
                } else {
                    $scope.checkLoggedInFailure = true;
                    $scope.checkingLogin = false;
                }
            });
        };

        $scope.tryCheckLoggedIn();

        $scope.contactUs = function() {
            var modalInstance = $uibModal.open({
                animation: false,
                templateUrl: '/components/contactUs/contactUs.html',
                controller: 'ContactUsCtrl',
                size: 'md'
            });
        };
    }]);
