package com.example.androidebook.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidebook.R;
import com.example.androidebook.activity.BookDetail;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.SearchBook;
import com.example.androidebook.adapter.BookHomeAdapter;
import com.example.androidebook.adapter.HomeAuthorAdapter;
import com.example.androidebook.adapter.HomeCatAdapter;
import com.example.androidebook.adapter.SliderAdapter;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.response.HomeRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.EnchantedViewPager;
import com.example.androidebook.util.Method;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.rd.PageIndicatorView;

import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private ProgressBar progressBar;
    private SliderAdapter sliderAdapter;
    private BookHomeAdapter latestAdapter;
    private BookHomeAdapter popularAdapter;
    private BookHomeAdapter continueAdapter;
    private HomeAuthorAdapter authorAdapter;
    private HomeCatAdapter homeCatAdapter;
    private InputMethodManager imm;
    private TextInputEditText editText_search;
    private PageIndicatorView pageIndicatorView;
    private EnchantedViewPager enchantedViewPager;
    private ConstraintLayout conMain, conNoData, conSlider, conContinue, conCategory, conLatest, conPopular, conAuthor;
    private RecyclerView recyclerViewContinue, recyclerViewLatest, recyclerViewPopular, recyclerViewCat, recyclerViewAuthor;

    private Timer timer;
    final long DELAY_MS = 500;//delay in milliseconds before task is to be executed
    final long PERIOD_MS = 3000;
    private Boolean isTimerStart = false;
    private final Handler handler = new Handler();
    private Runnable Update;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.home_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.home));
        }

        imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            if (type.equals("home_cat")) {
                if (otherData.equals("true")) {
                    SubCatBookFragment subCatBookFragment = new SubCatBookFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", id);
                    bundle.putString("title", title);
                    subCatBookFragment.setArguments(bundle);
                    getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, subCatBookFragment, title)
                            .addToBackStack(title).commitAllowingStateLoss();
                } else {
                    bookFragment(type, title, id, subId);
                }
            } else if (type.equals("home_author")) {
                AuthorBookFragment authorBookFragment = new AuthorBookFragment();
                Bundle bundle = new Bundle();
                bundle.putString("title", title);
                bundle.putString("id", id);
                bundle.putString("type", type);
                authorBookFragment.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main, authorBookFragment,
                        title).addToBackStack(title).commitAllowingStateLoss();
            } else {
                startActivity(new Intent(getActivity(), BookDetail.class)
                        .putExtra("bookId", id)
                        .putExtra("position", position)
                        .putExtra("type", type));
            }
        };
        method = new Method(requireActivity(), onClick);

        progressBar = view.findViewById(R.id.progressBar_home);
        conMain = view.findViewById(R.id.con_main_home);
        conNoData = view.findViewById(R.id.con_noDataFound);
        editText_search = view.findViewById(R.id.editText_home);
        enchantedViewPager = view.findViewById(R.id.slider_home);
        pageIndicatorView = view.findViewById(R.id.pageIndicatorView_home);
        conSlider = view.findViewById(R.id.con_slider_home);
        conContinue = view.findViewById(R.id.con_continue_home);
        conCategory = view.findViewById(R.id.con_category_home);
        conLatest = view.findViewById(R.id.con_latest_home);
        conPopular = view.findViewById(R.id.con_popular_home);
        conAuthor = view.findViewById(R.id.con_author_home);
        ImageView imageViewSearch = view.findViewById(R.id.imageView_search_home);
        MaterialTextView textViewContinue = view.findViewById(R.id.textView_continueViewAll_home);
        MaterialTextView textViewLatest = view.findViewById(R.id.textView_latestViewAll_home);
        MaterialTextView textViewPopular = view.findViewById(R.id.textView_popularViewAll_home);
        MaterialTextView textViewCat = view.findViewById(R.id.textView_categoryViewAll_home);
        MaterialTextView textViewAuthor = view.findViewById(R.id.textView_authorViewAll_home);
        recyclerViewContinue = view.findViewById(R.id.recyclerView_continue_home);
        recyclerViewLatest = view.findViewById(R.id.recyclerView_latest_home);
        recyclerViewPopular = view.findViewById(R.id.recyclerView_popular_home);
        recyclerViewCat = view.findViewById(R.id.recyclerView_category_home);
        recyclerViewAuthor = view.findViewById(R.id.recyclerView_author_home);

        conNoData.setVisibility(View.GONE);
        conMain.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        enchantedViewPager.useScale();
        enchantedViewPager.removeAlpha();

        recyclerViewContinue.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager_Continue = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewContinue.setLayoutManager(layoutManager_Continue);
        recyclerViewContinue.setFocusable(false);

        recyclerViewLatest.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerLatest = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewLatest.setLayoutManager(layoutManagerLatest);
        recyclerViewLatest.setFocusable(false);

        recyclerViewPopular.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerPopular = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewPopular.setLayoutManager(layoutManagerPopular);
        recyclerViewPopular.setFocusable(false);

        recyclerViewCat.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerCat = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewCat.setLayoutManager(layoutManagerCat);
        recyclerViewCat.setFocusable(false);

        recyclerViewAuthor.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManagerAuthor = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        recyclerViewAuthor.setLayoutManager(layoutManagerAuthor);
        recyclerViewAuthor.setFocusable(false);

        textViewContinue.setOnClickListener(v -> {
            hideKeyBord();
            bookFragment("continueBook", getResources().getString(R.string.continue_book), "", "");
        });

        textViewLatest.setOnClickListener(v -> {
            hideKeyBord();
            bookFragment("latest", getResources().getString(R.string.latest), "", "");
        });

        textViewCat.setOnClickListener(v -> {
            hideKeyBord();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.frameLayout_main, new CategoryFragment(), getResources().getString(R.string.category))
                    .addToBackStack(getResources().getString(R.string.category)).commitAllowingStateLoss();
        });

        textViewPopular.setOnClickListener(v -> {
            hideKeyBord();
            bookFragment("popular", getResources().getString(R.string.popular_books), "", "");
        });

        textViewAuthor.setOnClickListener(v -> {
            hideKeyBord();
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .add(R.id.frameLayout_main, new AuthorFragment(), getResources().getString(R.string.author))
                    .addToBackStack(getResources().getString(R.string.author)).commitAllowingStateLoss();
        });

        editText_search.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search();
            }
            return false;
        });

        imageViewSearch.setOnClickListener(v -> search());

        if (method.isNetworkAvailable()) {
            if (method.isLogin()) {
                home(method.userId());
            } else {
                home("0");
            }
        } else {
            method.alertBox(getResources().getString(R.string.internet_connection));
            progressBar.setVisibility(View.GONE);
        }

        setHasOptionsMenu(true);

        return view;
    }

    private void bookFragment(String type, String title, String id, String subId) {
        BookFragment bookFragment = new BookFragment();
        Bundle bundle = new Bundle();
        bundle.putString("type", type);
        if (type.equals("home_cat")) {
            bundle.putString("title", title);
            bundle.putString("id", id);
            bundle.putString("subId", subId);
        }
        bookFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.frameLayout_main,
                bookFragment, title).addToBackStack(title).commitAllowingStateLoss();
    }

    private void hideKeyBord() {
        if (requireActivity().getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(requireActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void search() {
        String search = editText_search.getText().toString();
        //do something
        if (!search.isEmpty() || !search.equals("")) {
            editText_search.clearFocus();
            imm.hideSoftInputFromWindow(editText_search.getWindowToken(), 0);
            startActivity(new Intent(getActivity(), SearchBook.class)
                    .putExtra("id", "0")
                    .putExtra("search", search)
                    .putExtra("type", "normal"));
        } else {
            if (getActivity().getCurrentFocus() != null) {
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
            method.alertBox(getResources().getString(R.string.please_enter_book));
        }

    }

    private void home(String userId) {

        if (getActivity() != null) {

            progressBar.setVisibility(View.VISIBLE);

            JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(getActivity()));
            jsObj.addProperty("user_id", userId);
            jsObj.addProperty("method_name", "get_home");
            ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
            Call<HomeRP> call = apiService.getHome(API.toBase64(jsObj.toString()));
            call.enqueue(new Callback<HomeRP>() {
                @Override
                public void onResponse(@NotNull Call<HomeRP> call, @NotNull Response<HomeRP> response) {

                    if (getActivity() != null) {

                        try {

                            HomeRP homeRP = response.body();
                            assert homeRP != null;

                            if (homeRP.getStatus().equals("1")) {

                                if (homeRP.getSliderLists().size() != 0) {

                                    int columnWidth = method.getScreenWidth();
                                    enchantedViewPager.setLayoutParams(new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, (int) (columnWidth / 1.8)));

                                    Update = () -> {
                                        isTimerStart = true;
                                        if (enchantedViewPager.getCurrentItem() == (sliderAdapter.getCount() - 1)) {
                                            enchantedViewPager.setCurrentItem(0, true);
                                        } else {
                                            enchantedViewPager.setCurrentItem(enchantedViewPager.getCurrentItem() + 1, true);
                                        }
                                    };

                                    sliderAdapter = new SliderAdapter(getActivity(), "slider", homeRP.getSliderLists(), onClick);
                                    enchantedViewPager.setAdapter(sliderAdapter);
                                    enchantedViewPager.setCurrentItem(1);

                                    if (sliderAdapter.getCount() > 1) {
                                        timer = new Timer(); // This will create a new Thread
                                        timer.schedule(new TimerTask() { // task to be scheduled
                                            @Override
                                            public void run() {
                                                handler.post(Update);
                                            }
                                        }, DELAY_MS, PERIOD_MS);
                                    } else {
                                        pageIndicatorView.setVisibility(View.GONE);
                                    }

                                } else {
                                    conSlider.setVisibility(View.GONE);
                                }

                                if (homeRP.getContinueLists().size() != 0) {
                                    conContinue.setVisibility(View.VISIBLE);
                                    continueAdapter = new BookHomeAdapter(getActivity(), homeRP.getContinueLists(), "home_continue", onClick);
                                    recyclerViewContinue.setAdapter(continueAdapter);
                                } else {
                                    conContinue.setVisibility(View.GONE);
                                }

                                if (homeRP.getCategoryLists().size() != 0) {
                                    homeCatAdapter = new HomeCatAdapter(getActivity(), homeRP.getCategoryLists(), "home_cat", onClick);
                                    recyclerViewCat.setAdapter(homeCatAdapter);
                                } else {
                                    conCategory.setVisibility(View.GONE);
                                }

                                if (homeRP.getLatestList().size() != 0) {
                                    latestAdapter = new BookHomeAdapter(getActivity(), homeRP.getLatestList(), "home_latest", onClick);
                                    recyclerViewLatest.setAdapter(latestAdapter);
                                } else {
                                    conLatest.setVisibility(View.GONE);
                                }

                                if (homeRP.getPopularList().size() != 0) {
                                    popularAdapter = new BookHomeAdapter(getActivity(), homeRP.getPopularList(), "home_most", onClick);
                                    recyclerViewPopular.setAdapter(popularAdapter);
                                } else {
                                    conPopular.setVisibility(View.GONE);
                                }

                                if (homeRP.getAuthorLists().size() != 0) {
                                    authorAdapter = new HomeAuthorAdapter(getActivity(), homeRP.getAuthorLists(), "home_author", onClick);
                                    recyclerViewAuthor.setAdapter(authorAdapter);
                                } else {
                                    conAuthor.setVisibility(View.GONE);
                                }

                                conMain.setVisibility(View.VISIBLE);

                            } else if (homeRP.getStatus().equals("2")) {
                                method.suspend(homeRP.getMessage());
                            } else {
                                conNoData.setVisibility(View.VISIBLE);
                                method.alertBox(homeRP.getMessage());
                            }

                        } catch (Exception e) {
                            Log.d("exception_error", e.toString());
                            method.alertBox(getResources().getString(R.string.failed_try_again));
                        }

                    }

                    progressBar.setVisibility(View.GONE);

                }

                @Override
                public void onFailure(@NotNull Call<HomeRP> call, @NotNull Throwable t) {
                    // Log error here since request failed
                    Log.e("fail", t.toString());
                    conNoData.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    method.alertBox(getResources().getString(R.string.failed_try_again));
                }
            });

        }
    }

}
