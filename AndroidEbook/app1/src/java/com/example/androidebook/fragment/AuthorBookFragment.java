package com.example.androidebook.fragment;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.activity.BookDetail;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
import com.example.androidebook.adapter.BookAdapterLV;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.BookList;
import com.example.androidebook.response.AuthorDetailRP;
import com.example.androidebook.response.BookRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.EndlessRecyclerViewScrollListener;
import com.example.androidebook.util.Method;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import me.chensir.expandabletextview.ExpandableTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorBookFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private Animation myAnim;
    private String title, id, type;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private List<BookList> bookLists;
    private BookAdapterLV bookAdapterLV;
    private ExpandableTextView textViewDes;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private ConstraintLayout conNoData;
    private LayoutAnimationController animation;
    private MaterialTextView textViewAuthorName, textViewCity;
    private ImageView imageViewAuthor, imageViewYoutube, imageViewFacebook, imageViewInstagram, imageViewWeb;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.author_book_fragment, container, false);

        assert getArguments() != null;
        title = getArguments().getString("title");
        id = getArguments().getString("id");
        type = getArguments().getString("type");

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(title);
        }

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) ->
                startActivity(new Intent(getActivity(), BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
        method = new Method(getActivity(), onClick);

        myAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.bounce);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        bookLists = new ArrayList<>();

        progressBar = view.findViewById(R.id.progressbar_authorBook_fragment);
        conNoData = view.findViewById(R.id.con_noDataFound);
        recyclerView = view.findViewById(R.id.recyclerView_authorBook_fragment);
        imageViewAuthor = view.findViewById(R.id.imageView_authorBook_fragment);
        imageViewYoutube = view.findViewById(R.id.imageView_youtube_authorBook_fragment);
        imageViewFacebook = view.findViewById(R.id.imageView_facebook_authorBook_fragment);
        imageViewInstagram = view.findViewById(R.id.imageView_instagram_authorBook_fragment);
        imageViewWeb = view.findViewById(R.id.imageView_web_authorBook_fragment);
        textViewAuthorName = view.findViewById(R.id.textView_name_authorBook_fragment);
        textViewCity = view.findViewById(R.id.textView_subName_authorBook_fragment);
        textViewDes = view.findViewById(R.id.textViewDes_authorBook_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        authorBook(id);
                    }, 1000);
                } else {
                    bookAdapterLV.hideHeader();
                }
            }
        });

        if (method.isNetworkAvailable()) {
            authorDetail(id);
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }

        setHasOptionsMenu(true);
        return view;

    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
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

        super.onCreateOptionsMenu(menu, inflater);

    }

    private void authorDetail(String id) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("author_id", id);
            jsObj.addProperty("method_name", "get_author_details");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorDetailRP> call = apiService.getAuthorDetail(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorDetailRP>() {
                @Override
                public void onResponse(@NotNull Call<AuthorDetailRP> call, @NotNull Response<AuthorDetailRP> response) {

                    if (getActivity() != null) {
                        try {
                            AuthorDetailRP authorDetailRP = response.body();
                            assert authorDetailRP != null;

                            if (authorDetailRP.getStatus().equals("1")) {

                                Glide.with(getActivity().getApplicationContext()).load(authorDetailRP.getAuthor_image())
                                        .placeholder(R.drawable.placeholder_portable)
                                        .into(imageViewAuthor);

                                textViewAuthorName.setText(authorDetailRP.getAuthor_name());
                                textViewCity.setText(authorDetailRP.getAuthor_city_name());
                                textViewDes.setText(Html.fromHtml(authorDetailRP.getAuthor_description()));

                                imageViewYoutube.setOnClickListener(v -> {
                                    imageViewYoutube.startAnimation(myAnim);
                                    String string = authorDetailRP.getAuthor_youtube();
                                    if (string.equals("")) {
                                        method.alertBox(getResources().getString(R.string.user_not_youtube_link));
                                    } else {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(string));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            method.alertBox(getResources().getString(R.string.wrong));
                                        }
                                    }
                                });

                                imageViewFacebook.setOnClickListener(v -> {
                                    imageViewFacebook.startAnimation(myAnim);
                                    String string = authorDetailRP.getAuthor_facebook();
                                    if (string.equals("")) {
                                        method.alertBox(getResources().getString(R.string.user_not_facebook_link));
                                    } else {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(string));
                                            startActivity(intent);
                                        } catch (Exception e) {
                                            method.alertBox(getResources().getString(R.string.wrong));
                                        }
                                    }
                                });

                                imageViewInstagram.setOnClickListener(v -> {
                                    imageViewInstagram.startAnimation(myAnim);
                                    String string = authorDetailRP.getAuthor_instagram();
                                    if (string.equals("")) {
                                        method.alertBox(getResources().getString(R.string.user_not_instagram_link));
                                    } else {
                                        Uri uri = Uri.parse(string);
                                        Intent likeIng = new Intent(Intent.ACTION_VIEW, uri);
                                        likeIng.setPackage("com.instagram.android");
                                        try {
                                            startActivity(likeIng);
                                        } catch (ActivityNotFoundException e) {
                                            try {
                                                startActivity(new Intent(Intent.ACTION_VIEW,
                                                        Uri.parse(string)));
                                            } catch (Exception e1) {
                                                method.alertBox(getResources().getString(R.string.wrong));
                                            }
                                        }
                                    }
                                });

                                imageViewWeb.setOnClickListener(v -> {
                                    imageViewWeb.startAnimation(myAnim);
                                    String string = authorDetailRP.getAuthor_website();
                                    if (string.equals("")) {
                                        method.alertBox(getResources().getString(R.string.user_not_web_link));
                                    } else {
                                        try {
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse(string));
                                            startActivity(intent);
                                        } catch (Exception e1) {
                                            method.alertBox(getResources().getString(R.string.wrong));
                                        }
                                    }
                                });

                                authorBook(id);

                            } else {
                                method.alertBox(authorDetailRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<AuthorDetailRP> call, @NotNull Throwable
                        t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    public void authorBook(String id) {

        if (getActivity() != null) {

            if (bookAdapterLV == null) {
                bookLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("author_id", id);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("method_name", "get_author_id");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<BookRP> call = apiService.getAuthorBook(API.toBase64(jsObj.toString()));
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

                                if (bookAdapterLV == null) {
                                    if (bookLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        bookAdapterLV = new BookAdapterLV(getActivity(), bookLists, "author_by_list", onClick);
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
