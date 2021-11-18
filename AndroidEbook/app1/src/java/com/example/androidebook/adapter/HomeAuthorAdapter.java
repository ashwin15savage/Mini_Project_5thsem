package com.example.androidebook.adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.AuthorList;
import com.example.androidebook.R;
import com.example.androidebook.util.Method;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeAuthorAdapter extends RecyclerView.Adapter<HomeAuthorAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private List<AuthorList> authorLists;

    public HomeAuthorAdapter(Activity activity, List<AuthorList> authorLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.authorLists = authorLists;
        method = new Method(activity, onClick);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.home_author_adapter, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {

        Glide.with(activity).load(authorLists.get(position).getAuthor_image())
                .placeholder(R.drawable.placeholder_portable)
                .into(holder.imageView);

        holder.textView.setText(authorLists.get(position).getAuthor_name());

        holder.constraintLayout.setOnClickListener(v -> method.onClickAd(position, type, authorLists.get(position).getAuthor_id(),"", authorLists.get(position).getAuthor_name(), "", "",""));

    }

    @Override
    public int getItemCount() {
        return authorLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private MaterialTextView textView;
        private ConstraintLayout constraintLayout;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.imageView_homeAuthor_adapter);
            textView = itemView.findViewById(R.id.textView_homeAuthor_adapter);
            constraintLayout = itemView.findViewById(R.id.con_homeAuthor_adapter);

        }
    }
}
