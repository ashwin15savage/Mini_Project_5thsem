package com.example.androidebook.fragment;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.androidebook.R;
import com.example.androidebook.activity.Login;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
import com.example.androidebook.adapter.AuthorAdapter;
import com.example.androidebook.adapter.SpinnerAuthorAdapter;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.AuthorList;
import com.example.androidebook.item.AuthorSpinnerList;
import com.example.androidebook.response.AuthorRP;
import com.example.androidebook.response.AuthorSpinnerRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.EndlessRecyclerViewScrollListener;
import com.example.androidebook.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthorFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private String authorType, authorId;
    private ProgressBar progressBar;
    private List<AuthorList> authorLists;
    private ConstraintLayout conNoData;
    private RecyclerView recyclerView;
    private AuthorAdapter authorAdapter;
    private LayoutAnimationController animation;
    private Boolean isOver = false;
    private String adsParam = "1";
    private int paginationIndex = 1;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.author_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.author));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        authorLists = new ArrayList<>();

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            AuthorBookFragment authorBookFragment = new AuthorBookFragment();
            Bundle bundle = new Bundle();
            bundle.putString("title", title);
            bundle.putString("id", id);
            bundle.putString("type", type);
            authorBookFragment.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                    title).addToBackStack(title).commitAllowingStateLoss();
        };

        method = new Method(getActivity(), onClick);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_author_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_author_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (authorAdapter.getItemViewType(position) == 1) {
                    return 1;
                } else {
                    return 3;
                }
            }
        });
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        recyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                if (!isOver) {
                    new Handler().postDelayed(() -> {
                        paginationIndex++;
                        callData();
                    }, 1000);
                } else {
                    authorAdapter.hideHeader();
                }
            }
        });

        callData();

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            author();
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.search_more, menu);
        MenuItem searchItem = menu.findItem(R.id.ic_search);
        searchItem.setOnMenuItemClickListener(item -> {
            authorName();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void authorName() {

        if (getActivity() != null) {

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "get_author_name");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorSpinnerRP> call = apiService.getAuthorSpinner(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorSpinnerRP>() {
                @Override
                public void onResponse(@NotNull Call<AuthorSpinnerRP> call, @NotNull Response<AuthorSpinnerRP> response) {

                    if (getActivity() != null) {

                        try {

                            AuthorSpinnerRP authorSpinnerRP = response.body();
                            assert authorSpinnerRP != null;
                            if (authorSpinnerRP.getStatus().equals("1")) {
                                showSearch(authorSpinnerRP);
                            } else {
                                method.alertBox(authorSpinnerRP.getMessage());
                            }
                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }
                    progressDialog.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<AuthorSpinnerRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void showSearch(AuthorSpinnerRP authorSpinnerRP) {

        Dialog dialog = new Dialog(requireActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.search_dialog);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(ViewPager.LayoutParams.MATCH_PARENT, ViewPager.LayoutParams.WRAP_CONTENT);

        MaterialTextView textViewTitle = dialog.findViewById(R.id.textView_title_search_dialog);
        ImageView imageViewClose = dialog.findViewById(R.id.imageView_close_search_dialog);
        AppCompatSpinner spinnerAuthor = dialog.findViewById(R.id.spinner_search_dialog);
        TextInputEditText editText = dialog.findViewById(R.id.editText_search_dialog);
        MaterialButton button = dialog.findViewById(R.id.button_search_dialog);

        textViewTitle.setText(getResources().getString(R.string.search_author));
        editText.setHint(getResources().getString(R.string.search_book_name));

        authorSpinnerRP.getAuthorSpinnerLists().add(0, new AuthorSpinnerList("", getResources().getString(R.string.select_author_type)));

        SpinnerAuthorAdapter typeAdapter = new SpinnerAuthorAdapter(getActivity(), authorSpinnerRP.getAuthorSpinnerLists());
        spinnerAuthor.setAdapter(typeAdapter);

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_spinner));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                }
                authorType = authorSpinnerRP.getAuthorSpinnerLists().get(position).getAuthor_name();
                authorId = authorSpinnerRP.getAuthorSpinnerLists().get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        imageViewClose.setOnClickListener(v -> dialog.dismiss());

        button.setOnClickListener(v -> {

            editText.setError(null);

            String search = editText.getText().toString();
            if (authorType.equals(getResources().getString(R.string.select_author_type)) || authorType.equals("") || authorType.isEmpty()) {
                method.alertBox(getResources().getString(R.string.please_select_author));
            } else if (search.isEmpty()) {
                editText.requestFocus();
                editText.setError(getResources().getString(R.string.please_enter_book));
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent intent = new Intent(requireActivity(), SearchBook.class);
                intent.putExtra("id", authorId);
                intent.putExtra("search", search);
                intent.putExtra("type", "author_search");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void author() {

        if (getActivity() != null) {

            if (authorAdapter == null) {
                authorLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_author");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<AuthorRP> call = apiService.getAuthor(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<AuthorRP>() {
                @Override
                public void onResponse(@NotNull Call<AuthorRP> call, @NotNull Response<AuthorRP> response) {

                    if (getActivity() != null) {

                        try {

                            AuthorRP authorRP = response.body();
                            assert authorRP != null;

                            if (authorRP.getStatus().equals("1")) {

                                adsParam = authorRP.getAds_param();

                                if (authorRP.getAuthorLists().size() == 0) {
                                    if (authorAdapter != null) {
                                        authorAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    authorLists.addAll(authorRP.getAuthorLists());
                                }

                                if (authorAdapter == null) {
                                    if (authorLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        authorAdapter = new AuthorAdapter(getActivity(), authorLists, "author", onClick);
                                        recyclerView.setAdapter(authorAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    authorAdapter.notifyDataSetChanged();
                                }

                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(authorRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onFailure(@NotNull Call<AuthorRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

}
