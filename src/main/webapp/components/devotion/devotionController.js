var callbackName = function(resp) {
    console.log(resp);
};

'use strict';

/* Controllers */

app.angularApp.controller('DevotionCtrl', ['$scope', '$routeParams', '$location', '$interval', '$http', '$window', '$uibModal', 'DevotionService', 'AccountService',
              'GlobalService', 'GroupMemberService', 'BibleService',
      function($scope, $routeParams, $location, $interval, $http, $window, $uibModal, DevotionService, AccountService,
               GlobalService, GroupMemberService, BibleService) {

          $scope.buttonsDisabled = false;
          $scope.message = {};
          $scope.devotionHasQuestions = false;
          $scope.accountabilitySettingsVisible = false;
          $scope.readingSettingsVisible = false;
          $scope.groupMember = undefined;
          // accountabilityEmails is a list of objects because angular does not work well with lists of strings
          $scope.accountabilityEmails = undefined;
          $scope.userInfo = GlobalService.getUserInfo();
          $scope.hideEmailReadingCompleteButton = false;
          GlobalService.setUserInfoChangeListener(function(userInfo) {
              $scope.userInfo = userInfo;
          });
          var atLeastOneLessonBibleReading = false;

          $scope.todaysLesson = function () {
              $scope.devotionData = {};
              $scope.viewingPrevious = false;
              $scope.devotionHasQuestions = false;
              $scope.accountabilitySettingsVisible = false;
              $scope.readingSettingsVisible = false;
              $scope.buttonsDisabled = false;
              GlobalService.scrollToView('topOfPage');
              var response = DevotionService.get({devotionId: 'today', time: (new Date()).toString()}, function () {
                  $scope.message = response.message;
                  if (response.operationSuccessful) {
                      replaceContentAfterRetrieval(response);
                      $scope.devotionData = response.data;
                      $scope.devotionHasQuestions = devotionDataContainsQuestions(response);
                      if (!atLeastOneLessonBibleReading) {
                          $interval(GlobalService.createBibleRefs, 0, 1);
                      }
                  }
                  GlobalService.setLocation(response.location);
              }, function () {
                  // make a function to build messages
                  $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
              });
          };

          $scope.todaysLesson();

          $scope.save = function() {
              $scope.buttonsDisabled = true;
              $scope.message = {};
              var response = DevotionService.post($scope.devotionData, function() {
                  $scope.buttonsDisabled = false;
                  $scope.message = response.message;
                  if (response.operationSuccessful) {
                      GlobalService.scrollToView('bottomOfPage');
                      if ($scope.answerForm) {
                          $scope.answerForm.$setPristine();
                      }
                      $scope.hideEmailReadingCompleteButton = true;
                  }
                  GlobalService.setLocation(response.location);
              },function() {
                  $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                  $scope.buttonsDisabled = false;
              });
          };

          $scope.readingComplete = function(studyLesson) {
              $scope.buttonsDisabled = true;
              $scope.message = {};
              var response = DevotionService.post({readingCompleteStudyLesson: studyLesson}, function() {
                  $scope.buttonsDisabled = false;
                  $scope.message = response.message;
                  if (response.operationSuccessful) {
                      if ($scope.answerForm) {
                          $scope.answerForm.$setPristine();
                      }
                      studyLesson.hideEmailReadingCompleteButton = true;
                  }
                  GlobalService.setLocation(response.location);
              },function() {
                  $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                  $scope.buttonsDisabled = false;
              });
          };

          $scope.logout = function() {
              $scope.buttonsDisabled = true;
              $scope.message = {};
              var response = GlobalService.logout(function(response) {
                  $scope.buttonsDisabled = false;
                  $scope.message = response.message;
              });
          };

          var devotionDataContainsQuestions = function(response) {
              var studyLessonWithQuestion = _.find(response.data.studyLessons,
                  function(studyLesson) {
                      return _.find(studyLesson.studySections,
                          function(section) {
                              return section.type.indexOf('QUESTION') > -1;
                          })
                  });
              return studyLessonWithQuestion != undefined;
          };

          var isBiblesOrgVersion = function(version) {
              var versions = ['nasb','esv','msg','kjva','gntd','cevd','cev','amp'];
              return versions.find(function(element) {
                  return version === element;
              });
          };

          var loadScriptureSection = function(section, version) {
              if (isBiblesOrgVersion(version)) {
                  BibleService.get({reference: section.content, version: version}, function (response) {
                      var jsonString = response.data;
                      var bibleObject = JSON.parse(jsonString);
                      if (bibleObject.response.search.result.passages.length > 0) {
                          section.bible = {passages: bibleObject.response.search.result.passages};
                          if (_BAPI) {
                              _BAPI.t(bibleObject.response.meta.fums_tid);
                          }
                      } else {
                          section.bible = {passages: [{text:'Scripture reference not found.'}]};
                      }
                      section.loading = false;
                  }, function (data, status) {
                      section.bible = {passages: [{text:'Scripture reference not found.'}]};
                  });
              } else {
                  // Remaining versions are getbible.net (not formatted as well so don't use as default)
                  $http.jsonp('https://getbible.net/index.php?callback=JSON_CALLBACK&option=com_getbible&view=json&p=' + section.content + '&v=' + version)
                      .success(function (json) {
                          section.bible = json;
                          if (!section.bible.chapter) {
                              section.content = 'Unable to find ' + section.content;
                          }
                          section.loading = false;
                      })
                      .error(function (data, status) {
                          section.content = 'Unable to find ' + section.content;
                      });
              }
          };

          var replaceContentAfterRetrieval = function(response) {
              _.each(response.data.studyLessons,
                  function(studyLesson) {
                      _.each(studyLesson.studySections,
                          function(section) {
                              if (section.type == 'SCRIPTURE_TO_LOAD') {
                                  atLeastOneLessonBibleReading = true;
                                  section.loading = true;
                                  section.version = studyLesson.bibleReadingVersion;
                                  loadScriptureSection(section, studyLesson.bibleReadingVersion);
                              } else {
                                  section.content = section.content.replace(/(?:\r\n|\r|\n)/g, '<br />');
                              }
                          })
                  });
          };

          var scrollToBottom = function(id) {
              $interval(function() {
                  GlobalService.scrollToView(id ? id : 'bottomOfPage');
                  GlobalService.setFocus('#settingsContainer input:first');
              }, 0, 1);
          };

          $scope.makeSettingsVisible = function(readingSettings) {
              $scope.message = GlobalService.info('Loading settings...');
              GroupMemberService.loadEntityToEdit($scope.userInfo.account.groupMemberKey, function() {
                      var groupMember = GroupMemberService.getEntityToEdit();
                      $scope.accountabilityEmails = [];
                      var index = 0;
                      _.each(groupMember.accountabilityEmails,
                          function(email) {
                              $scope.accountabilityEmails.push({index: index, address:email});
                              index = index + 1;
                          });
                      $scope.accountabilityEmails.push({index: index, address:""});

                      $scope.groupMember = groupMember;
                      if (readingSettings) {
                          $scope.readingSettingsVisible = true;
                      } else {
                          $scope.accountabilitySettingsVisible = true;
                          scrollToBottom(readingSettings);
                      }
                  },
                  $scope.message);
          };

          $scope.cancelSettings = function(readingSettings) {
              if (readingSettings) {
                  $scope.readingSettingsVisible = false;
              } else {
                  $scope.accountabilitySettingsVisible = false;
              }
              $scope.groupMember = undefined;
          };

          $scope.clearEmail = function(email) {
              email.address = "";
              GlobalService.setFocus('#settingsContainer input:first');
          };

          $scope.emailChanged = function() {
              var index = 0;
              var oneAddressIsEmpty = false;
              _.each($scope.accountabilityEmails, function(email) {
                  index = index + 1;
                   if (!email.address) {
                       oneAddressIsEmpty = true;
                   }
              });

              if (!oneAddressIsEmpty) {
                  $scope.accountabilityEmails.push({index: index, address:""});
              }
          };

          $scope.saveSettings = function(changingReadingPlan) {
              // copy wrapped emails back into groupMember minus empty addresses
              $scope.groupMember.accountabilityEmails = [];
              var accountabilityConfigured = false;
              _.each($scope.accountabilityEmails,
                  function(email) {
                      if (email.address) {
                          $scope.groupMember.accountabilityEmails.push(email.address);
                          accountabilityConfigured = true;
                      }
                  });
              GroupMemberService.saveEntity($scope.groupMember, function() {
                  $scope.devotionData.accountabilityConfigured = accountabilityConfigured;
                  if (changingReadingPlan) {
                      $scope.hideEmailReadingCompleteButton = true;
                      $window.location.reload(true);
                  }
                  $scope.cancelSettings();
              },$scope.message);
          };

          $scope.displayBlog = function(changingReadingPlan) {
              $location.path('/blog');
          };

          $scope.versionChange = function (section) {
              section.loading = true;
              loadScriptureSection(section, section.version);
          };

          // Services will set message tryAgain() functions upon failure.  The page can call it if set
          $scope.tryAgain = function () {
              if ($scope.message.text && $scope.message.tryAgain) {
                  $scope.message.tryAgain();
              }
          };

          $scope.previousLesson = function () {
              $scope.devotionData = {};
              GlobalService.scrollToView('topOfPage');
              $scope.devotionHasQuestions = false;
              var response = DevotionService.get({devotionId: 'previous'}, function() {
                  $scope.message = response.message;
                  if (response.operationSuccessful) {
                      $scope.viewingPrevious = true;

                      replaceContentAfterRetrieval(response);
                      $scope.devotionData = response.data;
                      $scope.devotionHasQuestions = devotionDataContainsQuestions(response);
                      if (!atLeastOneLessonBibleReading) {
                          $interval(GlobalService.createBibleRefs, 0, 1);
                      }
                  }
                  GlobalService.setLocation(response.location);
              },function() {
                  // make a function to build messages
                  $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
              });
          };

          // Check periodically if the day has changed and display a dialog to load the next day
          var timeoutPeriod = 2*60*60*1000;
          var today = function() {
              var date = new Date();
              return (date.getMonth() + 1) + "-" + date.getDate() + "-" + date.getFullYear();
          };
          var lastActivity = today();
          var refreshIfTomorrow = function() {
              if (today() !== lastActivity) {
                  lastActivity = today();
                  setTimerForNewDevotionalCheck();
                  $scope.todaysLesson();
              } else {
                  setTimerForNewDevotionalCheck();
              }
          };
          var setTimerForNewDevotionalCheck = function() {
              $interval(refreshIfTomorrow, timeoutPeriod, 1);
          };
          setTimerForNewDevotionalCheck();
      }]);
