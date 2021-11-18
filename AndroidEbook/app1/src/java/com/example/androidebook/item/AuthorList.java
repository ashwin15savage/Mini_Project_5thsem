package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AuthorList implements Serializable {

    @SerializedName("is_ads")
    private boolean is_ads;

    @SerializedName("native_ad_type")
    private String native_ad_type ;

    @SerializedName("native_ad_id")
    private String native_ad_id ;

    @SerializedName("author_id")
    private String author_id;

    @SerializedName("author_name")
    private String  author_name;

    @SerializedName("author_image")
    private String author_image;

    public boolean isIs_ads() {
        return is_ads;
    }

    public String getNative_ad_type() {
        return native_ad_type;
    }

    public String getNative_ad_id() {
        return native_ad_id;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getAuthor_image() {
        return author_image;
    }
}
