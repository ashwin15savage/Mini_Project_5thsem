package com.example.androidebook.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.activity.Login;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.response.ProfileRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.Events;
import com.example.androidebook.util.GlobalBus;
import com.example.androidebook.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ProfileFragment extends Fragment {

    private Method method;
    private ProgressBar progressBar;
    private MaterialButton buttonLogin;
    private CircleImageView imageView;
    private ConstraintLayout conMain, conNoData;
    private MaterialTextView textViewName, textViewNotLogin;
    private ImageView imageViewLoginType, imageViewEdit, imageViewData;
    private MaterialCardView cardViewPass, cardViewFavourite, cardViewContinue;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.profile_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.profile));
        }

        GlobalBus.getBus().register(this);

        method = new Method(getActivity());
        method.forceRTLIfSupported();

        conMain = view.findViewById(R.id.con_main_pro);
        conNoData = view.findViewById(R.id.con_not_login);
        progressBar = view.findViewById(R.id.progressbar_pro);
        buttonLogin = view.findViewById(R.id.button_not_login);
        imageViewData = view.findViewById(R.id.imageView_not_login);
        textViewNotLogin = view.findViewById(R.id.textView_not_login);
        imageViewLoginType = view.findViewById(R.id.imageView_loginType_pro);
        imageView = view.findViewById(R.id.imageView_pro);
        imageViewEdit = view.findViewById(R.id.imageView_edit_pro);
        textViewName = view.findViewById(R.id.textView_name_pro);
        cardViewPass = view.findViewById(R.id.cardView_changePassword_pro);
        cardViewFavourite = view.findViewById(R.id.cardView_fav_pro);
        cardViewContinue = view.findViewById(R.id.cardView_continue_pro);

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finishAffinity();
        });

        progressBar.setVisibility(View.GONE);
        data(false, false);
        conMain.setVisibility(View.GONE);

        callData();

        return view;

    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                profile(method.userId());
            } else {
                data(true, true);
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void data(boolean isShow, boolean isLogin) {
        if (isShow) {
            if (isLogin) {
                buttonLogin.setVisibility(View.VISIBLE);
                textViewNotLogin.setText(getResources().getString(R.string.you_have_not_login));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_login));
            } else {
                buttonLogin.setVisibility(View.GONE);
                textViewNotLogin.setText(getResources().getString(R.string.no_data_found));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_data));
            }
            conNoData.setVisibility(View.VISIBLE);
        } else {
            conNoData.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void getData(Events.ProfileUpdate profileUpdate) {
        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.profile));
        }
        data(false, false);
        conMain.setVisibility(View.GONE);
        callData();
    }

    public void profile(String userId) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("method_name", "user_profile");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<ProfileRP> call = apiService.getProfile(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<ProfileRP>() {
                @SuppressLint("UseCompatLoadingForDrawables")
                @Override
                public void onResponse(@NotNull Call<ProfileRP> call, @NotNull Response<ProfileRP> response) {

                    if (getActivity() != null) {

                        try {
                            ProfileRP profileRP = response.body();
                            assert profileRP != null;

                            if (profileRP.getStatus().equals("1")) {

                                method.editor.putString(method.userImage, profileRP.getUser_profile());
                                method.editor.commit();

                                String loginType = method.getLoginType();
                                if (loginType.equals("google") || loginType.equals("facebook")) {
                                    cardViewPass.setVisibility(View.GONE);
                                    imageViewLoginType.setVisibility(View.VISIBLE);
                                    if (loginType.equals("google")) {
                                        imageViewLoginType.setImageDrawable(getResources().getDrawable(R.drawable.google_user_pro));
                                    } else {
                                        imageViewLoginType.setImageDrawable(getResources().getDrawable(R.drawable.fb_user_pro));
                                    }
                                } else {
                                    cardViewPass.setVisibility(View.VISIBLE);
                                    imageViewLoginType.setVisibility(View.GONE);
                                }

                                Glide.with(getActivity().getApplicationContext()).load(profileRP.getUser_profile())
                                        .placeholder(R.drawable.profile).into(imageView);

                                textViewName.setText(profileRP.getName());

                                imageViewEdit.setOnClickListener(v -> {
                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                                            new EditProfileFragment(), getResources().getString(R.string.edit_profile))
                                            .addToBackStack(getResources().getString(R.string.edit_profile)).commitAllowingStateLoss();
                                });

                                cardViewPass.setOnClickListener(v -> {
                                    ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
                                    Bundle bundle = new Bundle();
                                    bundle.putString("name", profileRP.getName());
                                    bundle.putString("image", profileRP.getUser_profile());
                                    changePasswordFragment.setArguments(bundle);
                                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                                            changePasswordFragment, getResources().getString(R.string.change_pass))
                                            .addToBackStack(getResources().getString(R.string.change_pass)).commitAllowingStateLoss();
                                });

                                cardViewFavourite.setOnClickListener(v -> {
                                    bookFragment("favourite", getResources().getString(R.string.favorite));
                                });

                                cardViewContinue.setOnClickListener(v -> {
                                    bookFragment("continueBook", getResources().getString(R.string.continue_book));
                                });

                                conMain.setVisibility(View.VISIBLE);

                            } else if (profileRP.getStatus().equals("2")) {
                                method.suspend(profileRP.getMessage());
                            } else {
                                data(true, false);
                                method.alertBox(profileRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<ProfileRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    data(true, false);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });
        }
    }

    private void bookFragment(String type, String title) {
        BookFragment bookFragment = new BookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        bookFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                bookFragment, title).addToBackStack(title).commitAllowingStateLoss();
    }

}
