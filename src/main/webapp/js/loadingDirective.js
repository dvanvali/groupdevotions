app.directiveController.loadingController = function ($scope, $element, $attrs, $transclude) {
    var ctrl = this;
};

app.angularApp.directive("appLoading", ["StudyService", function () {
    return {
        restrict: 'E',
        scope: {
            loading: '='
        },
        template: '<span ng-if="load.loading"><img class="rotate" id="loading-icon" src="/images/load.svg" /> Loading...</span>',
        controller: app.directiveController.loadingController,
        controllerAs: 'load',
        bindToController: true
    };
}]);


