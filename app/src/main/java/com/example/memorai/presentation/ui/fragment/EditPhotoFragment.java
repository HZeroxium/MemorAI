// presentation/ui/fragment/EditPhotoFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentEditPhotoBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.viewmodel.EditPhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class EditPhotoFragment extends Fragment {

    private FragmentEditPhotoBinding binding;
    private EditPhotoViewModel editPhotoViewModel;
    private Photo currentPhoto;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEditPhotoBinding.inflate(inflater, container, false);
        binding.rvConstraintTools.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.rvFilterView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        return binding.getRoot();
    }

//    @Override
//    public void onViewCreated(@NonNull View view,
//                              @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        editPhotoViewModel = new ViewModelProvider(this).get(EditPhotoViewModel.class);
//
//        // Assume photo is passed as argument (e.g., photo id or URL)
//        if (getArguments() != null) {
//            String photoUrl = getArguments().getString("photo_url", "");
//            // In a real scenario, you might retrieve the full Photo object
//            currentPhoto = new Photo("id", "albumId", photoUrl, null, System.currentTimeMillis(), System.currentTimeMillis());
//            Glide.with(this)
//                    .load(currentPhoto.getFilePath())
//                    .placeholder(R.drawable.placeholder_image)
//                    .error(R.drawable.error_image)
//                    .into(binding.photoEditorView);
//        }
//
//        binding.btnSaveEdit.setOnClickListener(v -> {
//            // Perform edit operations; here we simulate by simply updating the photo timestamp
//            currentPhoto = new Photo(currentPhoto.getId(), currentPhoto.getAlbumId(),
//                    currentPhoto.getFilePath(), currentPhoto.getTags(),
//                    currentPhoto.getCreatedAt(), System.currentTimeMillis());
//            editPhotoViewModel.updatePhoto(currentPhoto);
//        });
//
//        editPhotoViewModel.getEditedPhoto().observe(getViewLifecycleOwner(), photo -> {
//            // Navigate back or show success message
//            getParentFragmentManager().popBackStack();
//        });
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
