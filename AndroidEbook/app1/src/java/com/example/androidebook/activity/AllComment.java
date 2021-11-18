package com.example.androidebook.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.adapter.AllCommentAdapter;
import com.example.androidebook.item.CommentList;
import com.example.androidebook.response.CommentRP;
import com.example.androidebook.response.UserCommentRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.EndlessRecyclerViewScrollListener;
import com.example.androidebook.util.Events;
import com.example.androidebook.util.GlobalBus;
import com.example.androidebook.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllComment extends AppCompatActivity {

    private MaterialToolbar toolbar;
    private String bookId;
    private Method method;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<CommentList> commentLists;
    private AllCommentAdapter allCommentAdapter;
    private TextInputEditText editTextComment;
    private ConstraintLayout conNoData;
    private InputMethodManager inputMethodManager;
    private InputMethodManager imm;
    private Boolean isOver = false;
    private int paginationIndex = 1;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_comment);

        method = new Method(AllComment.this);
        method.forceRTLIfSupported();

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");

        toolbar = findViewById(R.id.toolbar_all_comment);
        toolbar.setTitle(getResources().getString(R.string.all_comment));
        setSupportActionBar(toolbar);

        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        commentLists = new ArrayList<>();

        LinearLayout linearLayout = findViewById(R.id.linearLayout_adView_all_comment);
        method.adView(linearLayout);

        progressBar = findViewById(R.id.progressBar_allComment);
        conNoData = findViewById(R.id.con_noDataFound);
        editTextComment = findViewById(R.id.EditText_comment_allComment);
        recyclerView = findViewById(R.id.recyclerView_all_comment);
        ImageView imageViewSend = findViewById(R.id.imageView_send_allComment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        CircleImageView circleImageView = findViewById(R.id.imageView_allComment);
        if (method.isLogin()) {
            Glide.with(AllComment.this).load(method.getUserImage())
                    .placeholder(R.drawable.profile)
                    .into(circleImageView);
        }

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(AllComment.this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    allCommentAdapter.hideHeader();
                }
            }
        });

        imageViewSend.setOnClickListener(v -> {

            if (method.isLogin()) {

                editTextComment.setError(null);
                String comment = editTextComment.getText().toString();

                if (comment.isEmpty()) {
                    editTextComment.requestFocus();
                    editTextComment.setError(getResources().getString(R.string.please_enter_comment));
                } else {
                    if (method.isNetworkAvailable()) {
                        editTextComment.clearFocus();
                        inputMethodManager.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);
                        Comment(method.userId(), comment);
                    } else {
                        method.alertBox(getResources().getString(R.string.internet_connection));
                    }
                }

            } else {
                Method.loginBack = true;
                startActivity(new Intent(AllComment.this, Login.class));
            }
        });

        callData();

    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                getComment(bookId, method.userId());
            } else {
                getComment(bookId, "0");
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    public void getComment(String bookId, String userId) {

        if (allCommentAdapter == null) {
            commentLists.clear();
            progressBar.setVisibility(View.VISIBLE);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(AllComment.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("method_name", "get_all_comments");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<CommentRP> call = apiService.getAllComment(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<CommentRP>() {
            @Override
            public void onResponse(@NotNull Call<CommentRP> call, @NotNull Response<CommentRP> response) {

                try {
                    CommentRP commentRP = response.body();
                    assert commentRP != null;

                    if (commentRP.getStatus().equals("1")) {

                        if (commentRP.getCommentLists().size() == 0) {
                            if (allCommentAdapter != null) {
                                allCommentAdapter.hideHeader();
                                isOver = true;
                            }
                        } else {
                            commentLists.addAll(commentRP.getCommentLists());
                        }

                        if (allCommentAdapter == null) {
                            if (commentLists.size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                            } else {
                                allCommentAdapter = new AllCommentAdapter(AllComment.this, commentLists);
                                recyclerView.setAdapter(allCommentAdapter);
                            }
                        } else {
                            allCommentAdapter.notifyDataSetChanged();
                        }

                    } else if (commentRP.getStatus().equals("2")) {
                        method.suspend(commentRP.getMessage());
                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(commentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<CommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void Comment(final String userId, final String comment) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(AllComment.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("comment_text", comment);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "user_comment");

        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<UserCommentRP> call = apiService.submitComment(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<UserCommentRP>() {
            @Override
            public void onResponse(@NotNull Call<UserCommentRP> call, @NotNull Response<UserCommentRP> response) {

                try {
                    UserCommentRP userCommentRP = response.body();
                    assert userCommentRP != null;

                    if (userCommentRP.getStatus().equals("1")) {

                        if (userCommentRP.getSuccess().equals("1")) {

                            conNoData.setVisibility(View.GONE);
                            editTextComment.setText("");

                            commentLists.add(0, new CommentList(userCommentRP.getComment_id(),
                                    userCommentRP.getBook_id(), userCommentRP.getUser_id(), userCommentRP.getUser_name(),
                                    userCommentRP.getUser_profile(), userCommentRP.getComment_text(), userCommentRP.getComment_date()));

                            if (allCommentAdapter == null) {
                                allCommentAdapter = new AllCommentAdapter(AllComment.this, commentLists);
                                recyclerView.setAdapter(allCommentAdapter);
                            } else {
                                allCommentAdapter.notifyDataSetChanged();
                            }

                            Events.AddComment addCommentNotify = new Events.AddComment(userCommentRP.getComment_id(),
                                    userCommentRP.getBook_id(), userCommentRP.getUser_id(), userCommentRP.getUser_name(),
                                    userCommentRP.getUser_profile(), userCommentRP.getComment_text(), userCommentRP.getComment_date(), userCommentRP.getTotal_comment());
                            GlobalBus.getBus().post(addCommentNotify);

                            Toast.makeText(AllComment.this, userCommentRP.getMsg(), Toast.LENGTH_SHORT).show();

                        }
                    } else if (userCommentRP.getStatus().equals("2")) {
                        method.suspend(userCommentRP.getMessage());
                    } else {
                        method.alertBox(userCommentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

            }

            @Override
            public void onFailure(@NotNull Call<UserCommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }

}
