'use strict';

var app = {};
app.directiveController = {};

app.angularApp = angular.module('myApp', [
    'ngRoute',
    'ngSanitize',
    'ngResource',
    'ngTouch',
    'app-templates',
    'ui.bootstrap'
]);

app.angularApp.config(['$resourceProvider', function($resourceProvider) {
    // Don't strip trailing slashes from calculated URLs
        $resourceProvider.defaults.stripTrailingSlashes = false;
    }]);
