package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class UserViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<Boolean> isAuthenticated;

    public UserViewModel() {
        firebaseAuth = FirebaseAuth.getInstance();
        userLiveData = new MutableLiveData<>();
        isAuthenticated = new MutableLiveData<>(false);

        // Kiểm tra trạng thái đăng nhập
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userLiveData.setValue(user);
            isAuthenticated.setValue(true);
        }
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return userLiveData;
    }

    public LiveData<Boolean> getIsAuthenticated() {
        return isAuthenticated;
    }

    public void signOut() {
        firebaseAuth.signOut();
        userLiveData.setValue(null);
        isAuthenticated.setValue(false);
    }
}
