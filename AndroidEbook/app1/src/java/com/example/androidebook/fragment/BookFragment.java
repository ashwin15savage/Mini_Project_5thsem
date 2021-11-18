package com.example.androidebook.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidebook.R;
import com.example.androidebook.activity.BookDetail;
import com.example.androidebook.activity.Login;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private String type, title, id, subId;
    private ProgressBar progressBar;
    private List<BookList> bookLists;
    private RecyclerView recyclerView;
    private BookAdapterLV bookAdapterLV;
    private BookAdapterGV bookAdapterGV;
    private boolean isListView = true;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private MaterialCardView cardView;
    private ConstraintLayout conNoData;
    private MaterialButton buttonLogin;
    private LayoutAnimationController animation;
    private MaterialTextView textViewData, textViewCount;
    private EndlessRecyclerViewScrollListener scrollListener;
    private ImageView imageViewData, imageViewGridView, imageViewListView;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.book_fragment, container, false);

        bookLists = new ArrayList<>();

        assert getArguments() != null;
        type = getArguments().getString("type");
        if (type.equals("home_cat") || type.equals("category") || type.equals("subCategory")) {
            title = getArguments().getString("title");
            id = getArguments().getString("id");
            subId = getArguments().getString("subId");
        }

        assert type != null;
        if (MainActivity.toolbar != null) {
            switch (type) {
                case "latest":
                    MainActivity.toolbar.setTitle(getResources().getString(R.string.latest));
                    break;
                case "favourite":
                    MainActivity.toolbar.setTitle(getResources().getString(R.string.favorite));
                    break;
                case "popular":
                    MainActivity.toolbar.setTitle(getResources().getString(R.string.popular_books));
                    break;
                case "continueBook":
                    MainActivity.toolbar.setTitle(getResources().getString(R.string.continue_book));
                    break;
                case "category":
                case "home_cat":
                    MainActivity.toolbar.setTitle(title);
                    break;
                default:
                    break;
            }
        }

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> startActivity(new Intent(getActivity(), BookDetail.class)
                .putExtra("bookId", id)
                .putExtra("position", position)
                .putExtra("type", type));
        method = new Method(getActivity(), onClick);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        conNoData = view.findViewById(R.id.con_not_login);
        progressBar = view.findViewById(R.id.progressBar_book_fragment);
        imageViewData = view.findViewById(R.id.imageView_not_login);
        buttonLogin = view.findViewById(R.id.button_not_login);
        textViewData = view.findViewById(R.id.textView_not_login);
        textViewCount = view.findViewById(R.id.textView_num_book_fragment);
        cardView = view.findViewById(R.id.cardView_book_fragment);
        imageViewGridView = view.findViewById(R.id.imageView_gridView_book_fragment);
        imageViewListView = view.findViewById(R.id.imageView_listView_book_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_book_fragment);

        data(false, false);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        loadMoreData(layoutManager);

        cardView.setOnClickListener(v -> {

        });

        buttonLogin.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), Login.class));
            getActivity().finishAffinity();
        });

        imageViewListView.setImageDrawable(getResources().getDrawable(R.drawable.ic_list_hov));
        imageViewGridView.setOnClickListener(v -> {
            isListView = false;
            viewChange();
            scrollListener.resetState();
            adsParam = "1";
            paginationIndex = 1;
            isOver = false;
            bookAdapterGV = null;
            GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), 3);
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
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
            recyclerView.setLayoutManager(gridLayoutManager);
            loadMoreData(gridLayoutManager);
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
            RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager1);
            loadMoreData(layoutManager1);
            callData();
        });

        isListView = true;
        callData();

        if (type.equals("subCategory")) {
            Log.d("", "");
        } else {
            setHasOptionsMenu(true);
        }
        return view;
    }

    public void callData() {
        if (method.isNetworkAvailable()) {
            if (type.equals("favourite") || type.equals("continueBook")) {
                if (method.isLogin()) {
                    bookShowType();
                } else {
                    data(true, true);
                }
            } else {
                bookShowType();
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    private void bookShowType() {
        if (isListView) {
            bookLv();
        } else {
            bookGv();
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void data(boolean isShow, boolean isLogin) {
        if (isShow) {
            if (isLogin) {
                buttonLogin.setVisibility(View.VISIBLE);
                textViewData.setText(getResources().getString(R.string.you_have_not_login));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_login));
            } else {
                buttonLogin.setVisibility(View.GONE);
                textViewData.setText(getResources().getString(R.string.no_data_found));
                imageViewData.setImageDrawable(getResources().getDrawable(R.drawable.no_data));
            }
            conNoData.setVisibility(View.VISIBLE);
        } else {
            conNoData.setVisibility(View.GONE);
        }
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
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.ic_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextListener((new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startActivity(new Intent(getActivity(), SearchBook.class)
                        .putExtra("id", "0")
                        .putExtra("search", query)
                        .putExtra("type", "normal"));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return true;
            }
        }));
    }

    public void bookLv() {

        if (getActivity() != null) {

            if (bookAdapterLV == null) {
                bookLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            switch (type) {
                case "latest":
                    jsObj.addProperty("method_name", "get_latest_books");
                    break;
                case "favourite":
                    jsObj.addProperty("user_id", method.userId());
                    jsObj.addProperty("method_name", "get_favourite_book");
                    break;
                case "popular":
                    jsObj.addProperty("method_name", "get_popular_books");
                    break;
                case "continueBook":
                    jsObj.addProperty("user_id", method.userId());
                    jsObj.addProperty("method_name", "get_continue_book");
                    break;
                default:
                    jsObj.addProperty("cat_id", id);
                    jsObj.addProperty("sub_cat_id", subId);
                    jsObj.addProperty("method_name", "get_cat_id");
                    break;
            }
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("is_book", "list_book");
            jsObj.addProperty("page", paginationIndex);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<BookRP> call = apiService.getCatBook(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<BookRP>() {
                @Override
                public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                    if (getActivity() != null) {

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
                                        data(true, false);
                                    } else {
                                        bookAdapterLV = new BookAdapterLV(getActivity(), bookLists, "cat_by_list", onClick);
                                        recyclerView.setAdapter(bookAdapterLV);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    bookAdapterLV.notifyDataSetChanged();
                                }


                            } else if (bookRP.getStatus().equals("2")) {
                                method.suspend(bookRP.getMessage());
                            } else {
                                data(true, false);
                                method.alertBox(bookRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

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

    }

    public void bookGv() {

        if (getActivity() != null) {

            if (bookAdapterGV == null) {
                bookLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            switch (type) {
                case "latest":
                    jsObj.addProperty("method_name", "get_latest_books");
                    break;
                case "favourite":
                    jsObj.addProperty("user_id", method.userId());
                    jsObj.addProperty("method_name", "get_favourite_book");
                    break;
                case "popular":
                    jsObj.addProperty("method_name", "get_popular_books");
                    break;
                case "continueBook":
                    jsObj.addProperty("user_id", method.userId());
                    jsObj.addProperty("method_name", "get_continue_book");
                    break;
                default:
                    jsObj.addProperty("cat_id", id);
                    jsObj.addProperty("sub_cat_id", subId);
                    jsObj.addProperty("method_name", "get_cat_id");
                    break;
            }
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("is_book", "grid_book");
            jsObj.addProperty("page", paginationIndex);
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<BookRP> call = apiService.getLatestBook(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<BookRP>() {
                @Override
                public void onResponse(@NotNull Call<BookRP> call, @NotNull Response<BookRP> response) {

                    if (getActivity() != null) {

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
                                        data(true, false);
                                    } else {
                                        bookAdapterGV = new BookAdapterGV(getActivity(), bookLists, "cat_by_list", onClick);
                                        recyclerView.setAdapter(bookAdapterGV);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    bookAdapterGV.notifyDataSetChanged();
                                }


                            } else if (bookRP.getStatus().equals("2")) {
                                method.suspend(bookRP.getMessage());
                            } else {
                                data(true, false);
                                method.alertBox(bookRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

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

    }

}
