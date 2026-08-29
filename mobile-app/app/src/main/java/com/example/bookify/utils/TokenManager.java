package com.example.bookify.utils;

import android.content.Context;
import android.content.SharedPreferences;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.subjects.PublishSubject;

import dagger.hilt.android.qualifiers.ApplicationContext;

@Singleton
public class TokenManager {
    private static final String PREF_NAME = "BookifyPrefs";
    private static final String KEY_ACCESS_TOKEN = "jwt_token";

    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private final SharedPreferences prefs;

    // Subject phát sự kiện: khi phiên đăng nhập thực sự hết hạn
    private final PublishSubject<Boolean> sessionExpiredSubject = PublishSubject.create();

    public Observable<Boolean> getSessionExpiredObservable() {
        return sessionExpiredSubject.hide();
    }

    public void notifySessionExpired() {
        sessionExpiredSubject.onNext(true);
    }

    @Inject
    public TokenManager(@ApplicationContext Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveTokens(String accessToken, String refreshToken) {
        prefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }
    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public void clearTokens() {
        prefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .apply();
    }

    public String getToken() {
        return getAccessToken();
    }





}