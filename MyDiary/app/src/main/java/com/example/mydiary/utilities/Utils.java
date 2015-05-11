package com.example.mydiary.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.mydiary.Constants;

/**
 * Created by Liza on 10.5.2015 г..
 */
public class Utils {
    public static int getId(String noteText){
        return Math.abs(noteText.hashCode());
    }

    public static boolean isOffline(Context context){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        return sharedPreferences.getBoolean(Constants.IS_OFFLINE, false);
    }
}
