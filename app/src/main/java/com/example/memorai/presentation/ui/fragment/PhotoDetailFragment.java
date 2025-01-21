// presentation/ui/fragment/PhotoDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PhotoDetailFragment extends Fragment {

    private ImageView imageViewDetailPhoto;
    private FloatingActionButton fabShare;

    // Giá trị URL (hoặc URI) của ảnh
    private String photoUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout
        return inflater.inflate(R.layout.fragment_photo_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // View initialization
        imageViewDetailPhoto = view.findViewById(R.id.imageViewDetailPhoto);
        fabShare = view.findViewById(R.id.fabShare);

        // Get photo URL from arguments
        if (getArguments() != null) {
            photoUrl = getArguments().getString("photo_url", "");
        }

        // Set up toolbar
        MaterialToolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        // Load photo using Glide
        Glide.with(requireContext())
                .load(photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(imageViewDetailPhoto);

        // Share button click listener
        fabShare.setOnClickListener(v -> {
            // Tham khảo: https://developer.android.com/training/sharing/send
            sharePhoto(photoUrl);
        });

        // Setup shared element transition
        setupSharedElementTransition();
    }

    private void sharePhoto(String url) {
        // Tùy theo mục đích chia sẻ:
        // - TEXT/PLAIN: gửi link
        // - IMAGE/*: cần URI ảnh (thường file local) + FileProvider
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
    }

    private void setupSharedElementTransition() {
        // Kích hoạt shared element transition khi fragment xuất hiện
        // Lưu ý: Cần thêm dependency 'androidx.transition:transition:...' nếu chưa có
        setSharedElementEnterTransition(
                new androidx.transition.TransitionSet()
                        .addTransition(new androidx.transition.ChangeBounds())
                        .addTransition(new androidx.transition.ChangeTransform())
                        .addTransition(new androidx.transition.ChangeImageTransform())
        );
        // setSharedElementReturnTransition(...) nếu muốn áp dụng hiệu ứng ngược
    }
}
