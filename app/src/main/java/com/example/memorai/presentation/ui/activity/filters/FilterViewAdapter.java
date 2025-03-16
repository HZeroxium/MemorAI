package com.example.memorai.presentation.ui.activity.filters;

import ja.burhanrashid52.photoeditor.PhotoFilter;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.util.Pair;
import android.widget.TextView;
import android.widget.ImageView;

import com.example.memorai.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder>
{
    private final FilterListener mFilterListener;

    public FilterViewAdapter(FilterListener mFilterListener) {
        this.mFilterListener = mFilterListener;
    }

    private final List<Pair<Integer, PhotoFilter>> mPairList = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // TODO: Inflate layout & return ViewHolder
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_filter, parent, false);
        return new ViewHolder(view, mFilterListener, mPairList);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // TODO: Bind dữ liệu vào ViewHolder
        Pair<Integer, PhotoFilter> filterPair = mPairList.get(position);


        holder.mImageFilterView.setImageResource(filterPair.first);
        holder.mTxtFilterName.setText(filterPair.second.name().replace("_", ""));;

    }

    @Override
    public int getItemCount() {
        return mPairList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        View v;
        ImageView mImageFilterView;
        TextView mTxtFilterName;
        public ViewHolder(View v, final FilterListener mFilterListener, final List<Pair<Integer, PhotoFilter>> mPairList) {
            super(v);
            this.v = v;
            mImageFilterView = v.findViewById(R.id.imgFilterView);
            mTxtFilterName = v.findViewById(R.id.txtFilterName);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mFilterListener.onFilterSelected(mPairList.get(getLayoutPosition()).second);
                }
            });
        }
    }

    private void setupFilters() {
        mPairList.add(new Pair<>(R.drawable.filter_original, PhotoFilter.NONE));
        mPairList.add(new Pair<>(R.drawable.filter_auto_fix, PhotoFilter.AUTO_FIX));
        mPairList.add(new Pair<>(R.drawable.filter_brightness, PhotoFilter.BRIGHTNESS));
        mPairList.add(new Pair<>(R.drawable.filter_contrast, PhotoFilter.CONTRAST));
        mPairList.add(new Pair<>(R.drawable.filter_documentary, PhotoFilter.DOCUMENTARY));
        mPairList.add(new Pair<>(R.drawable.filter_dual_tone, PhotoFilter.DUE_TONE));
        mPairList.add(new Pair<>(R.drawable.filter_fill_light, PhotoFilter.FILL_LIGHT));
        mPairList.add(new Pair<>(R.drawable.filter_fish_eye, PhotoFilter.FISH_EYE));
        mPairList.add(new Pair<>(R.drawable.filter_grain, PhotoFilter.GRAIN));
        mPairList.add(new Pair<>(R.drawable.filter_gray_scale, PhotoFilter.GRAY_SCALE));
        mPairList.add(new Pair<>(R.drawable.filter_lomish, PhotoFilter.LOMISH));
        mPairList.add(new Pair<>(R.drawable.filter_negative, PhotoFilter.NEGATIVE));
        mPairList.add(new Pair<>(R.drawable.filter_posterize, PhotoFilter.POSTERIZE));
        mPairList.add(new Pair<>(R.drawable.filter_saturate, PhotoFilter.SATURATE));
        mPairList.add(new Pair<>(R.drawable.filter_sepia, PhotoFilter.SEPIA));
        mPairList.add(new Pair<>(R.drawable.filter_sharpen, PhotoFilter.SHARPEN));
        mPairList.add(new Pair<>(R.drawable.filter_temprature, PhotoFilter.TEMPERATURE));
        mPairList.add(new Pair<>(R.drawable.filter_tint, PhotoFilter.TINT));
        mPairList.add(new Pair<>(R.drawable.filter_vignette, PhotoFilter.VIGNETTE));
        mPairList.add(new Pair<>(R.drawable.filter_cross_process, PhotoFilter.CROSS_PROCESS));
        mPairList.add(new Pair<>(R.drawable.filter_flip_horizental, PhotoFilter.FLIP_HORIZONTAL));
        mPairList.add(new Pair<>(R.drawable.filter_flip_vertical, PhotoFilter.FLIP_VERTICAL));
        mPairList.add(new Pair<>(R.drawable.filter_rotate, PhotoFilter.ROTATE));
    }




}
