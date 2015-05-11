package com.example.mydiary.utilities;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Liza on 3.5.2015 г..
 */
public class DateManager {
    private static final String TAG = "DateManager";

    public static String getDateTimeStringFromCalendar(GregorianCalendar calendar){
        if(calendar == null){
            return "";
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        fmt.setCalendar(calendar);
        return fmt.format(calendar.getTime());
    }

    public static String getTimeStringFromCalendar(GregorianCalendar calendar){
        if(calendar == null){
            return "";
        }

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

    public static String getBGDateTimeStringFromCalendar(GregorianCalendar calendar){
        if(calendar == null){
            return "";
        }

        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
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

    public static int[] getDateInNumbersFromString(String date){
        int[] numbers = new int[3];
        //day
        numbers[0] = Integer.parseInt(date.substring(0, 2));
        //month
        numbers[1] = Integer.parseInt(date.substring(3, 5));
        //year
        numbers[2] = Integer.parseInt(date.substring(6, date.length()));
        return numbers;
    }

    public static GregorianCalendar getGregorianCalendarFromNumbers(int day, int month, int year, int hour, int minutes){
        String date = year + "-" + month + "-" + day + "T" + hour + ":" + minutes + ":00";
        return  DateManager.getGregorianCalendarFromString(date);
    }

    public static long getTimeInMilisFromString(String date){
        long milis = 0;
        try {
            GregorianCalendar calendar = new GregorianCalendar();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            Date parsedDate = df.parse(date);
            calendar = new GregorianCalendar();
            calendar.setTime(parsedDate);
            milis = calendar.getTimeInMillis();
        } catch(Exception e) {
            Log.d(TAG, "Date parsing exception: " + e.toString() + " | Message: " + e.getMessage());
        }

        return milis;
    }
}
