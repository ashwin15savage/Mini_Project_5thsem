package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class SubCategoryList implements Serializable {

    @SerializedName("is_ads")
    private boolean is_ads;

    @SerializedName("native_ad_type")
    private String native_ad_type ;

    @SerializedName("native_ad_id")
    private String native_ad_id ;

    @SerializedName("sub_cat_id")
    private String sub_cat_id;

    @SerializedName("cat_id")
    private String cat_id;

    @SerializedName("sub_cat_name")
    private String sub_cat_name;

    @SerializedName("total_books")
    private String total_books;

    @SerializedName("sub_cat_image")
    private String sub_cat_image;

    @SerializedName("sub_category_image_thumb")
    private String sub_category_image_thumb;

    public boolean isIs_ads() {
        return is_ads;
    }

    public String getNative_ad_type() {
        return native_ad_type;
    }

    public String getNative_ad_id() {
        return native_ad_id;
    }

    public String getSub_cat_id() {
        return sub_cat_id;
    }

    public String getCat_id() {
        return cat_id;
    }

    public String getSub_cat_name() {
        return sub_cat_name;
    }

    public String getTotal_books() {
        return total_books;
    }

    public String getSub_cat_image() {
        return sub_cat_image;
    }

    public String getSub_category_image_thumb() {
        return sub_category_image_thumb;
    }
}
