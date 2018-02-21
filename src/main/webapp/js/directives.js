'use strict';

/* Directives */


app.angularApp.directive('appVersion', ['version', function(version) {
    return function(scope, elm, attrs) {
      elm.text(version);
    };
  }])
.directive('autoFocus', ['GlobalService', '$timeout', function(GlobalService, $timeout) {
  return {
    restrict: 'AC',
    link: function(_scope, _element) {
        if (GlobalService.isNotMobile()) {
            $timeout(function () {
                _element[0].focus();
            }, 0);
        }
    }
  };
}])
.directive("whenScrolled", function ($window) {
  return{
    restrict: 'A',
    link: function (scope, elem, attrs) {
        var raw = elem[0];
        var checkBounds = function (evt) {
            var rectObject = raw.getBoundingClientRect();
            if ($window.innerHeight > rectObject.bottom - 50) {
              if ($(elem).is(":visible")) {
                //console.log("FIRING whenScrolled function");
                scope.$apply(attrs.whenScrolled);
              } else {
                //console.log("not firing whenScrolled because hidden");
              }
            }
        };
        angular.element($window).bind('scroll load', checkBounds);
    }
  };
});

