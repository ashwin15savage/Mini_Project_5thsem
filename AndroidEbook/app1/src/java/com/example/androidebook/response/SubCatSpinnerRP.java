package com.example.androidebook.response;

import com.example.androidebook.item.CatSpinnerList;
import com.example.androidebook.item.SubCatSpinnerList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class SubCatSpinnerRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("sub_category_name")
    private List<SubCatSpinnerList> subCatSpinnerLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<SubCatSpinnerList> getSubCatSpinnerLists() {
        return subCatSpinnerLists;
    }
}
