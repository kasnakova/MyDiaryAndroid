package com.example.mydiary;

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
}
