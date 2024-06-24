package com.hope0163.chattingapp.model;

public class Chatting {
    public String message;
    public String profileImgUrl;
    public String time;
    public String userName;

    public Chatting(String message, String profileImgUrl, String time, String userName) {
        this.message = message;
        this.profileImgUrl = profileImgUrl;
        this.time = time;
        this.userName = userName;
    }


}
