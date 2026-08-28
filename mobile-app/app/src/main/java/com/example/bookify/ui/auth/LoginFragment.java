package com.example.bookify.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.credentials.CredentialManager;
import androidx.credentials.CredentialManagerCallback;
import androidx.credentials.CustomCredential;
import androidx.credentials.GetCredentialRequest;
import androidx.credentials.GetCredentialResponse;
import androidx.credentials.exceptions.GetCredentialCancellationException;
import androidx.credentials.exceptions.GetCredentialException;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.bookify.R;
import com.example.bookify.databinding.FragmentLoginBinding;
import com.example.bookify.ui.home.HomeActivity;
import com.example.bookify.BuildConfig;
import com.google.android.libraries.identity.googleid.GetGoogleIdOption;
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;

    // biến quản lí xác thực danh tính (credentials)
    // -> để lấy id token từ GG
    private CredentialManager credentialManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        credentialManager = CredentialManager.create(requireContext());
        setupClickListeners();

        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String password = binding.etPassword.getText().toString();
            authViewModel.login(username, password);
        });
        binding.tvGoToRegister.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registerFragment);
        });

        binding.tvForgotPassword.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_forgotPasswordFragment);
        });

        binding.btnGoogleLogin.setOnClickListener(v -> handleGoogleLogin());

    }

    private void observeViewModel() {
        //  Trạng thái Loading
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnLogin.setEnabled(!isLoading);
            binding.btnLogin.setText(isLoading ? "Logging in..." : getString(R.string.btn_login));
        });

        //  Field Error: Username
        authViewModel.getLoginUsernameError().observe(getViewLifecycleOwner(), error -> {
            binding.tilUsername.setError(error);
        });

        //  Field Error: Password
        authViewModel.getLoginPasswordError().observe(getViewLifecycleOwner(), error -> {
            binding.tilPassword.setError(error);
        });

        //  API Error (Toast thông báo hệ thống / kết nối)
        authViewModel.getApiError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        //  Business Event: Chưa xác thực email -> Điều hướng sang màn hình OTP
        authViewModel.getUnverifiedEmailEvent().observe(getViewLifecycleOwner(), email -> {
            if (email != null && !email.isEmpty()) {
                authViewModel.resetUnverifiedEmailEvent();
                Toast.makeText(requireContext(), "Please verify your email before logging in", Toast.LENGTH_LONG).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_loginFragment_to_otpVerifyFragment);
            }
        });

        //  Đăng nhập thành công
        authViewModel.getAuthSuccessUser().observe(getViewLifecycleOwner(), userModel -> {
            if (userModel != null) {
                binding.tilUsername.setError(null);
                binding.tilPassword.setError(null);

                Toast.makeText(requireContext().getApplicationContext(), "Login successful!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(requireContext(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        //  TextWatchers xóa lỗi khi người dùng gõ
        binding.etUsername.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilUsername.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });

        binding.etPassword.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilPassword.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        });
    }

    public void handleGoogleLogin() {
        // Lấy webclient id từ cấu hình bảo mật
        String webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID;

        // Cấu hình Option để lấy GG Id Token(gg credentials)
        GetGoogleIdOption googleIdOption = new GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false) // Cho phép chọn tài khoản GG kh giới hạn việc đã từng authorize
                                                      // hay chưa ,
                // nếu true -> credential manager ưu tiên lọc theo các acc đã authorized
                .setServerClientId(webClientId) // tạo id token dành cho server/client id này (aud : BE sẽ check ở claim
                                                // này)
                .setAutoSelectEnabled(false) // kh tự động chọn acc nếu có thể
                .build();

        // request hoàn chỉnh để lấy GG Id Token
        GetCredentialRequest request = new GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption) // thêm cấu hình GG option
                .build();

        authViewModel.updateLoadingState(true);

        // Gọi CredentialManager (chạy bất đồng bộ) thực hiện request
        credentialManager.getCredentialAsync( // mở cửa sổ dialog để chọn tài khoản GG
                requireContext(),
                request,
                new CancellationSignal(), // obj cho phép hủy request bất đồng bộ
                ContextCompat.getMainExecutor(requireContext()), // lấy Executor để chạy callback trên main thread
                new CredentialManagerCallback<GetCredentialResponse, GetCredentialException>() {
                    // -> Khi credential Manager trả kq, Executor gọi onResult() / onError() trên
                    // Main Thread.

                    // Success
                    @Override
                    public void onResult(GetCredentialResponse result) {
                        // Hứng kết quả Thành Công -> Gửi Token cho Backend

                        if (result.getCredential() instanceof CustomCredential) { // check cre mana trả về credential có
                                                                                  // phải kiểu CustomCredential kh(vì
                                                                                  // mana trả nhiều loại cre)
                            CustomCredential credential = (CustomCredential) result.getCredential(); // ép kiểu
                                                                                                     // Credential về
                                                                                                     // CustomCredential
                            if (credential.getType().equals(GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL)) { // Check:
                                                                                                                        // Credential
                                                                                                                        // này
                                                                                                                        // có
                                                                                                                        // thực
                                                                                                                        // sự
                                                                                                                        // là
                                                                                                                        // Google
                                                                                                                        // ID
                                                                                                                        // Token
                                                                                                                        // kh
                                try {
                                    // credential.getData() lấy raw data
                                    // biến raw data thành object Google
                                    GoogleIdTokenCredential idTokenCredential = GoogleIdTokenCredential
                                            .createFrom(credential.getData());
                                    String idToken = idTokenCredential.getIdToken(); // lấy id token

                                    // Gọi ViewModel để ném lên Backend
                                    authViewModel.loginWithGoogle(idToken);
                                } catch (Exception e) {
                                    Toast.makeText(requireContext(), "Error parsing Google ID token: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    }

                    // Error
                    @Override
                    public void onError(GetCredentialException e) {
                        // Xử lý Lỗi hoặc do User bấm Hủy
                        authViewModel.updateLoadingState(false);
                        if (e instanceof GetCredentialCancellationException) { // User tự bấm hủy
                        } else {
                            Toast.makeText(requireContext(), "Login with Google failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Tránh memory leak
    }
}
