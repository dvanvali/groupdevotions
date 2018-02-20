app.angularApp.controller('MaintainLessonCtrl', ['$scope', '$location', 'LessonService', 'StudyService',
    function ($scope, $location, LessonService, StudyService) {
        $scope.message = {};
        $scope.lesson = LessonService.getEntityToEdit();
        $scope.study = StudyService.findEntityByKey($scope.lesson.studyKey);

        var prepareData = function () {
            if ($scope.lesson.month) {
                $scope.lesson.month = $scope.lesson.month.toString();
            }
            if ($scope.study.studyType === 'SERIES') {
                $scope.lesson.studyInfoIndex = '-1';
                $scope.studyLessonInfos = [];
                _.forEach($scope.study.studyLessonInfos, function (info, index) {
                    if (info.studyLessonKey === $scope.lesson.key) {
                        // The last one should remain -1
                        if (index !== $scope.study.studyLessonInfos.length - 1) {
                            $scope.lesson.studyInfoIndex = index.toString();
                        }
                    } else {
                        $scope.studyLessonInfos.push(info);
                    }
                });
            }
        };
        prepareData();

        // Add an empty section to the end and remove empty sections before saving
        var addSection = function (index) {
            var emptySection = {content: "", type: "DIALOG", creationTimestamp: new Date().toString(), rawHtml: false};
            if (index == undefined) {
                index = $scope.lesson.studySections.length;
            }
            $scope.lesson.studySections.splice(index, 0, emptySection);
        };

        var removeEmptySections = function () {
            for (var i = $scope.lesson.studySections.length - 1; i > 0; i--) {
                var section = $scope.lesson.studySections[i];
                if (!section.content || section.content.trim() === "") {
                    $scope.lesson.studySections.splice(i, 1);
                }
            }
        };

        addSection();

        var onSuccessReturnToStudies = function () {
            LessonService.setEntityToEdit(undefined);
            $location.path('/studies');
        };

        var onSaveCallback = function (newEntity) {
            if ($scope.study) {
                if ($scope.lesson.accountabilityLesson) {
                    $scope.study.accountabilityLessonKey = newEntity.key;
                } else {
                    $scope.study.studyLessonInfos = newEntity.study.studyLessonInfos;
                }
            }
            onSuccessReturnToStudies();
        };

        $scope.save = function () {
            removeEmptySections();
            LessonService.saveEntity($scope.lesson, onSaveCallback, $scope.message);
        };

        $scope.delete = function () {
            if (confirm("Delete this lesson?")) {
                var lessonKeyToDelete = $scope.lesson.key;
                LessonService.deleteEntity($scope.lesson, function () {
                    if (lessonKeyToDelete) {
                        var study = StudyService.findEntityByKey($scope.lesson.studyKey);
                        if (study) {
                            if ($scope.lesson.accountabilityLesson) {
                                study.accountabilityLessonKey = undefined;
                            } else {
                                study.studyLessonInfos = _.filter(study.studyLessonInfos, function (info) {
                                    return info.studyLessonKey !== lessonKeyToDelete;
                                });
                            }
                        }
                    }
                    onSuccessReturnToStudies();
                }, $scope.message);
            }
        };

        $scope.cancel = function () {
            LessonService.cancelEdit($scope.lesson, onSuccessReturnToStudies, $scope.message);
        };

        $scope.tryAgain = function () {
            if ($scope.message.text && $scope.message.tryAgain) {
                $scope.message.tryAgain();
            }
        };

        $scope.addSectionAbove = function (index) {
            addSection(index);
        };

        $scope.deleteThisSection = function (index) {
            $scope.lesson.studySections.splice(index, 1);
        };

        $scope.onChangeCheckToAddSection = function (index) {
            // If is this the last section add a new one so there is always a place for a new section
            if (index + 1 == $scope.lesson.studySections.length) {
                addSection();
            }
        };
    }]);

