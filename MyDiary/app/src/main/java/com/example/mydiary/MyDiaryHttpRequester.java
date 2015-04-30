package com.example.mydiary;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

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
    private final String URL_SEND_NOTE = URL_BASE + "api/Notes";

    private final String METHOD_POST = "POST";
    private final String METHOD_GET = "GET";

    private final String FORMAT_LOGIN = "grant_type=password&username=%s&password=%s";
    private final String FORMAT_REGISTER = "Email=%s&Password=%s&ConfirmPassword=%s&Name=%s";
    private final String FORMAT_SAVE_NOTE = "NoteText=%s&Date=%s";

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
                            MyDiaryUser.getToken());
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
                            MyDiaryUser.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in getName: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    public void sendNote(String noteText, GregorianCalendar calendar){
        try {
            SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
            fmt.setCalendar(calendar);
            String date = fmt.format(calendar.getTime());
            final String urlParameters = String.format(FORMAT_SAVE_NOTE, noteText, date);
            new HttpRequester(context)
                    .execute(
                            URL_SEND_NOTE,
                            METHOD_POST,
                            urlParameters,
                            MyDiaryUser.getToken());
        } catch(Exception ex) {
            Log.d(TAG, "Exception in sendNote: " + ex.toString() + " | Message: " + ex.getMessage());
        }
    }

    @Override
    public void processFinish(String data) {
        try {
            MyDiaryHttpResult result = null;
            if(data != null) {
                JSONObject obj = Utils.makeJson(data);
                if(obj != null) {
                    Log.d(TAG, "data: " + data);
                    boolean success = obj.getBoolean("success");
                    MyDiaryHttpServices service = null;
                    String url = obj.getString("url");
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
