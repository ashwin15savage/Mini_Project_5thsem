package com.example.androidebook.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.database.DatabaseHandler;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.DownloadList;
import com.example.androidebook.util.Method;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<DownloadAdapter.ViewHolder> {

    private Method method;
    private Activity activity;
    private String type;
    private Animation myAnim;
    private DatabaseHandler db;
    private List<DownloadList> downloadLists;

    public DownloadAdapter(Activity activity, List<DownloadList> downloadLists, String type, OnClick onClick) {
        this.activity = activity;
        this.type = type;
        this.downloadLists = downloadLists;
        db = new DatabaseHandler(activity);
        method = new Method(activity, onClick);
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.download_adapter, parent, false);

        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull ViewHolder holder, final int position) {

        if (position % 2 == 1) {
            holder.constraintLayout.setBackgroundColor(activity.getResources().getColor(R.color.bg_one_download_adapter));
        } else {
            holder.constraintLayout.setBackgroundColor(activity.getResources().getColor(R.color.bg_two_download_adapter));
        }

        holder.textViewName.setText(downloadLists.get(position).getTitle());
        holder.textViewAuthor.setText(activity.getString(R.string.by) + " " + downloadLists.get(position).getAuthor());

        Glide.with(activity).load("file://" + downloadLists.get(position).getImage()).placeholder(R.drawable.placeholder_landscape).into(holder.imageView);

        holder.constraintLayout.setOnClickListener(v -> method.onClickAd(position, type, downloadLists.get(position).getId(), "", downloadLists.get(position).getTitle(), "", downloadLists.get(position).getUrl(), ""));

        holder.button.setOnClickListener(v -> {

            holder.button.startAnimation(myAnim);

            MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
            builder.setMessage(activity.getResources().getString(R.string.delete_msg));
            builder.setCancelable(false);
            builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                    (arg0, arg1) -> {

                        if (!db.checkIdDownloadBook(downloadLists.get(position).getId())) {
                            db.deleteDownloadBook(downloadLists.get(position).getId());
                            File file = new File(downloadLists.get(position).getUrl());
                            File file_image = new File(downloadLists.get(position).getImage());
                            file.delete();
                            file_image.delete();
                            downloadLists.remove(position);
                            notifyDataSetChanged();
                        } else {
                            Toast.makeText(activity, activity.getResources().getString(R.string.no_data_found), Toast.LENGTH_SHORT).show();
                        }

                    });
            builder.setNegativeButton(activity.getResources().getString(R.string.cancel),
                    (dialog, which) -> dialog.dismiss());

            AlertDialog alertDialog = builder.create();
            alertDialog.show();


        });

    }

    @Override
    public int getItemCount() {
        return downloadLists.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private MaterialButton button;
        private ConstraintLayout constraintLayout;
        private MaterialTextView textViewName, textViewAuthor;

        public ViewHolder(View itemView) {
            super(itemView);

            constraintLayout = itemView.findViewById(R.id.con_download_adapter);
            button = itemView.findViewById(R.id.button_delete_adapter);
            imageView = itemView.findViewById(R.id.imageView_download_adapter);
            textViewName = itemView.findViewById(R.id.textViewName_download_adapter);
            textViewAuthor = itemView.findViewById(R.id.textView_subTitle_download_adapter);

        }
    }
}
