package com.example.androidebook.activity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.example.androidebook.R;
import com.example.androidebook.response.LoginRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.Method;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashScreen extends AppCompatActivity {

    private Method method;
    private ProgressBar progressBar;
    private Boolean isCancelled = false;
    private static int SPLASH_TIME_OUT = 1000;
    private String id = "0", subId = "", type = "", title = "";

    //Google login
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        method = new Method(SplashScreen.this);
        method.login();
        method.forceRTLIfSupported();
        switch (method.themMode()) {
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "light":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case "dark":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

        setContentView(R.layout.activity_splash_screen);

        // Making notification bar transparent
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        // making notification bar transparent
        method.changeStatusBarColor();

        Intent intent = getIntent();
        if (intent != null) {
            if (intent.hasExtra("id")) {
                id = intent.getStringExtra("id");
                subId = intent.getStringExtra("subId");
                type = intent.getStringExtra("type");
                title = intent.getStringExtra("title");
            } else {
                Uri data = intent.getData();
                if (data != null) {
                    String[] strings = data.toString().split("book_id=");
                    id = strings[strings.length - 1];
                    type = "deepLink";
                }
            }
        }

        progressBar = findViewById(R.id.progressBar_splash_screen);
        progressBar.setVisibility(View.GONE);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        if (method.isNetworkAvailable()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                // This method will be executed once the timer is over
                // Start your app main activity
                if (!isCancelled) {
                    if (method.isLogin()) {
                        login(method.userId());
                    } else {
                        callActivity();
                    }
                }
            }, SPLASH_TIME_OUT);
        } else {
            startActivity(new Intent(SplashScreen.this, MainActivity.class));
            finishAffinity();
        }
    }

    public void login(String userId) {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SplashScreen.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "user_status");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<LoginRP> call = apiService.getLoginDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<LoginRP>() {
            @Override
            public void onResponse(@NotNull Call<LoginRP> call, @NotNull Response<LoginRP> response) {

                try {
                    LoginRP loginRP = response.body();
                    assert loginRP != null;

                    String loginType = method.getLoginType();

                    if (loginRP.getStatus().equals("1")) {

                        if (loginRP.getSuccess().equals("1")) {

                            if (loginType.equals("google")) {
                                if (GoogleSignIn.getLastSignedInAccount(SplashScreen.this) != null) {
                                    callActivity();
                                } else {
                                    method.editor.putBoolean(method.pref_login, false);
                                    method.editor.commit();
                                    startActivity(new Intent(SplashScreen.this, Login.class));
                                    finishAffinity();
                                }
                            } else if (loginType.equals("facebook")) {

                                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                                boolean isLoggedIn = accessToken != null && !accessToken.isExpired();

                                if (isLoggedIn) {
                                    callActivity();
                                } else {

                                    LoginManager.getInstance().logOut();

                                    method.editor.putBoolean(method.pref_login, false);
                                    method.editor.commit();
                                    startActivity(new Intent(SplashScreen.this, Login.class));
                                    finishAffinity();

                                }

                            } else {
                                callActivity();
                            }
                        } else {

                            if (loginType.equals("google")) {

                                mGoogleSignInClient.signOut()
                                        .addOnCompleteListener(SplashScreen.this, task -> {

                                        });


                            } else if (loginType.equals("facebook")) {
                                LoginManager.getInstance().logOut();
                            }

                            method.editor.putBoolean(method.pref_login, false);
                            method.editor.commit();
                            startActivity(new Intent(SplashScreen.this, Login.class));
                            finishAffinity();
                        }

                    } else if (loginRP.getStatus().equals("2")) {
                        method.suspend(loginRP.getMessage());
                    } else {
                        method.alertBox(loginRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<LoginRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    private void callActivity() {
        switch (type) {
            case "category":
            case "subCategory":
            case "author":
                startActivity(new Intent(SplashScreen.this, MainActivity.class)
                        .putExtra("id", id)
                        .putExtra("subId", subId)
                        .putExtra("type", type)
                        .putExtra("title", title));
                finishAffinity();
                break;
            case "book":
            case "deepLink":
                startActivity(new Intent(SplashScreen.this, BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", 0)
                        .putExtra("type", "external"));
                finishAffinity();
                break;
            default:
                if (method.pref.getBoolean(method.show_login, true)) {
                    method.editor.putBoolean(method.show_login, false);
                    method.editor.commit();
                    startActivity(new Intent(SplashScreen.this, Login.class));
                } else {
                    startActivity(new Intent(SplashScreen.this, MainActivity.class));
                }
                finishAffinity();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        isCancelled = true;
        super.onDestroy();
    }

}
