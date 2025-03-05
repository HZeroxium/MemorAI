// presentation/ui/fragment/PhotoDetailFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoDetailBinding;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PhotoDetailFragment extends Fragment {

    private FragmentPhotoDetailBinding binding;
    private PhotoViewModel photoViewModel;
    private String photoUrl;
    private String photoId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            photoUrl = getArguments().getString("photo_url", "");
            photoId = getArguments().getString("photo_id", "");
        }

        // ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());

        Glide.with(this)
                .load(photoUrl)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .into(binding.imageViewDetailPhoto);

        setSharedElementTransition();

        // Attach menu to toolbar
        binding.toolbar.setOnMenuItemClickListener(item -> {

            if (item.getItemId() == R.id.action_share_photo) {
                sharePhoto(photoUrl);
                return true;
            }

            if (item.getItemId() == R.id.action_edit_photo) {
                Bundle args = new Bundle();
                args.putString("photo_url", photoUrl);
                Navigation.findNavController(view).navigate(R.id.editPhotoFragment, args);
                return true;
            }

            if (item.getItemId() == R.id.action_delete_photo) {
                if (photoId == null || photoId.isEmpty()) {
                    Toast.makeText(requireContext(), "No photoId provided", Toast.LENGTH_SHORT).show();
                    return true;
                }
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Photo")
                        .setMessage("Are you sure you want to delete this photo?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            photoViewModel.deletePhoto(photoId);
                            Toast.makeText(requireContext(), "Photo deleted", Toast.LENGTH_SHORT).show();
                            Navigation.findNavController(view).popBackStack();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }

            return false;
        });
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
