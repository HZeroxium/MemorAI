package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.memorai.R;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;

import java.util.ArrayList;
import java.util.List;

public class PhotoListFragment extends Fragment {

    private RecyclerView recyclerView;
    private PhotoAdapter photoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Gắn layout cho fragment
        return inflater.inflate(R.layout.fragment_photo_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recyclerViewPhotos);

        // Dùng GridLayoutManager để hiển thị 2 cột (ví dụ)
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        // Tạo danh sách ảnh mẫu
        List<Photo> samplePhotos = getSamplePhotos();

        // Tạo adapter
        photoAdapter = new PhotoAdapter(requireContext(), samplePhotos);
        // Xử lý click
        photoAdapter.setOnPhotoClickListener((photo, position) -> {
            // TODO: Chuyển sang màn hình chi tiết ảnh
            // Hoặc Toast, v.v.
        });

        // Gán adapter cho RecyclerView
        recyclerView.setAdapter(photoAdapter);
    }

    /**
     * Tạm trả về danh sách ảnh giả lập (dummy) để kiểm thử UI.
     * Sau này thay thế bằng ViewModel + Repository + Dữ liệu thật (Room, Firebase, v.v.).
     */
    private List<Photo> getSamplePhotos() {
        List<Photo> list = new ArrayList<>();
        list.add(new Photo("https://picsum.photos/id/237/800/600")); // random image
        list.add(new Photo("https://picsum.photos/id/238/800/600"));
        list.add(new Photo("https://picsum.photos/id/239/800/600"));
        list.add(new Photo("https://picsum.photos/id/240/800/600"));
        // ... thêm ảnh tuỳ ý
        return list;
    }
}
