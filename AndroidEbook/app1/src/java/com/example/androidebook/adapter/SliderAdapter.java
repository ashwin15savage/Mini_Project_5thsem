package com.example.androidebook.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.example.androidebook.R;
import com.example.androidebook.interfaces.OnClick;
import com.example.androidebook.item.SliderList;
import com.example.androidebook.util.EnchantedViewPager;
import com.example.androidebook.util.Method;
import com.github.ornolfr.ratingview.RatingView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class SliderAdapter extends PagerAdapter {

    private Method method;
    private Activity activity;
    private String string;
    private List<SliderList> sliderLists;
    private LayoutInflater inflater;

    public SliderAdapter(Activity activity, String string, List<SliderList> sliderLists, OnClick onClick) {
        this.activity = activity;
        this.sliderLists = sliderLists;
        this.string = string;
        method = new Method(activity, onClick);
        // TODO Auto-generated constructor stub
        inflater = activity.getLayoutInflater();
    }


    @SuppressLint("SetTextI18n")
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        View view;
        if (sliderLists.get(position).getBook_type().equals("external")) {

            view = inflater.inflate(R.layout.slider_external_adapter, container, false);
            assert view != null;

            MaterialCardView cardView = view.findViewById(R.id.cardView_sliderExternal_adapter);
            ImageView imageView = view.findViewById(R.id.imageView_sliderExternal_adapter);
            MaterialTextView textView = view.findViewById(R.id.textView_sliderExternal_adapter);

            Glide.with(activity).load(sliderLists.get(position).getBook_bg_img())
                    .placeholder(R.drawable.placeholder_landscape)
                    .into(imageView);

            textView.setText(sliderLists.get(position).getBook_title());

            cardView.setOnClickListener(v -> {
                try {
                    activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sliderLists.get(position).getExternal_link())));
                } catch (Exception e) {
                    method.alertBox(activity.getResources().getString(R.string.wrong));
                }
            });

        } else {

            view = inflater.inflate(R.layout.slider_adapter, container, false);
            assert view != null;

            MaterialCardView cardView = view.findViewById(R.id.cardView_slider_adapter);
            ImageView imageView = view.findViewById(R.id.imageView_slider_adapter);
            RatingView ratingView = view.findViewById(R.id.ratingBar_slider_adapter);
            MaterialTextView textView_name = view.findViewById(R.id.textView_name_slider_adapter);
            MaterialTextView textView_author = view.findViewById(R.id.textView_author_slider_adapter);
            MaterialTextView textView_ratingView = view.findViewById(R.id.textView_ratingView_slider_adapter);

            Glide.with(activity).load(sliderLists.get(position).getBook_bg_img())
                    .placeholder(R.drawable.placeholder_landscape)
                    .into(imageView);

            textView_name.setText(sliderLists.get(position).getBook_title());

            ratingView.setRating(Float.parseFloat(sliderLists.get(position).getRate_avg()));
            textView_author.setText(sliderLists.get(position).getAuthor_name());
            textView_ratingView.setText(sliderLists.get(position).getTotal_rate());

            cardView.setOnClickListener(v -> {
                if (sliderLists.get(position).getBook_type().equals("external")) {
                    try {
                        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(sliderLists.get(position).getExternal_link())));
                    } catch (Exception e) {
                        method.alertBox(activity.getResources().getString(R.string.wrong));
                    }
                } else {
                    method.onClickAd(position, string, sliderLists.get(position).getBook_id(),"", sliderLists.get(position).getBook_title(), "", "","");
                }
            });

        }

        view.setTag(EnchantedViewPager.ENCHANTED_VIEWPAGER_POSITION + position);
        container.addView(view, 0);
        return view;

    }

    @Override
    public int getCount() {
        return sliderLists.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view.equals(object);
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        (container).removeView((View) object);
    }
}

