// presentation/ui/fragment/SearchFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter searchAdapter;
    private boolean isSearchActive = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater,
            @Nullable android.view.ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view,
            @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Setup navigation/back logic (system Back)
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (binding.searchView.isShowing()) {
                            if (isSearchActive) {
                                // If search results are showing, clear search and return to initial state
                                clearSearch();
                                isSearchActive = false;
                            } else {
                                // Just hide search view if no search is active
                                binding.searchView.hide();
                            }
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                });

        // SearchBar
        binding.searchBar.setNavigationIcon(R.drawable.ic_back);
        binding.searchBar.setNavigationOnClickListener(v -> {
            // If SearchView is showing, hide it. Otherwise pop back.
            if (binding.searchView.isShowing()) {
                if (isSearchActive) {
                    // If search results are showing, clear search and return to initial state
                    clearSearch();
                    isSearchActive = false;
                } else {
                    // Just hide search view if no search is active
                    binding.searchView.hide();
                }
            } else {
                Navigation.findNavController(v).popBackStack();
            }
        });

        // Menu click on SearchBar
        binding.searchBar.setOnMenuItemClickListener(this::onMenuItemClick);

        // Update the hint to indicate tag-based search
        binding.searchBar.setHint("Search by tags");
        binding.searchView.setHint("Enter a tag to search");

        // Clicking search bar => show SearchView
        binding.searchBar.setOnClickListener(v -> binding.searchView.show());

        // Link searchView with searchBar
        binding.searchView.setupWithSearchBar(binding.searchBar);

        // When user presses "enter" or "search" on the keyboard
        binding.searchView.getEditText().setOnEditorActionListener((v, actionId, event) -> {
            performSearch();
            return true;
        });

        // RecyclerView + adapter
        searchAdapter = new PhotoAdapter();
        searchAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            // Navigate to Photo Detail
            Bundle args = new Bundle();
            args.putString("photo_id", photo.getId());
            args.putByteArray("photo_url",
                    photo.getBitmap() != null ? convertBitmapToByteArray(photo.getBitmap()) : null);
            Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
        });

        binding.recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewSearchResults.setAdapter(searchAdapter);

        // Setup clear button in search results
        binding.buttonClearSearch.setOnClickListener(v -> clearSearch());

        // ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);

        // Observe the search results instead of all photos
        photoViewModel.getSearchResults().observe(getViewLifecycleOwner(), this::handleSearchResults);
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_search) {
            clearSearch();
            return true;
        }
        return false;
    }

    private void clearSearch() {
        binding.searchView.setText("");
        photoViewModel.clearSearch();
        isSearchActive = false;

        // Hide results views
        binding.textViewResultCount.setVisibility(View.GONE);
        binding.textViewNoResults.setVisibility(View.GONE);
        binding.buttonClearSearch.setVisibility(View.GONE);
        binding.recyclerViewSearchResults.setVisibility(View.GONE);
    }

    private void performSearch() {
        String query = binding.searchView.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a tag to search", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading indicator
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textViewNoResults.setVisibility(View.GONE);
        binding.textViewResultCount.setVisibility(View.GONE);

        // Keep search view visible and update search bar text
        binding.searchBar.setText(query);

        // Mark search as active (important for back button behavior)
        isSearchActive = true;

        // Start search
        photoViewModel.searchPhotosByTag(query, requireContext());
    }

    private void handleSearchResults(List<Photo> photos) {
        // Hide loading indicator
        binding.progressBar.setVisibility(View.GONE);

        // Always show the clear search button when search has been performed
        binding.buttonClearSearch.setVisibility(View.VISIBLE);

        // Always make the recycler view visible (even if empty)
        binding.recyclerViewSearchResults.setVisibility(View.VISIBLE);

        if (photos != null && !photos.isEmpty()) {
            // Show results count
            int resultCount = photos.size();
            binding.textViewResultCount.setVisibility(View.VISIBLE);
            binding.textViewResultCount.setText(resultCount + " photos found with tag: " +
                    binding.searchView.getText().toString().trim());

            // Show results
            searchAdapter.submitList(photos);
            binding.textViewNoResults.setVisibility(View.GONE);
        } else {
            // Show no results message
            searchAdapter.submitList(null);
            binding.textViewNoResults.setVisibility(View.VISIBLE);
            binding.textViewResultCount.setVisibility(View.GONE);
            binding.textViewNoResults.setText("No photos found with the tag: " +
                    binding.searchView.getText().toString().trim());
        }
    }

    private byte[] convertBitmapToByteArray(android.graphics.Bitmap bitmap) {
        java.io.ByteArrayOutputStream stream = new java.io.ByteArrayOutputStream();
        bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
