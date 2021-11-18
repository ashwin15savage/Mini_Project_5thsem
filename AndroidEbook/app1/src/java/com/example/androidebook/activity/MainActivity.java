package com.example.androidebook.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.androidebook.BuildConfig;
import com.example.androidebook.R;
import com.example.androidebook.fragment.AuthorBookFragment;
import com.example.androidebook.fragment.AuthorFragment;
import com.example.androidebook.fragment.BookFragment;
import com.example.androidebook.fragment.CategoryFragment;
import com.example.androidebook.fragment.DownloadFragment;
import com.example.androidebook.fragment.HomeFragment;
import com.example.androidebook.fragment.ProfileFragment;
import com.example.androidebook.fragment.SettingFragment;
import com.example.androidebook.fragment.SubCatBookFragment;
import com.example.androidebook.response.AppRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.Constant;
import com.example.androidebook.util.Events;
import com.example.androidebook.util.GlobalBus;
import com.example.androidebook.util.Method;
import com.facebook.login.LoginManager;
import com.google.ads.consent.ConsentForm;
import com.google.ads.consent.ConsentFormListener;
import com.google.ads.consent.ConsentInfoUpdateListener;
import com.google.ads.consent.ConsentInformation;
import com.google.ads.consent.ConsentStatus;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private Method method;
    private ConsentForm form;
    public static MaterialToolbar toolbar;
    private ProgressBar progressBar;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private LinearLayout linearLayout;
    private InputMethodManager imm;
    private boolean isAdMOb = false;
    private boolean doubleBackToExitPressedOnce = false;
    private String id = "", subId = "", title = "", type = "";

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GlobalBus.getBus().register(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        if (intent.hasExtra("id")) {
            id = intent.getStringExtra("id");
            subId = intent.getStringExtra("subId");
            title = intent.getStringExtra("title");
            type = intent.getStringExtra("type");
        }

        method = new Method(MainActivity.this);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_main);
        toolbar.setTitle(getResources().getString(R.string.app_name));

        progressBar = findViewById(R.id.progressBar_main);
        linearLayout = findViewById(R.id.linearLayout_main);

        progressBar.setVisibility(View.GONE);

        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        toolbar.setNavigationIcon(R.drawable.ic_side_nav);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        checkLogin();

        if (method.isNetworkAvailable()) {
            appDetail();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
            } else {
                if (getSupportFragmentManager().getBackStackEntryCount() != 0) {
                    String title = getSupportFragmentManager().getFragments().get(getSupportFragmentManager().getBackStackEntryCount() - 1).getTag();
                    if (title != null) {
                        toolbar.setTitle(title);
                    }
                    super.onBackPressed();
                } else {
                    this.doubleBackToExitPressedOnce = true;
                    Toast.makeText(this, getResources().getString(R.string.Please_click_BACK_again_to_exit), Toast.LENGTH_SHORT).show();

                    new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, 2000);
                }
            }
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NotNull MenuItem item) {

        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        // Handle navigation view item clicks here.
        item.setChecked(!item.isChecked());

        //Closing drawer on item click
        drawer.closeDrawers();

        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {

            case R.id.home:
                selectDrawerItem(0);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeFragment(),
                        getResources().getString(R.string.home)).commitAllowingStateLoss();
                return true;

            case R.id.latest:
                selectDrawerItem(1);
                backStackRemove();
                BookFragment bookFragment = new BookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", "latest");
                bookFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main,
                        bookFragment, getResources().getString(R.string.latest)).commitAllowingStateLoss();
                return true;

            case R.id.category:
                selectDrawerItem(2);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new CategoryFragment(),
                        getResources().getString(R.string.category)).commitAllowingStateLoss();
                return true;

            case R.id.author:
                selectDrawerItem(3);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new AuthorFragment(),
                        getResources().getString(R.string.author)).commitAllowingStateLoss();
                return true;

            case R.id.download:
                selectDrawerItem(4);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new DownloadFragment(),
                        getResources().getString(R.string.download)).commitAllowingStateLoss();
                return true;

            case R.id.profile:
                selectDrawerItem(5);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new ProfileFragment(),
                        getResources().getString(R.string.profile)).commitAllowingStateLoss();
                return true;

            case R.id.setting:
                selectDrawerItem(6);
                backStackRemove();
                getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new SettingFragment(),
                        getResources().getString(R.string.settings)).commitAllowingStateLoss();
                return true;

            case R.id.login:
                deselectDrawerItem(7);
                if (method.isLogin()) {
                    logout();
                } else {
                    startActivity(new Intent(MainActivity.this, Login.class));
                    finishAffinity();
                }
                return true;

            default:
                return true;
        }
    }

    public void backStackRemove() {
        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            getSupportFragmentManager().popBackStack();
        }
    }

    public void selectDrawerItem(int position) {
        navigationView.getMenu().getItem(position).setChecked(true);
    }

    public void deselectDrawerItem(int position) {
        navigationView.getMenu().getItem(position).setCheckable(false);
        navigationView.getMenu().getItem(position).setChecked(false);
    }

    public void appDetail() {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(MainActivity.this));
        jsObj.addProperty("method_name", "get_app_details");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<AppRP> call = apiService.getAppData(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<AppRP>() {
            @Override
            public void onResponse(@NotNull Call<AppRP> call, @NotNull Response<AppRP> response) {

                try {

                    Constant.appRP = response.body();
                    assert Constant.appRP != null;

                    if (Constant.appRP.getStatus().equals("1")) {

                        if (Constant.appRP.isApp_update_status() && Constant.appRP.getApp_new_version() > BuildConfig.VERSION_CODE) {
                            showAppDialog(Constant.appRP.getApp_update_desc(),
                                    Constant.appRP.getApp_redirect_url(),
                                    Constant.appRP.isCancel_update_status());
                        }

                        if ((Constant.appRP.isBanner_ad()) || (Constant.appRP.isInterstitial_ad()) || (Constant.appRP.isNative_ad())) {
                            if (getBannerAdType() || Constant.appRP.getInterstitial_ad_type().equals("admob")) {
                                if (getBannerAdType() && Constant.appRP.isBanner_ad()) {
                                    isAdMOb = true;
                                }
                                if (!getBannerAdType() && Constant.appRP.isBanner_ad()) {
                                    method.FbBannerAd(linearLayout);
                                } else {
                                    if (!Constant.appRP.isBanner_ad()) {
                                        linearLayout.setVisibility(View.GONE);
                                    }
                                }
                                checkForConsent();
                            } else {
                                if (Constant.appRP.isBanner_ad()) {
                                    method.FbBannerAd(linearLayout);
                                } else {
                                    linearLayout.setVisibility(View.GONE);
                                }
                                if (Constant.appRP.isNative_ad()) {
                                    checkForConsent();
                                }
                            }
                        } else {
                            linearLayout.setVisibility(View.GONE);
                        }

                        try {
                            Constant.AD_COUNT_SHOW = Integer.parseInt(Constant.appRP.getInterstitial_ad_click());
                        } catch (Exception e) {
                            Constant.AD_COUNT_SHOW = 0;
                        }

                        try {
                            switch (type) {
                                case "category":
                                    SubCatBookFragment subCatBookFragment = new SubCatBookFragment();
                                    Bundle bundleCat = new Bundle();
                                    bundleCat.putString("id", id);
                                    bundleCat.putString("title", title);
                                    subCatBookFragment.setArguments(bundleCat);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, subCatBookFragment, title)
                                            .commitAllowingStateLoss();
                                    toolbar.setTitle(title);
                                    break;
                                case "subCategory":
                                    BookFragment bookFragment = new BookFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("type", type);
                                    bundle.putString("title", title);
                                    bundle.putString("id", id);
                                    bundle.putString("subId", subId);
                                    bookFragment.setArguments(bundle);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, bookFragment,
                                            title).commitAllowingStateLoss();
                                    toolbar.setTitle(title);
                                    break;
                                case "author":
                                    AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                                    Bundle bundleAuthor = new Bundle();
                                    bundleAuthor.putString("title", title);
                                    bundleAuthor.putString("id", id);
                                    bundleAuthor.putString("type", type);
                                    authorBookFragment.setArguments(bundleAuthor);
                                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, authorBookFragment,
                                            title).commitAllowingStateLoss();
                                    break;
                                default:
                                    getSupportFragmentManager().beginTransaction().replace(R.id.frameLayout_main, new HomeFragment(),
                                            getResources().getString(R.string.home)).commitAllowingStateLoss();
                                    selectDrawerItem(0);
                                    break;
                            }
                        } catch (Exception e) {
                            method.alertBox(getResources().getString(R.string.wrong));
                        }

                    } else {
                        method.alertBox(Constant.appRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<AppRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void checkForConsent() {

        ConsentInformation consentInformation = ConsentInformation.getInstance(MainActivity.this);
        String[] publisherIds = {Constant.appRP.getPublisher_id()};
        consentInformation.requestConsentInfoUpdate(publisherIds, new ConsentInfoUpdateListener() {
            @Override
            public void onConsentInfoUpdated(ConsentStatus consentStatus) {
                Log.d("consentStatus", consentStatus.toString());
                // User's consent status successfully updated.
                switch (consentStatus) {
                    case PERSONALIZED:
                        Method.personalizationAd = true;
                        if (isAdMOb) {
                            method.showPersonalizedAds(linearLayout);
                        }
                        break;
                    case NON_PERSONALIZED:
                        Method.personalizationAd = false;
                        if (isAdMOb) {
                            method.showNonPersonalizedAds(linearLayout);
                        }
                        break;
                    case UNKNOWN:
                        if (ConsentInformation.getInstance(getBaseContext()).isRequestLocationInEeaOrUnknown()) {
                            requestConsent();
                        } else {
                            Method.personalizationAd = true;
                            if (isAdMOb) {
                                method.showPersonalizedAds(linearLayout);
                            }
                        }
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onFailedToUpdateConsentInfo(String errorDescription) {
                // User's consent status failed to update.
            }
        });

    }

    public void requestConsent() {
        URL privacyUrl = null;
        try {
            // TODO: Replace with your app's privacy policy URL.
            privacyUrl = new URL(Constant.appRP.getPrivacy_policy_link());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            // Handle error.
        }
        form = new ConsentForm.Builder(MainActivity.this, privacyUrl)
                .withListener(new ConsentFormListener() {
                    @Override
                    public void onConsentFormLoaded() {
                        showForm();
                        // Consent form loaded successfully.
                    }

                    @Override
                    public void onConsentFormOpened() {
                        // Consent form was displayed.
                    }

                    @Override
                    public void onConsentFormClosed(ConsentStatus consentStatus, Boolean userPrefersAdFree) {
                        Log.d("consentStatus_form", consentStatus.toString());
                        switch (consentStatus) {
                            case PERSONALIZED:
                                Method.personalizationAd = true;
                                if (isAdMOb) {
                                    method.showPersonalizedAds(linearLayout);
                                }
                                break;
                            case NON_PERSONALIZED:
                            case UNKNOWN:
                                Method.personalizationAd = false;
                                if (isAdMOb) {
                                    method.showNonPersonalizedAds(linearLayout);
                                }
                                break;
                        }
                    }

                    @Override
                    public void onConsentFormError(String errorDescription) {
                        Log.d("errorDescription", errorDescription);
                    }
                })
                .withPersonalizedAdsOption()
                .withNonPersonalizedAdsOption()
                .build();
        form.load();
    }

    private void showForm() {
        if (form != null) {
            form.show();
        }
    }

    private boolean getBannerAdType() {
        return Constant.appRP.getBanner_ad_type().equals("admob");
    }

    @Subscribe
    public void getLogin(Events.Login login) {
        if (method != null) {
            checkLogin();
        }
    }

    private void checkLogin() {
        if (navigationView != null) {
            int position = 7;
            if (method.isLogin()) {
                navigationView.getMenu().getItem(position).setIcon(R.drawable.ic_logout);
                navigationView.getMenu().getItem(position).setTitle(getResources().getString(R.string.logout));
            } else {
                navigationView.getMenu().getItem(position).setIcon(R.drawable.ic_login);
                navigationView.getMenu().getItem(position).setTitle(getResources().getString(R.string.login));
            }
        }
    }

    //alert message box
    public void logout() {

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(MainActivity.this, R.style.DialogTitleTextStyle);
        builder.setCancelable(false);
        builder.setMessage(getResources().getString(R.string.logout_message));
        builder.setPositiveButton(getResources().getString(R.string.logout),
                (arg0, arg1) -> {
                    if (method.getLoginType().equals("google")) {

                        // Configure sign-in to request the ic_user_login's ID, email address, and basic
                        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                .requestEmail()
                                .build();

                        // Build a GoogleSignInClient with the options specified by gso.
                        //Google login
                        GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(MainActivity.this, gso);

                        mGoogleSignInClient.signOut()
                                .addOnCompleteListener(MainActivity.this, task -> {
                                    method.editor.putBoolean(method.pref_login, false);
                                    method.editor.commit();
                                    startActivity(new Intent(MainActivity.this, Login.class));
                                    finishAffinity();
                                });
                    } else if (method.getLoginType().equals("facebook")) {
                        LoginManager.getInstance().logOut();
                        method.editor.putBoolean(method.pref_login, false);
                        method.editor.commit();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finishAffinity();
                    } else {
                        method.editor.putBoolean(method.pref_login, false);
                        method.editor.commit();
                        startActivity(new Intent(MainActivity.this, Login.class));
                        finishAffinity();
                    }
                });
        builder.setNegativeButton(getResources().getString(R.string.cancel),
                (dialogInterface, i) -> {

                });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    private void showAppDialog(String description, String link, boolean isCancel) {

        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update_app);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

        MaterialTextView textViewDes = dialog.findViewById(R.id.textView_description_dialog_update);
        MaterialButton buttonUpdate = dialog.findViewById(R.id.button_update_dialog_update);
        MaterialButton buttonCancel = dialog.findViewById(R.id.button_cancel_dialog_update);

        if (isCancel) {
            buttonCancel.setVisibility(View.VISIBLE);
        } else {
            buttonCancel.setVisibility(View.GONE);
        }
        textViewDes.setText(description);

        buttonUpdate.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }

}

