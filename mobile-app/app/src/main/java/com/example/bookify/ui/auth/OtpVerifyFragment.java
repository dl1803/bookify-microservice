package com.example.bookify.ui.auth;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.bookify.R;
import com.example.bookify.databinding.FragmentOtpVerifyBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OtpVerifyFragment extends Fragment {
    private AuthViewModel authViewModel;
    private FragmentOtpVerifyBinding binding;
    private CountDownTimer countDownTimer; // Đếm ngược thời gian

    @NonNull
    @Override
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOtpVerifyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // requireActivity() : lấy ViewModel từ Activity hiện tại, mục đích lấy ViewModel từ Fragment khác (cụ thể là email từ RegisterFragment)
        // nếu dùng this -> tạo 1 viewmodel mới
        // nếu dùng requireActivity() -> lấy ViewModel từ Activity hiện tại (chứa nhiều Fragment dùng chung Activity: buộc các fragment khác muốn share data phải dùng requireActivity() giống như bên nhận)
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        String email = authViewModel.getRegisterEmail();
        binding.tvSubtitle.setText("We sent a 4-digit verification code to \" + email + \". Please enter it below.");

        setupClickListeners();
        observeViewModel();

        // Bắt đầu đếm ngược thời gian : 30s
        startResendTimer();
    }

    public void setupClickListeners(){
        binding.btnBack.setOnClickListener(v -> {
            Navigation.findNavController(v).popBackStack();
        });

        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.pinViewOtp.getText().toString();
            authViewModel.verifyEmail(otp);
        });

        binding.tvResend.setOnClickListener(v -> {
            authViewModel.resendOtp();
            startResendTimer();
            Toast.makeText(requireContext(), "New OTP sent!", Toast.LENGTH_SHORT).show();
        });
    }

    public void observeViewModel(){
        // Loading
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnVerify.setEnabled(!isLoading);
            binding.btnVerify.setText(isLoading? "Verifying..." : "Verify Code");
        });

        // Error
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getVerifySuccess().observe(getViewLifecycleOwner(), isSucess -> {
            if (isSucess){
                Toast.makeText(requireContext(), "Email verified! Please login.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_otpVerifyFragment_to_loginFragment);
            }
        });
    }

    public void startResendTimer() {
        binding.tvResend.setEnabled(false);
        binding.tvResend.setTextColor(getResources().getColor(R.color.text_disabled, null));

        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        countDownTimer = new CountDownTimer(30000, 1000) {

            // Đếm ngược thời gian
            @Override
            public void onTick(long millisUntilFinished) {
                binding.tvResend.setText("Resend in 00:" + (millisUntilFinished / 1000));
            }

            // Kết thúc đếm ngược
            @Override
            public void onFinish() {
                binding.tvResend.setEnabled(true);
                binding.tvResend.setTextColor(getResources().getColor(R.color.primary, null));
                binding.tvResend.setText("Resend Now");
            }
        }.start();
    }



    @Override
    public void onDestroyView() {
        binding = null;
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroyView();
    }
}
