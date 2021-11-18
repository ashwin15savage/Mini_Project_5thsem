package com.example.androidebook.fragment;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.activity.AboutUs;
import com.example.androidebook.activity.ContactUs;
import com.example.androidebook.activity.Faq;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.PrivacyPolicy;
import com.example.androidebook.activity.SplashScreen;
import com.example.androidebook.util.Method;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textview.MaterialTextView;
import com.onesignal.OneSignal;

import org.jetbrains.annotations.NotNull;

public class SettingFragment extends Fragment {

    private Method method;
    private String themMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.setting_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.settings));
        }

        method = new Method(getActivity());

        SwitchMaterial switchMaterial = view.findViewById(R.id.switch_setting);
        MaterialTextView textViewContactUs = view.findViewById(R.id.textView_contactUs_setting);
        MaterialTextView textViewFaq = view.findViewById(R.id.textView_faq_setting);
        MaterialTextView textViewPP = view.findViewById(R.id.textView_privacy_policy_setting);
        MaterialTextView textViewShareApp = view.findViewById(R.id.textView_shareApp_setting);
        MaterialTextView textViewRateApp = view.findViewById(R.id.textView_rateApp_setting);
        MaterialTextView textViewMoreApp = view.findViewById(R.id.textView_moreApp_setting);
        MaterialTextView textViewAboutUs = view.findViewById(R.id.textView_aboutUs_setting);
        MaterialTextView textViewThemType = view.findViewById(R.id.textView_themType_setting);
        ConstraintLayout conThem = view.findViewById(R.id.con_them_setting);
        ImageView imageView = view.findViewById(R.id.imageView_them_setting);

        if (method.isDarkMode()) {
            Glide.with(getActivity().getApplicationContext()).load(R.drawable.mode_dark)
                    .placeholder(R.drawable.placeholder_portable)
                    .into(imageView);
        } else {
            Glide.with(getActivity().getApplicationContext()).load(R.drawable.mode_icon)
                    .placeholder(R.drawable.placeholder_portable)
                    .into(imageView);
        }

        switchMaterial.setChecked(method.pref.getBoolean(method.notification, true));

        switchMaterial.setOnCheckedChangeListener((buttonView, isChecked) -> {
            OneSignal.unsubscribeWhenNotificationsAreDisabled(isChecked);
            method.editor.putBoolean(method.notification, isChecked);
            method.editor.commit();
        });

        switch (method.themMode()) {
            case "system":
                textViewThemType.setText(getResources().getString(R.string.system_default));
                break;
            case "light":
                textViewThemType.setText(getResources().getString(R.string.light));
                break;
            case "dark":
                textViewThemType.setText(getResources().getString(R.string.dark));
                break;
        }

        textViewContactUs.setOnClickListener(v -> startActivity(new Intent(getActivity(), ContactUs.class)));

        textViewFaq.setOnClickListener(v -> startActivity(new Intent(getActivity(), Faq.class)));

        textViewShareApp.setOnClickListener(v -> shareApp());

        textViewRateApp.setOnClickListener(v -> rateApp());

        textViewMoreApp.setOnClickListener(v -> moreApp());

        textViewPP.setOnClickListener(v -> startActivity(new Intent(getActivity(), PrivacyPolicy.class)));

        textViewAboutUs.setOnClickListener(v -> startActivity(new Intent(getActivity(), AboutUs.class)));

        conThem.setOnClickListener(v -> {

            Dialog dialog = new Dialog(requireActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialogbox_them);
            if (method.isRtl()) {
                dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            }
            dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

            RadioGroup radioGroup = dialog.findViewById(R.id.radioGroup_them);
            MaterialTextView textViewOk = dialog.findViewById(R.id.textView_ok_them);
            MaterialTextView textViewCancel = dialog.findViewById(R.id.textView_cancel_them);

            switch (method.themMode()) {
                case "system":
                    radioGroup.check(radioGroup.getChildAt(0).getId());
                    break;
                case "light":
                    radioGroup.check(radioGroup.getChildAt(1).getId());
                    break;
                case "dark":
                    radioGroup.check(radioGroup.getChildAt(2).getId());
                    break;
            }

            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                MaterialRadioButton rb = group.findViewById(checkedId);
                if (null != rb && checkedId > -1) {
                    switch (checkedId) {
                        case R.id.radioButton_system_them:
                            themMode = "system";
                            break;
                        case R.id.radioButton_light_them:
                            themMode = "light";
                            break;
                        case R.id.radioButton_dark_them:
                            themMode = "dark";
                            break;
                        default:
                            break;
                    }
                }
            });

            textViewOk.setOnClickListener(vOk -> {
                method.editor.putString(method.themSetting, themMode);
                method.editor.commit();
                dialog.dismiss();

                startActivity(new Intent(getActivity(), SplashScreen.class));
                getActivity().finishAffinity();

            });

            textViewCancel.setOnClickListener(vCancel -> dialog.dismiss());

            dialog.show();

        });

        setHasOptionsMenu(false);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        // TODO Add your menu entries here
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    private void rateApp() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getApplication().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        // To count with Play market backstack, After pressing back button,
        // to taken back to our application, we need to add following flags to intent.
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName())));
        }
    }

    private void moreApp() {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getResources().getString(R.string.play_more_app))));
    }

    private void shareApp() {

        try {

            String string = getResources().getString(R.string.Let_me_recommend_you_this_application) + "\n\n" + "https://play.google.com/store/apps/details?id=" + getActivity().getApplication().getPackageName();

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.app_name));
            intent.putExtra(Intent.EXTRA_TEXT, string);
            startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_one)));

        } catch (Exception e) {
            Log.d("app_data", "");
        }

    }

}
