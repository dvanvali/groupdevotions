'use strict';

/* Services */
var serviceBase = function(restUrl, entityName, entityNamePlural, $resource, GlobalService) {
    var messageCallback;
    var entityArray;
    var entityToEdit;
    var failureCallbackForSaveEntity;

    var resetLoad = function () {
        entityToEdit = undefined;
        entityArray = undefined;
    };

    var displayMessage = function(response) {
        if (response.message) {
            if (messageCallback) {
                messageCallback.text = response.message.text;
                messageCallback.type = response.message.type;
                GlobalService.scrollToView('message');
            }
        }
    };

    var transformResponse = function(rawResponse, headers) {
        var response = angular.fromJson(rawResponse);
        var data = undefined;
        if (response.location) {
            GlobalService.setLocation(response.location);
        } else {
            displayMessage(response);
            if (response.operationSuccessful) {
                data = response.data;
            } else if (failureCallbackForSaveEntity) {
                failureCallbackForSaveEntity();
            }
        }
        // Returning undefined here is bad, this should be fixed.
        return data;
    };

    var entityResource = $resource(restUrl + '/:key', {key: "@key"},
        { delete: {method: "DELETE", transformResponse: transformResponse},
            get: {method: "GET", transformResponse: transformResponse},
            query: {method: "GET", transformResponse: transformResponse, isArray: true},
            save: {method: "POST", transformResponse: transformResponse}});

    var registerAndClearMessage = function(messageContainer, failureCallback) {
        // currently only used in saveEntity.  This is ugly and should be fixed.
        failureCallbackForSaveEntity = failureCallback;
        if (messageContainer) {
            messageCallback = messageContainer;
        }
        if (messageCallback) {
            messageCallback.text = undefined;
        }
    };

    // typically called with parameters = {accountKey: accountKeyValue}
    var loadEntities = function(parameters, messageContainer, successCallback) {
        registerAndClearMessage(messageContainer);
        if (entityArray) {
            if (successCallback) {
                successCallback(entityArray);
            }
        } else {
            entityArray = entityResource.query(parameters);
            entityArray.$promise.then(function (entities) {
                if (successCallback) {
                    successCallback(entityArray);
                }
            }, function() {
                if (messageCallback) {
                    entityArray = undefined;
                    messageCallback.text = 'Unable to load ' + entityNamePlural + '.  Please click to try again.';
                    messageCallback.type = 'warning';
                    messageCallback.tryAgain = function() {
                        loadEntities(parameters, messageContainer, successCallback);
                    };
                    GlobalService.scrollToView('message');
                }
            });
        }
    };

    var cancelEdit = function(entity, successCallback, messageContainer) {
        registerAndClearMessage(messageContainer);
        if (entity && entity.key && entity.$get) {
            entity.$get().then(function (newStudies) {
                if ((!messageCallback || !messageCallback.text || messageCallback.type === 'info') && successCallback) {
                    successCallback();
                }
            }, function() {
                if (messageCallback) {
                    messageCallback.text = 'Unable to cancel changes.  Please click to try again.';
                    messageCallback.type = 'warning';
                    messageCallback.tryAgain = function() {
                        cancelEdit(entity, successCallback);
                    };
                    GlobalService.scrollToView('message');
                }
            });
        } else {
            if (successCallback) {
                successCallback();
            }
        }
    };

    var saveEntity = function(entity, successCallback, messageContainer, failureCallback) {
        registerAndClearMessage(messageContainer, failureCallback);
        var handleRestFailure = function() {
            if (messageCallback) {
                messageCallback.text = 'Unable to save ' + entityName + '.  Please click to try again.';
                messageCallback.type = 'warning';
                messageCallback.tryAgain = function () {
                    saveEntity(entity, successCallback, messageContainer, failureCallback);
                };
                GlobalService.scrollToView('message');
            }
            if (failureCallback) {
                failureCallback();
            }
        };

        if (entity) {
            if (entity.key) {
                entity.$save().then(function(updatedEntity) {
                    updatedEntity = new entityResource(updatedEntity);
                    if (!messageCallback || !messageCallback.text || messageCallback.type === 'info') {
                        setEntityToEdit(undefined);
                        if (entityArray) {
                            var index = _.findIndex(entityArray, function (item) {
                                return updatedEntity.key === item.key;
                            });
                            if (index >= 0) {
                                entityArray[index] = updatedEntity;
                            }
                        }
                        if (successCallback) {
                            successCallback(updatedEntity);
                        }
                    }
                }, handleRestFailure);
            } else {
                new entityResource(entity).$save().then(function (newEntity) {
                    newEntity = new entityResource(newEntity);
                    if (!messageCallback || !messageCallback.text || messageCallback.type === 'info') {
                        if (entityArray) {
                            entityArray.unshift(newEntity);
                        }
                        setEntityToEdit(undefined);
                        if (successCallback) {
                            successCallback(newEntity);
                        }
                    }
                }, handleRestFailure);
            }
        }
    };

    var deleteEntity = function(entity, successCallback, messageContainer, failureCallback) {
        registerAndClearMessage(messageContainer, failureCallback);
        var indexToDelete = _.findIndex(entityArray, function(entityToSearch) {
            return entityToSearch.key === entity.key;
        });
        if (entity && entity.$delete) {
            entity.$delete().then(function () {
                if (!messageCallback || !messageCallback.text || messageCallback.type === 'info') {
                    if (indexToDelete > -1) {
                        entityArray.splice(indexToDelete, 1);
                    }
                    if (successCallback) {
                        successCallback();
                    }
                }
            }, function() {
                if (messageCallback) {
                    messageCallback.text = 'Unable to delete ' + entityName + '.  Please click to try again.';
                    messageCallback.type = 'warning';
                    messageCallback.tryAgain = function() {
                        deleteEntity(entity, successCallback, messageContainer, failureCallback);
                    };
                    GlobalService.scrollToView('message');
                }
            });
        }
    };

    var getEntityToEdit = function() {
        return entityToEdit;
    };

    var setEntityToEdit = function(entity) {
        entityToEdit = entity;
    };

    // If loading one at a time instead of using loadEntities, use this to load a single entity
    var loadEntityToEdit = function(entityKey, successCallback, messageContainer) {
        registerAndClearMessage(messageContainer);
        entityToEdit = entityResource.get({key: entityKey});
        entityToEdit.$promise.then(function (newEntity) {
            // entityToEdit is a promise so nothing to do
            entityArray = undefined;  // Should not use loadEntityToEdit with entityArray
            if (successCallback) {
                successCallback();
            }
        }, function() {
            if (messageCallback) {
                messageCallback.text = 'Unable to load ' + entityName + '.  Please click to try again.';
                messageCallback.type = 'warning';
                messageCallback.tryAgain = function() {
                    loadEntityToEdit(key, successCallback);
                };
                GlobalService.scrollToView('message');
            }
        });
    };

    var findEntityByKey = function(key) {
        var entityFound = undefined;
        _.each(entityArray, function(entity) {
            if (entity.key === key) {
                entityFound = entity;
            }
        });
        return entityFound;
    };

    return {
        loadEntities: loadEntities,
        loadEntityToEdit: loadEntityToEdit,
        getEntityToEdit: getEntityToEdit,
        setEntityToEdit: setEntityToEdit,
        cancelEdit: cancelEdit,
        saveEntity: saveEntity,
        deleteEntity: deleteEntity,
        findEntityByKey: findEntityByKey,
        resetLoad: resetLoad};
};


// Demonstrate how to register services
// In this case it is a simple value service.
app.angularApp.value('version', '0.1');

app.angularApp.factory('BlogService', ['$resource',
        function ($resource) {
            return $resource('rest/blog/query', {}, {
                'query': {
                    method: 'GET',
                    url: 'rest/blog/query',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'save': {
                    method: 'POST',
                    url: 'rest/blog/save',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'delete': {
                    method: 'POST',
                    url: 'rest/blog/delete',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'activities': {
                    method: 'GET',
                    url: 'rest/blog/activities',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'groupMember': {
                    method: 'GET',
                    url: 'rest/blog/groupMember',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('ConfigService', ['$resource',
        function ($resource) {
            return $resource('rest/config/terms', {}, {
                'terms': {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'agree': {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'resetTestData': {
                    method: 'POST',
                    url: 'rest/config/resetTestData',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('JournalService', ['$resource',
        function ($resource) {
            return $resource('rest/journal/query', {}, {
                'query': {
                    method: 'GET',
                    url: 'rest/journal/query',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'save': {
                    method: 'POST',
                    url: 'rest/journal/save',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'instructions': {
                    method: 'GET',
                    url: 'rest/journal/instructions',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('DevotionService', ['$resource',
        function ($resource) {
            return $resource('rest/devotion/:devotionId?anticache=:time', {}, {
                'get': {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'post': {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('AccountService', ['$resource',
        function ($resource) {
            return $resource('', {}, {
                'get': {
                    method: 'GET',
                    url: 'rest/account/:key',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'login': {
                    method: 'POST',
                    url: 'rest/account/localLogin',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'createAccount': {
                    method: 'POST',
                    url: 'rest/account/createAccount',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'logout': {
                    method: 'GET',
                    url: 'rest/account/logout',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'checkLoggedIn': {
                    method: 'GET',
                    url: 'rest/account/checkLoggedIn',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'forgotYourPassword': {
                    method: 'POST',
                    url: 'rest/account/forgotYourPassword',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'resetPassword': {
                    method: 'POST',
                    url: 'rest/account/resetPassword',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'setPasswordOrgAccount': {
                    method: 'POST',
                    url: 'rest/account/setPasswordOrgAccount',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'loadPossibleStudies': {
                    method: 'GET',
                    url: 'rest/account/loadPossibleStudies',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'saveSettings': {
                    method: 'POST',
                    url: 'rest/account/saveSettings',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'changePassword': {
                    method: 'POST',
                    url: 'rest/account/changePassword',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                },
                'contactUs': {
                    method: 'POST',
                    url: 'rest/account/contactUs',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('BibleService', ['$resource',
        function ($resource) {
            return $resource('rest/bible', {}, {
                'get': {
                    method: 'GET',
                    headers: {
                        'Content-Type': 'application/json'
                    },
                    isArray: false
                }
            });
        }])
    .factory('AdminAccountService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/account', 'account', 'accounts', $resource, GlobalService);
    }])
    .factory('GroupService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/group', 'group', 'groups', $resource, GlobalService);
    }])
    .factory('GroupMemberService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/groupMember', 'group member', 'group members', $resource, GlobalService);
    }])
    .factory('LessonService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/lesson', 'lesson', 'lessons', $resource, GlobalService);
    }])
    .factory('OrganizationService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/organization', 'organization', 'organizations', $resource, GlobalService);
    }])
    .factory('StudyService', ['$resource', 'GlobalService', function($resource, GlobalService) {
        return serviceBase('rest/study', 'study', 'studies', $resource, GlobalService);
    }]);
