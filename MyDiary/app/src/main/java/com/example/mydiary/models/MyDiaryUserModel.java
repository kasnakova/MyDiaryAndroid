package com.example.mydiary.models;

/**
 * Created by Liza on 18.4.2015 г..
 */
public class MyDiaryUserModel {
    private static String name = "";
    private static String token = "";

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
