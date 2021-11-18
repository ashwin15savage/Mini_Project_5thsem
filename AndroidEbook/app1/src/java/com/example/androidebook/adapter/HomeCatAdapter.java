package com.example.androidebook.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.CategoryList;
import com.example.androidebook.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HomeCatAdapter extends RecyclerView.Adapter<HomeCatAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<CategoryList> categoryLists;

    public HomeCatAdapter(Activity activity, List<CategoryList> categoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.categoryLists = categoryLists;
        method = new Method(activity, onClick);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_category_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        holder.textView.setText(categoryLists.get(position).getCategory_name());
        String itemCount = activity.getResources().getString(R.string.items) + " " + "(" + categoryLists.get(position).getTotal_books() + ")";
        holder.textViewItem.setText(itemCount);

        Glide.with(activity).load(categoryLists.get(position).getCat_image_thumb())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

        holder.cardView.setOnClickListener(v -> method.onClickAd(position, type, categoryLists.get(position).getCid(), "0", categoryLists.get(position).getCategory_name(), "", "", categoryLists.get(position).getSub_cat_status()));

    }

    @Override
    public int getItemCount() {
        return categoryLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialCardView cardView;
        private MaterialTextView textView, textViewItem;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_homeCategory_adapter);
            textView = itemView.findViewById(R.id.textView_homeCategory_adapter);
            textViewItem = itemView.findViewById(R.id.textView_item_homeCategory_adapter);
            cardView = itemView.findViewById(R.id.cardView_homeCategory_adapter);

        }
    }
}
