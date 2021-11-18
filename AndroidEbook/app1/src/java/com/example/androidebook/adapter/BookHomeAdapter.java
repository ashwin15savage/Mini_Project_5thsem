package com.example.androidebook.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.BookList;
import com.example.androidebook.R;
import com.example.androidebook.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BookHomeAdapter extends RecyclerView.Adapter<BookHomeAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<BookList> subCategoryLists;

    public BookHomeAdapter(Activity activity, List<BookList> subCategoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.subCategoryLists = subCategoryLists;
        this.type = type;
        method = new Method(activity, onClick);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.book_home_adapter, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint({"SetTextI18n", "UseCompatLoadingForDrawables"})
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {

        holder.textViewName.setText(subCategoryLists.get(position).getBook_title());
        holder.textViewAuthor.setText(activity.getString(R.string.by) + "\u0020" + subCategoryLists.get(position).getAuthor_name());
        holder.textViewRatingCount.setText(subCategoryLists.get(position).getTotal_rate());
        holder.ratingBar.setRating(Float.parseFloat(subCategoryLists.get(position).getRate_avg()));

        if (!subCategoryLists.get(position).getBook_cover_img().equals("")) {
            Glide.with(activity).load(subCategoryLists.get(position).getBook_cover_img())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(holder.imageView);
        }

        holder.cardView.setOnClickListener(v -> method.onClickAd(position, type, subCategoryLists.get(position).getId(),"", "", "", "",""));

    }

    @Override
    public int getItemCount() {
        return subCategoryLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private RatingView ratingBar;
        private MaterialCardView cardView;
        private MaterialTextView textViewName, textViewAuthor, textViewRatingCount;


        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_book_home_adapter);
            cardView = itemView.findViewById(R.id.cardView_book_home_adapter);
            textViewName = itemView.findViewById(R.id.textView_title_book_home_adapter);
            textViewAuthor = itemView.findViewById(R.id.textView_author_book_home_adapter);
            ratingBar = itemView.findViewById(R.id.ratingBar_book_home_adapter);
            textViewRatingCount = itemView.findViewById(R.id.textView_ratingCount_book_home_adapter);

        }
    }

}
