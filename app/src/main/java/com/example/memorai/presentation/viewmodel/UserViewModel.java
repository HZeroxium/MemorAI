package com.example.memorai.presentation.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserViewModel extends ViewModel {
    private final FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    private final MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isAuthenticated = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public UserViewModel() {
        checkAuthState();
    }

    private void checkAuthState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            userLiveData.setValue(user);
            isAuthenticated.setValue(true);
        }
    }


    public void signOut() {
        firebaseAuth.signOut();
        userLiveData.setValue(null);
        isAuthenticated.setValue(false);

    }

    // Getters
    public LiveData<FirebaseUser> getUserLiveData() { return userLiveData; }
    public LiveData<Boolean> getIsAuthenticated() { return isAuthenticated; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

}