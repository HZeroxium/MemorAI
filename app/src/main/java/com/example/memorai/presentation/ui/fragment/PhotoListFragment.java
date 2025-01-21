// presentation/ui/fragment/PhotoListFragment.java
package com.example.memorai.presentation.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.memorai.R;
import com.example.memorai.databinding.FragmentPhotoListBinding;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.presentation.ui.adapter.PhotoAdapter;
import com.example.memorai.presentation.viewmodel.PhotoViewModel;

import java.util.ArrayList;
import java.util.List;

public class PhotoListFragment extends Fragment {

    private FragmentPhotoListBinding binding;
    private PhotoViewModel photoViewModel;
    private PhotoAdapter photoAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPhotoListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

//    @Override
//    public void onViewCreated(@NonNull View view,
//                              @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        recyclerView = view.findViewById(R.id.recyclerViewPhotos);
//
//        // Dùng GridLayoutManager để hiển thị 2 cột (ví dụ)
//        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), 2);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // Tạo danh sách ảnh mẫu
//        List<Photo> samplePhotos = getSamplePhotos();
//
//        // Tạo adapter
//        photoAdapter = new PhotoAdapter(requireContext(), samplePhotos);
//        // Xử lý click
//        photoAdapter.setOnPhotoClickListener((photo, position) -> {
//            // Tạo PhotoDetailFragment mới
//            PhotoDetailFragment fragment = new PhotoDetailFragment();
//
//            // Gói dữ liệu ảnh
//            Bundle bundle = new Bundle();
//            bundle.putString("photo_url", photo.getUrl());
//            fragment.setArguments(bundle);
//
//            // Nếu dùng SharedElement Transition, ta cần View ImageView
//            // Từ PhotoAdapter,
//            // => Nên trả về holder.imageViewPhoto hoặc holder.itemView
//            // Giả sử ta gọi "viewHolderRef" là tham chiếu
//            // (hoặc di chuyển logic này vào Adapter)
//
//            // Chuyển sang Fragment chi tiết
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    // .addSharedElement(viewHolderRef, "shared_photo_transition")
//                    //  ^ Để shared element effect, khớp với transitionName
//                    .replace(R.id.container, fragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
//
//        // Gán adapter cho RecyclerView
//        recyclerView.setAdapter(photoAdapter);
//
//        // Handle Add Photo button
//        FloatingActionButton fabAddPhoto = view.findViewById(R.id.fabAddPhoto);
//        fabAddPhoto.setOnClickListener(v -> {
//            // Chuyển sang AddPhotoFragment
//            requireActivity().getSupportFragmentManager()
//                    .beginTransaction()
//                    .replace(R.id.container, new AddPhotoFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });
//    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        photoAdapter = new PhotoAdapter();
        binding.recyclerViewPhotos.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        binding.recyclerViewPhotos.setAdapter(photoAdapter);

        photoAdapter.setOnPhotoClickListener((sharedView, photo) -> {
            PhotoDetailFragment fragment = new PhotoDetailFragment();
            Bundle args = new Bundle();
            args.putString("photo_url", photo.getUrl());
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .addSharedElement(sharedView, "shared_photo_transition")
                    .replace(R.id.container, fragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.fabAddPhoto.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container, new AddPhotoFragment())
                    .addToBackStack(null)
                    .commit();
        });

        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.observeAllPhotos().observe(getViewLifecycleOwner(), this::updatePhotos);
        photoViewModel.loadAllPhotos();
    }


    private List<Photo> getSamplePhotos() {
        List<Photo> list = new ArrayList<>();
        list.add(new Photo("https://picsum.photos/id/237/800/600")); // random image
        list.add(new Photo("https://picsum.photos/id/238/800/600"));
        list.add(new Photo("https://picsum.photos/id/239/800/600"));
        list.add(new Photo("https://picsum.photos/id/240/800/600"));
        // ...
        return list;
    }

    private void updatePhotos(List<Photo> photos) {
        photoAdapter.submitList(photos);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
