package com.example.memorai.presentation.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.example.memorai.R;

import java.util.ArrayList;
import java.util.List;

public class StickerBSFragment extends BottomSheetDialogFragment {

    private StickerListener mStickerListener;
    private final List<Integer> stickerList = new ArrayList<>();

    public void setStickerListener(StickerListener stickerListener) {
        this.mStickerListener = stickerListener;
    }

    public interface StickerListener {
        void onStickerClick(Bitmap bitmap);
    }

    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback = new BottomSheetBehavior.BottomSheetCallback() {
        @Override
        public void onStateChanged(@NonNull View bottomSheet, int newState) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss();
            }
        }

        @Override
        public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            // No-op
        }
    };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji, null);
        dialog.setContentView(contentView);

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) ((View) contentView.getParent()).getLayoutParams();
        BottomSheetBehavior<?> behavior = (BottomSheetBehavior<?>) params.getBehavior();
        if (behavior != null) {
            behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback);
        }

        ((View) contentView.getParent()).setBackgroundColor(getResources().getColor(android.R.color.transparent));

        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);
        rvEmoji.setLayoutManager(new GridLayoutManager(getActivity(), 3));

        // Load sticker từ drawable
        loadStickers();

        StickerAdapter stickerAdapter = new StickerAdapter(stickerList);
        rvEmoji.setAdapter(stickerAdapter);
    }

    private void loadStickers() {
        stickerList.clear();
        stickerList.add(R.drawable.sticker_1);
        stickerList.add(R.drawable.sticker_2);
        stickerList.add(R.drawable.sticker_3);
        stickerList.add(R.drawable.sticker_4);
        stickerList.add(R.drawable.sticker_5);
        stickerList.add(R.drawable.sticker_6);
        stickerList.add(R.drawable.sticker_7);
        stickerList.add(R.drawable.sticker_8);
        stickerList.add(R.drawable.sticker_9);
        stickerList.add(R.drawable.sticker_10);
        stickerList.add(R.drawable.sticker_11);
        stickerList.add(R.drawable.sticker_12);
        stickerList.add(R.drawable.sticker_13);
        stickerList.add(R.drawable.sticker_14);
        Log.d("StickerBSFragment", "Stickers loaded: " + stickerList.size());
    }

    private class StickerAdapter extends RecyclerView.Adapter<StickerAdapter.ViewHolder> {
        private final List<Integer> stickers;

        StickerAdapter(List<Integer> stickers) {
            this.stickers = stickers;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_sticker, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.imgSticker.setImageResource(stickers.get(position));
            Log.d("StickerBSFragment", "Sticker bound at position: " + position);
        }

        @Override
        public int getItemCount() {
            return stickers.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {
            ImageView imgSticker;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                imgSticker = itemView.findViewById(R.id.imgSticker);

                itemView.setOnClickListener(v -> {
                    if (mStickerListener != null) {
                        int stickerRes = stickers.get(getLayoutPosition());
                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), stickerRes);
                        mStickerListener.onStickerClick(bitmap);
                    }
                    dismiss();
                });
            }
        }
    }

}
