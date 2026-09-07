package com.example.bookify.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.data.remote.dto.UpdateProfileRequest;
import com.example.bookify.data.remote.dto.UserProfileResponse;
import com.example.bookify.data.repository.ProfileRepository;
import com.example.bookify.utils.ErrorUtils;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.MultipartBody;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    public enum FriendState {
        SELF, NOT_FRIEND, REQUEST_SENT, FRIEND
    }

    private final ProfileRepository profileRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private final MutableLiveData<FriendState> friendStateLiveData = new MutableLiveData<>(FriendState.SELF);
    private final MutableLiveData<Integer> selectedTabLiveData = new MutableLiveData<>(0); 
    private final MutableLiveData<Integer> followersCountLiveData = new MutableLiveData<>(1200);

    // Profile Data
    private final MutableLiveData<UserProfileResponse> profileData = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>(null);

    @Inject
    public ProfileViewModel(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.clear();
    }

    public LiveData<UserProfileResponse> getProfileData() {
        return profileData;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<FriendState> getFriendState() {
        return friendStateLiveData;
    }

    public LiveData<Integer> getSelectedTab() {
        return selectedTabLiveData;
    }

    public LiveData<Integer> getFollowersCount() {
        return followersCountLiveData;
    }

    public void cycleFriendState() {
        FriendState current = friendStateLiveData.getValue();
        if (current == null) current = FriendState.NOT_FRIEND;

        switch (current) {
            case NOT_FRIEND:
                friendStateLiveData.setValue(FriendState.REQUEST_SENT);
                break;
            case REQUEST_SENT:
                friendStateLiveData.setValue(FriendState.FRIEND);
                break;
            case FRIEND:
                friendStateLiveData.setValue(FriendState.SELF);
                break;
            case SELF:
                friendStateLiveData.setValue(FriendState.NOT_FRIEND);
                break;
        }

        Integer currentCount = followersCountLiveData.getValue();
        int count = currentCount != null ? currentCount : 1200;
        if (friendStateLiveData.getValue() == FriendState.FRIEND) {
            followersCountLiveData.setValue(count + 1);
        } else if (current == FriendState.FRIEND) {
            followersCountLiveData.setValue(Math.max(1200, count - 1));
        }
    }

    public void selectTab(int position) {
        selectedTabLiveData.setValue(position);
    }

    public void fetchMyProfile() {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        compositeDisposable.add(
                profileRepository.getMyProfile()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading.setValue(false);
                                    if (response.getCode() == 1000) {
                                        profileData.setValue(response.getResult());
                                    } else {
                                        errorMessage.setValue(response.getMessage());
                                    }
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue(ErrorUtils.parseError(throwable, "Error fetching profile"));
                                }
                        )
        );
    }

    public void updateProfile(UpdateProfileRequest request) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        compositeDisposable.add(
                profileRepository.updateProfile(request)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading.setValue(false);
                                    if (response.getCode() == 1000) {
                                        profileData.setValue(response.getResult());
                                    } else {
                                        errorMessage.setValue(response.getMessage());
                                    }
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue(ErrorUtils.parseError(throwable, "Error updating profile"));
                                }
                        )
        );
    }

    public void uploadAvatar(MultipartBody.Part filePart) {
        isLoading.setValue(true);
        errorMessage.setValue(null);
        compositeDisposable.add(
                profileRepository.uploadAvatar(filePart)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                response -> {
                                    isLoading.setValue(false);
                                    if (response.getCode() == 1000) {
                                        profileData.setValue(response.getResult());
                                    } else {
                                        errorMessage.setValue(response.getMessage());
                                    }
                                },
                                throwable -> {
                                    isLoading.setValue(false);
                                    errorMessage.setValue(ErrorUtils.parseError(throwable, "Error uploading avatar"));
                                }
                        )
        );
    }
}
