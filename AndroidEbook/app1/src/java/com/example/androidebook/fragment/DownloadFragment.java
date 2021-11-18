package com.example.androidebook.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.androidebook.R;
import com.example.androidebook.activity.MainActivity;
import com.example.androidebook.activity.PDFShow;
import com.example.androidebook.adapter.DownloadAdapter;
import com.example.androidebook.database.DatabaseHandler;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.DownloadList;
import com.example.androidebook.util.Method;
import com.folioreader.FolioReader;
import com.folioreader.model.locators.ReadLocator;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DownloadFragment extends Fragment {

    private Method method;
    private OnClick onClick;
    private DatabaseHandler db;
    private ConstraintLayout conNoData;
    private ProgressBar progressBar;
    private List<File> inFiles;
    private List<DownloadList> databaseLists;
    private List<DownloadList> downloadLists;
    private RecyclerView recyclerView;
    private DownloadAdapter downloadAdapter;
    private LayoutAnimationController animation;

    @Nullable
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.download_fragment, container, false);

        if (MainActivity.toolbar != null) {
            MainActivity.toolbar.setTitle(getResources().getString(R.string.download));
        }

        int resId = R.anim.layout_animation_fall_down;
        animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);

        db = new DatabaseHandler(getActivity());
        databaseLists = new ArrayList<>();

        onClick = (position, type, id, subId, title, fileType, fileUrl, otherData) -> {
            if (fileUrl.contains(".epub")) {
                FolioReader folioReader = FolioReader.get();
                folioReader.setOnHighlightListener((highlight, type1) -> {

                });
                if (!db.checkIdEpub(id)) {

                    String string = db.getEpub(id);
                    ReadLocator readPosition = ReadLocator.fromJson(string);
                    folioReader.setReadLocator(readPosition);

                }
                folioReader.openBook(fileUrl);
                folioReader.setReadLocatorListener(readLocator -> {
                    if (db.checkIdEpub(id)) {
                        db.addEpub(id, readLocator.toJson());
                    } else {
                        db.updateEpub(id, readLocator.toJson());
                    }
                });
            } else {

                String[] strings = fileUrl.split("filename-");
                String[] idPdf = strings[1].split(".pdf");

                startActivity(new Intent(getActivity(), PDFShow.class)
                        .putExtra("id", idPdf[0])
                        .putExtra("link", fileUrl)
                        .putExtra("title", title)
                        .putExtra("type", "file"));
            }
        };
        method = new Method(getActivity(), onClick);

        inFiles = new ArrayList<>();
        downloadLists = new ArrayList<>();

        progressBar = view.findViewById(R.id.progressbar_download_fragment);
        conNoData = view.findViewById(R.id.con_noDataFound);
        recyclerView = view.findViewById(R.id.recyclerView_download_fragment);

        conNoData.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setFocusable(false);

        new Execute().execute();

        return view;
    }

    @SuppressLint("StaticFieldLeak")
    class Execute extends AsyncTask<String, String, String> {

        File file;

        @Override
        protected void onPreExecute() {

            progressBar.setVisibility(View.VISIBLE);
            databaseLists.clear();
            inFiles.clear();
            downloadLists.clear();
            db = new DatabaseHandler(getContext());
            file = new File(method.bookStorage());

            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {

            try {
                databaseLists.addAll(db.getDownload());
                Queue<File> files = new LinkedList<>(Arrays.asList(file.listFiles()));
                while (!files.isEmpty()) {
                    File file = files.remove();
                    if (file.isDirectory()) {
                        files.addAll(Arrays.asList(file.listFiles()));
                    } else if (file.getName().endsWith(".epub") || file.getName().endsWith(".pdf")) {
                        inFiles.add(file);
                    }
                }
                for (int i = 0; i < databaseLists.size(); i++) {
                    for (int j = 0; j < inFiles.size(); j++) {
                        if (inFiles.get(j).toString().contains(databaseLists.get(i).getUrl())) {
                            downloadLists.add(databaseLists.get(i));
                            break;
                        } else {
                            if (j == inFiles.size() - 1) {
                                db.deleteDownloadBook(databaseLists.get(i).getId());
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("error", e.toString());
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {

            if (downloadLists.size() == 0) {
                conNoData.setVisibility(View.VISIBLE);
            } else {
                downloadAdapter = new DownloadAdapter(getActivity(), downloadLists, "download", onClick);
                recyclerView.setAdapter(downloadAdapter);
                recyclerView.setLayoutAnimation(animation);
            }

            progressBar.setVisibility(View.GONE);
            super.onPostExecute(s);
        }
    }

}
