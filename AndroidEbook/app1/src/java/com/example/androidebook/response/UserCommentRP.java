package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class UserCommentRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("comment_status")
    private String comment_status;

    @SerializedName("total_comment")
    private String total_comment;

    @SerializedName("comment_id")
    private String comment_id;

    @SerializedName("user_id")
    private String user_id;

    @SerializedName("user_name")
    private String user_name;

    @SerializedName("user_profile")
    private String user_profile;

    @SerializedName("book_id")
    private String book_id;

    @SerializedName("comment_text")
    private String comment_text;

    @SerializedName("comment_date")
    private String comment_date;

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

    public String getComment_status() {
        return comment_status;
    }

    public String getTotal_comment() {
        return total_comment;
    }

    public String getComment_id() {
        return comment_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public String getUser_profile() {
        return user_profile;
    }

    public String getBook_id() {
        return book_id;
    }

    public String getComment_text() {
        return comment_text;
    }

    public String getComment_date() {
        return comment_date;
    }
}
