package com.example.bookify.ui.menu;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.data.remote.dto.UserProfileResponse;
import com.example.bookify.data.repository.AuthRepository;
import com.example.bookify.data.repository.ProfileRepository;
import com.example.bookify.utils.TokenManager;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

@HiltViewModel
public class GlobalMenuViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final ProfileRepository profileRepository;
    private final TokenManager tokenManager;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<UserProfileResponse> profileData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();

    @Inject
    public GlobalMenuViewModel(AuthRepository authRepository, ProfileRepository profileRepository, TokenManager tokenManager) {
        this.authRepository = authRepository;
        this.profileRepository = profileRepository;
        this.tokenManager = tokenManager;
    }

    public LiveData<UserProfileResponse> getProfileData() {
        return profileData;
    }

    public LiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }

    public void fetchProfile() {
        compositeDisposable.add(
                profileRepository.getMyProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    if (response.getResult() != null) {
                                        profileData.setValue(response.getResult());
                                    }
                                },
                                throwable -> {
                                    // Ignore or handle error
                                }
                        )
        );
    }

    public void logout() {
        // Xóa token local NGAY LẬP TỨC — không chờ API
        // Đảm bảo user luôn bị đăng xuất ở local dù mạng có lỗi hay không
        tokenManager.clearTokens();
        logoutSuccess.setValue(true);

        // Gọi API best-effort để server revoke token — không cần quan tâm kết quả
        compositeDisposable.add(
                authRepository.logout()
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                                response -> { /* server revoked, done */ },
                                throwable -> { /* ignore — local already cleared */ }
                        )
        );
    }

    public void resetLogoutState() {
        logoutSuccess.setValue(null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }
}
