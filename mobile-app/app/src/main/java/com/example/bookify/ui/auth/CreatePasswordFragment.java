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
                binding.tilConfirmPassword.setError(error);
                binding.tilNewPassword.setError(" ");
            } else {
                binding.tilNewPassword.setError(null);
                binding.tilConfirmPassword.setError(null);
            }
        });

        authViewModel.getResetPasswordSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                authViewModel.resetResetPasswordState();
                binding.tilNewPassword.setError(null);
                binding.tilConfirmPassword.setError(null);
                Toast.makeText(requireContext(), "Password changed successfully! Please login.", Toast.LENGTH_SHORT).show();

                Navigation.findNavController(requireView()).navigate(R.id.action_createPasswordFragment_to_loginFragment);
            }
        });

        android.text.TextWatcher clearErrors = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tilNewPassword.setError(null);
                binding.tilConfirmPassword.setError(null);
            }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        binding.etNewPassword.addTextChangedListener(clearErrors);
        binding.etConfirmPassword.addTextChangedListener(clearErrors);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
