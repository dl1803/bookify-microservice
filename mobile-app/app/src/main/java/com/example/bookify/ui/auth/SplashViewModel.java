package com.example.bookify.ui.auth;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class SplashViewModel extends ViewModel {
    private final TokenManager tokenManager;
    private final MutableLiveData<Boolean> _isLoggedIn = new MutableLiveData<>();

    public MutableLiveData<Boolean> isLoggedIn() {
        return _isLoggedIn;
    }

    @Inject
    public SplashViewModel(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    public void checkSession() {
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String token = tokenManager.getToken();
            boolean hasToken = token != null && !token.trim().isEmpty();
            _isLoggedIn.setValue(hasToken);
        }, 1500);
    }


}

