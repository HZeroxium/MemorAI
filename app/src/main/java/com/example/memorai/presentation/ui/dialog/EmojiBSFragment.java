package com.example.memorai.presentation.ui.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;

import java.util.ArrayList;
import java.util.List;

public class EmojiBSFragment extends BottomSheetDialogFragment {
    private EmojiListener mEmojiListener;

    public interface EmojiListener {
        void onEmojiClick(String emojiUnicode);
    }

    private final BottomSheetBehavior.BottomSheetCallback mBottomSheetBehaviorCallback =
            new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        dismiss();
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {}
            };

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NonNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);
        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji, null);
        dialog.setContentView(contentView);

        View parent = (View) contentView.getParent();
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parent.getLayoutParams();
        BottomSheetBehavior<?> behavior = (BottomSheetBehavior<?>) params.getBehavior();
        if (behavior != null) {
            behavior.setBottomSheetCallback(mBottomSheetBehaviorCallback);
        }
        parent.setBackgroundColor(getResources().getColor(android.R.color.transparent));

        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);
        rvEmoji.setLayoutManager(new GridLayoutManager(getActivity(), 5));
        EmojiAdapter emojiAdapter = new EmojiAdapter(getEmojis(getContext()));
        rvEmoji.setAdapter(emojiAdapter);
        rvEmoji.setHasFixedSize(true);
    }

    public void setEmojiListener(EmojiListener emojiListener) {
        mEmojiListener = emojiListener;
    }

    private class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {
        private final List<String> emojiList;

        EmojiAdapter(List<String> emojiList) {
            this.emojiList = emojiList;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_emoji, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.txtEmoji.setText(emojiList.get(position));
        }

        @Override
        public int getItemCount() {
            return emojiList.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView txtEmoji;

            ViewHolder(View itemView) {
                super(itemView);
                txtEmoji = itemView.findViewById(R.id.txtEmoji);
                itemView.setOnClickListener(v -> {
                    if (mEmojiListener != null) {
                        mEmojiListener.onEmojiClick(emojiList.get(getLayoutPosition()));
                    }
                    dismiss();
                });
            }
        }
    }

    public static List<String> getEmojis(Context context) {
        List<String> convertedEmojiList = new ArrayList<>();
        String[] emojiArray = context.getResources().getStringArray(R.array.photo_editor_emoji);
        for (String emojiUnicode : emojiArray) {
            convertedEmojiList.add(convertEmoji(emojiUnicode));
        }
        return convertedEmojiList;
    }

    private static String convertEmoji(String emoji) {
        try {
            int convertEmojiToInt = Integer.parseInt(emoji.substring(2), 16);
            return new String(Character.toChars(convertEmojiToInt));
        } catch (NumberFormatException e) {
            return "";
        }
    }
}
