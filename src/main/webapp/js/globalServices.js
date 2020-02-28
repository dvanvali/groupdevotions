app.angularApp.factory('GlobalService', ['$location', '$window', '$timeout', '$anchorScroll', 'AccountService',
    function ($location, $window, $timeout, $anchorScroll, AccountService) {
        var savedUserInfo = undefined;
        var callbacks = [];

        var setUserInfo = function (userInfo) {
            savedUserInfo = userInfo;
            if (savedUserInfo && savedUserInfo.isSignedIn) {
                savedUserInfo.isDesktop = isScreenDesktop();
            }
            _.each(callbacks, function (callback) {
                callback(userInfo);
            });
        };

        var getUserInfo = function () {
            return savedUserInfo;
        };

        var isStudyContributor = function() {
            return savedUserInfo
                && savedUserInfo.isSignedIn
                && savedUserInfo.account
                && ((savedUserInfo.account.studyContributors && savedUserInfo.account.studyContributors.length > 0)
                || savedUserInfo.account.createStudies);
        };

        var setUserInfoChangeListener = function (callback) {
            callbacks.push(callback);
        };

        var danger = function (message) {
            return {
                type: 'danger',
                text: message
            };
        };

        var warning = function (message) {
            return {
                type: 'warning',
                text: message
            };
        };

        var info = function (message) {
            return {
                type: 'info',
                text: message
            };
        };

        var success = function (message) {
            return {
                type: 'success',
                text: message
            };
        };

        var logout = function (callback) {
            var response = AccountService.logout([], function () {
                var copyUserInfo = savedUserInfo;
                if (callback) {
                    callback(response);
                }
                setUserInfo({googleSignInUrl:savedUserInfo.googleSignInUrl,googleSignOutUrl: savedUserInfo.googleSignOutUrl,isSignedIn:false});
                if (response.operationSuccessful) {
                    if (copyUserInfo.googleSignOutUrl) {
                        $window.location.href = copyUserInfo.googleSignOutUrl;
                    } else {
                        // Forcing a reload causes the login checking which sets google login url
                        $window.location.href = $location.protocol() + "://" + $location.host() + ":" + $location.port();
                    }
                }
            }, function () {
                callback({
                    operationSuccessful: false,
                    message: {type: 'danger', text: 'Unable to communicate with the server.  Please try again.'}
                });
            });
        };

        var scrollToView = function(id){
            $timeout(function() {
                var element = jQuery('#'+id);
                if (element && element.offset && element.offset()) {
                    var offset = element.offset().top;
                    if(!element.is(":visible")) {
                        element.css({"visiblity":"hidden"}).show();
                        var offset = element.offset().top;
                        element.css({"visiblity":"", "display":""});
                    }

                    // 50 is the fixed header size
                    var visible_area_start = $(window).scrollTop() + 50;
                    var visible_area_end = visible_area_start + window.innerHeight - 50;

                    if(offset < visible_area_start || offset > visible_area_end){
                        // Not in view so scroll to it
                        $('html,body').animate({scrollTop: offset - window.innerHeight/3}, 1000);
                    }
                }
            },100);
        };

        var isNotMobile = function () {
            var viewportSize = jQuery($window).width();
            return viewportSize >= 1024;
        };

        var setFocus = function(selector, alwaysSetFocus) {
            if (alwaysSetFocus || isNotMobile()) {
                $timeout(function () {
                    if (selector) {
                        $(selector).focus();
                    } else {
                        $('input[auto-focus]:visible:first').focus();
                    }
                }, 0);
            }
        };

        var isScreenDesktop = function () {
            var screenFormat = 'MONITOR';
            if (savedUserInfo) {
                screenFormat = savedUserInfo.account.screenFormat;
                if (screenFormat == 'DETECT') {
                    if (isNotMobile()) {
                        screenFormat = 'MONITOR';
                    } else {
                        screenFormat = 'PHONE';
                    }
                }
            }
            return screenFormat == 'MONITOR';
        };

        var isGroupMember = function() {
            return savedUserInfo && savedUserInfo.account && savedUserInfo.account.groupMemberKey;
        };

        var firstUrl = undefined;
        var recordFirstUrl = function() {
            if (!firstUrl) {
                firstUrl = $location.absUrl();
            }
        };
        var getFirstUrl = function() {
            return firstUrl;
        };
        var clearFirstUrl = function() {
            firstUrl = undefined;
        };

        var showLoggedInHomePage = function () {
            if (!isGroupMember()) {
                if (savedUserInfo.account.adminOrganizationKey) {
                    $location.path('/groups');
                } else {
                    $location.path('/configureGroup');
                }
                return;
            } else if (firstUrl) {
                // refreshes should detect page
                if (firstUrl.indexOf('devotion') > -1) {
                    if (isScreenDesktop()) {
                        $location.path('/desktop');
                    } else {
                        $location.path('/devotion');
                    }
                    return;
                } else if (firstUrl.indexOf('journal') > -1) {
                    if (isScreenDesktop()) {
                        $location.path('/desktop');
                    } else {
                        $location.path('/journal');
                    }
                    return;
                } else if (firstUrl.indexOf('blog') > -1) {
                    if (isScreenDesktop()) {
                        $location.path('/desktop');
                    } else {
                        $location.path('/blog');
                    }
                    return;
                } else if (firstUrl.indexOf('desktop') > -1) {
                    $location.path('/desktop');
                    return;
                }
            }
            if (isScreenDesktop()) {
                $location.path('/desktop');
            } else {
                $location.path('/devotion');
            }
        };

        var createBibleRefs = function() {
            $timeout(function() {
                if (!isScreenDesktop()) {
                    BLB.Tagger.hostname = 'http://m.blb.org';
                }
                BLB.Tagger.mouseOverHandler = function(){};
                BLB.Tagger.walkDomTree(document.body);
            }, 15);
        };

        var settings = {};
        var getSettings = function() { return settings; };

        var buildDefaultGroup = function (name, description) {
            var group = {};
            group.name = name;
            group.description = description;
            group.blogInstructions = "This is a private blog just for your group.";
            group.inviteEmailSubject = "Invitation to GroupDevotions";
            group.inviteEmailBody = "You are invited to a private devotional group from one of your friends.\n\n" +
                "Click the following link to accept the invitation and create a devotional account if you don't have one yet.\n\n" +
                "<link>";
            group.ownerOrganizationKey = savedUserInfo.account.adminOrganizationKey;

            return group;
        };

        var checkLoggedIn = function (callbackWhenDone) {
            var firstUrl = getFirstUrl();
            // Not sure this is a good idea since logging in will redirect later... but maybe it is ok since the check causes a http promise to be fulfilled
            //if (firstUrl && (firstUrl.indexOf('devotion') > -1 || firstUrl.indexOf('blog') > -1 || firstUrl.indexOf('journal') > -1 || firstUrl.indexOf('desktop') > -1)) {
            //    $location.path('/home');
            //}
            var loginResult = AccountService.checkLoggedIn({url: getFirstUrl()},
                function () {
                    setUserInfo(loginResult.data);
                    if (loginResult.location) {
                        $location.path('/' + loginResult.location);
                    } else if (loginResult.operationSuccessful && savedUserInfo && savedUserInfo.isSignedIn) {
                        showLoggedInHomePage();
                    }
                    if (callbackWhenDone) {
                        callbackWhenDone(true);
                    }
                },
                function () {
                    // No way to communicate from the header?  maybe we could add a message at the top for all server
                    // communication eventually
                    if (callbackWhenDone) {
                        callbackWhenDone(false);
                    }
                });
        };

        var setLocation = function(location) {
            if (location) {
                if (location === 'checkLogin') {
                    setUserInfo(undefined);
                    checkLoggedIn();
                }
                $location.path('/' + location);
            }
        };

        return {
            setUserInfo: setUserInfo,
            getUserInfo: getUserInfo,
            setUserInfoChangeListener: setUserInfoChangeListener,
            logout: logout,
            scrollToView: scrollToView,
            info: info,
            danger: danger,
            success: success,
            warning: warning,
            showLoggedInHomePage: showLoggedInHomePage,
            isScreenDesktop: isScreenDesktop,
            recordFirstUrl: recordFirstUrl,
            getFirstUrl: getFirstUrl,
            clearFirstUrl: clearFirstUrl,
            isGroupMember: isGroupMember,
            createBibleRefs: createBibleRefs,
            isStudyContributor: isStudyContributor,
            getSettings: getSettings,
            buildDefaultGroup: buildDefaultGroup,
            setFocus: setFocus,
            isNotMobile: isNotMobile,
            setLocation: setLocation,
            checkLoggedIn: checkLoggedIn
        };
    }
]);
