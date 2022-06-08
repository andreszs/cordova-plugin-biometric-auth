var exec = require('cordova/exec');

var BiometricAuth = {
    isAvailable: function (successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BiometricAuth", "isAvailable", []);
    },
    authenticate: function (args, successCallback, errorCallback) {
        cordova.exec(successCallback, errorCallback, "BiometricAuth", "authenticate", [args]);
    },
};

module.exports = BiometricAuth;
