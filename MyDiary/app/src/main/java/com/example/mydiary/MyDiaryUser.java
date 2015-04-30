package com.example.mydiary;

/**
 * Created by Liza on 18.4.2015 г..
 */
public class MyDiaryUser {
    private static String name = "";
    private static String token = "";

    public static String getName(){
        return name;
    }

    public static String getToken(){
        return token;
    }

    public static void setName(String name){
        MyDiaryUser.name = name;
    }

    public static void setToken(String accessToken){
        MyDiaryUser.token = accessToken;
    }
}
