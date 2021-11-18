package com.example.androidebook.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.androidebook.R;
import com.example.androidebook.response.RegisterRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private Method method;
    private ProgressDialog progressDialog;
    private InputMethodManager imm;
    private MaterialCheckBox checkBox;
    private TextInputEditText editTextName, editTextEmail, editTextPassword, editTextConformPassword, editTextPhoneNo;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        method = new Method(Register.this);
        method.forceRTLIfSupported();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        progressDialog = new ProgressDialog(Register.this);

        editTextName = findViewById(R.id.editText_name_register);
        editTextEmail = findViewById(R.id.editText_email_register);
        editTextPassword = findViewById(R.id.editText_password_register);
        editTextConformPassword = findViewById(R.id.editText_conform_password_register);
        editTextPhoneNo = findViewById(R.id.editText_phoneNo_register);

        MaterialButton buttonSubmit = findViewById(R.id.button_submit);
        MaterialTextView textViewLogin = findViewById(R.id.textView_login_register);
        MaterialTextView textViewTerms = findViewById(R.id.textView_terms_register);
        checkBox = findViewById(R.id.checkbox_register);

        textViewTerms.setOnClickListener(v -> startActivity(new Intent(Register.this, PrivacyPolicy.class)));

        textViewLogin.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finishAffinity();
        });

        buttonSubmit.setOnClickListener(v -> form());

    }

    private boolean isValidMail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void form() {

        String name = editTextName.getText().toString();
        String email = editTextEmail.getText().toString();
        String password = editTextPassword.getText().toString();
        String conformPassword = editTextConformPassword.getText().toString();
        String phoneNo = editTextPhoneNo.getText().toString();

        editTextName.setError(null);
        editTextEmail.setError(null);
        editTextPassword.setError(null);
        editTextConformPassword.setError(null);
        editTextPhoneNo.setError(null);

        if (name.equals("") || name.isEmpty()) {
            editTextName.requestFocus();
            editTextName.setError(getResources().getString(R.string.please_enter_name));
        } else if (!isValidMail(email) || email.isEmpty()) {
            editTextEmail.requestFocus();
            editTextEmail.setError(getResources().getString(R.string.please_enter_email));
        } else if (password.equals("") || password.isEmpty()) {
            editTextPassword.requestFocus();
            editTextPassword.setError(getResources().getString(R.string.please_enter_password));
        } else if (conformPassword.equals("") || conformPassword.isEmpty()) {
            editTextConformPassword.requestFocus();
            editTextConformPassword.setError(getResources().getString(R.string.please_enter_confirm_password));
        } else if (phoneNo.equals("") || phoneNo.isEmpty()) {
            editTextPhoneNo.requestFocus();
            editTextPhoneNo.setError(getResources().getString(R.string.please_enter_phone));
        } else if (!password.equals(conformPassword)) {
            method.alertBox(getResources().getString(R.string.password_not_match));
        } else if (!checkBox.isChecked()) {
            method.alertBox(getResources().getString(R.string.please_select_terms));
        } else {

            editTextName.clearFocus();
            editTextEmail.clearFocus();
            editTextPassword.clearFocus();
            editTextConformPassword.clearFocus();
            editTextPhoneNo.clearFocus();
            imm.hideSoftInputFromWindow(editTextName.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextEmail.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPassword.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextConformPassword.getWindowToken(), 0);
            imm.hideSoftInputFromWindow(editTextPhoneNo.getWindowToken(), 0);

            if (method.isNetworkAvailable()) {
                register(name, email, password, phoneNo);
            } else {
                method.alertBox(getResources().getString(R.string.internet_connection));
            }

        }
    }

    public void register(String sendName, String sendEmail, String sendPassword, String sendPhone) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(Register.this));
        jsObj.addProperty("name", sendName);
        jsObj.addProperty("email", sendEmail);
        jsObj.addProperty("password", sendPassword);
        jsObj.addProperty("phone", sendPhone);
        jsObj.addProperty("type", "normal");
        jsObj.addProperty("device_id", method.getDeviceId());
        jsObj.addProperty("method_name", "user_register");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RegisterRP> call = apiService.getRegisterDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RegisterRP>() {
            @Override
            public void onResponse(@NotNull Call<RegisterRP> call, @NotNull Response<RegisterRP> response) {

                try {
                    RegisterRP registerRP = response.body();
                    assert registerRP != null;

                    if (registerRP.getStatus().equals("1")) {

                        if (registerRP.getSuccess().equals("1")) {

                            Toast.makeText(Register.this, registerRP.getMsg(), Toast.LENGTH_SHORT).show();

                            editTextName.setText("");
                            editTextEmail.setText("");
                            editTextPassword.setText("");
                            editTextConformPassword.setText("");
                            editTextPhoneNo.setText("");
                            checkBox.setChecked(false);

                            startActivity(new Intent(Register.this, Login.class));
                            finishAffinity();

                        } else {
                            method.alertBox(registerRP.getMsg());
                        }

                    } else {
                        method.alertBox(registerRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<RegisterRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

}
