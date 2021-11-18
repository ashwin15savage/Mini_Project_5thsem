package com.example.androidebook.adapter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.item.CommentList;
import com.example.androidebook.response.DeleteCommentRP;
import com.example.androidebook.response.UserCommentRP;
import com.example.androidebook.rest.ApiClient;
import com.example.androidebook.rest.ApiInterface;
import com.example.androidebook.util.API;
import com.example.androidebook.util.Events;
import com.example.androidebook.util.GlobalBus;
import com.example.androidebook.util.Method;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllCommentAdapter extends RecyclerView.Adapter {

    private Activity activity;
    private Animation myAnim;
    private Method method;
    private List<CommentList> commentLists;

    private final int VIEW_TYPE_LOADING = 0;
    private final int VIEW_TYPE_ITEM = 1;

    public AllCommentAdapter(Activity activity, List<CommentList> commentLists) {
        this.activity = activity;
        method = new Method(activity);
        this.commentLists = commentLists;
        myAnim = AnimationUtils.loadAnimation(activity, R.anim.bounce);
    }

    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(activity).inflate(R.layout.comment_adapter, parent, false);
            return new ViewHolder(view);
        } else if (viewType == VIEW_TYPE_LOADING) {
            View v = LayoutInflater.from(activity).inflate(R.layout.layout_loading_item, parent, false);
            return new ProgressViewHolder(v);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {

        if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

            final ViewHolder viewHolder = (ViewHolder) holder;

            if (!commentLists.get(position).getUser_profile().equals("")) {
                Glide.with(activity).load(commentLists.get(position).getUser_profile())
                        .placeholder(R.drawable.profile).into(viewHolder.circleImageView);
            } else {
                // make sure Glide doesn't load anything into this view until told otherwise
                Glide.with(activity).clear(viewHolder.circleImageView);
            }

            if (method.isLogin()) {
                if (method.userId().equals(commentLists.get(position).getUser_id())) {
                    viewHolder.textViewDelete.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.textViewDelete.setVisibility(View.GONE);
                }
            } else {
                viewHolder.textViewDelete.setVisibility(View.GONE);
            }

            viewHolder.textViewName.setText(commentLists.get(position).getUser_name());
            viewHolder.textViewDate.setText(commentLists.get(position).getComment_date());
            viewHolder.textViewComment.setText(commentLists.get(position).getComment_text());

            viewHolder.textViewDelete.setOnClickListener(v -> {

                viewHolder.textViewDelete.startAnimation(myAnim);

                MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity, R.style.DialogTitleTextStyle);
                builder.setMessage(activity.getResources().getString(R.string.delete_comment));
                builder.setCancelable(false);
                builder.setPositiveButton(activity.getResources().getString(R.string.delete),
                        (arg0, arg1) -> delete(commentLists.get(position).getBook_id(), commentLists.get(position).getComment_id(), position));
                builder.setNegativeButton(activity.getResources().getString(R.string.cancel),
                        (dialog, which) -> dialog.dismiss());

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            });

        }

    }

    @Override
    public int getItemCount() {
        if (commentLists.size() != 0) {
            return commentLists.size() + 1;
        } else {
            return commentLists.size();
        }
    }

    public void hideHeader() {
        ProgressViewHolder.progressBar.setVisibility(View.GONE);
    }

    private boolean isHeader(int position) {
        return position == commentLists.size();
    }

    @Override
    public int getItemViewType(int position) {
        return isHeader(position) ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView circleImageView;
        private MaterialTextView textViewName, textViewDate, textViewComment, textViewDelete;

        public ViewHolder(View itemView) {
            super(itemView);

            circleImageView = itemView.findViewById(R.id.imageView_comment_adapter);
            textViewName = itemView.findViewById(R.id.textView_userName_comment_adapter);
            textViewDate = itemView.findViewById(R.id.textView_date_comment_adapter);
            textViewComment = itemView.findViewById(R.id.textView_comment_adapter);
            textViewDelete = itemView.findViewById(R.id.textView_delete_adapter);

        }
    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public static ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = v.findViewById(R.id.progressBar_loading);
        }
    }

    public void delete(String bookId, String commentId, int position) {

        ProgressDialog progressDialog = new ProgressDialog(activity);

        progressDialog.show();
        progressDialog.setMessage(activity.getResources().getString(R.string.loading));
        progressDialog.setCancelable(false);

        JsonObject jsObj = (JsonObject) new Gson().toJsonTree(new API(activity));
        jsObj.addProperty("book_id", bookId);
        jsObj.addProperty("comment_id", commentId);
        jsObj.addProperty("method_name", "delete_comment");
        ApiInterface apiService = ApiClient.getClient().create(ApiInterface.class);
        Call<DeleteCommentRP> call = apiService.deleteComment(API.toBase64(jsObj.toString()));
        call.enqueue(new Callback<DeleteCommentRP>() {
            @Override
            public void onResponse(@NotNull Call<DeleteCommentRP> call, @NotNull Response<DeleteCommentRP> response) {

                try {
                    DeleteCommentRP deleteCommentRP = response.body();
                    assert deleteCommentRP != null;

                    if (deleteCommentRP.getStatus().equals("1")) {

                        if (deleteCommentRP.getSuccess().equals("1")) {

                            commentLists.remove(position);

                            notifyDataSetChanged();

                            Events.DeleteComment deleteComment = new Events.DeleteComment(deleteCommentRP.getTotal_comment(), bookId, "all_comment", deleteCommentRP.getCommentLists());
                            GlobalBus.getBus().post(deleteComment);

                            Toast.makeText(activity, deleteCommentRP.getMsg(), Toast.LENGTH_SHORT).show();

                        } else {
                            method.alertBox(deleteCommentRP.getMsg());
                        }

                    } else {
                        method.alertBox(deleteCommentRP.getMessage());
                    }

                } catch (Exception e) {
                    Log.d("exception_error", e.toString());
                    method.alertBox(activity.getResources().getString(R.string.failed_try_again));
                }

                progressDialog.dismiss();

            }

            @Override
            public void onFailure(@NotNull Call<DeleteCommentRP> call, @NotNull Throwable t) {
                // Log error here since request failed
                Log.e("fail", t.toString());
                progressDialog.dismiss();
                method.alertBox(activity.getResources().getString(R.string.failed_try_again));
            }
        });

    }

}
