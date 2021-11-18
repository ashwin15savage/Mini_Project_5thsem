package com.example.androidebook.fragment;

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
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
import com.example.androidebook.adapter.CategoryAdapter;
import com.example.androidebook.adapter.SpinnerCatAdapter;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.CatSpinnerList;
import com.example.androidebook.item.CategoryList;
import com.example.androidebook.response.CatRP;
import com.example.androidebook.response.CatSpinnerRP;
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

public class CategoryFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ConstraintLayout conNoData;
    private String catType, catId;
    private ProgressBar progressBar;
    private List<CategoryList> categoryLists;
    private RecyclerView recyclerView;
    private CategoryAdapter categoryAdapter;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    private String adsParam = "1";
    private InputMethodManager imm;
    private LayoutAnimationController animation;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.category_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.category));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        categoryLists = new ArrayList<>();

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            if (otherData.equals("true")) {
                SubCatBookFragment subCatBookFragment = new SubCatBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("id", id);
                bundle.putString("title", title);
                subCatBookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, subCatBookFragment, title)
                        .addToBackStack(title).commitAllowingStateLoss();
            } else {
                BookFragment bookFragment = new BookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("type", type);
                bundle.putString("title", title);
                bundle.putString("id", id);
                bundle.putString("subId", subId);
                bookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, bookFragment, title)
                        .addToBackStack(title).commitAllowingStateLoss();
            }
        };
        method = new Method(getActivity(), onClick);

        conNoData = view.findViewById(R.id.con_noDataFound);
        progressBar = view.findViewById(R.id.progressbar_category_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_category_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), 3);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (categoryAdapter.getItemViewType(position) == 1) {
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
                    categoryAdapter.hideHeader();
                }
            }
        });

        callData();

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            category();
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
            categoryName();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void categoryName() {

        if (getActivity() != null) {

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("method_name", "get_category_name");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<CatSpinnerRP> call = apiService.getCatSpinner(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<CatSpinnerRP>() {
                @Override
                public void onResponse(@NotNull Call<CatSpinnerRP> call, @NotNull Response<CatSpinnerRP> response) {

                    if (getActivity() != null) {

                        try {

                            CatSpinnerRP catSpinnerRP = response.body();
                            assert catSpinnerRP != null;
                            if (catSpinnerRP.getStatus().equals("1")) {
                                showSearch(catSpinnerRP);
                            } else {
                                method.alertBox(catSpinnerRP.getMessage());
                            }
                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressDialog.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<CatSpinnerRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void showSearch(CatSpinnerRP catSpinnerRP) {

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

        textViewTitle.setText(getResources().getString(R.string.search_cat));
        editText.setHint(getResources().getString(R.string.search_book_name));

        catSpinnerRP.getCatSpinnerLists().add(0, new CatSpinnerList("", getResources().getString(R.string.select_category_type)));

        SpinnerCatAdapter typeAdapter = new SpinnerCatAdapter(getActivity(), catSpinnerRP.getCatSpinnerLists());
        spinnerAuthor.setAdapter(typeAdapter);

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_spinner));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                }
                catType = catSpinnerRP.getCatSpinnerLists().get(position).getCategory_name();
                catId = catSpinnerRP.getCatSpinnerLists().get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });
        imageViewClose.setOnClickListener(v -> dialog.dismiss());

        button.setOnClickListener(v -> {

            editText.setError(null);

            String search = editText.getText().toString();
            if (catType.equals(getResources().getString(R.string.select_category_type)) || catType.equals("") || catType.isEmpty()) {
                method.alertBox(getResources().getString(R.string.please_select_category));
            } else if (search.isEmpty() || search.equals("")) {
                editText.requestFocus();
                editText.setError(getResources().getString(R.string.please_enter_book));
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent intent = new Intent(requireActivity(), SearchBook.class);
                intent.putExtra("id", catId);
                intent.putExtra("search", search);
                intent.putExtra("type", "category_search");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void category() {

        if (getActivity() != null) {

            if (categoryAdapter == null) {
                categoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("ads_param", adsParam);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_category");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<CatRP> call = apiService.getCategory(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<CatRP>() {
                @Override
                public void onResponse(@NotNull Call<CatRP> call, @NotNull Response<CatRP> response) {

                    if (getActivity() != null) {

                        try {

                            CatRP catRP = response.body();
                            assert catRP != null;

                            if (catRP.getStatus().equals("1")) {

                                adsParam = catRP.getAds_param();

                                if (catRP.getCategoryLists().size() == 0) {
                                    if (categoryAdapter != null) {
                                        categoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    categoryLists.addAll(catRP.getCategoryLists());
                                }

                                if (categoryAdapter == null) {
                                    if (categoryLists.size() == 0) {
                                        conNoData.setVisibility(View.VISIBLE);
                                    } else {
                                        categoryAdapter = new CategoryAdapter(getActivity(), categoryLists, "category", onClick);
                                        recyclerView.setAdapter(categoryAdapter);
                                        recyclerView.setLayoutAnimation(animation);
                                    }
                                } else {
                                    categoryAdapter.notifyDataSetChanged();
                                }

                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(catRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<CatRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

}
