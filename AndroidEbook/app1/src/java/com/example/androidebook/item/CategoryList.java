package com.example.androidebook.item;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class CategoryList implements Serializable {

    @SerializedName("is_ads")
    private boolean is_ads;

    @SerializedName("native_ad_type")
    private String native_ad_type ;

    @SerializedName("native_ad_id")
    private String native_ad_id ;

    @SerializedName("cid")
    private String cid;

    @SerializedName("category_name")
    private String category_name;

    @SerializedName("total_books")
    private String total_books;

    @SerializedName("cat_image_thumb")
    private String cat_image_thumb;

    @SerializedName("cat_image")
    private String cat_image;

    @SerializedName("sub_cat_status")
    private String sub_cat_status;

    public boolean isIs_ads() {
        return is_ads;
    }

    public String getNative_ad_type() {
        return native_ad_type;
    }

    public String getNative_ad_id() {
        return native_ad_id;
    }

    public String getCid() {
        return cid;
    }

    public String getCategory_name() {
        return category_name;
    }

    public String getTotal_books() {
        return total_books;
    }

    public String getCat_image_thumb() {
        return cat_image_thumb;
    }

    public String getCat_image() {
        return cat_image;
    }

    public String getSub_cat_status() {
        return sub_cat_status;
    }
}
