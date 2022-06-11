![npm](https://img.shields.io/npm/dt/cordova-plugin-biometric-auth) ![npm](https://img.shields.io/npm/v/cordova-plugin-biometric-auth) ![GitHub package.json version](https://img.shields.io/github/package-json/v/andreszs/cordova-plugin-biometric-auth?color=FF6D00&label=master&logo=github) ![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/andreszs/cordova-plugin-biometric-auth) ![GitHub top language](https://img.shields.io/github/languages/top/andreszs/cordova-plugin-biometric-auth) ![GitHub](https://img.shields.io/github/license/andreszs/cordova-plugin-biometric-auth) ![GitHub last commit](https://img.shields.io/github/last-commit/andreszs/cordova-plugin-biometric-auth)

# cordova-plugin-biometric-auth

Biometric authentication with optional KeyguardManager API for Cordova.

# Platforms

- Android 5+
- Browser (filler)

# Features

- AndroidX ready
- Authenticate with [BiometricManager](https://developer.android.com/reference/androidx/biometric/BiometricManager) (fingerprint, iris, face, device credentials) since API 23
- Authenticate with [KeyguardManager](https://developer.android.com/reference/android/app/KeyguardManager) (pin, pattern, password, biometric if enrolled) since API 21
- Auto fallback to KeyguarManager in Android 5.x
- Supports all authentication modes (WEAK, STRONG, DEVICE CREDENTIALS)
- Supports [API level](https://apilevels.com/ "API level") 21 and over

# Installation

## Install latest version from NPM

```bash
  cordova plugin add cordova-plugin-biometric-auth
```

# Methods

## isAvailable

Checks if the user can authenticate with either biometrics, fallback PIN, pattern or password. Biometric requires at least one biometric sensor to be present, enrolled, and available on the device.

```javascript
cordova.plugins.BiometricAuth.isAvailable(successCallback, errorCallback, [optionalParams])
```

| **optionalParams** | |
| --- | --- |
| **Android-specific** | |
| authenticators | **int**: An optional bit field representing the types of [BiometricManager.Authenticators](https://developer.android.com/reference/androidx/biometric/BiometricManager.Authenticators) that may be used for authentication on Android. Omit or use `0` to check for either biometrics or device credentials. Use `1`  to check for KeyguardManager authentication. |

#### Android quirks

Not all combinations of authenticator types are supported prior to Android 11 (API 30). Specifically, `DEVICE_CREDENTIAL` alone is unsupported prior to API 30, and `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` is unsupported on API 28-29.

#### Browser quirks

This filler platform always returns **BIOMETRIC_SUCCESS** and does not check nor use a real biometric device.

### successCallback return values

- **BIOMETRIC_SUCCESS**: The user can authenticate with the requested method(s).
- **KEYGUARD_MANAGER**: Android only: Returned on API 21-22, or when biometric is not enrolled and `authenticator` value passed is `0` or `1`: The user can authenticate with KeyuardManager methods.

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
cordova.plugins.BiometricAuth.authenticate(successCallback, errorCallback, [optionalParams])
```

| optionalParams | |
| --- | --- |
| title | **string**: The title to be displayed on the prompt. Defaults to *Enter unlock credentials*. |
| subtitle | **string**: The subtitle to be displayed on the prompt. |
| disableBackup | **boolean**: Removes the backup option from the prompt. Defaults to `false`. Available since API 23. |
| **Android-specific** | |
| authenticators | **int:** A bit field representing all valid [authenticator](https://developer.android.com/reference/androidx/biometric/BiometricManager.Authenticators) types that may be invoked by the prompt. Use `0` to allow either biometrics or device credentials. Use `1`  to invoke KeyguardManager PIN, pattern, password or biometric if enrolled authentication. Available since API 23. |
| negativeButtonText | **string**: Sets the text for the cancel button on the prompt. Required whenever fallback is disabled. Available since API 23. |

#### Android quirks

Not all combinations of authenticator types are supported prior to Android 11 (API 30). Specifically, `DEVICE_CREDENTIAL` alone is unsupported prior to API 30, and `BIOMETRIC_STRONG | DEVICE_CREDENTIAL` is unsupported on API 28-29.

#### Browser quirks

This filler platform always returns **AUTHENTICATION_SUCCEEDED** and does not check nor use a real biometric device.

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

- To request device credentials by default (with biometric as fallback) enable the **useKeyguardManager** option.
- Do not use `BIOMETRIC_STRONG` without checking its availability with **isAvailable** first.
- Do not use `DEVICE_CREDENTIALS` alone prior to API 30.
- Do not use `BIOMETRIC_STRONG + DEVICE_CREDENTIAL` on API 28-29.
- Using an **authenticators** value other than `0` or `1` will discard the **disableBackup** option.
- Always provide a **negativeButtonText** when  using **disableBackup** or not using `DEVICE_CREDENTIAL` authenticator.
- Android 5.x will use the KeyguardManager PIN, pattern or password regardless of any options.

# Plugin demo app

Get the [Biometric Auth Plugin Demo app](https://www.andreszsogon.com/cordova-biometric-auth-plugin-demo/) to test the plugin in all possible scenarios.

<img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/biometric-auth-authenticators.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/android-9.png?raw=true" width="200" /> <img src="https://github.com/andreszs/cordova-plugin-demos/blob/main/com.andreszs.biometric.auth.demo/screenshots/android/android-9-b.png?raw=true" width="200" />

# Contributing

Please report any issue with this plugin in GitHub by providing detailed context and sample code.
**PR** to improve and add new features or platforms are always welcome.