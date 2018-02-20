exports.config = {
    directConnect: true,
    rootElement: 'html',
    baseUrl: 'http://localhost:8080/',
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    //specs: ['**/e2e/test-blog*.js'],
    //specs: ['**/e2e/test-dev*.js'],
    //specs: ['**/e2e/test-extra*.js'],
    //specs: ['**/e2e/test-jour*.js'],
    //specs: ['**/e2e/test-login*.js'],
    //specs: ['**/e2e/test-nav*.js'],
    //specs: ['**/e2e/test-new*.js'],
    //specs: ['**/e2e/test-org-maint.js'],
    //specs: ['**/e2e/test-org-maint-invite.js'],
    //specs: ['**/e2e/test-security.js'],
    //specs: ['**/e2e/test-sett*.js'],
    //specs: ['**/e2e/test-stud*.js'],
    //specs: ['**/e2e/test-with-ext*.js'],

    specs: ['**/e2e/test-*.js'],
    getPageTimeout: 60000,
    jasmineNodeOpts: {defaultTimeoutInterval: 60000},
    webdirverManagerUpdate: true,
    allScriptsTimeout: 60000,
    params: {
        waitingForCondition: false
    },
    multiCapabilities: [
        {'browserName': 'chrome'}
        //,{'browserName': 'firefox'}
    ]
};