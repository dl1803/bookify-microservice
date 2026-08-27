package com.example.bookify.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bookify.data.model.PostModel;
import com.example.bookify.data.repository.PostRepository;
import com.example.bookify.utils.TokenManager;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

/**
 * ViewModel cho HomeActivity.
 * Quản lý feed posts và lắng nghe sự kiện session expired từ TokenManager.
 */
@HiltViewModel
public class HomeViewModel extends ViewModel {

    private final PostRepository repository;
    private final LiveData<List<PostModel>> postsLiveData;
    private final CompositeDisposable disposable = new CompositeDisposable();

    private final MutableLiveData<Boolean> _sessionExpired = new MutableLiveData<>();
    public LiveData<Boolean> getSessionExpired() {
        return _sessionExpired;
    }

    @Inject
    public HomeViewModel(TokenManager tokenManager) {
        repository = PostRepository.getInstance();
        postsLiveData = repository.getHomeFeedPosts();

        disposable.add(
                tokenManager.getSessionExpiredObservable()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(expired -> {
                            if (expired != null && expired) {
                                _sessionExpired.setValue(true);
                            }
                        })
        );
    }

    public LiveData<List<PostModel>> getPosts() {
        return postsLiveData;
    }

    public void createNewPost(String content, String bookTitle, String bookAuthor, int bookCoverResId) {
        repository.createPost(content, bookTitle, bookAuthor, bookCoverResId);
    }

    public void toggleLike(int position) {
        repository.toggleLike(position);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        disposable.clear();
    }
}
