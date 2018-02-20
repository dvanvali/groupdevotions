app.angularApp.config(['$routeProvider', function ($routeProvider) {
        $routeProvider.when('/blog',
            {templateUrl: 'components/blog/blog.html', controller: 'BlogCtrl'});
        $routeProvider.when('/configureGroup',
            {templateUrl: 'components/configureGroup/configureGroup.html', controller: 'ConfigureGroupCtrl'});
        $routeProvider.when('/desktop',
            {templateUrl: 'partials/desktop.html', controller: 'DesktopCtrl'});
        $routeProvider.when('/devotion',
            {templateUrl: 'components/devotion/devotion.html', controller: 'DevotionCtrl'});
        $routeProvider.when('/groups',
            {templateUrl: 'components/groupMaint/groups.html', controller: 'GroupsCtrl'});
        $routeProvider.when('/home',
            {templateUrl: 'components/home/home.html', controller: 'HomeCtrl'});
        $routeProvider.when('/journal',
            {templateUrl: 'components/journal/journal.html', controller: 'JournalCtrl'});
        $routeProvider.when('/login',
            {templateUrl: 'components/login/login.html', controller: 'LoginCtrl'});
        $routeProvider.when('/members',
            {templateUrl: 'components/groupMaint/members.html', controller: 'MembersCtrl'});
        $routeProvider.when('/organizations',
            {templateUrl: 'components/organizationMaint/organizations.html', controller: 'OrganizationsCtrl'});
        $routeProvider.when('/maintainOrganization',
            {templateUrl: 'components/organizationMaint/maintainOrganization.html', controller: 'MaintainOrganizationCtrl'});
        $routeProvider.when('/resetPassword',
            {templateUrl: 'partials/resetPassword.html', controller: 'ResetPasswordCtrl'});
        $routeProvider.when('/resetTestData',
            {templateUrl: 'partials/resetTestData.html', controller: 'ResetTestDataCtrl'});
        $routeProvider.when('/settings',
            {templateUrl: 'components/settings/settings.html', controller: 'SettingsCtrl'});
        $routeProvider.when('/studies',
            {templateUrl: 'components/studyMaint/studies.html', controller: 'StudiesCtrl'});
        $routeProvider.when('/terms',
            {templateUrl: 'components/terms/terms.html', controller: 'TermsCtrl'});
        $routeProvider.when('/maintainGroup',
            {templateUrl: 'components/groupMaint/maintainGroup.html', controller: 'MaintainGroupCtrl'});
        $routeProvider.when('/maintainStudy',
            {templateUrl: 'components/studyMaint/maintainStudy.html', controller: 'MaintainStudyCtrl'});
        $routeProvider.when('/maintainLesson',
            {templateUrl: 'components/studyMaint/maintainLesson.html', controller: 'MaintainLessonCtrl'});
        $routeProvider.otherwise({redirectTo: '/home'});
    }]);
