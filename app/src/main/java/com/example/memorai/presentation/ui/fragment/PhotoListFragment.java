// presentation/ui/fragment/PhotoListFragment.java
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
            // Tạo PhotoDetailFragment mới
            PhotoDetailFragment fragment = new PhotoDetailFragment();

            // Gói dữ liệu ảnh
            Bundle bundle = new Bundle();
            bundle.putString("photo_url", photo.getUrl());
            fragment.setArguments(bundle);

            // Nếu dùng SharedElement Transition, ta cần View ImageView
            // Từ PhotoAdapter,
            // => Nên trả về holder.imageViewPhoto hoặc holder.itemView
            // Giả sử ta gọi "viewHolderRef" là tham chiếu
            // (hoặc di chuyển logic này vào Adapter)

            // Chuyển sang Fragment chi tiết
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    // .addSharedElement(viewHolderRef, "shared_photo_transition")
                    //  ^ Để shared element effect, khớp với transitionName
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
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
