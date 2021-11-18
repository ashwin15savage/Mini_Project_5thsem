package com.example.androidebook.response;

import com.example.androidebook.item.BookList;
import com.example.androidebook.item.CommentList;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class BookDetailRP implements Serializable {

    @SerializedName("status")
    private String status;

    @SerializedName("message")
    private String message;

    @SerializedName("id")
    private String id;

    @SerializedName("cat_id")
    private String cat_id;

    @SerializedName("sub_cat_id")
    private String sub_cat_id;

    @SerializedName("aid")
    private String aid;

    @SerializedName("book_title")
    private String book_title;

    @SerializedName("book_description")
    private String book_description;

    @SerializedName("book_cover_img")
    private String book_cover_img;

    @SerializedName("book_bg_img")
    private String book_bg_img;

    @SerializedName("book_file_type")
    private String book_file_type;

    @SerializedName("book_file_url")
    private String book_file_url;

    @SerializedName("is_fav")
    private String is_fav;

    @SerializedName("total_rate")
    private String total_rate;

    @SerializedName("rate_avg")
    private String rate_avg;

    @SerializedName("book_views")
    private String book_views;

    @SerializedName("total_comment")
    private String total_comment;

    @SerializedName("author_name")
    private String author_name;

    @SerializedName("share_link")
    private String share_link;

    @SerializedName("related_books")
    private List<BookList> bookLists;

    @SerializedName("user_comments")
    private List<CommentList> commentLists;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }

    public String getCat_id() {
        return cat_id;
    }

    public String getAid() {
        return aid;
    }

    public String getBook_title() {
        return book_title;
    }

    public String getBook_description() {
        return book_description;
    }

    public String getBook_cover_img() {
        return book_cover_img;
    }

    public String getBook_bg_img() {
        return book_bg_img;
    }

    public String getBook_file_type() {
        return book_file_type;
    }

    public String getBook_file_url() {
        return book_file_url;
    }

    public String getIs_fav() {
        return is_fav;
    }

    public String getTotal_rate() {
        return total_rate;
    }

    public void setTotal_rate(String total_rate) {
        this.total_rate = total_rate;
    }

    public String getRate_avg() {
        return rate_avg;
    }

    public void setRate_avg(String rate_avg) {
        this.rate_avg = rate_avg;
    }

    public String getBook_views() {
        return book_views;
    }

    public void setBook_views(String book_views) {
        this.book_views = book_views;
    }

    public String getTotal_comment() {
        return total_comment;
    }

    public String getAuthor_name() {
        return author_name;
    }

    public String getShare_link() {
        return share_link;
    }

    public List<BookList> getBookLists() {
        return bookLists;
    }

    public List<CommentList> getCommentLists() {
        return commentLists;
    }
}
