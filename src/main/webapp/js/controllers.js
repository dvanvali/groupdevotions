'use strict';

/* Controllers  */

app.angularApp
    .controller('ResetTestDataCtrl', ['$scope', '$location', 'ConfigService', 'GlobalService',
        function ($scope, $location, ConfigService, GlobalService) {
            $scope.message = undefined;

            var response = ConfigService.resetTestData({url: $location.absUrl()}, function () {
                $scope.message = response.message;
            }, function () {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
            });
        }])
    .controller('DesktopCtrl', ['$scope', function ($scope) {
        $scope.blogVisible = true;
        $scope.journalVisible = false;

        $scope.showJournal = function () {
            $scope.blogVisible = false;
            $scope.journalVisible = true;
        };

        $scope.showBlog = function () {
            $scope.blogVisible = true;
            $scope.journalVisible = false;
        };

    }])
;
