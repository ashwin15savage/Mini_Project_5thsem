package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FavouriteRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("is_favourite")
    private String is_favourite;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSuccess() {
        return success;
    }

    public String getMsg() {
        return msg;
    }

    public String getIs_favourite() {
        return is_favourite;
    }
}
