package com.example.memorai.presentation.ui.dialog;

import android.view.View;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.bottomsheet.BottomSheetBehavior;

public class EmojiBSFragment extends BottomSheetDialogFragment {
    private EmojiListener mEmojiListener;

    public interface EmojiListener {
        void onEmojiClick(String emojiUnicode);
    }

    //private static List<String> emojisLíst = EmojiUtils.getEmojisList();
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

//    @SuppressLint("RestrictedApi")
//    @Override
//    public void setupDialog(@NonNull Dialog dialog, int style) {
//        super.setupDialog(dialog, style);
//        View contentView = View.inflate(getContext(), R.layout.fragment_bottom_sticker_emoji, null);
//        dialog.setContentView(contentView);
//
//        View parentView = (View) contentView.getParent();
//        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) parentView.getLayoutParams();
//        BottomSheetBehavior<?> behavior = (BottomSheetBehavior<?>) params.getBehavior();
//
//        if (behavior != null) {
//            behavior.addBottomSheetCallback(mBottomSheetBehaviorCallback);
//        }
//
//        parentView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
//
//        RecyclerView rvEmoji = contentView.findViewById(R.id.rvEmoji);
//        rvEmoji.setLayoutManager(new GridLayoutManager(getActivity(), 5));
//        EmojiAdapter emojiAdapter = new EmojiAdapter();
//        rvEmoji.setAdapter(emojiAdapter);
//        rvEmoji.setHasFixedSize(true);
//        rvEmoji.setItemViewCacheSize(emojisList.size());
//    }
//
//    public void setEmojiListener(EmojiListener emojiListener) {
//        this.mEmojiListener = emojiListener;
//    }
//
//    private class EmojiAdapter extends RecyclerView.Adapter<EmojiAdapter.ViewHolder> {
//        @NonNull
//        @Override
//        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_edit_emoji, parent, false);
//            return new ViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
//            holder.txtEmoji.setText(emojisList.get(position));
//        }
//
//        @Override
//        public int getItemCount() {
//            return emojisList.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            TextView txtEmoji;
//
//            public ViewHolder(@NonNull View itemView) {
//                super(itemView);
//                txtEmoji = itemView.findViewById(R.id.txtEmoji);
//
//                itemView.setOnClickListener(v -> {
//                    if (mEmojiListener != null) {
//                        mEmojiListener.onEmojiClick(emojisList.get(getAdapterPosition()));
//                    }
//                    dismiss();
//                });
//            }
//        }
//    }


}
