package com.andreszs.biometricauth;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricManager.Authenticators;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

interface Constants {
    static final String AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED";
    static final String AUTHENTICATION_SUCCEEDED = "AUTHENTICATION_SUCCEEDED";
    static final String AVAILABLE = "AVAILABLE";
    static final String DEVICE_CREDENTIALS = "DEVICE_CREDENTIALS";
    static final String FAILED_OLDER_SDK = "FAILED_OLDER_SDK";
    static final String KEYGUARD_NOT_AVAILABLE = "KEYGUARD_NOT_AVAILABLE";
    static final String METHOD_AUTHENTICATE = "authenticate";
    static final String METHOD_IS_AVAILABLE = "isAvailable";
    static final String PIN_OR_PATTERN_DISMISSED = "PIN_OR_PATTERN_DISMISSED";
    static final String SCREEN_GUARD_UNSECURED = "SCREEN_GUARD_UNSECURED";

    static final String BIOMETRIC_ERROR_HW_UNAVAILABLE = "BIOMETRIC_ERROR_HW_UNAVAILABLE";
    static final String BIOMETRIC_ERROR_NONE_ENROLLED = "BIOMETRIC_ERROR_NONE_ENROLLED";
    static final String BIOMETRIC_ERROR_NO_HARDWARE = "BIOMETRIC_ERROR_NO_HARDWARE";
    static final String BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED = "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED";
    static final String BIOMETRIC_ERROR_UNSUPPORTED = "BIOMETRIC_ERROR_UNSUPPORTED";
    static final String BIOMETRIC_STATUS_UNKNOWN = "BIOMETRIC_STATUS_UNKNOWN";

    static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 2;
}

public class BiometricAuth extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private BiometricPrompt mBiometricPrompt;
    private PluginResult pluginResult;
    private static final String TAG = "BiometricAuth";

    @Override
    public boolean execute(String action, JSONArray jsonArgs, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;

        if (action.equals(Constants.METHOD_IS_AVAILABLE)) {
            executeIsAvailable();
            return true;
        } else if (action.equals(Constants.METHOD_AUTHENTICATE)) {
            executeAuthenticate(jsonArgs);
            return true;
        } else {
            Log.e(TAG, String.format("Invalid action passed: %s", action));
            pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }

    private void executeIsAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            /* API level <= 20: No API available */
            finishWithError(Constants.FAILED_OLDER_SDK);
        } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            /* API levels 21/22: BiometricManager not available */
            KeyguardManager keyguardManager = ContextCompat.getSystemService(cordova.getContext(), KeyguardManager.class);
            if (keyguardManager == null) {
                finishWithError(Constants.KEYGUARD_NOT_AVAILABLE);
            } else if (keyguardManager.isKeyguardSecure()) {
                finishWithSuccess(Constants.DEVICE_CREDENTIALS);
            } else {
                finishWithError(Constants.SCREEN_GUARD_UNSECURED);
            }
        } else {
            /* API level >= 23: BiometricManager is available: The library makes all the features announced in Android 10 (API level 29) available all the way back to Android 6 (API level 23). */
            int canAuthenticate = BiometricManager.from(cordova.getContext()).canAuthenticate();
            switch (canAuthenticate) {
                case 0:
                    finishWithSuccess(Constants.AVAILABLE);
                    break;
                case 1:
                    finishWithError(Constants.BIOMETRIC_ERROR_HW_UNAVAILABLE);
                    break;
                case 11:
                    finishWithError(Constants.BIOMETRIC_ERROR_NONE_ENROLLED);
                    break;
                case 12:
                    finishWithError(Constants.BIOMETRIC_ERROR_NO_HARDWARE);
                    break;
                case 15:
                    finishWithError(Constants.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED);
                    break;
                case -2:
                    finishWithError(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
                    break;
                default:
                    /* Unable to determine whether the user can authenticate.*/
                    finishWithError(Constants.BIOMETRIC_STATUS_UNKNOWN);
            }
        }
    }

    private void executeAuthenticate(JSONArray jsonArgs) {
        Boolean useKeyguardManager = this.getBoolean(jsonArgs, "useKeyguardManager", false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            finishWithError(Constants.FAILED_OLDER_SDK);
        } else if (useKeyguardManager || Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            showKeyguardManagerAuth(jsonArgs);
        } else {
            showBiometricManagerAuth(jsonArgs);
        }

    }

    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            finishWithError(errString.toString());
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            finishWithSuccess(Constants.AUTHENTICATION_SUCCEEDED);
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            finishWithError(Constants.AUTHENTICATION_FAILED);
        }
    };

    private void finishWithError(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        mCallbackContext.sendPluginResult(result);
    }

    private void finishWithSuccess(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.OK, message);
        mCallbackContext.sendPluginResult(result);
    }

    private void showBiometricManagerAuth(JSONArray jsonArgs) {
        cordova.getActivity().runOnUiThread(() -> {
            Boolean deviceCredentialAllowed = this.getBoolean(jsonArgs, "deviceCredentialAllowed", false);
            String title = this.getString(jsonArgs, "title", "Enter unlock credentials");
            String subtitle = this.getString(jsonArgs, "subtitle", "");
            String negativeButtonText = this.getString(jsonArgs, "negativeButtonText", "CANCEL");

            final Handler handler = new Handler(Looper.getMainLooper());
            Executor executor = handler::post;
            mBiometricPrompt = new BiometricPrompt(cordova.getActivity(), executor, mAuthenticationCallback);
            BiometricPrompt.PromptInfo.Builder promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(title)
                    .setSubtitle(subtitle);

            if (deviceCredentialAllowed) {
                // Authenticator combination is unsupported on API 29: BIOMETRIC_STRONG | DEVICE_CREDENTIAL
                promptInfo.setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK | Authenticators.DEVICE_CREDENTIAL);
            } else {
                promptInfo.setAllowedAuthenticators(Authenticators.BIOMETRIC_WEAK);
                // Can't call setNegativeButtonText() and setAllowedAuthenticators(...|DEVICE_CREDENTIAL) at the same time.
                if (negativeButtonText.isEmpty()) negativeButtonText = "CANCEL";
                promptInfo.setNegativeButtonText(negativeButtonText);
            }

            mBiometricPrompt.authenticate(promptInfo.build());
        });
    }

    private void showKeyguardManagerAuth(JSONArray jsonArgs) {
        String title = this.getString(jsonArgs, "title", "Enter unlock credentials");
        String subtitle = this.getString(jsonArgs, "subtitle", "");

        pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        KeyguardManager keyguardManager = ContextCompat.getSystemService(cordova.getContext(), KeyguardManager.class);
        if (keyguardManager == null) {
            finishWithError(Constants.KEYGUARD_NOT_AVAILABLE);
        } else if (keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(title, subtitle);
            // Result will be handled by onActivityResult.
            cordova.startActivityForResult((CordovaPlugin) this, intent, Constants.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        } else {
            // Show a message that the user hasn't set up a lock screen.
            mCallbackContext.error(Constants.SCREEN_GUARD_UNSECURED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            Log.e(TAG, String.valueOf(resultCode));
            if (resultCode == Activity.RESULT_OK) {
                finishWithSuccess(Constants.AUTHENTICATION_SUCCEEDED);
            } else {
                finishWithError(Constants.PIN_OR_PATTERN_DISMISSED);
            }
        }
    }

    private Boolean getBoolean(@NonNull JSONArray jsonArray, String key, Boolean defaultValue) {
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getBoolean(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + key + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    private String getString(@NonNull JSONArray jsonArray, String key, String defaultValue) {
        try {
            JSONObject jsonObject = jsonArray.getJSONObject(0);
            if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                return jsonObject.getString(key);
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + key + "'. Default will be used.", e);
        }
        return defaultValue;
    }

}
