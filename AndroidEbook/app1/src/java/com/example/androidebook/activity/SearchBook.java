package com.example.androidebook.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidebook.R;
import com.example.androidebook.adapter.BookAdapterGV;
import com.example.androidebook.adapter.BookAdapterLV;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.BookList;
import com.example.androidebook.response.BookRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.EndlessRecyclerViewScrollListener;
import com.example.androidebook.util.Method;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchBook extends AppCompatActivity {

    private OnClick onClick;
    private Method method;
    private String id, search, type;
    public MaterialToolbar toolbar;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private List<BookList> bookLists;
    private LinearLayout linearLayout;
    private ConstraintLayout conNoData;
    private BookAdapterGV bookAdapterGV;
    private BookAdapterLV bookAdapterLV;
    private boolean isListView = true;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private MaterialTextView textViewCount;
    private ImageView imageViewGridView, imageViewListView;
    private EndlessRecyclerViewScrollListener scrollListener;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_list);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> startActivity(new Intent(SearchBook.this, BookDetail.class)
                .putExtra("bookId", id)
                .putExtra("position", position)
                .putExtra("type", type));
        method = new Method(SearchBook.this, onClick);
        method.forceRTLIfSupported();

        bookLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(SearchBook.this, resId);

        Intent intent = getIntent();
        id = intent.getStringExtra("id");
        search = intent.getStringExtra("search");
        type = intent.getStringExtra("type");

        toolbar = findViewById(R.id.toolbar_bookList);
        toolbar.setTitle(search);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        conNoData = findViewById(R.id.con_noDataFound);
        progressBar = findViewById(R.id.progressBar_bookList);
        imageViewListView = findViewById(R.id.imageView_listView_bookList);
        imageViewGridView = findViewById(R.id.imageView_gridView_bookList);
        recyclerView = findViewById(R.id.recyclerView_bookList);
        textViewCount = findViewById(R.id.textView_num_bookList);
        linearLayout = findViewById(R.id.linearLayout_bookList);
        method.adView(linearLayout);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(SearchBook.this);
        recyclerView.setLayoutManager(layoutManager);
        loadMoreData(layoutManager);

        imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
        imageViewGridView.setOnClickListener(v -> {
            isListView = false;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterGV = null;
            GridLayoutManager layoutManagerGr = new GridLayoutManager(SearchBook.this, 3);
            layoutManagerGr.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    if (bookAdapterGV != null) {
                        if (bookAdapterGV.getItemViewType(position) == 1) {
                            return 1;
                        } else {
                            return 3;
                        }
                    }
                    return 3;
                }
            });
            recyclerView.setLayoutManager(layoutManagerGr);
            loadMoreData(layoutManagerGr);
            callData();
        });

        imageViewListView.setOnClickListener(v -> {
            isListView = true;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterLV = null;
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(SearchBook.this);
            recyclerView.setLayoutManager(layoutManager1);
            loadMoreData(layoutManager1);
            callData();
        });

        isListView = true;
        callData();

    }

    public void loadMoreData(RecyclerView.LayoutManager layoutManager) {
        scrollListener = new EndlessRecyclerViewScrollListener((LinearLayoutManager) layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    if (isListView) {
                        bookAdapterLV.hideHeader();
                    } else {
                        bookAdapterGV.hideHeader();
                    }
                }
            }
        };
        recyclerView.addOnScrollListener(scrollListener);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    public void viewChange() {
        if (isListView) {
            imageViewGridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid));
            imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
        } else {
            imageViewGridView.setImageDrawable(getResources().getDrawable(R.drawable.ic_grid_hov));
            imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // action with ID action_refresh was selected
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

    public void callData() {
        if (method.isNetworkAvailable()) {
            if (isListView) {
                bookLV(id);
            } else {
                bookGV(id);
            }
        } else {
            progressBar.setVisibility(View.GONE);
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    public void bookLV(String id) {

        if (bookAdapterLV == null) {
            bookLists.clear();
            progressBar.setVisibility(View.VISIBLE);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SearchBook.this));
        switch (type) {
            case "category_search":
                jsObj.addProperty("category_id", id);
                break;
            case "sub_category_search":
                jsObj.addProperty("sub_cat_id", id);
                break;
            case "author_search":
                jsObj.addProperty("author_id", id);
                break;
        }
        jsObj.addProperty("search_text", search);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("ads_param", adsParam);
        jsObj.addProperty("is_book", "list_book");
        jsObj.addProperty("method_name", "get_search_books");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookRP> call = apiService.getSearchBook(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookRP>() {
            @Override
            public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                try {
                    BookRP bookRP = response.body();
                    assert bookRP != null;

                    if (bookRP.getStatus().equals("1")) {

                        adsParam = bookRP.getAds_param();

                        if (bookRP.getBookLists().size() == 0) {
                            if (bookAdapterLV != null) {
                                bookAdapterLV.hideHeader();
                                isOver = true;
                            }
                        } else {
                            bookLists.addAll(bookRP.getBookLists());
                        }

                        String count = bookRP.getTotal_books() + " " + getResources().getString(R.string.items_capital);
                        textViewCount.setText(count);

                        if (bookAdapterLV == null) {
                            if (bookRP.getBookLists().size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                            } else {
                                bookAdapterLV = new BookAdapterLV(SearchBook.this, bookLists, "cat_by_list", onClick);
                                recyclerView.setAdapter(bookAdapterLV);
                                recyclerView.setLayoutAnimation(animation);
                            }
                        } else {
                            bookAdapterLV.notifyDataSetChanged();
                        }


                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(bookRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<BookRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    public void bookGV(String id) {

        if (bookAdapterGV == null) {
            bookLists.clear();
            progressBar.setVisibility(View.VISIBLE);
        }

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(SearchBook.this));
        switch (type) {
            case "category_search":
                jsObj.addProperty("category_id", id);
                break;
            case "sub_category_search":
                jsObj.addProperty("sub_cat_id", id);
                break;
            case "author_search":
                jsObj.addProperty("author_id", id);
                break;
        }
        jsObj.addProperty("search_text", search);
        jsObj.addProperty("page", paginationIndex);
        jsObj.addProperty("ads_param", adsParam);
        jsObj.addProperty("is_book", "grid_book");
        jsObj.addProperty("method_name", "get_search_books");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<BookRP> call = apiService.getSearchBook(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<BookRP>() {
            @Override
            public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                try {
                    BookRP bookRP = response.body();
                    assert bookRP != null;

                    if (bookRP.getStatus().equals("1")) {

                        adsParam = bookRP.getAds_param();

                        if (bookRP.getBookLists().size() == 0) {
                            if (bookAdapterGV != null) {
                                bookAdapterGV.hideHeader();
                                isOver = true;
                            }
                        } else {
                            bookLists.addAll(bookRP.getBookLists());
                        }

                        String count = bookRP.getTotal_books() + " " + getResources().getString(R.string.items_capital);
                        textViewCount.setText(count);

                        if (bookAdapterGV == null) {
                            if (bookRP.getBookLists().size() == 0) {
                                conNoData.setVisibility(View.VISIBLE);
                            } else {
                                bookAdapterGV = new BookAdapterGV(SearchBook.this, bookLists, "cat_by_list", onClick);
                                recyclerView.setAdapter(bookAdapterGV);
                                recyclerView.setLayoutAnimation(animation);
                            }
                        } else {
                            bookAdapterGV.notifyDataSetChanged();
                        }


                    } else {
                        conNoData.setVisibility(View.VISIBLE);
                        method.alertBox(bookRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }

                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onFailure(@NotNull Call<BookRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressBar.setVisibility(View.GONE);
                method.alertBox(getResources().getString(R.string.failed_try_again));
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}
