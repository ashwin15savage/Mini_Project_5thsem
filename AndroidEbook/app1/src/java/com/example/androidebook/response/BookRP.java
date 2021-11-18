package com.example.androidebook.response;

import com.example.androidebook.item.BookList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BookRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("ads_param")
    private String ads_param;

    @SerializedName("total_books")
    private String total_books;

    @SerializedName("EBOOK_APP")
    private List<BookList> bookLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAds_param() {
        return ads_param;
    }

    public String getTotal_books() {
        return total_books;
    }

    public List<BookList> getBookLists() {
        return bookLists;
    }
}

