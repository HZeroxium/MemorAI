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
import androidx.recyclerview.widget.RecyclerView;
import com.example.memorai.R;
import com.example.memorai.presentation.ui.adapter.AlbumAdapter;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;

public class AlbumListFragment extends Fragment {
    private AlbumViewModel albumViewModel;
    private AlbumAdapter albumAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_album_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(this).get(AlbumViewModel.class);
        RecyclerView recyclerView = view.findViewById(R.id.rv_album_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        albumAdapter = new AlbumAdapter();
        recyclerView.setAdapter(albumAdapter);

        // Observe data from ViewModel
        albumViewModel.getAlbumList().observe(getViewLifecycleOwner(), albumList -> {
            albumAdapter.submitList(albumList);
        });

        // Load dummy data
        albumViewModel.loadDummyAlbums();
    }
}
