package com.example.mydiary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;

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
public class Utils {
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

    public static String getDateTimeStringFromCalendar(GregorianCalendar calendar){
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static String getTimeStringFromCalendar(GregorianCalendar calendar){
        SimpleDateFormat fmt = new SimpleDateFormat("HH:mm:ss");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static String getDateStringFromCalendar(GregorianCalendar calendar){
        SimpleDateFormat fmt = new SimpleDateFormat("MM/dd/yyyy");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static String getBGDateStringFromCalendar(GregorianCalendar calendar){
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static GregorianCalendar getGregorianCalendarFromString(String date){
        GregorianCalendar calendar = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
            Date parsedDate = df.parse(date);
            calendar = new GregorianCalendar();
            calendar.setTime(parsedDate);
        } catch(Exception e) {
            Log.d(TAG, "Date parsing exception: " + e.toString() + " | Message: " + e.getMessage());
        }

        return calendar;
    }

    public static GregorianCalendar getGregorianCalendarFromDate(Date date){
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return  calendar;
    }
}
