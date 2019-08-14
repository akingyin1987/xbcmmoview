package com.xbcmmoview.tools;

import android.content.Context;
import android.content.SharedPreferences;

public class SpUtil {
    private static SharedPreferences sharedPreferences;

    public static void putBoolean(Context context, String key, Boolean value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        sharedPreferences.edit().putBoolean(key, value.booleanValue()).commit();
    }

    public static Boolean getBoolean(Context context, String key, Boolean defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        return Boolean.valueOf(sharedPreferences.getBoolean(key, defValue.booleanValue()));
    }

    public static void putString(Context context, String key, String value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        sharedPreferences.edit().putString(key, value).commit();
    }

    public static String getString(Context context, String key, String defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        return sharedPreferences.getString(key, defValue);
    }

    public static void putInt(Context context, String key, Integer value) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        sharedPreferences.edit().putInt(key, value.intValue()).commit();
    }

    public static Integer getInt(Context context, String key, Integer defValue) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        return Integer.valueOf(sharedPreferences.getInt(key, defValue.intValue()));
    }

    public static void remove(Context context, String key) {
        if (sharedPreferences == null) {
            sharedPreferences = context.getSharedPreferences("settingValue", 0);
        }
        sharedPreferences.edit().remove(key).commit();
    }
}
