// presentation/ui/fragment/SearchFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentSearchBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * A fragment that searches photos by keyword or tag.
 */
@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter searchAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup toolbar with "Search" title, back arrow
        binding.toolbarSearch.setTitle("Search");
        binding.toolbarSearch.setNavigationIcon(R.drawable.ic_back);
        binding.toolbarSearch.setNavigationOnClickListener(v -> {
            // Or popBackStack
            Navigation.findNavController(requireView()).popBackStack();
        });

        // Setup ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        // Setup RecyclerView
        searchAdapter = new PhotoAdapter();
        searchAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            // Navigate to detail
            Bundle args = new Bundle();
            args.putString("photo_id", photo.getId());
            args.putString("photo_url", photo.getFilePath());
            Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
        });
        binding.recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewSearchResults.setAdapter(searchAdapter);

        // Observe changes to the "all photos" or "search results"
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handleSearchResults);

        // Setup search button
        binding.buttonSearch.setOnClickListener(v -> {
            String query = getQueryText();
            if (TextUtils.isEmpty(query)) {
                Toast.makeText(requireContext(), "Enter a tag or keyword", Toast.LENGTH_SHORT).show();
                return;
            }
            photoViewModel.searchPhotos(query);
        });

        // Optionally handle "Enter" key on the text field
        binding.editTextSearch.setOnEditorActionListener((textView, actionId, event) -> {
            binding.buttonSearch.performClick();
            return true;
        });

        // Optionally load a default set of photos or show blank initially
        // photoViewModel.loadAllPhotos();

        // If you want a "swipe to refresh" or something, do it here
    }

    private String getQueryText() {
        TextInputEditText edit = binding.editTextSearch;
        return (edit != null && edit.getText() != null) ? edit.getText().toString().trim() : "";
    }

    private void handleSearchResults(List<Photo> photos) {
        if (photos != null && !photos.isEmpty()) {
            searchAdapter.submitList(photos);
            binding.textViewNoResults.setVisibility(View.GONE);
        } else {
            searchAdapter.submitList(null);
            binding.textViewNoResults.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
