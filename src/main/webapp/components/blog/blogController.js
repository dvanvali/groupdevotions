'use strict';

/* Controllers */

app.angularApp.controller('BlogCtrl', ['$scope', '$location', '$timeout', 'BlogService', 'GroupMemberService', 'GroupService', 'GlobalService',
            function($scope, $location, $timeout, BlogService, GroupMemberService, GroupService, GlobalService)
    {
        $scope.loading = true;
        $scope.userInfo = GlobalService.getUserInfo();
        GlobalService.setUserInfoChangeListener(function(userInfo) {
            $scope.userInfo = userInfo;
        });
        $scope.groupBlogs = [];
        $scope.activities = [];
        // Never bind an input on a view to a scalar.  Always make an object to avoid odd behavior.
        $scope.comment = {value:''};
        $scope.fullyLoaded = false;
        $scope.VIEW = 1;
        $scope.COMMENTMODE = 2;

        $scope.mode = $scope.VIEW;
        $scope.forms = {};
        $scope.instructions = undefined;
        $scope.groupName = undefined;
        $scope.buttonsDisabled = false;
        $scope.myGroupMember = undefined;

        $scope.commentOn = function() {
            $scope.comment.value = '';
            $scope.forms.commentForm.$setPristine();
            $scope.mode = $scope.COMMENTMODE;
            $timeout(function() {$('textarea[auto-focus]:visible:first').focus();}, 0);
        };

        $scope.cancelComment = function() {
            $scope.mode = $scope.VIEW;
            $scope.comment.value = '';
        };

        var savedEditBlogContent;
        $scope.editBlog = function(groupBlog, blogEntry) {
            savedEditBlogContent = blogEntry.content;
            blogEntry.mode = $scope.COMMENTMODE;
            $timeout(function() {$('textarea[auto-focus]:visible:first').focus();}, 0);
        };

        $scope.cancelEditBlog = function(blogEntry) {
            blogEntry.content = savedEditBlogContent;
            blogEntry.mode = $scope.VIEW;
        };

        $scope.saveEditBlog = function(groupBlog, blogEntry) {
            if (savedEditBlogContent == blogEntry.content) {
                $scope.cancelEditBlog(blogEntry);
            } else {
                $scope.buttonsDisabled = true;
                var index = groupBlog.blogEntries.indexOf(blogEntry);
                var response = BlogService.save(blogEntry, function () {
                    $scope.buttonsDisabled = false;
                    $scope.message = response.message;
                    if (response.operationSuccessful) {
                        replaceBlogEntryWithBRs(response.data);
                        groupBlog.blogEntries[index] = response.data;
                    }
                    GlobalService.setLocation(response.location);
                }, function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                });
            }
        };

        $scope.loadBlogsFromRest = function() {
            $scope.loading = true;
            var sinceDay = undefined;
            if ($scope.groupBlogs.length > 0) {
                sinceDay = $scope.groupBlogs[$scope.groupBlogs.length - 1].blogDate;
            }
            var response = BlogService.query({'since': sinceDay}, function() {
                $scope.message = response.message;
                if (response.operationSuccessful) {
                    var firstLoad = $scope.groupBlogs.length == 0;
                    replaceContentWithBRs(response);
                    $scope.fullyLoaded = response.data.length == 0;
                    if (!$scope.fullyLoaded) {
                        $scope.groupBlogs = $scope.groupBlogs.concat(response.data);
                    }
                    GlobalService.createBibleRefs();
                }
                GlobalService.setLocation(response.location);
                $scope.loading = false;
            },function() {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                $scope.loading = false;
            });
        };

        $scope.loadMore = function() {
            if (!$scope.loading && !$scope.fullyLoaded) {
                $scope.loadBlogsFromRest();
            }
        };

        $scope.loadBlogData = function() {
            var response = BlogService.activities({}, function() {
                $scope.message = response.message;
                if (response.operationSuccessful) {
                    $scope.activities = response.data.groupMemberActivities;
                    $scope.instructions = response.data.blogInstructions;
                    $scope.groupName = response.data.groupName;
                    loadMyGroupMemberData();
                }
                GlobalService.setLocation(response.location);
            },function() {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                $scope.loading = false;
            });
        };

        $scope.openActivity = function(activity) {
            activity.showDetail = true;
        };

        $scope.closeActivity = function(activity) {
            activity.showDetail = false;
        };

        $scope.saveComment = function() {
            if (!$scope.comment.value) {
                $scope.cancelComment();
            } else {
                $scope.buttonsDisabled = true;
                var response = BlogService.save({'content': $scope.comment.value}, function () {
                    $scope.buttonsDisabled = false;
                    $scope.message = response.message;
                    if (response.operationSuccessful) {
                        var linkified = linkifyStr(response.data.content, {target: '_blank'});
                        response.data.htmlContent = linkified.replace(/(?:\r\n|\r|\n)/g, '<br />');
                        response.data.mode = $scope.VIEW;
                        $scope.groupBlogs[0].blogEntries.push(response.data);
                        $scope.mode = $scope.VIEW;
                        $scope.comment.value = '';
                    }
                    GlobalService.setLocation(response.location);
                }, function () {
                    $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                });
            }
        };

        $scope.deleteBlog = function(groupBlog, blogEntry) {
            $scope.buttonsDisabled = true;
            var index = $scope.groupBlogs.indexOf(groupBlog);
            var response = BlogService.delete(blogEntry, function () {
                $scope.buttonsDisabled = false;
                $scope.message = response.message;
                if (response.operationSuccessful) {
                    replaceGroupBlogWithBRs(response.data);
                    $scope.groupBlogs[index] = response.data;
                }
                GlobalService.setLocation(response.location);
            }, function () {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
            });
        };

        var replaceBlogEntryWithBRs = function(blogEntry) {
            var linkified = linkifyStr(blogEntry.content, {target: '_blank'});
            blogEntry.htmlContent = linkified.replace(/(?:\r\n|\r|\n)/g, '<br />');
            blogEntry.mode = $scope.VIEW;
        };

        var replaceGroupBlogWithBRs = function(groupBlog) {
            _.each(groupBlog.blogEntries, replaceBlogEntryWithBRs);
        };

        var replaceContentWithBRs = function(response) {
            _.each(response.data, replaceGroupBlogWithBRs);
        };

       $scope.loadGroupMemberData = function(activity) {
            $scope.groupMember = {'name': activity.name,
                                  'email': "Loading...",
                                  'phone': "Loading..."};
            var response = BlogService.groupMember({'groupMemberKey':activity.groupMemberKey}, function() {
                $scope.message = response.message;
                if (response.operationSuccessful) {
                    $scope.groupMember = response.data;
                }
                GlobalService.setLocation(response.location);
            },function() {
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
            });
        };

        $scope.displayJournal = function() {
            $location.path('/journal');
        };

        $scope.editMembers = function() {
            GroupService.setEntityToEdit(undefined);
            // Members will edit the group set, or it will edit the current group from groupMemberKey
            $location.path('/members');
        };

        var loadMyGroupMemberData = function() {
            GroupMemberService.loadEntityToEdit($scope.userInfo.account.groupMemberKey, function() {
                    $scope.myGroupMember = GroupMemberService.getEntityToEdit();
                },
                $scope.message);
        };

        $scope.loadBlogsFromRest();
        $scope.loadBlogData();
    }]);
