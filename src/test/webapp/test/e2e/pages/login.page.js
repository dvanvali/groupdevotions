var loginPage = function () {
    var userName = by.id('email');
    var password = by.id('password');
    var password2 = by.model('form.signInPassword2');
    var signinButton = by.buttonText('Sign In');
    var googleButton = by.id('googleButton');
    var resetHeading = by.id('resetHeading');
    var resetButton = by.id('resetButton');
    var message = by.id('message');
    var forgotLink = by.id('forgotLink');
    var forgotUserName = by.id('forgotEmail');
    var forgotHeading = by.id('forgotHeading');
    var forgotButton = by.id('forgotButton');
    var forgotCancel = by.id('forgotCancel');

    return {
        userName: userName,
        password: password,
        password2: password2,
        signinButton: signinButton,
        googleButton: googleButton,
        resetHeading: resetHeading,
        resetButton: resetButton,
        message: message,
        forgotLink: forgotLink,
        forgotUserName: forgotUserName,
        forgotHeading: forgotHeading,
        forgotButton: forgotButton,
        forgotCancel: forgotCancel,

        get: function() {
            browser.setLocation('/login');
        }
    };
};


module.exports = loginPage;
