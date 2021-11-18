package com.example.androidebook.util;

import android.app.Activity;
import android.app.Dialog;
import android.util.Log;
import android.view.View;
import android.view.Window;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.androidebook.R;
import com.example.androidebook.database.DatabaseHandler;
import com.folioreader.FolioReader;
import com.folioreader.model.locators.ReadLocator;
import com.google.android.material.textview.MaterialTextView;

import java.io.File;
import java.io.IOException;

import io.github.lizhangqu.coreprogress.ProgressHelper;
import io.github.lizhangqu.coreprogress.ProgressUIListener;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class DownloadEpub {

    private Activity activity;
    private Method method;
    private DatabaseHandler db;
    private MaterialTextView textViewCancel;
    private Dialog dialog;
    private OkHttpClient client;
    private static final String CANCEL_TAG = "c_tag_epub";

    public DownloadEpub(Activity activity) {
        this.activity = activity;
        method = new Method(activity);
        db = new DatabaseHandler(activity);
    }

    public void pathEpub(String path, String bookId) {

        dialog = new Dialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.progressbar_custom);
        dialog.setCancelable(false);
        if (method.isRtl()) {
            dialog.getWindow().getDecorView().setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
        }
        dialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        textViewCancel = dialog.findViewById(R.id.textView_cancel_progressBar_custom);
        dialog.show();

        try {

            String filePath = activity.getExternalCacheDir().getAbsolutePath();
            String fileName = "file" + bookId + ".epub";
            File fileOpen = new File(filePath, fileName);

            textViewCancel.setOnClickListener(v -> {
                fileOpen.delete();
                dialog.dismiss();
                if (client != null) {
                    for (Call call1 : client.dispatcher().queuedCalls()) {
                        if (call1.request().tag().equals(CANCEL_TAG))
                            call1.cancel();
                    }
                    for (Call call1 : client.dispatcher().runningCalls()) {
                        if (call1.request().tag().equals(CANCEL_TAG))
                            call1.cancel();
                    }
                }
            });

            if (fileOpen.exists()) {
                dialog.dismiss();
                openBook(fileOpen.toString(), bookId);
            } else {
                client = new OkHttpClient();
                Request.Builder builder = new Request.Builder()
                        .url(path)
                        .addHeader("Accept-Encoding", "identity")
                        .get()
                        .tag(CANCEL_TAG);

                Call call = client.newCall(builder.build());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        Log.e("TAG", "=============onFailure===============");
                        e.printStackTrace();
                        Log.d("error_downloading", e.toString());
                        dialog.dismiss();
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        Log.e("TAG", "=============onResponse===============");
                        Log.e("TAG", "request headers:" + response.request().headers());
                        Log.e("TAG", "response headers:" + response.headers());
                        assert response.body() != null;
                        ResponseBody responseBody = ProgressHelper.withProgress(response.body(), new ProgressUIListener() {

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressStart(long totalBytes) {
                                super.onUIProgressStart(totalBytes);
                                Log.e("TAG", "onUIProgressStart:" + totalBytes);
                            }

                            @Override
                            public void onUIProgressChanged(long numBytes, long totalBytes, float percent, float speed) {
                                Log.e("TAG", "=============start===============");
                                Log.e("TAG", "numBytes:" + numBytes);
                                Log.e("TAG", "totalBytes:" + totalBytes);
                                Log.e("TAG", "percent:" + percent);
                                Log.e("TAG", "speed:" + speed);
                                Log.e("TAG", "============= end ===============");
                            }

                            //if you don't need this method, don't override this methd. It isn't an abstract method, just an empty method.
                            @Override
                            public void onUIProgressFinish() {
                                super.onUIProgressFinish();
                                Log.e("TAG", "onUIProgressFinish:");
                                dialog.dismiss();
                                openBook(fileOpen.toString(), bookId);
                            }
                        });

                        try {

                            BufferedSource source = responseBody.source();
                            File outFile = new File(filePath + "/" + fileName);
                            BufferedSink sink = Okio.buffer(Okio.sink(outFile));
                            source.readAll(sink);
                            sink.flush();
                            source.close();

                        } catch (Exception e) {
                            Log.d("show_data", e.toString());
                        }
                    }
                });

            }
        } catch (Exception e) {
            dialog.dismiss();
            Log.d("exception_error", e.toString());
            method.alertBox(activity.getResources().getString(R.string.failed_try_again));
        }

    }

    private void openBook(String path, String id) {

        final FolioReader folioReader = FolioReader.get();
        folioReader.setOnHighlightListener((highlight, type) -> {

        });

        if (!db.checkIdEpub(id)) {
            String string = db.getEpub(id);
            ReadLocator readPosition = ReadLocator.fromJson(string);
            folioReader.setReadLocator(readPosition);
        }
        folioReader.openBook(path);
        folioReader.setReadLocatorListener(readLocator -> {
            if (db.checkIdEpub(id)) {
                db.addEpub(id, readLocator.toJson());
            } else {
                db.updateEpub(id, readLocator.toJson());
            }
        });

    }

}
