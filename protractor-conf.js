exports.config = {
    directConnect: true,
    rootElement: 'html',
    baseUrl: 'http://localhost:8080/',
    //seleniumAddress: 'http://localhost:4444/wd/hub',
    // To override this and test just one spec, change the describe to fdescribe
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