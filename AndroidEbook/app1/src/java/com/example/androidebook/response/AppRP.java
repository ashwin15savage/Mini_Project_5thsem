package com.example.androidebook.response;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class AppRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("publisher_id")
    private String publisher_id;

    @SerializedName("interstitial_ad_id")
    private String interstitial_ad_id;

    @SerializedName("interstitial_ad_click")
    private String interstitial_ad_click;

    @SerializedName("banner_ad_id")
    private String banner_ad_id;

    @SerializedName("interstitial_ad")
    private boolean interstitial_ad = false;

    @SerializedName("banner_ad")
    private boolean banner_ad = false;

    @SerializedName("native_ad")
    private boolean native_ad = false;

    @SerializedName("banner_ad_type")
    private String banner_ad_type;

    @SerializedName("interstitial_ad_type")
    private String interstitial_ad_type;

    @SerializedName("privacy_policy_link")
    private String privacy_policy_link;

    @SerializedName("app_update_status")
    private boolean app_update_status;

    @SerializedName("app_new_version")
    private int app_new_version;

    @SerializedName("app_update_desc")
    private String app_update_desc;

    @SerializedName("app_redirect_url")
    private String app_redirect_url;

    @SerializedName("cancel_update_status")
    private boolean cancel_update_status;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getPublisher_id() {
        return publisher_id;
    }

    public String getInterstitial_ad_id() {
        return interstitial_ad_id;
    }

    public String getInterstitial_ad_click() {
        return interstitial_ad_click;
    }

    public String getBanner_ad_id() {
        return banner_ad_id;
    }

    public boolean isInterstitial_ad() {
        return interstitial_ad;
    }

    public boolean isBanner_ad() {
        return banner_ad;
    }

    public boolean isNative_ad() {
        return native_ad;
    }

    public String getBanner_ad_type() {
        return banner_ad_type;
    }

    public String getInterstitial_ad_type() {
        return interstitial_ad_type;
    }

    public String getPrivacy_policy_link() {
        return privacy_policy_link;
    }

    public boolean isApp_update_status() {
        return app_update_status;
    }

    public int getApp_new_version() {
        return app_new_version;
    }

    public String getApp_update_desc() {
        return app_update_desc;
    }

    public String getApp_redirect_url() {
        return app_redirect_url;
    }

    public boolean isCancel_update_status() {
        return cancel_update_status;
    }
}
