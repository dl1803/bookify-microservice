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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.bookify.R;
import com.example.bookify.databinding.FragmentVerifyResetPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class VerifyResetPasswordFragment extends Fragment {
    private AuthViewModel authViewModel;
    private FragmentVerifyResetPasswordBinding binding;
    private CountDownTimer countDownTimer;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVerifyResetPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
        binding.tvSubtitle.setText("We sent a 4-digit verification code to " + authViewModel.getRegisterEmail() + " to authorize your password change. Please enter it below.");
        setupClickListeners();
        setupObservers();

        // Tự động focus vào ô OTP và mở bàn phím
        binding.pinViewOtp.post(() -> {
            if (binding != null && isAdded()) {
                binding.pinViewOtp.requestFocus();
                android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) requireContext().getSystemService(android.content.Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.showSoftInput(binding.pinViewOtp, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        startResendTimer();
    }
    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.btnVerify.setOnClickListener(v -> {
            String otp = binding.pinViewOtp.getText() != null ? binding.pinViewOtp.getText().toString() : "";
            if (otp.length() == 4) {
                authViewModel.verifyResetOtp(otp);
            } else {
                Toast.makeText(requireContext(), "Please enter all 4 OTP numbers", Toast.LENGTH_SHORT).show();
            }
        });
        binding.tvResend.setOnClickListener(v -> {
            authViewModel.forgotPassword(authViewModel.getRegisterEmail());
            // startResendTimer() and Toast will be handled in observer
        });
    }

    private void setupObservers() {
        authViewModel.getVerifyResetOtpSuccess().observe(
                getViewLifecycleOwner(), isSuccess -> {
                    if (isSuccess) {
                        authViewModel.resetVerifyResetOtpState();
                        Navigation.findNavController(requireView()).navigate(R.id.action_verifyResetPasswordFragment_to_createPasswordFragment);
                    }
                }
        );

        authViewModel.getOtpError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnVerify.setEnabled(!isLoading);
            binding.btnVerify.setText(isLoading ? "Verifying..." : "Verify");
        });

        authViewModel.getForgotPasswordSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                authViewModel.resetForgotPasswordState();
                startResendTimer();
                Toast.makeText(requireContext(), "OTP sent successfully!", Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getForgotEmailError().observe(getViewLifecycleOwner(), errorMsg -> {
            if (errorMsg != null && !errorMsg.isEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void startResendTimer() {
        if (binding == null || !isAdded()) return;
        binding.tvResend.setEnabled(false);
        binding.tvResend.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.text_disabled));

        if (countDownTimer != null) countDownTimer.cancel();
        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (binding != null && isAdded()) {
                    binding.tvResend.setText(String.format("Resend in 00:%02d", millisUntilFinished / 1000));
                }
            }
            @Override
            public void onFinish() {
                if (binding != null && isAdded()) {
                    binding.tvResend.setEnabled(true);
                    binding.tvResend.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.primary));
                    binding.tvResend.setText("Resend Now");
                }
            }
        }.start();
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (countDownTimer != null) countDownTimer.cancel();
        binding = null;
    }
}