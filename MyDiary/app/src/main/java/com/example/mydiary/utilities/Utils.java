package com.example.mydiary.utilities;

import android.content.Context;

import com.example.mydiary.models.MyDiaryUserModel;

/**
 * Created by Liza on 10.5.2015 г..
 */
public class Utils {
    public static int getId(String noteText){
        return Math.abs(noteText.hashCode());
    }

    public static boolean returnToHome(Context context){
        boolean isLoggedIn = !MyDiaryUserModel.getName().equals(Constants.EMPTY_STRING);
        return (isLoggedIn || SettingsManager.isOffline(context));
    }
}
