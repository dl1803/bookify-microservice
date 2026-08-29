package com.example.bookify.ui.auth;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.data.remote.api.ProfileApiService;
import com.example.bookify.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class SplashViewModel extends ViewModel {
    private final TokenManager tokenManager;
    private final ProfileApiService profileApiService;
    private final MutableLiveData<Boolean> _isLoggedIn = new MutableLiveData<>();
    private final CompositeDisposable disposable = new CompositeDisposable();

    public LiveData<Boolean> isLoggedIn() {
        return _isLoggedIn;
    }

    @Inject
    public SplashViewModel(TokenManager tokenManager, ProfileApiService profileApiService) {
        this.tokenManager = tokenManager;
        this.profileApiService = profileApiService;
    }

    public void checkSession() {
        String token = tokenManager.getToken();
        if (token == null || token.trim().isEmpty()) {
            new Handler(Looper.getMainLooper()).postDelayed(() -> _isLoggedIn.setValue(false), 2000);
            return;
        }

        // Gọi API để verify session thực sự
        disposable.add(
                profileApiService.getMyProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getCode() == 1000) {
                                        _isLoggedIn.setValue(true);
                                    } else {
                                        _isLoggedIn.setValue(false);
                                    }
                                },
                                error -> {
                                    if (error instanceof retrofit2.HttpException) {
                                        retrofit2.HttpException httpException = (retrofit2.HttpException) error;
                                        if (httpException.code() == 401) {
                                            _isLoggedIn.setValue(false);
                                            return;
                                        }
                                    }
                                    // Lỗi mạng hoặc 5xx -> vẫn cho vào Home, Home sẽ hiển thị lỗi mạng khi load feed
                                    _isLoggedIn.setValue(true);
                                }
                        )
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }


}

