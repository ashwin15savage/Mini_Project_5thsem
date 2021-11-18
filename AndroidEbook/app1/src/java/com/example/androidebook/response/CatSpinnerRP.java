package com.example.androidebook.response;

import com.example.androidebook.item.CatSpinnerList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class CatSpinnerRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("category_name")
    private List<CatSpinnerList> catSpinnerLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<CatSpinnerList> getCatSpinnerLists() {
        return catSpinnerLists;
    }
}
