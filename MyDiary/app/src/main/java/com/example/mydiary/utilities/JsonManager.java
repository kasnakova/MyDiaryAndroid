package com.example.mydiary.utilities;

import android.util.Log;

import com.example.mydiary.models.NoteModel;
import com.example.mydiary.models.NoteType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.GregorianCalendar;

/**
 * Created by Liza on 2.5.2015 г..
 */
public class JsonManager {
    private static final String TAG = "Json";

    public static JSONObject makeJson(String data){
        try {
            JSONObject obj = new JSONObject(data);
            return obj;
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString() + " | Message: " + e.getMessage());
            return null;
        }
    }

    public static String getErrorMessage(String data){
        try {
            JSONObject obj = JsonManager.makeJson(data);
            return obj.getString("Message");
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString() + " | Message: " + e.getMessage());
            return null;
        }
    }

    public static ArrayList<NoteModel> makeNotesFromJson(String data){
        ArrayList<NoteModel> notes = new ArrayList<NoteModel>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject noteObj = JsonManager.makeJson(jsonArray.get(i).toString());
                int id = noteObj.getInt("Id");
                String noteText = noteObj.getString("NoteText");
                boolean hasPassword = noteObj.getBoolean("HasPassword");
                NoteType noteType = NoteType.values()[noteObj.getInt("NoteType")];
                GregorianCalendar calendar = DateManager.getGregorianCalendarFromString(noteObj.getString("Date"));
                NoteModel note = new NoteModel(id, noteText, calendar, noteType, hasPassword, true);
                notes.add(note);
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString() + " | Message: " + e.getMessage());
        }

        return notes;
    }

    public static ArrayList<GregorianCalendar> makeGregorianCalendarArrayFromData(String data){
        ArrayList<GregorianCalendar>  dates = new ArrayList<GregorianCalendar>();
        try {
            JSONArray jsonArray = new JSONArray(data);
            for (int i = 0; i < jsonArray.length(); i++){
                GregorianCalendar calendar = DateManager.getGregorianCalendarFromString(jsonArray.get(i).toString());
                dates.add(calendar);
            }
        } catch (JSONException e) {
            Log.d(TAG, "JSONException: " + e.toString() + " | Message: " + e.getMessage());
        }

        return dates;
    }
}
