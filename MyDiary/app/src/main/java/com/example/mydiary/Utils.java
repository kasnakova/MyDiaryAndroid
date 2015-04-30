package com.example.mydiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Liza on 26.4.2015 г..
 */
public class Utils {
    private static final String TAG = "Utils";

    public static JSONObject makeJson(String data){
        try {
            JSONObject obj = new JSONObject(data);
            return obj;
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString() + " | Message: " + e.getMessage());
            return null;
        }
    }

    public static void makeAlert(Context context, String title, String message){
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setMessage(message)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setIcon(R.drawable.diary)
                .show();
    }

    public static void NoInternetOrServerAlert(Context context){
        makeAlert(context, "A problem occurred", "Please check your internet connection or whether the server is running");
    }
}
