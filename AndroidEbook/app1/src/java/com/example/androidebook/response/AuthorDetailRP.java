package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthorDetailRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("author_id")
    private String author_id;

    @SerializedName("author_name")
    private String author_name;

    @SerializedName("author_city_name")
    private String author_city_name;

    @SerializedName("author_description")
    private String author_description;

    @SerializedName("author_image")
    private String author_image;

    @SerializedName("author_instagram")
    private String author_instagram;

    @SerializedName("author_facebook")
    private String author_facebook;

    @SerializedName("author_website")
    private String author_website;

    @SerializedName("author_youtube")
    private String author_youtube;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getAuthor_city_name() {
        return author_city_name;
    }

    public String getAuthor_description() {
        return author_description;
    }

    public String getAuthor_image() {
        return author_image;
    }

    public String getAuthor_instagram() {
        return author_instagram;
    }

    public String getAuthor_facebook() {
        return author_facebook;
    }

    public String getAuthor_website() {
        return author_website;
    }

    public String getAuthor_youtube() {
        return author_youtube;
    }
}
