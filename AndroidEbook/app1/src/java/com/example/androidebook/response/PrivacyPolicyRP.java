package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class PrivacyPolicyRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("app_privacy_policy")
    private String app_privacy_policy;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getApp_privacy_policy() {
        return app_privacy_policy;
    }
}
