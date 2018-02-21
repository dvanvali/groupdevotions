var resetPasswordPage = function () {
    var that = this;

    var signInEmail = by.model("form.signInEmail");
    var signInPassword = by.model("form.signInPassword");
    var signInPassword2 = by.model('form.signInPassword2');
    var resetButton = by.id('resetButton');
    var googleLogin = by.id('googleLogin');
    var loginButton = by.id('loginButton');

    return {
        signInEmail: signInEmail,
        signInPassword: signInPassword,
        signInPassword2: signInPassword2,
        resetButton: resetButton,
        googleLogin: googleLogin,
        loginButton: loginButton
    };
};

module.exports = resetPasswordPage;
