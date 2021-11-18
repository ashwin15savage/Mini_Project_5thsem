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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import com.example.androidebook.R;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
import com.example.androidebook.adapter.SpinnerSubCatAdapter;
import com.example.androidebook.adapter.SubCategoryAdapter;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.SubCatSpinnerList;
import com.example.androidebook.item.SubCategoryList;
import com.example.androidebook.response.SubCatRP;
import com.example.androidebook.response.SubCatSpinnerRP;
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

public class SubCatBookFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private String id, title;
    private String subCatType, subCatId;
    private ProgressBar progressBar;
    private ConstraintLayout conSubCat;
    private RecyclerView recyclerView;
    private List<SubCategoryList> subCategoryLists;
    private SubCategoryAdapter subCategoryAdapter;
    private Boolean isOver = false;
    private int paginationIndex = 1;
    private InputMethodManager imm;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.subcat_book_fragment, container, false);

        assert getArguments() != null;
        id = getArguments().getString("id");
        title = getArguments().getString("title");

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(title);
        }

        subCategoryLists = new ArrayList<>();

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            BookFragment bookFragment = new BookFragment();
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putString("title", title);
            bundle.putString("id", id);
            bundle.putString("subId", subId);
            bookFragment.setArguments(bundle);
            getChildFragmentManager().beginTransaction().replace(R.id.frameLayout_subCatBook_fragment,
                    bookFragment, title).commitAllowingStateLoss();
        };

        method = new Method(getActivity(), onClick);

        progressBar = view.findViewById(R.id.progressbar_subCatBook_fragment);
        conSubCat = view.findViewById(R.id.con_subCatBook_fragment);
        recyclerView = view.findViewById(R.id.recyclerView_subCatBook_fragment);

        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), RecyclerView.HORIZONTAL, false);
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
                    subCategoryAdapter.hideHeader();
                }
            }
        });

        callData();

        BookFragment bookFragment = new BookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", "subCategory");
        bundle.putString("title", title);
        bundle.putString("id", id);
        bundle.putString("subId", "0");
        bookFragment.setArguments(bundle);
        getChildFragmentManager().beginTransaction().replace(R.id.frameLayout_subCatBook_fragment,
                bookFragment, title).commitAllowingStateLoss();

        setHasOptionsMenu(true);
        return view;
    }

    private void callData() {
        if (method.isNetworkAvailable()) {
            subCategory();
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
            subCategoryName();
            return false;
        });
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void subCategoryName() {

        if (getActivity() != null) {

            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.show();
            progressDialog.setMessage(getResources().getString(R.string.loading));
            progressDialog.setCancelable(false);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("cat_id", id);
            jsObj.addProperty("method_name", "get_sub_cat_name");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<SubCatSpinnerRP> call = apiService.getSubCatSpinner(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<SubCatSpinnerRP>() {
                @Override
                public void onResponse(@NotNull Call<SubCatSpinnerRP> call, @NotNull Response<SubCatSpinnerRP> response) {

                    if (getActivity() != null) {

                        try {

                            SubCatSpinnerRP subCatSpinnerRP = response.body();
                            assert subCatSpinnerRP != null;
                            if (subCatSpinnerRP.getStatus().equals("1")) {
                                showSearch(subCatSpinnerRP);
                            } else {
                                method.alertBox(subCatSpinnerRP.getMessage());
                            }
                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressDialog.dismiss();

                }

                @Override
                public void onFailure(@NotNull Call<SubCatSpinnerRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressDialog.dismiss();
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }

    }

    private void showSearch(SubCatSpinnerRP subCatSpinnerRP) {

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

        textViewTitle.setText(getResources().getString(R.string.search_sub_cat));
        editText.setHint(getResources().getString(R.string.search_book_name));

        subCatSpinnerRP.getSubCatSpinnerLists().add(0, new SubCatSpinnerList("", getResources().getString(R.string.select_sub_category_type)));

        SpinnerSubCatAdapter typeAdapter = new SpinnerSubCatAdapter(getActivity(), subCatSpinnerRP.getSubCatSpinnerLists());
        spinnerAuthor.setAdapter(typeAdapter);

        spinnerAuthor.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_spinner));
                } else {
                    ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.textView_app_color));
                }
                subCatType = subCatSpinnerRP.getSubCatSpinnerLists().get(position).getSub_cat_name();
                subCatId = subCatSpinnerRP.getSubCatSpinnerLists().get(position).getSid();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        imageViewClose.setOnClickListener(v -> dialog.dismiss());

        button.setOnClickListener(v -> {

            editText.setError(null);

            String search = editText.getText().toString();
            if (subCatType.equals(getResources().getString(R.string.select_sub_category_type)) || subCatType.equals("") || subCatType.isEmpty()) {
                method.alertBox(getResources().getString(R.string.please_select_sub_category));
            } else if (search.isEmpty() || search.equals("")) {
                editText.requestFocus();
                editText.setError(getResources().getString(R.string.please_enter_book));
            } else {

                editText.clearFocus();
                imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);

                Intent intent = new Intent(requireActivity(), SearchBook.class);
                intent.putExtra("id", subCatId);
                intent.putExtra("search", search);
                intent.putExtra("type", "sub_category_search");
                startActivity(intent);
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private void subCategory() {

        if (getActivity() != null) {

            if (subCategoryAdapter == null) {
                subCategoryLists.clear();
                progressBar.setVisibility(View.VISIBLE);
            }

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("cat_id", id);
            jsObj.addProperty("page", paginationIndex);
            jsObj.addProperty("method_name", "get_sub_category");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<SubCatRP> call = apiService.getSubCategory(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<SubCatRP>() {
                @Override
                public void onResponse(@NotNull Call<SubCatRP> call, @NotNull Response<SubCatRP> response) {

                    if (getActivity() != null) {

                        try {

                            SubCatRP subCatRP = response.body();
                            assert subCatRP != null;

                            if (subCatRP.getStatus().equals("1")) {

                                if (subCatRP.getSubCategoryLists().size() == 0) {
                                    if (subCategoryAdapter != null) {
                                        subCategoryAdapter.hideHeader();
                                        isOver = true;
                                    }
                                } else {
                                    subCategoryLists.addAll(subCatRP.getSubCategoryLists());
                                }

                                if (subCategoryAdapter == null) {
                                    if (subCategoryLists.size() == 0) {
                                        conSubCat.setVisibility(View.GONE);
                                    } else {
                                        conSubCat.setVisibility(View.VISIBLE);
                                        subCategoryAdapter = new SubCategoryAdapter(getActivity(), subCategoryLists, "subCategory", onClick);
                                        recyclerView.setAdapter(subCategoryAdapter);
                                    }
                                } else {
                                    subCategoryAdapter.notifyDataSetChanged();
                                }

                            } else {
                                method.alertBox(subCatRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<SubCatRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

}
