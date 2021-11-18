package com.example.androidebook.util;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.StrictMode;

import androidx.multidex.MultiDex;

import com.example.androidebook.R;
import com.example.androidebook.activity.SplashScreen;
import com.google.android.gms.ads.MobileAds;
import com.onesignal.OSNotificationOpenedResult;
import com.onesignal.OneSignal;

import org.json.JSONException;
import org.json.JSONObject;

import io.github.inflationx.calligraphy3.CalligraphyConfig;
import io.github.inflationx.calligraphy3.CalligraphyInterceptor;
import io.github.inflationx.viewpump.ViewPump;

public class YouApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(YouApplication.this, initializationStatus -> {

        });

        // OneSignal Initialization
        OneSignal.initWithContext(this);
        OneSignal.setAppId("xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
        OneSignal.setNotificationOpenedHandler(new NotificationExtenderExample());

        ViewPump.init(ViewPump.builder()
                .addInterceptor(new CalligraphyInterceptor(
                        new CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/poppins_medium.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build()))
                .build());

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();

    }

    class NotificationExtenderExample implements OneSignal.OSNotificationOpenedHandler {

        @Override
        public void notificationOpened(OSNotificationOpenedResult result) {

            try {

                JSONObject jsonObject = result.getNotification().getAdditionalData();

                String id = jsonObject.getString("id");
                String subId = jsonObject.getString("sub_id");
                String type = jsonObject.getString("type");
                String titleName = jsonObject.getString("title");
                String url = jsonObject.getString("external_link");

                Intent intent;
                if (id.equals("0") && !url.equals("false") && !url.trim().isEmpty()) {
                    intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.setData(Uri.parse(url));
                } else {
                    intent = new Intent(YouApplication.this, SplashScreen.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("id", id);
                    intent.putExtra("subId", subId);
                    intent.putExtra("type", type);
                    intent.putExtra("title", titleName);
                }
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

}
