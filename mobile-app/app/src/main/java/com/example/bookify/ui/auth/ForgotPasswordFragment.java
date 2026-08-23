package com.example.bookify.ui.auth;

import android.os.Bundle;
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
import com.example.bookify.databinding.FragmentForgotPasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ForgotPasswordFragment extends Fragment {
    private AuthViewModel authViewModel;
    private FragmentForgotPasswordBinding binding;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();

    }

    private void setupClickListeners() {
        binding.btnSubmit.setOnClickListener(v -> {
            String email = binding.etEmail.getText() != null ? binding.etEmail.getText().toString() : "";
            authViewModel.forgotPassword(email);
        });
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
        binding.btnReturnToLogin.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());
    }
    private void observeViewModel() {
        // Đang xử lý API
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSubmit.setEnabled(!isLoading);
            binding.btnSubmit.setText(isLoading ? "Sending..." : "Send Reset OTP");
        });
        // Báo lỗi
        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
        authViewModel.getForgotPasswordSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                authViewModel.resetForgotPasswordState();

                Toast.makeText(requireContext(), "Reset OTP sent successfully!", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(requireView()).navigate(R.id.action_forgotPasswordFragment_to_verifyResetPasswordFragment);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
