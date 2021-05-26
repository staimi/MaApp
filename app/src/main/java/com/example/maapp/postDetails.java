package com.example.maapp;


public class postDetails {
    private String postText = "";
    private String picture = "";
    private String time = "";

    public postDetails(){ }

    public void setPostText(String postText) { this.postText = postText; }

    public void setPicture(String picture) { this.picture = picture; }

    public void setTime(String time) { this.time = time; }

    public postDetails(String time, String postText, String picture){
        this.time = time;
        this.postText = postText;
        this.picture = picture;
    }


    public String getPostText() {return postText;}

    public String getPicture() {return picture;}

    public String getTime() {return time;}
}
