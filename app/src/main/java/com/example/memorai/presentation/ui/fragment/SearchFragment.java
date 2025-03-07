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
                            binding.searchView.hide();
                        } else {
                            setEnabled(false);
                            requireActivity().onBackPressed();
                        }
                    }
                }
        );

        // SearchBar
        binding.searchBar.setNavigationIcon(R.drawable.ic_back);
        binding.searchBar.setNavigationOnClickListener(v -> {
            // If SearchView is showing, hide it. Otherwise pop back.
            if (binding.searchView.isShowing()) {
                binding.searchView.hide();
            } else {
                Navigation.findNavController(v).popBackStack();
            }
        });

        // Menu click on SearchBar
        binding.searchBar.setOnMenuItemClickListener(this::onMenuItemClick);

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
            args.putString("photo_url", photo.getFilePath());
            Navigation.findNavController(requireView()).navigate(R.id.photoDetailFragment, args);
        });

        binding.recyclerViewSearchResults.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewSearchResults.setAdapter(searchAdapter);

        // ViewModel
        photoViewModel = new ViewModelProvider(requireActivity()).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::handleSearchResults);

        // Optional: loadAllPhotos if you want initial data
        // photoViewModel.loadAllPhotos();
    }

    private boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.action_clear_search) {
            binding.searchView.setText("");
            photoViewModel.clearSearch();
            return true;
        }
        return false;
    }

    private void performSearch() {
        String query = binding.searchView.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(requireContext(), "Enter a tag or keyword", Toast.LENGTH_SHORT).show();
            return;
        }
        photoViewModel.searchPhotos(query);
        binding.searchView.hide();
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
