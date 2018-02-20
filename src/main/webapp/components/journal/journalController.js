'use strict';

/* Controllers */

app.angularApp.controller('JournalCtrl', ['$scope', '$location', '$interval', 'JournalService', 'GlobalService',
        function($scope, $location, $interval, JournalService, GlobalService) {
        $scope.journals = [];
        $scope.forms = {};
        $scope.instructions = undefined;
        $scope.fullyLoaded = false;
        $scope.loading = true;
        $scope.EDITING = 1;
        $scope.SAVING = 2;
        $scope.SAVED = 3;
        $scope.READONLY = 4;

        var autoSaveInterval = null;
        var cancelAutoSave = function() {
            if (autoSaveInterval) {
                $interval.cancel(autoSaveInterval);
                autoSaveInterval = null;
            }
        };
        var autoSave = function(newVal, oldVal) {
            if (!$scope.editJournal.saving) {
                if (newVal != oldVal) {
                    cancelAutoSave();
                    autoSaveInterval = $interval($scope.save, 4000); // 4 sec
                }
            }
        };

        var unwatchEditJournalContent;
        $scope.edit = function(journal, focus) {
            if (!$scope.editJournal || $scope.editJournal.status != $scope.SAVING) {
                cancelAutoSave();
                if ($scope.editJournal) {
                    $scope.save(true);
                }
                $scope.editJournal = journal;
                $scope.editJournal.savedContent = $scope.editJournal.content;
                $scope.editJournal.status = $scope.EDITING;
                if ($scope.forms.journalForm) {
                    $scope.forms.journalForm.$setPristine();
                }
                unwatchEditJournalContent = $scope.$watch('editJournal.content', autoSave);
                if (focus) {
                    GlobalService.setFocus('#journalTextArea', true);
                }
            }
        };

        $scope.loadJournalsFromRest = function() {
            $scope.loading = true;
            var forDay = undefined;
            if ($scope.journals.length > 0) {
                forDay = $scope.journals[$scope.journals.length - 1].forDay;
            }
            var response = JournalService.query({'since': forDay}, function() {
                $scope.message = response.message;
                if (response.operationSuccessful) {
                    replaceContentWithBRs(response);
                    $scope.fullyLoaded = response.data.length == 0;
                    if (!$scope.fullyLoaded) {
                        $scope.journals = $scope.journals.concat(response.data);
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
                $scope.loadJournalsFromRest();
            }
        };

        $scope.save = function(finalSaveForThisEntry) {
            if ($scope.editJournal.status != $scope.SAVING) {
                $scope.editJournal.status = $scope.SAVING;
                cancelAutoSave();
                if (finalSaveForThisEntry && unwatchEditJournalContent) {
                    unwatchEditJournalContent();
                }
                var contentToSave = $scope.editJournal.content;
                var journalBeingSaved = $scope.editJournal;
                if (finalSaveForThisEntry) {
                    $scope.editJournal = undefined;
                }
                if (journalBeingSaved.content != journalBeingSaved.savedContent) {
                    var response = JournalService.save(journalBeingSaved, function () {
                        $scope.message = response.message;
                        if (response.operationSuccessful) {
                            if (finalSaveForThisEntry) {
                                journalBeingSaved.status = $scope.READONLY;
                            } else if (contentToSave == journalBeingSaved.content) {
                                $scope.forms.journalForm.$setPristine();
                                journalBeingSaved.status = $scope.SAVED;
                            } else {
                                journalBeingSaved.status = $scope.EDITING;
                            }
                            journalBeingSaved.htmlContent = journalBeingSaved.content.replace(/(?:\r\n|\r|\n)/g, '<br />');
                            journalBeingSaved.savedContent = contentToSave;
                        } else {
                            journalBeingSaved.status = $scope.EDITING;
                        }
                        GlobalService.setLocation(response.location);
                    }, function () {
                        journalBeingSaved.status = $scope.EDITING;
                        $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                    });
                } else {
                    if (finalSaveForThisEntry) {
                        journalBeingSaved.status = $scope.READONLY;
                    } else {
                        journalBeingSaved.status = $scope.EDITING;
                    }
                }
            }
        };

        var replaceContentWithBRs = function(response) {
            _.each(response.data,
                function(journal) {
                    if (journal.forDateDisplay == 'Today') {
                        journal.status = $scope.EDITING;
                        $scope.edit(journal, GlobalService.isNotMobile());
                    } else {
                        journal.status = $scope.READONLY;
                    }
                });
        };

        var loadJournalInstructions = function() {
            var response = JournalService.instructions({}, function() {
                if (response.operationSuccessful) {
                    $scope.instructions = response.data;
                }
                GlobalService.setLocation(response.location);
            },function() {
                // make a function to build messages
                $scope.message = GlobalService.danger('Unable to communicate with the server.  Please try again.');
                $scope.loading = false;
            });
        };

        loadJournalInstructions();
        $scope.loadJournalsFromRest();
    }]);
