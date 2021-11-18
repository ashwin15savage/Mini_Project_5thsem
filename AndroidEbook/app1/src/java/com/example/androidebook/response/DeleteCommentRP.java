package com.example.androidebook.response;

import com.example.androidebook.item.CommentList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DeleteCommentRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("success")
    private String success;

    @SerializedName("msg")
    private String msg;

    @SerializedName("total_comment")
    private String total_comment;

    @SerializedName("user_comments")
    private List<CommentList> commentLists;

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

    public String getTotal_comment() {
        return total_comment;
    }

    public List<CommentList> getCommentLists() {
        return commentLists;
    }
}
