package com.example.androidebook.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.SubCategoryList;
import com.example.androidebook.util.Method;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SubCategoryAdapter extends RecyclerView.Adapter {

    private Method method;
    private Activity activity;
    private String type;
    private List<SubCategoryList> subCategoryLists;
    private int lastSelectedPosition = -1;

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    public SubCategoryAdapter(Activity activity, List<SubCategoryList> subCategoryLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.subCategoryLists = subCategoryLists;
        method = new Method(activity, onClick);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.sub_category_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_horizontal_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            if (lastSelectedPosition == position) {
                viewHolder.cardView.setStrokeWidth(4);
                viewHolder.cardView.setStrokeColor(activity.getResources().getColor(R.color.textView_app_color));
            } else {
                viewHolder.cardView.setStrokeWidth(0);
            }

            viewHolder.textView.setText(subCategoryLists.get(position).getSub_cat_name());
            String itemCount = activity.getResources().getString(R.string.items) + " " + "(" + subCategoryLists.get(position).getTotal_books() + ")";
            viewHolder.textViewItem.setText(itemCount);

            Glide.with(activity).load(subCategoryLists.get(position).getSub_category_image_thumb())
                    .placeholder(R.drawable.placeholder_portable)
                    .into(viewHolder.imageView);

        }

    }

    @Override
    public int getItemCount() {
        return subCategoryLists.size() + 1;
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    private boolean isHeader(int position) {
        return position == subCategoryLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialCardView cardView;
        private MaterialTextView textView, textViewItem;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_subCat_adapter);
            textView = itemView.findViewById(R.id.textView_subCat_adapter);
            textViewItem = itemView.findViewById(R.id.textView_item_subCat_adapter);
            cardView = itemView.findViewById(R.id.cardView_subCat_adapter);

            cardView.setOnClickListener(v -> {
                lastSelectedPosition = getAdapterPosition();
                method.onClickAd(getAdapterPosition(), type, subCategoryLists.get(getAdapterPosition()).getCat_id(), subCategoryLists.get(getAdapterPosition()).getSub_cat_id(), subCategoryLists.get(getAdapterPosition()).getSub_cat_name(), "", "", "");
                notifyDataSetChanged();
            });

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar_horizontal_loading);
        }
    }

}
