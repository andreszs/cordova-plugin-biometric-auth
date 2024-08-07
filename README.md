![npm](https://img.shields.io/npm/dt/cordova-plugin-biometric-auth) ![npm](https://img.shields.io/npm/v/cordova-plugin-biometric-auth) ![GitHub package.json version](https://img.shields.io/github/package-json/v/andreszs/cordova-plugin-biometric-auth?color=FF6D00&label=master&logo=github) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/andreszs/cordova-plugin-biometric-auth) ![GitHub top language](https://img.shields.io/github/languages/top/andreszs/cordova-plugin-biometric-auth) ![GitHub](https://img.shields.io/github/license/andreszs/cordova-plugin-biometric-auth) ![GitHub last commit](https://img.shields.io/github/last-commit/andreszs/cordova-plugin-biometric-auth)

# cordova-plugin-biometric-auth

Biometric authentication with optional KeyguardManager API for Cordova.

# Platforms

- Android ([minSdk 21](https://apilevels.com/))
- Browser (filler)

# Features

- AndroidX ready
- Authenticate with [BiometricManager](https://developer.android.com/reference/androidx/biometric/BiometricManager) (fingerprint, iris, face, device credentials) since API 23
- Authenticate with [KeyguardManager](https://developer.android.com/reference/android/app/KeyguardManager) (pin, pattern, password, biometric if enrolled) since API 21
- Auto fallback to KeyguardManager when no biometric enrolled or supported
- Supports all authentication modes (WEAK, STRONG, DEVICE CREDENTIALS)

# Installation

#### Install latest version from NPM

```bash
  cordova plugin add cordova-plugin-biometric-auth
```

#### Install latest version from master

```bash
  cordova plugin add https://github.com/andreszs/cordova-plugin-biometric-auth
```

# Methods

## isAvailable

Checks if the user can authenticate with either biometrics, fallback PIN, pattern or password. Biometric requires at least one biometric sensor to be present, enrolled, and available on the device.

```javascript
cordova.plugins.BiometricAuth.isAvailable(successCallback, errorCallback, [args])
```

#### optional `args` parameters object

| parameter | type | default | description |
| --- | --- | --- | --- |
| authenticators | int | 0 | An optional bit field representing the types of [Authenticators](https://developer.android.com/reference/androidx/biometric/BiometricManager.Authenticators) that may be used for authentication. Omit or use `0` to check for either biometrics or device credentials. Use `1`  to check for KeyguardManager authentication. |

#### Android quirks

Not all combinations of authenticator types are supported prior to Android 11 (API 30). Specifically, `DEVICE_CREDENTIAL` alone is unsupported prior to API 30, and `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` is unsupported on API 28-29.

#### Browser quirks

This filler platform always returns **BIOMETRIC_SUCCESS** and does not check nor use a real biometric device.

### successCallback return values

- **BIOMETRIC_SUCCESS**: The user can authenticate with the requested method(s).
- **KEYGUARD_SUCCESS**: Returned on API 21-22, or when biometric is not enrolled and `authenticators` value passed is `0` or `1`: The user can authenticate with KeyguardManager methods.

### errorCallback return values

- **BIOMETRIC_ERROR_HW_UNAVAILABLE**
- **BIOMETRIC_ERROR_NONE_ENROLLED**
- **BIOMETRIC_ERROR_NO_HARDWARE**
- **BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED**
- **BIOMETRIC_ERROR_UNSUPPORTED**
- **BIOMETRIC_STATUS_UNKNOWN**

### Example 1

Check for any biometric enrolled, PIN, pattern or password availability.

```javascript
var onSuccess = function (strSuccess) {
	console.log(strSuccess);
};
var onError = function (strError) {
	console.warn(strError);
};
cordova.plugins.BiometricAuth.isAvailable(onSuccess, onError);
```

### Example 2

Check for any biometric (e.g. fingerprint, iris, or face) on the device that meets or exceeds the requirements for Class 2. Requires at least API 23 (Android 6).

```javascript
var Authenticators = {
	KEYGUARD_MANAGER: 1,
	BIOMETRIC_STRONG: 15,
	BIOMETRIC_WEAK: 255,
	DEVICE_CREDENTIAL: 32768
};
var onSuccess = function (strSuccess) {
	console.log(strSuccess);
};
var onError = function (strError) {
	console.warn(strError);
};
var optionalParams = {
	authenticators = Authenticators.BIOMETRIC_WEAK;
};
cordova.plugins.BiometricAuth.isAvailable(onSuccess, onError, optionalParams);
```

## authenticate

Shows the biometric prompt or the fallback device credential dialog for authentication.

```javascript
cordova.plugins.BiometricAuth.authenticate(successCallback, errorCallback, [args])
```

#### optional `args` parameters object

| parameter | type | default | description |
| --- | --- | --- | --- |
| title | String | *Enter unlock credentials* | The title to be displayed on the prompt. |
| subtitle | String | | The subtitle to be displayed on the prompt. |
| disableBackup | Boolean | false | Removes the backup option from the prompt. |
| authenticators | int | 0 | A bit field representing all valid [authenticator](https://developer.android.com/reference/androidx/biometric/BiometricManager.Authenticators) types that may be invoked by the prompt. Use `0` to allow either biometrics or device credentials. Use `1`  to invoke KeyguardManager PIN, pattern, password or biometric if enrolled authentication. |
| negativeButtonText | String | | Sets the text for the cancel button on the prompt. Required whenever fallback is disabled. |

#### Android quirks

Not all combinations of authenticator types are supported prior to Android 11 (API 30). Specifically, `DEVICE_CREDENTIAL` alone is unsupported prior to API 30, and `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` is unsupported on API 28-29.

#### Browser quirks

Browser platforms shows a dialog to manually select either of these results and does not perform any actual biometric check:

 - **AUTHENTICATION_FAILED**
 - **BIOMETRIC_DISMISSED**
 - **BIOMETRIC_SUCCESS**

### successCallback return values

- **AUTHENTICATION_SUCCEEDED**

### errorCallback return values

- **AUTHENTICATION_FAILED**
- Please test demo app provided for other values.

### Example

Prompt the user for biometric, PIN, pattern or password credentials.

```javascript
var onSuccess = function (strSuccess) {
	console.log(strSuccess);
};
var onError = function (strError) {
	console.warn(strError);
};
var optionalParams = {
	title = "Confirm operation",
	subtitle = "Verify with biometrics to continue",
};
cordova.plugins.BiometricAuth.authenticate(onSuccess, onError, optionalParams);
```

### Remarks

- Do not use `BIOMETRIC_STRONG` without checking its availability with **isAvailable** first.
- Do not use `DEVICE_CREDENTIALS` alone prior to API 30.
- Do not use `BIOMETRIC_STRONG + DEVICE_CREDENTIAL` on API 28-29.
- To force usage of KeyguardManager instead of BiometricManager, set `1` to the **authenticators** param.
- Using an **authenticators** value other than `0` or `1` will discard the **disableBackup** option.
- Always provide a **negativeButtonText** when  using **disableBackup** or not using `DEVICE_CREDENTIAL` authenticator.
- Android 5 will use the KeyguardManager PIN, pattern or password regardless of any options.

# Plugin demo app

- [Compiled debug APK](https://github.com/andreszs/cordova-plugin-demos/tree/main/com.andreszs.biometric.auth.demo/apk)
- [Source code for www folder](https://github.com/andreszs/cordova-plugin-demos)

<img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth-authenticators.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth-strong.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth-credentials.png?raw=true" width="200" />

# Contributing

Please report any issue with this plugin in GitHub by providing detailed context and sample code.
PRs to improve and add new features or platforms are always welcome.

# Changelog

### 1.0.2 (Jul 21, 2024)

* Updated README to fix `isAvailable` return values and minor format improvements.

### 1.0.1 (Jun 13, 2022)

* Improved README.

### 1.0.0 (Jun 11, 2022)

* Initial release with Android and browser as filler platform.
