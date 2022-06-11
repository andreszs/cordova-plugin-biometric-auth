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
    static final String PIN_OR_PATTERN_DISMISSED = "PIN_OR_PATTERN_DISMISSED";
    static final String KEYGUARD_SUCCESS = "KEYGUARD_SUCCESS";
    static final String METHOD_AUTHENTICATE = "authenticate";
    static final String METHOD_IS_AVAILABLE = "isAvailable";

    static final String BIOMETRIC_SUCCESS = "BIOMETRIC_SUCCESS";
    static final String BIOMETRIC_ERROR_UNSUPPORTED = "BIOMETRIC_ERROR_UNSUPPORTED";
    static final String BIOMETRIC_ERROR_NONE_ENROLLED = "BIOMETRIC_ERROR_NONE_ENROLLED";
    static final String BIOMETRIC_ERROR_HW_UNAVAILABLE = "BIOMETRIC_ERROR_HW_UNAVAILABLE";
    static final String BIOMETRIC_ERROR_NO_HARDWARE = "BIOMETRIC_ERROR_NO_HARDWARE";
    static final String BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED = "BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED";
    static final String BIOMETRIC_STATUS_UNKNOWN = "BIOMETRIC_STATUS_UNKNOWN";

    static final int REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS = 2;
    static final int AUTHENTICATOR_KEYGUARD_MANAGER = 1;
}

public class BiometricAuth extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private BiometricPrompt mBiometricPrompt;
    private PluginResult pluginResult;
    private static final String TAG = "BiometricAuth";

    @Override
    public boolean execute(String action, JSONArray jsonArgs, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;

        if (action.equals(Constants.METHOD_AUTHENTICATE)) {
            executeAuthenticate(jsonArgs);
            return true;
        } else if (action.equals(Constants.METHOD_IS_AVAILABLE)) {
            executeIsAvailable(jsonArgs);
            return true;
        } else {
            Log.e(TAG, String.format("Invalid action passed: %s", action));
            pluginResult = new PluginResult(PluginResult.Status.INVALID_ACTION);
            callbackContext.sendPluginResult(pluginResult);
            return false;
        }
    }

    private void executeIsAvailable(JSONArray jsonArgs) {
        Log.d(TAG, "executeCanAuthenticate");
        int mAllowedAuthenticators = this.getInt(jsonArgs, "authenticators", 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            /* API <= 20: No API available */
            mCallbackContext.error(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
        } else if (mAllowedAuthenticators == 1 || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            /* API 21-22: BiometricManager not available, or forced KeyguardManager by params */
            if (mAllowedAuthenticators < 2) {
                /* authenticator 0 = any; 1 = KeyguardManager */
                KeyguardManager keyguardManager = ContextCompat.getSystemService(cordova.getContext(), KeyguardManager.class);
                if (keyguardManager == null) {
                    mCallbackContext.error(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
                } else if (keyguardManager.isKeyguardSecure()) {
                    mCallbackContext.success(Constants.KEYGUARD_SUCCESS);
                } else {
                    mCallbackContext.error(Constants.BIOMETRIC_ERROR_NONE_ENROLLED);
                }
            } else {
                /* not supported on API 21-22 */
                mCallbackContext.error(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
            }
        } else {
            /* API >= 23: Check if the user can authenticate with biometrics. This requires at least one biometric sensor to be present, enrolled, and available on the device. */
            int canAuthenticate;
            int authenticators = getInt(jsonArgs, "authenticators", 0);

            /* Note that not all combinations of authenticator types are supported prior to Android 11 (API 30). Specifically, DEVICE_CREDENTIAL alone is unsupported prior to API 30, and BIOMETRIC_STRONG | DEVICE_CREDENTIAL is unsupported on API 28-29 */
            if (authenticators > 0) {
                /* Return result for specified authenticator(s) */
                canAuthenticate = BiometricManager.from(cordova.getContext()).canAuthenticate(authenticators);
                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    mCallbackContext.success(Constants.BIOMETRIC_SUCCESS);
                } else {
                    mCallbackContext.error(getMessageByCode(canAuthenticate));
                }
            } else {
                canAuthenticate = BiometricManager.from(cordova.getContext()).canAuthenticate();
                if (canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS) {
                    /* Any biometric enrolled */
                    mCallbackContext.success(getMessageByCode(canAuthenticate));
                } else {
                    /* Check if KeyguardManager auth is available */
                    KeyguardManager keyguardManager = ContextCompat.getSystemService(cordova.getContext(), KeyguardManager.class);
                    if (keyguardManager != null && keyguardManager.isKeyguardSecure()) {
                        mCallbackContext.success(Constants.KEYGUARD_SUCCESS);
                    } else {
                        mCallbackContext.error(getMessageByCode(canAuthenticate));
                    }
                }
            }

        }
    }

    private void executeAuthenticate(JSONArray jsonArgs) {
        Log.d(TAG, "executeAuthenticate");
        int mAllowedAuthenticators = this.getInt(jsonArgs, "authenticators", 0);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            mCallbackContext.error(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
        } else if (mAllowedAuthenticators == Constants.AUTHENTICATOR_KEYGUARD_MANAGER || Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            showKeyguardManagerAuth(jsonArgs);
        } else {
            showBiometricManagerAuth(jsonArgs);
        }

    }

    private BiometricPrompt.AuthenticationCallback mAuthenticationCallback = new BiometricPrompt.AuthenticationCallback() {
        @Override
        public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
            super.onAuthenticationError(errorCode, errString);
            pluginResult = new PluginResult(PluginResult.Status.ERROR, errString.toString());
            mCallbackContext.sendPluginResult(pluginResult);
        }

        @Override
        public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
            super.onAuthenticationSucceeded(result);
            pluginResult = new PluginResult(PluginResult.Status.OK, Constants.AUTHENTICATION_SUCCEEDED);
            mCallbackContext.sendPluginResult(pluginResult);
        }

        @Override
        public void onAuthenticationFailed() {
            super.onAuthenticationFailed();
            pluginResult = new PluginResult(PluginResult.Status.ERROR, Constants.AUTHENTICATION_FAILED);
            mCallbackContext.sendPluginResult(pluginResult);
        }
    };

    private void finishWithError(String message) {
        PluginResult result = new PluginResult(PluginResult.Status.ERROR, message);
        mCallbackContext.sendPluginResult(result);
    }

    private void showBiometricManagerAuth(JSONArray jsonArgs) {
        cordova.getActivity().runOnUiThread(() -> {
            Boolean disableBackup = this.getBoolean(jsonArgs, "disableBackup", false);
            String title = this.getString(jsonArgs, "title", "Enter unlock credentials");
            String subtitle = this.getString(jsonArgs, "subtitle", "");
            String negativeButtonText = this.getString(jsonArgs, "negativeButtonText", "");
            int mAllowedAuthenticators = this.getInt(jsonArgs, "authenticators", 0);

            final Handler handler = new Handler(Looper.getMainLooper());
            Executor executor = handler::post;
            mBiometricPrompt = new BiometricPrompt(cordova.getActivity(), executor, mAuthenticationCallback);
            BiometricPrompt.PromptInfo.Builder promptInfo = new BiometricPrompt.PromptInfo.Builder();
            promptInfo.setTitle(title);
            promptInfo.setSubtitle(subtitle);

            if (mAllowedAuthenticators == 0) {
                if (disableBackup) {
                    mAllowedAuthenticators = Authenticators.BIOMETRIC_WEAK;
                } else {
                    mAllowedAuthenticators = Authenticators.BIOMETRIC_WEAK + Authenticators.DEVICE_CREDENTIAL;
                }
            }
            promptInfo.setAllowedAuthenticators(mAllowedAuthenticators);

            if (!isDeviceCredentialAllowed(mAllowedAuthenticators)) {
                promptInfo.setNegativeButtonText(negativeButtonText);
            }

            try {
                mBiometricPrompt.authenticate(promptInfo.build());
            } catch (Exception e) {
                mCallbackContext.error(e.getMessage());
            }
        });
    }

    private void showKeyguardManagerAuth(JSONArray jsonArgs) {
        String title = this.getString(jsonArgs, "title", "Enter unlock credentials");
        String subtitle = this.getString(jsonArgs, "subtitle", "");

        pluginResult = new PluginResult(PluginResult.Status.NO_RESULT);
        pluginResult.setKeepCallback(true);

        KeyguardManager keyguardManager = ContextCompat.getSystemService(cordova.getContext(), KeyguardManager.class);
        if (keyguardManager == null) {
            mCallbackContext.error(Constants.BIOMETRIC_ERROR_UNSUPPORTED);
        } else if (keyguardManager.isKeyguardSecure()) {
            Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(title, subtitle);
            // Result will be handled by onActivityResult.
            cordova.startActivityForResult((CordovaPlugin) this, intent, Constants.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS);
        } else {
            // Show a message that the user hasn't set up a lock screen.
            mCallbackContext.error(Constants.BIOMETRIC_ERROR_NONE_ENROLLED);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.REQUEST_CODE_CONFIRM_DEVICE_CREDENTIALS) {
            Log.e(TAG, String.valueOf(resultCode));
            if (resultCode == Activity.RESULT_OK) {
                mCallbackContext.success(Constants.AUTHENTICATION_SUCCEEDED);
            } else {
                mCallbackContext.error(Constants.PIN_OR_PATTERN_DISMISSED);
            }
        }
    }

    static Boolean getBoolean(@NonNull JSONArray jsonArray, String key, Boolean defaultValue) {
        try {
            if (!jsonArray.isNull(0)) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                    return jsonObject.getBoolean(key);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + key + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    static int getInt(@NonNull JSONArray jsonArray, String key, int defaultValue) {
        try {
            if (!jsonArray.isNull(0)) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                    return jsonObject.getInt(key);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + key + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    static String getString(@NonNull JSONArray jsonArray, String key, String defaultValue) {
        try {
            if (!jsonArray.isNull(0)) {
                JSONObject jsonObject = jsonArray.getJSONObject(0);
                if (jsonObject.has(key) && !jsonObject.isNull(key)) {
                    return jsonObject.getString(key);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Can't parse '" + key + "'. Default will be used.", e);
        }
        return defaultValue;
    }

    /**
     * Get message string by result code.
     *
     * @param {BiometricManager.Constants} result constants.
     * @return Message code as String.
     */
    static String getMessageByCode(int code) {
        switch (code) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                return Constants.BIOMETRIC_SUCCESS;
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED:
                return Constants.BIOMETRIC_ERROR_UNSUPPORTED;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                return Constants.BIOMETRIC_ERROR_HW_UNAVAILABLE;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                return Constants.BIOMETRIC_ERROR_NONE_ENROLLED;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                return Constants.BIOMETRIC_ERROR_NO_HARDWARE;
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:
                return Constants.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED;
            default:
                return Constants.BIOMETRIC_STATUS_UNKNOWN;
        }
    }

    /**
     * Checks if a device credential is included in the given set of allowed authenticator types.
     *
     * @param authenticators A bit field representing a set of allowed authenticator types.
     * @return Whether {@link Authenticators#DEVICE_CREDENTIAL} is an allowed authenticator type.
     */
    static boolean isDeviceCredentialAllowed(int authenticators) {
        return (authenticators & Authenticators.DEVICE_CREDENTIAL) != 0;
    }

}
