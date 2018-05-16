package util;

import android.content.SharedPreferences;

import com.cipherScriptDevs.backDrop.AppController;
import com.cipherScriptDevs.backDrop.BuildConfig;

import static android.content.Context.MODE_PRIVATE;

public class LPref {

    private static SharedPreferences mSharedPrefs;

    static {
        mSharedPrefs = AppController.getAppContext()
                .getSharedPreferences(BuildConfig.APPLICATION_ID, MODE_PRIVATE);
    }

    public static void putIntPref(String prefKey, int value) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putInt(prefKey, value);
        editor.apply();
    }

    public static void putBooleanPref(String prefKey, boolean value) {
        SharedPreferences.Editor editor = mSharedPrefs.edit();
        editor.putBoolean(prefKey, value);
        editor.apply();
    }
//
//    public static void putStringPref(String prefKey, String value) {
//        SharedPreferences.Editor editor = mSharedPrefs.edit();
//        editor.putString(prefKey, value);
//        editor.apply();
//    }

    public static int getIntPref(String prefKey, int defaultValue) {
        return mSharedPrefs.getInt(prefKey, defaultValue);
    }

    public static boolean getBooleanPref(String prefKey, boolean defaultValue) {
        return mSharedPrefs.getBoolean(prefKey, defaultValue);
    }
//
//    public static String getStringPref(String prefKey, String defaultValue) {
//        return mSharedPrefs.getString(prefKey, defaultValue);
//    }
}
