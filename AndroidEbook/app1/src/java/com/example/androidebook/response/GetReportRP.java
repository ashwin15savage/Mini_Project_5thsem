package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class GetReportRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("report")
    private String report;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getReport() {
        return report;
    }
}
