package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubCatSpinnerList implements Serializable {

    @SerializedName("sid")
    private String sid;

    @SerializedName("sub_cat_name")
    private String sub_cat_name;

    public SubCatSpinnerList(String sid, String sub_cat_name) {
        this.sid = sid;
        this.sub_cat_name = sub_cat_name;
    }

    public String getSid() {
        return sid;
    }

    public String getSub_cat_name() {
        return sub_cat_name;
    }
}
