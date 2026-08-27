package com.example.bookify.di;

import com.example.bookify.data.remote.api.IdentityApiService;
import com.example.bookify.data.remote.api.ProfileApiService;
import com.example.bookify.data.remote.api.RefreshApiService;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    private static final String BASE_URL = "http://10.0.2.2:8888/";


    // config cho OkHttp -> Hlit gọi và auto inject cho hàm provideRetrofit()
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(AuthInterceptor authInterceptor,
                                            TokenRefreshAuthenticator authenticator) {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .addInterceptor(authInterceptor) // đính kèm actoken vào header
                .addInterceptor(loggingInterceptor) // in request/response ra Logcat
                .authenticator(authenticator) // thực thi khi gặp 401 : refresh token và retry request
                .build();
    }


    // config cho Retrofit -> Hlit gọi và auto inject cho các hàm cấn retrofit như: provideIdentityApiService() ...
    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                    // Retrofit sử dụng OkHttpClient để thực hiện HTTP request.
                    // OkHttp nhận HTTP response (okhttp3.Response) từ server,
                    // sau đó Retrofit xử lý response đó.
                .client(okHttpClient)
                    // sử dụng Gson để chuyển đổi JSON -> Object
                    // -> (sử dụng RxJava Adapter đã config) bọc obj vào Single<obj> trả cho repo tự subcribe lấy data obj
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // cho phép Retrofit sử dụng kiểu RxJava như Single, Observable...
                .build();
    }

    // retrofit.create(...) tạo implementation cho interface của hàm gọi api cụ thể để gọi BE
    // thực thi hàm api cụ thể khi repo gọi, v dụ : identityApiService.login(),...
    // OkHttp gửi request -> ..
    @Provides
    @Singleton
    public IdentityApiService provideIdentityApiService(Retrofit retrofit) {
        return retrofit.create(IdentityApiService.class);
    }

    @Provides
    @Singleton
    public ProfileApiService provideProfileApiService(Retrofit retrofit) {
        return retrofit.create(ProfileApiService.class);
    }

    @Provides
    @Singleton
    public RefreshApiService provideRefreshApiService() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        // không add AuthInterceptor và Authenticator
        OkHttpClient refreshClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build();
        Retrofit refreshRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(refreshClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build();
        return refreshRetrofit.create(RefreshApiService.class);
    }
}
