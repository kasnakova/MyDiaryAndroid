package com.example.mydiary.models;

import com.example.mydiary.utilities.Constants;

/**
 * Created by Liza on 18.4.2015 г..
 */
public class MyDiaryUserModel {
    private static String name = Constants.EMPTY_STRING;
    private static String token = Constants.EMPTY_STRING;

    public static String getName(){
        return name;
    }

    public static String getToken(){
        return token;
    }

    public static void setName(String name){
        MyDiaryUserModel.name = name;
    }

    public static void setToken(String accessToken){
        MyDiaryUserModel.token = accessToken;
    }
}
