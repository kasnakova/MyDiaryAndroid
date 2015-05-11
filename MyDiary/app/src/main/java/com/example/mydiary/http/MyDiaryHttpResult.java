package com.example.mydiary.http;

/**
 * Created by Liza on 26.4.2015 г..
 */
public class MyDiaryHttpResult {
    private boolean success;
    private MyDiaryHttpServices service;
    private String data;

    public MyDiaryHttpResult(boolean success, MyDiaryHttpServices service, String data){
        this.success = success;
        this.service = service;
        this.data = data;
    }

    public boolean getSuccess(){
        return this.success;
    }

    public MyDiaryHttpServices getService(){
        return this.service;
    }

    public String getData(){
        return this.data;
    }
}
