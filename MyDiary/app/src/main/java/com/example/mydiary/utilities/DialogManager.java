package com.example.mydiary.utilities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

import com.example.mydiary.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import hirondelle.date4j.DateTime;

/**
 * Created by Liza on 26.4.2015 г..
 */
public class DialogManager {
    private static final String TAG = "Utils";

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
