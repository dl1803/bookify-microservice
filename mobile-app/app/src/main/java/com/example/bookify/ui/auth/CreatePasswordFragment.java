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
import com.example.bookify.databinding.FragmentCreatePasswordBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class CreatePasswordFragment extends Fragment {

    private AuthViewModel authViewModel;
    private FragmentCreatePasswordBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCreatePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        setupClickListeners();
        observeViewModel();
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> Navigation.findNavController(v).popBackStack());

        binding.btnSubmit.setOnClickListener(v -> {
            String newPassword = binding.etNewPassword.getText() != null ? binding.etNewPassword.getText().toString() : "";
            String confirmPassword = binding.etConfirmPassword.getText() != null ? binding.etConfirmPassword.getText().toString() : "";
            authViewModel.resetPassword(newPassword, confirmPassword);
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnSubmit.setEnabled(!isLoading);
            binding.btnSubmit.setText(isLoading ? "Saving..." : "Save New Password");
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        authViewModel.getResetPasswordSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                // Reset cờ tránh lỗi
                authViewModel.resetResetPasswordState();
                Toast.makeText(requireContext(), "Mật khẩu đã được đổi thành công!", Toast.LENGTH_SHORT).show();

                // Trở về trang Đăng nhập
                Navigation.findNavController(requireView()).navigate(R.id.action_createPasswordFragment_to_loginFragment);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
