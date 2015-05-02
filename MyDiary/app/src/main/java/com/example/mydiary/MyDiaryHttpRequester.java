package com.example.mydiary;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.GregorianCalendar;
import java.util.List;

/**
 * Created by Liza on 18.4.2015 г..
 */
public class MyDiaryHttpRequester implements IAsyncResponse {
    private  final String TAG = "MyDiaryHttpRequester";

    private final String URL_BASE = "http://192.168.0.147:50264/"; //at home
                                    //"http://192.168.199.127:50264/"; //at the academy
                                    //"http://192.168.1.198:50264/"; //at the academy cafe
    private final String URL_LOGIN = URL_BASE + "Token";
    private final String URL_REGISTER = URL_BASE + "api/Account/Register";
    private final String URL_NAME = URL_BASE + "api/Account/UserName";
    private final String URL_LOGOUT = URL_BASE + "api/Account/Logout";
    private final String URL_SEND_NOTE = URL_BASE + "api/Notes/SaveNote";
    private final String URL_GET_NOTES_FOR_DATE = URL_BASE + "api/Notes/GetNotes";
    private final String URL_DELETE_NOTE = URL_BASE + "api/Notes/DeleteNote";
    private final String URL_GET_DATES_WITH_NOTES = URL_BASE + "api/Notes/GetDatesWithNotes";

    private final String METHOD_GET = "GET";
    private final String METHOD_POST = "POST";
    private final String METHOD_DELETE = "DELETE";

    private final String FORMAT_LOGIN = "grant_type=password&username=%s&password=%s";
    private final String FORMAT_REGISTER = "Email=%s&Password=%s&ConfirmPassword=%s&Name=%s";
    private final String FORMAT_SAVE_NOTE = "NoteText=%s&Date=%s";
    private final String FORMAT_GET_NOTES_FOR_DATE = "?date=%s";
    private final String FORMAT_DELETE_NOTE = "?id=%d";
    private final String FORMAT_GET_DATES_WITH_NOTES = "?month=%d&year=%d";

    private IAsyncResponse context = this;
    private IMyDiaryHttpResponse delegate;


    public MyDiaryHttpRequester(IMyDiaryHttpResponse delegate){
        this.delegate = delegate;
    }

    public void register(String email, String password, String name) {
        try {
            final String urlParameters = String.format(FORMAT_REGISTER, email, password, password, name);

            new HttpRequester(context)
                    .execute(
                            URL_REGISTER,
                            METHOD_POST,
                            urlParameters);
        } catch(Exception ex) {
            Log.d(TAG, "Exception in register: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void login(String email, String password){
        try {
            final String urlParameters = String.format(FORMAT_LOGIN, email, password);

            new HttpRequester(context)
                    .execute(
                            URL_LOGIN,
                            METHOD_POST,
                            urlParameters);
        } catch(Exception ex) {
            Log.d(TAG, "Exception in login: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void logout(){
        try {
            new HttpRequester(context)
                    .execute(
                            URL_LOGOUT,
                            METHOD_POST,
                            "",
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in logout: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void getName(){
        try {
            new HttpRequester(context)
                    .execute(
                            URL_NAME,
                            METHOD_GET,
                            "",
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in getName: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void sendNote(String noteText, GregorianCalendar calendar){
        try {
            String date = Utils.getDateTimeStringFromCalendar(calendar);
            final String urlParameters = String.format(FORMAT_SAVE_NOTE, noteText, date);
            new HttpRequester(context)
                    .execute(
                            URL_SEND_NOTE,
                            METHOD_POST,
                            urlParameters,
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in sendNote: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void getNotesForDate(GregorianCalendar calendar){
        try{
        String date = Utils.getDateStringFromCalendar(calendar);
        String url = URL_GET_NOTES_FOR_DATE + String.format(FORMAT_GET_NOTES_FOR_DATE, date);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in getNotesForDate: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void deleteNote(int id){
        try{
            String url = URL_DELETE_NOTE + String.format(FORMAT_DELETE_NOTE, id);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_DELETE,
                            "",
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in deleteNote: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void getDatesWithNotes(int month, int year){
        try{
            String url = URL_GET_DATES_WITH_NOTES + String.format(FORMAT_GET_DATES_WITH_NOTES, month, year);
            new HttpRequester(context)
                    .execute(
                            url,
                            METHOD_GET,
                            "",
                            MyDiaryUserModel.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in getDatesWithNotes: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    @Override
    public void processFinish(String data) {
        try {
            MyDiaryHttpResult result = null;
            if(data != null) {
                JSONObject obj = Json.makeJson(data);
                if(obj != null) {
                    Log.d(TAG, "data: " + data);
                    boolean success = obj.getBoolean("success");
                    MyDiaryHttpServices service = null;
                    String url = obj.getString("url");
                    int endIndex = url.indexOf('?');
                    if(endIndex > 0) {
                        url = url.substring(0, endIndex);
                    }

                    if (url.equalsIgnoreCase(URL_LOGIN)) {
                        service = MyDiaryHttpServices.Login;
                    } else if (url.equalsIgnoreCase(URL_REGISTER)) {
                        service = MyDiaryHttpServices.Register;
                    } else if (url.equalsIgnoreCase(URL_NAME)) {
                        service = MyDiaryHttpServices.Name;
                    } else if (url.equalsIgnoreCase(URL_LOGOUT)) {
                        service = MyDiaryHttpServices.Logout;
                    } else if(url.equalsIgnoreCase(URL_SEND_NOTE)) {
                        service = MyDiaryHttpServices.SaveNote;
                    } else if(url.equalsIgnoreCase(URL_GET_NOTES_FOR_DATE)) {
                        service = MyDiaryHttpServices.GetNotesForDate;
                    } else if(url.equalsIgnoreCase(URL_DELETE_NOTE)) {
                        service = MyDiaryHttpServices.DeleteNote;
                    } else if(url.equalsIgnoreCase(URL_GET_DATES_WITH_NOTES)) {
                        service = MyDiaryHttpServices.GetDatesWithNotes;
                    }

                    String theData = obj.getString("data");
                    result = new MyDiaryHttpResult(success, service, theData);
                }
            }

            delegate.myDiaryProcessFinish(result);
        } catch (JSONException ex) {
            Log.d(TAG, "JSONException: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }
}
