package com.example.androidebook.response;

import com.example.androidebook.item.AuthorList;
import com.example.androidebook.item.BookList;
import com.example.androidebook.item.CategoryList;
import com.example.androidebook.item.SliderList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class HomeRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("slider_books")
    private List<SliderList> sliderLists;

    @SerializedName("continue_books")
    private List<BookList> continueLists;

    @SerializedName("latest_books")
    private List<BookList> latestList;

    @SerializedName("popular_books")
    private List<BookList> popularList;

    @SerializedName("category_list")
    private List<CategoryList> categoryLists;

    @SerializedName("author_list")
    private List<AuthorList> authorLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<SliderList> getSliderLists() {
        return sliderLists;
    }

    public List<BookList> getContinueLists() {
        return continueLists;
    }

    public List<BookList> getLatestList() {
        return latestList;
    }

    public List<BookList> getPopularList() {
        return popularList;
    }

    public List<CategoryList> getCategoryLists() {
        return categoryLists;
    }

    public List<AuthorList> getAuthorLists() {
        return authorLists;
    }
}
