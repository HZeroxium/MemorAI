// presentation/ui/fragment/PhotoDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoDetailBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoDetailFragment extends Fragment {
    private FragmentPhotoDetailBinding binding;
    private String photoUrl;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (getArguments() != null) photoUrl = getArguments().getString("photo_url", "");

        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.imageViewDetailPhoto);

        binding.fabShare.setOnClickListener(v -> sharePhoto(photoUrl));

        setSharedElementTransition();
    }

    private void sharePhoto(String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_with)));
    }

    private void setSharedElementTransition() {
        setSharedElementEnterTransition(
                new androidx.transition.TransitionSet()
                        .addTransition(new androidx.transition.ChangeBounds())
                        .addTransition(new androidx.transition.ChangeTransform())
                        .addTransition(new androidx.transition.ChangeImageTransform())
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

