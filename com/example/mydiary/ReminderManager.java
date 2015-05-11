package com.example.mydiary;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.mydiary.models.ReminderModel;
import com.example.mydiary.utilities.DateManager;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Liza on 3.5.2015 г..
 */
public class ReminderManager {
    private static final String DELIMITER = "~|^";
    private static final String DELIMITER_REGEX = "~\\|\\^";

    public static void addReminder(Context context, int id, String noteText, GregorianCalendar date) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String reqCodes = sharedPreferences.getString(Constants.REQUEST_CODES, Constants.EMPTY_STRING);
        reqCodes += DELIMITER + id;
        sharedPreferences.edit().putString(Constants.REQUEST_CODES, reqCodes).commit();
        String notes = sharedPreferences.getString(Constants.NOTES, Constants.EMPTY_STRING);
        notes += DELIMITER + noteText;
        sharedPreferences.edit().putString(Constants.NOTES, notes).commit();
        String dates = sharedPreferences.getString(Constants.DATES, Constants.EMPTY_STRING);
        dates += DELIMITER + DateManager.getDateTimeStringFromCalendar(date);
        sharedPreferences.edit().putString(Constants.DATES, dates).commit();
    }

    public static void removeReminder(Context context, int id){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String[] reqCodes = sharedPreferences.getString(Constants.REQUEST_CODES, Constants.EMPTY_STRING).split(DELIMITER_REGEX, -1);
        String[] notes = sharedPreferences.getString(Constants.NOTES, Constants.EMPTY_STRING).split(DELIMITER_REGEX, -1);
        String[] dates = sharedPreferences.getString(Constants.DATES, Constants.EMPTY_STRING).split(DELIMITER_REGEX, -1);
        StringBuffer newReqCodes = new StringBuffer();
        StringBuffer newNotes= new StringBuffer();
        StringBuffer newDates = new StringBuffer();
        for (int i = 1; i < reqCodes.length; i++) {
            if(!(String.valueOf(id).equals(reqCodes[i]))){
                newReqCodes.append(DELIMITER + reqCodes[i]);
                newNotes.append(DELIMITER + notes[i]);
                newDates.append(DELIMITER + dates[i]);
            }
        }

        sharedPreferences.edit().putString(Constants.REQUEST_CODES, newReqCodes.toString()).commit();
        sharedPreferences.edit().putString(Constants.NOTES, newNotes.toString()).commit();
        sharedPreferences.edit().putString(Constants.DATES, newDates.toString()).commit();
    }

    public static HashMap<String, ReminderModel> getAllReminders(Context context){
        HashMap<String, ReminderModel> map = new HashMap<String, ReminderModel>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String reqCodes = sharedPreferences.getString(Constants.REQUEST_CODES, Constants.EMPTY_STRING);
        String notes = sharedPreferences.getString(Constants.NOTES, Constants.EMPTY_STRING);
        String dates = sharedPreferences.getString(Constants.DATES, Constants.EMPTY_STRING);
        String[] reqCodesArr = reqCodes.split(DELIMITER_REGEX, -1);
        String[] notesArr = notes.split(DELIMITER_REGEX, -1);
        String[] datesArr = dates.split(DELIMITER_REGEX, -1);

        for (int i = 1; i < reqCodesArr.length; i++){
            String noteText = notesArr[i];
            GregorianCalendar date = DateManager.getGregorianCalendarFromString(datesArr[i]);
            ReminderModel reminder = new ReminderModel(noteText, date);
            map.put(reqCodesArr[i], reminder);
        }

        return map;
    }

    public static List<ReminderModel> getAllRemindersList(Context context){
        List<ReminderModel> reminders = new ArrayList<ReminderModel>();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String notes = sharedPreferences.getString(Constants.NOTES, Constants.EMPTY_STRING);
        String dates = sharedPreferences.getString(Constants.DATES, Constants.EMPTY_STRING);
        String[] notesArr = notes.split(DELIMITER_REGEX, -1);
        String[] datesArr = dates.split(DELIMITER_REGEX, -1);

        for (int i = 1; i < notesArr.length; i++){
            String noteText = notesArr[i];
            GregorianCalendar date = DateManager.getGregorianCalendarFromString(datesArr[i]);
            ReminderModel reminder = new ReminderModel(noteText, date);
            reminders.add(reminder);
        }

        return reminders;
    }
}
