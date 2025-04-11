package com.example.memorai.presentation.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.memorai.databinding.FragmentLoginBinding;
import com.example.memorai.domain.model.Album;
import com.example.memorai.domain.model.Photo;
import com.example.memorai.domain.model.User;
import com.example.memorai.presentation.ui.activity.MainActivity;
import com.example.memorai.presentation.viewmodel.AlbumViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

public class LoginFragment extends BottomSheetDialogFragment {
    private FragmentLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private static final int RC_SIGN_IN = 9001;
    private AlbumViewModel albumViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("619405178592-3ri6lcne9ejli7bt6dt6elj3vo0132t0.apps.googleusercontent.com")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        albumViewModel = new ViewModelProvider(requireActivity()).get(AlbumViewModel.class);
        binding.btnGoogleSignIn.setOnClickListener(v -> signIn());
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            try {
                GoogleSignInAccount account = GoogleSignIn.getSignedInAccountFromIntent(data)
                        .getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                Log.w("LoginFragment", "Google sign-in failed", e);
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        AuthCredential credential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(requireActivity(), task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveUserToFirestore(user);
                        }
                    } else {
                        Log.w("LoginFragment", "Đăng nhập Firebase thất bại", task.getException());
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference usersRef = database.getReference("users").child(firebaseUser.getUid());

        usersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (!task.getResult().exists()) {
                    User user = new User(
                            firebaseUser.getUid(),
                            firebaseUser.getDisplayName(),
                            firebaseUser.getEmail(),
                            firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : null,
                            "123456"
                    );

                    createPrivateAlbum();

                    PinFragment pinFragment = new PinFragment();
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("user", user);
                    bundle.putString("mode", "create");
                    pinFragment.setArguments(bundle);
                    pinFragment.show(getParentFragmentManager(), "PinFragment");

                    usersRef.setValue(user)
                            .addOnSuccessListener(aVoid -> Log.d("LoginFragment", "Lưu dữ liệu thành công"))
                            .addOnFailureListener(e -> Log.w("LoginFragment", "Lưu dữ liệu thất bại", e));
                } else {
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            } else {
                Log.e("LoginFragment", "Lỗi khi kiểm tra người dùng", task.getException());
            }
        });
    }

    private void createDefaultPrivateAlbum(String userId) {
        String albumId = UUID.randomUUID().toString();
        Map<String, Object> albumData = new HashMap<>();
        albumData.put("id", albumId);
        albumData.put("name", "Private");
        albumData.put("description", "My private photos");
        albumData.put("photos", new ArrayList<String>());
        albumData.put("coverPhotoUrl", "");
        albumData.put("createdAt", System.currentTimeMillis());
        albumData.put("updatedAt", System.currentTimeMillis());
        albumData.put("isPrivate", true);

        firestore.collection("photos")
                .document(userId)
                .collection("user_albums")
                .document(albumId)
                .set(albumData)
                .addOnSuccessListener(aVoid -> Log.d("LoginFragment", "Default private album created"))
                .addOnFailureListener(e -> Log.w("LoginFragment", "Failed to create default album", e));
    }

    private void createPrivateAlbum() {
        List<String> photoIds = new ArrayList<>();

        // Create new album
        String albumId = UUID.randomUUID().toString();

        Album newAlbum = new Album(
                albumId,
                "Private",
                "",
                photoIds,
                "",
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                true
        );

        List<Photo> listPhoto = Collections.emptyList();
        // Sử dụng AlbumViewModel để tạo album
        albumViewModel.createAlbumWithPhotos(newAlbum, listPhoto);
        albumViewModel.loadAlbums();

        // Hiển thị thông báo và quay lại
        Toast.makeText(requireContext(), "Album created!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}