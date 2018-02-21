'use strict';

/* Filters  */

app.angularApp.filter('interpolate', ['version', function(version) {
    return function(text) {
      return String(text).replace(/\%VERSION\%/mg, version);
    };
  }]);

app.angularApp.filter('titlecase', function() {
    return function(s) {
        s = ( s === undefined || s === null ) ? '' : s;
        return s.toString().toLowerCase().replace( /\b([a-z])/g, function(ch) {
            return ch.toUpperCase();
        });
    };
});

app.angularApp.filter("filterMembers", function() {
    return function (members, filterValue) {
        if (!filterValue) {
            return members;
        }
        var filteredMembers = [];
        _.forEach(members, function(member) {
            if (member.status == filterValue) {
                filteredMembers.push(member);
            }
        });
        return filteredMembers;
    }
});
