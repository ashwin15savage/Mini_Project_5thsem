package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthorSpinnerList implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("author_name")
    private String author_name;

    public AuthorSpinnerList(String id, String author_name) {
        this.id = id;
        this.author_name = author_name;
    }

    public String getId() {
        return id;
    }

    public String getAuthor_name() {
        return author_name;
    }
}
