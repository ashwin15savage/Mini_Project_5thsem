package com.example.androidebook.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
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
import com.example.androidebook.adapter.BookHomeAdapter;
import com.example.androidebook.adapter.CommentAdapter;
import com.example.androidebook.interfaces.FavouriteIF;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.CommentList;
import com.example.androidebook.response.BookDetailRP;
import com.example.androidebook.response.DataRP;
import com.example.androidebook.response.GetReportRP;
import com.example.androidebook.response.MyRatingRP;
import com.example.androidebook.response.RatingRP;
import com.example.androidebook.response.UserCommentRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.DownloadEpub;
import com.example.androidebook.util.Events;
import com.example.androidebook.util.GlobalBus;
import com.example.androidebook.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.greenrobot.eventbus.Subscribe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookDetail extends AppCompatActivity {

    private OnClick onClick;
    private Method method;
    private Animation myAnim;
    public MaterialToolbar toolbar;
    private String bookId, type;
    private int rate, position = 0;
    private InputMethodManager imm;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private BookDetailRP bookDetailRP;
    private List<CommentList> commentLists;
    private CommentAdapter commentAdapter;
    private WebView webView;
    private RatingView ratingBar;
    private TextInputEditText editTextComment;
    private CircleImageView imageViewUser;
    private RecyclerView recyclerViewComment, recyclerViewRelated;
    private ConstraintLayout conMain, conNoData, conFavorite, conDownload, conRead, conReport, conRelated, conComment;
    private ImageView imageView, imageViewBookCover, imageViewFav, imageViewDownload, imageViewRead, imageViewReport, imageViewRating, imageViewSend;
    private MaterialTextView textViewBookName, textViewAuthor, textViewRating, textViewView, textViewRelViewAll, textViewComment;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_detail);

        GlobalBus.getBus().register(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Intent intent = getIntent();
        bookId = intent.getStringExtra("bookId");
        type = intent.getStringExtra("type");
        position = intent.getIntExtra("position", 0);

        myAnim = AnimationUtils.loadAnimation(BookDetail.this, R.anim.bounce);

        commentLists = new ArrayList<>();
        progressDialog = new ProgressDialog(BookDetail.this);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            startActivity(new Intent(BookDetail.this, BookDetail.class)
                    .putExtra("bookId", id)
                    .putExtra("position", position)
                    .putExtra("type", type));
            finish();
        };
        method = new Method(BookDetail.this, onClick);
        method.forceRTLIfSupported();

        toolbar = findViewById(R.id.toolbar_bookDetail);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        conMain = findViewById(R.id.con_main_bookDetail);
        conNoData = findViewById(R.id.con_noDataFound);
        imageView = findViewById(R.id.imageView_bookDetail);
        imageViewBookCover = findViewById(R.id.imageView_book_bookDetail);
        imageViewDownload = findViewById(R.id.imageView_download_bookDetail);
        imageViewRead = findViewById(R.id.imageView_read_bookDetail);
        imageViewReport = findViewById(R.id.imageView_report_bookDetail);
        conDownload = findViewById(R.id.con_download_bookDetail);
        conFavorite = findViewById(R.id.con_favorite_bookDetail);
        conRead = findViewById(R.id.con_read_bookDetail);
        conReport = findViewById(R.id.con_report_bookDetail);
        imageViewRating = findViewById(R.id.imageView_rating_bookDetail);
        textViewRelViewAll = findViewById(R.id.textView_relatedViewAll_bookDetail);
        textViewComment = findViewById(R.id.textView_comment_bookDetail);
        webView = findViewById(R.id.webView_bookDetail);
        textViewBookName = findViewById(R.id.textView_bookName_bookDetail);
        textViewAuthor = findViewById(R.id.textView_authorName_bookDetail);
        progressBar = findViewById(R.id.progressBar_bookDetail);
        recyclerViewRelated = findViewById(R.id.recyclerView_related_bookDetail);
        recyclerViewComment = findViewById(R.id.recyclerView_comment_bookDetail);
        conRelated = findViewById(R.id.con_related_bookDetail);
        textViewRating = findViewById(R.id.textView_ratingCount_bookDetail);
        textViewView = findViewById(R.id.textView_view_bookDetail);
        ratingBar = findViewById(R.id.ratingBar_bookDetail);
        imageViewUser = findViewById(R.id.imageView_commentPro_bookDetail);
        imageViewSend = findViewById(R.id.imageView_comment_bookDetail);
        editTextComment = findViewById(R.id.editText_comment_bookDetail);
        imageViewFav = findViewById(R.id.image_favorite_bookDetail);
        conComment = findViewById(R.id.con_commentList_bookDetail);

        conMain.setVisibility(View.GONE);
        conNoData.setVisibility(View.GONE);

        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, (int) (method.getScreenWidth() / 1.8)));

        LinearLayout linearLayout = findViewById(R.id.linearLayout_bookDetail);
        method.adView(linearLayout);

        if (method.isLogin()) {
            Glide.with(BookDetail.this).load(method.getUserImage())
                    .placeholder(R.drawable.profile)
                    .into(imageViewUser);
        }

        textViewBookName.setSelected(true);

        recyclerViewRelated.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerRelatedBook = new LinearLayoutManager(BookDetail.this, LinearLayoutManager.HORIZONTAL, false);
        recyclerViewRelated.setLayoutManager(layoutManagerRelatedBook);
        recyclerViewRelated.setFocusable(false);
        recyclerViewRelated.setNestedScrollingEnabled(false);

        recyclerViewComment.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(BookDetail.this);
        recyclerViewComment.setLayoutManager(layoutManager);
        recyclerViewComment.setFocusable(false);
        recyclerViewComment.setNestedScrollingEnabled(false);

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                scd(method.userId());
            } else {
                scd("0");
            }
        } else {
            progressBar.setVisibility(View.GONE);
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

    }

    @Subscribe
    public void getNotify(Events.AddComment comment) {
        if (bookDetailRP != null) {
            if (bookDetailRP.getId().equals(comment.getBook_id())) {
                commentLists.add(0, new CommentList(comment.getComment_id(),
                        comment.getBook_id(), comment.getUser_id(), comment.getUser_name(), comment.getUser_image(),
                        comment.getComment_text(), comment.getComment_date()));
                if (commentAdapter != null) {
                    commentAdapter.notifyDataSetChanged();
                    String textView_total = getResources().getString(R.string.view_all) + " " + "(" + comment.getTotal_comment() + ")";
                    textViewComment.setText(textView_total);
                }
            }
            if (commentLists.size() == 0) {
                conComment.setVisibility(View.GONE);
            } else {
                conComment.setVisibility(View.VISIBLE);
            }
        }
    }

    @Subscribe
    public void getNotify(Events.DeleteComment deleteComment) {
        if (bookDetailRP != null) {
            if (bookDetailRP.getId().equals(deleteComment.getBookId())) {
                if (textViewComment != null) {
                    String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + deleteComment.getTotalComment() + ")";
                    textViewComment.setText(buttonTotal);
                }
            }
            if (deleteComment.getType().equals("all_comment")) {
                if (bookDetailRP.getId().equals(deleteComment.getBookId())) {
                    commentLists.clear();
                    commentLists.addAll(deleteComment.getCommentLists());
                    if (commentAdapter != null) {
                        commentAdapter.notifyDataSetChanged();
                    }
                }
            }
            if (commentLists.size() == 0) {
                conComment.setVisibility(View.GONE);
            } else {
                conComment.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_scd, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_share:
                if (bookDetailRP != null && bookDetailRP.getShare_link() != null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, bookDetailRP.getShare_link());
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.choose_one)));
                } else {
                    method.alertBox(getResources().getString(R.string.wrong));
                }
                break;

            default:
                return super.onOptionsItemSelected(menuItem);
        }
        return true;
    }

    // single book detail
    public void scd(String userId) {

        progressBar.setVisibility(View.VISIBLE);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "get_single_book");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookDetailRP> call = apiService.getBookDetail(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookDetailRP>() {
            @SuppressLint({"SetTextI18n", "SetJavaScriptEnabled", "UseCompatLoadingForDrawables"})
            @Override
            public void onResponse(@NotNull Call<BookDetailRP> call, @NotNull Response<BookDetailRP> response) {

                try {
                    bookDetailRP = response.body();
                    assert bookDetailRP != null;

                    if (bookDetailRP.getStatus().equals("1")) {

                        toolbar.setTitle(bookDetailRP.getBook_title());

                        if (bookDetailRP.getIs_fav().equals("true")) {
                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
                        } else {
                            if (method.isDarkMode()) {
                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_unfav_white));
                            } else {
                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_unfav));
                            }
                        }

                        int value = Integer.parseInt(bookDetailRP.getBook_views());
                        value++;
                        bookDetailRP.setBook_views(String.valueOf(value));

                        Glide.with(BookDetail.this).load(bookDetailRP.getBook_cover_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageViewBookCover);

                        Glide.with(BookDetail.this).load(bookDetailRP.getBook_bg_img())
                                .placeholder(R.drawable.placeholder_portable)
                                .into(imageView);

                        WebSettings webSettings = webView.getSettings();
                        webSettings.setJavaScriptEnabled(true);
                        webSettings.setPluginState(WebSettings.PluginState.ON);

                        webView.setBackgroundColor(Color.TRANSPARENT);
                        webView.setFocusableInTouchMode(false);
                        webView.setFocusable(false);
                        webView.getSettings().setDefaultTextEncodingName("UTF-8");
                        String mimeType = "text/html";
                        String encoding = "utf-8";

                        String text = "<html dir=" + method.isWebViewTextRtl() + "><head>"
                                + "<style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/fonts/poppins_medium.ttf\")}body{font-family: MyFont;color: " + method.webViewText() + "line-height:1.6}"
                                + "a {color:" + method.webViewLink() + "text-decoration:none}"
                                + "</style></head>"
                                + "<body>"
                                + bookDetailRP.getBook_description()
                                + "</body></html>";

                        webView.loadDataWithBaseURL(null, text, mimeType, encoding, null);

                        textViewBookName.setText(bookDetailRP.getBook_title());
                        textViewAuthor.setText(getString(R.string.by) + " " + bookDetailRP.getAuthor_name());
                        textViewRating.setText(bookDetailRP.getTotal_rate());
                        textViewView.setText(Method.Format(Integer.parseInt(bookDetailRP.getBook_views())));
                        ratingBar.setRating(Float.parseFloat(bookDetailRP.getRate_avg()));

                        //related book
                        if (bookDetailRP.getBookLists().size() == 0) {
                            conRelated.setVisibility(View.GONE);
                        } else {
                            conRelated.setVisibility(View.VISIBLE);
                            BookHomeAdapter bookHomeAdapter = new BookHomeAdapter(BookDetail.this, bookDetailRP.getBookLists(), "related_scd", onClick);
                            recyclerViewRelated.setAdapter(bookHomeAdapter);
                        }

                        commentLists.addAll(bookDetailRP.getCommentLists());
                        textViewComment.setText(getResources().getString(R.string.view_all) + " " + "(" + bookDetailRP.getTotal_comment() + ")");

                        //book comment
                        if (commentLists.size() == 0) {
                            conComment.setVisibility(View.GONE);
                        } else {
                            conComment.setVisibility(View.VISIBLE);
                            commentAdapter = new CommentAdapter(BookDetail.this, commentLists);
                            recyclerViewComment.setAdapter(commentAdapter);
                        }

                        conMain.setVisibility(View.VISIBLE);

                        imageView.setOnClickListener(v -> continueBook(bookDetailRP.getId()));

                        conDownload.setOnClickListener(view -> {
                            imageViewDownload.startAnimation(myAnim);
                            if (method.isNetworkAvailable()) {
                                if (bookDetailRP.getBook_file_url().contains(".epub")) {
                                    method.download(bookDetailRP.getId(),
                                            bookDetailRP.getBook_title(),
                                            bookDetailRP.getBook_cover_img(),
                                            bookDetailRP.getAuthor_name(),
                                            bookDetailRP.getBook_file_url(), "epub");
                                } else {
                                    method.download(bookDetailRP.getId(),
                                            bookDetailRP.getBook_title(),
                                            bookDetailRP.getBook_cover_img(),
                                            bookDetailRP.getAuthor_name(),
                                            bookDetailRP.getBook_file_url(), "pdf");
                                }
                            } else {
                                method.alertBox(getResources().getString(R.string.internet_connection));
                            }
                        });

                        conFavorite.setOnClickListener(v -> {
                            imageViewFav.startAnimation(myAnim);
                            if (method.isLogin()) {
                                FavouriteIF favouriteIF = (isFavourite, message) -> {
                                    if (!isFavourite.equals("")) {
                                        if (isFavourite.equals("true")) {
                                            imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_fav_hov));
                                        } else {
                                            if (method.isDarkMode()) {
                                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_unfav_white));
                                            } else {
                                                imageViewFav.setImageDrawable(getResources().getDrawable(R.drawable.ic_unfav));
                                            }
                                        }
                                    }
                                };
                                method.addToFav(bookDetailRP.getId(), method.userId(), favouriteIF);
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Login.class));
                            }
                        });

                        conRead.setOnClickListener(v -> {
                            imageViewRead.startAnimation(myAnim);
                            if (method.isNetworkAvailable()) {
                                continueBook(bookDetailRP.getId());
                            } else {
                                method.alertBox(getResources().getString(R.string.internet_connection));
                            }
                        });

                        conReport.setOnClickListener(v -> {
                            imageViewReport.startAnimation(myAnim);
                            if (method.isLogin()) {
                                getReport(method.userId(), bookDetailRP.getId());
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Login.class));
                            }
                        });

                        textViewComment.setOnClickListener(v -> startActivity(new Intent(BookDetail.this, AllComment.class)
                                .putExtra("bookId", bookDetailRP.getId())));

                        textViewRelViewAll.setOnClickListener(v ->
                                startActivity(new Intent(BookDetail.this, RelatedBook.class)
                                        .putExtra("cat_id", bookDetailRP.getCat_id())
                                        .putExtra("sub_cat_id", bookDetailRP.getCat_id())
                                        .putExtra("aid", bookDetailRP.getAid())
                                        .putExtra("book_id", bookDetailRP.getId())));

                        imageViewRating.setOnClickListener(v -> {
                            imageViewRating.startAnimation(myAnim);
                            if (method.isLogin()) {
                                myRating(bookDetailRP.getId(), method.userId());
                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Login.class));
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

                                    editTextComment.clearFocus();
                                    imm.hideSoftInputFromWindow(editTextComment.getWindowToken(), 0);

                                    if (method.isNetworkAvailable()) {
                                        Comment(method.userId(), bookDetailRP.getId(), comment);
                                    } else {
                                        method.alertBox(getResources().getString(R.string.internet_connection));
                                    }

                                }

                            } else {
                                Method.loginBack = true;
                                startActivity(new Intent(BookDetail.this, Login.class));
                            }
                        });

                    } else if (bookDetailRP.getStatus().equals("2")) {
                        method.suspend(bookDetailRP.getMessage());
                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(bookDetailRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<BookDetailRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                conNoData.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void continueBook(String bookId) {

        if (method.isLogin()) {

            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
            jsObj.addProperty("user_id", method.userId());
            jsObj.addProperty("book_id", bookId);
            jsObj.addProperty("method_name", "user_continue_book");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<DataRP> call = apiService.submitContinueReading(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<DataRP>() {
                @Override
                public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                    try {
                        DataRP dataRP = response.body();
                        assert dataRP != null;

                        if (dataRP.getStatus().equals("1")) {

                            if (dataRP.getSuccess().equals("1")) {
                                openBook();
                            } else {
                                method.alertBox(dataRP.getMsg());
                            }

                        } else if (dataRP.getStatus().equals("2")) {
                            method.suspend(dataRP.getMessage());
                        } else {
                            method.alertBox(dataRP.getMessage());
                        }

                    } catch (Exception e) {
                        Log.d("exception_error", e.toString());
                        method.alertBox(getResources().getString(R.string.failed_try_again));
                    }

                    progressDialog.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        } else {
            openBook();
        }

    }

    private void openBook() {
        if (bookDetailRP.getBook_file_url().contains(".epub")) {
            DownloadEpub downloadEpub = new DownloadEpub(BookDetail.this);
            downloadEpub.pathEpub(bookDetailRP.getBook_file_url(), bookDetailRP.getId());
        } else {
            startActivity(new Intent(BookDetail.this, PDFShow.class)
                    .putExtra("id", bookDetailRP.getId())
                    .putExtra("link", bookDetailRP.getBook_file_url())
                    .putExtra("toolbarTitle", bookDetailRP.getBook_title())
                    .putExtra("type", "link"));
        }
    }

    //get user rating
    private void myRating(String bookId, String userId) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("method_name", "get_rating");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<MyRatingRP> call = apiService.getMyRating(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<MyRatingRP>() {
            @Override
            public void onResponse(@NotNull Call<MyRatingRP> call, @NotNull Response<MyRatingRP> response) {

                try {
                    MyRatingRP myRatingRP = response.body();
                    assert myRatingRP != null;

                    if (myRatingRP.getStatus().equals("1")) {

                        if (myRatingRP.getSuccess().equals("1")) {

                            final Dialog dialog = new Dialog(BookDetail.this);
                            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                            dialog.setContentView(R.layout.rating_dialog);
                            if (method.isRtl()) {
                                dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                            }
                            dialog.setCancelable(false);
                            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
                            MaterialButton button = dialog.findViewById(R.id.button_ratingDialog);
                            ImageView imageViewClose = dialog.findViewById(R.id.image_close_ratingDialog);
                            RatingView ratingBar = dialog.findViewById(R.id.ratingBar_ratingDialog);

                            ratingBar.setRating(0);
                            ratingBar.setOnRatingChangedListener((oldRating, newRating) -> rate = (int) newRating);

                            imageViewClose.setOnClickListener(v -> dialog.dismiss());

                            button.setOnClickListener(v -> {
                                if (method.isNetworkAvailable()) {
                                    if (rate > 0) {
                                        ratingSend(rate, userId, bookId);
                                        dialog.dismiss();
                                    } else {
                                        method.alertBox(getResources().getString(R.string.rating_status));
                                    }
                                } else {
                                    method.alertBox(getResources().getString(R.string.internet_connection));
                                }
                            });

                            ratingBar.setRating(Float.parseFloat(myRatingRP.getUser_rate()));

                            dialog.show();

                        }

                    } else if (myRatingRP.getStatus().equals("2")) {
                        method.suspend(myRatingRP.getMessage());
                    } else {
                        method.alertBox(myRatingRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<MyRatingRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    //send user rating
    public void ratingSend(final int rate, String userId, final String bookId) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("rate", rate);
        jsObj.addProperty("method_name", "book_rating");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<RatingRP> call = apiService.submitRating(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<RatingRP>() {
            @Override
            public void onResponse(@NotNull Call<RatingRP> call, @NotNull Response<RatingRP> response) {

                try {
                    RatingRP ratingRP = response.body();
                    assert ratingRP != null;

                    if (ratingRP.getStatus().equals("1")) {

                        if (ratingRP.getSuccess().equals("1")) {
                            bookDetailRP.setRate_avg(ratingRP.getRate_avg());
                            bookDetailRP.setTotal_rate(ratingRP.getTotal_rate());
                            ratingBar.setRating(Float.parseFloat(ratingRP.getRate_avg()));
                            textViewRating.setText(ratingRP.getTotal_rate());
                        }

                        method.alertBox(ratingRP.getMsg());

                    } else if (ratingRP.getStatus().equals("2")) {
                        method.suspend(ratingRP.getMessage());
                    } else {
                        method.alertBox(ratingRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<RatingRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    //get book report
    public void getReport(String userId, String bookId) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("method_name", "get_report");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<GetReportRP> call = apiService.getBookReport(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<GetReportRP>() {
            @Override
            public void onResponse(@NotNull Call<GetReportRP> call, @NotNull Response<GetReportRP> response) {

                try {
                    GetReportRP getReportRP = response.body();
                    assert getReportRP != null;
                    if (getReportRP.getStatus().equals("1")) {

                        final Dialog dialog = new Dialog(BookDetail.this);
                        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                        dialog.setContentView(R.layout.report_dialog);
                        if (method.isRtl()) {
                            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
                        }
                        dialog.setCancelable(false);
                        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);

                        TextInputEditText editText = dialog.findViewById(R.id.editText_report_dialog);
                        MaterialButton buttonSubmit = dialog.findViewById(R.id.button_submit_review_dialog);
                        MaterialButton buttonClose = dialog.findViewById(R.id.button_close_review_dialog);

                        editText.setText(getReportRP.getReport());

                        buttonSubmit.setOnClickListener(v -> {

                            editText.setError(null);
                            String stringReport = editText.getText().toString();

                            if (stringReport.equals("") || stringReport.isEmpty()) {
                                editText.requestFocus();
                                editText.setError(getResources().getString(R.string.please_enter_comment));
                            } else {
                                editText.clearFocus();
                                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                                submitReport(userId, bookId, stringReport);
                                dialog.dismiss();
                            }

                        });

                        buttonClose.setOnClickListener(v -> {
                            dialog.dismiss();
                        });

                        dialog.show();

                    } else if (getReportRP.getStatus().equals("2")) {
                        method.suspend(getReportRP.getMessage());
                    } else {
                        method.alertBox(getReportRP.getMessage());
                    }

                } catch (Exception e) {
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                    Log.d("exception_error", e.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(@NotNull Call<GetReportRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }

    //send book report
    public void submitReport(String userId, String bookId, String report) {

        progressDialog.show();
        progressDialog.setMessage(getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("report", report);
        jsObj.addProperty("method_name", "book_report");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DataRP> call = apiService.submitBookReport(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DataRP>() {
            @Override
            public void onResponse(@NotNull Call<DataRP> call, @NotNull Response<DataRP> response) {

                try {
                    DataRP dataRP = response.body();
                    assert dataRP != null;

                    if (dataRP.getStatus().equals("1")) {
                        method.alertBox(dataRP.getMsg());
                    } else if (dataRP.getStatus().equals("2")) {
                        method.suspend(dataRP.getMessage());
                    } else {
                        method.alertBox(dataRP.getMessage());
                    }

                } catch (Exception e) {
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                    Log.d("exception_error", e.toString());
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<DataRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });
    }


    //comment
    private void Comment(final String userId, String bookId, final String comment) {

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(BookDetail.this));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("user_id", userId);
        jsObj.addProperty("comment_text", comment);
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

                            editTextComment.setText("");

                            commentLists.add(0, new CommentList(userCommentRP.getComment_id(),
                                    userCommentRP.getBook_id(), userCommentRP.getUser_id(), userCommentRP.getUser_name(),
                                    userCommentRP.getUser_profile(), userCommentRP.getComment_text(), userCommentRP.getComment_date()));

                            if (commentLists.size() == 0) {
                                conComment.setVisibility(View.GONE);
                            } else {
                                conComment.setVisibility(View.VISIBLE);
                                if (commentAdapter == null) {
                                    commentAdapter = new CommentAdapter(BookDetail.this, commentLists);
                                    recyclerViewComment.setAdapter(commentAdapter);
                                } else {
                                    commentAdapter.notifyDataSetChanged();
                                }
                            }

                            String buttonTotal = getResources().getString(R.string.view_all) + " " + "(" + userCommentRP.getTotal_comment() + ")";
                            textViewComment.setText(buttonTotal);

                        }

                        Toast.makeText(BookDetail.this, userCommentRP.getMsg(), Toast.LENGTH_SHORT).show();

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
    public void onBackPressed() {
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        GlobalBus.getBus().unregister(this);
        super.onDestroy();
    }


}
