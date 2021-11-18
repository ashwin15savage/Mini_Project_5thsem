package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class FaqRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("app_faq")
    private String app_faq;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getApp_faq() {
        return app_faq;
    }
}
