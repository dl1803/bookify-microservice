package com.example.bookify.ui.auth;

import android.os.Bundle;
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
import com.example.bookify.databinding.FragmentRegisterBinding;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterFragment extends Fragment {
    private AuthViewModel  authViewModel;
    private FragmentRegisterBinding binding;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
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
        binding.btnRegister.setOnClickListener(v -> {
            String username = binding.etUsername.getText().toString();
            String email = binding.etEmail.getText().toString();
            String firstName = binding.etFirstName.getText().toString();
            String lastName = binding.etLastName.getText().toString();
            String dob = binding.etDob.getText().toString();
            String city = binding.etCity.getText().toString();
            String password = binding.etPassword.getText().toString();
            String confirmPassword = binding.etConfirmPassword.getText().toString();
            boolean isTermsAccepted = binding.cbTerms.isChecked();

            authViewModel.register(username, email, firstName, lastName, dob, city, password, confirmPassword, isTermsAccepted);
        });

        // Chọn ngày sinh
        binding.etDob.setOnClickListener(v -> {
           showDatePickerDialog();
        });

        // Chuyển sang trang Verify OTP
        binding.tvGoToLogin.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_registerFragment_to_loginFragment);
        });
    }

    private void showDatePickerDialog() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder
                .datePicker()
                .setTitleText("Chọn ngày sinh")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            // Đổi long milisecond sang chuỗi ngày tháng năm : dd/MM/yyyy
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
            String formattedDate = simpleDateFormat.format(selection);
            binding.etDob.setText(formattedDate);
        });

        datePicker.show(getParentFragmentManager(), "DATE_PICKER");
    }

    private void observeViewModel(){
        // Loading
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnRegister.setEnabled(!isLoading);
            binding.btnRegister.setText(isLoading? "Đang đăng ký..." : "Đăng ký");
        });

        authViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show();
                binding.tilUsername.setError(errorMessage);
            } else {
                clearAllErrors();
            }
        });

        authViewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), isSucess -> {
            if (isSucess){
                authViewModel.resetRegisterState();
                clearAllErrors();
                Toast.makeText(requireContext(), "Registration successful! Please verify your email.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_otpVerifyFragment);
            }
        });

        android.text.TextWatcher clearWatcher = new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { clearAllErrors(); }
            @Override public void afterTextChanged(android.text.Editable s) {}
        };
        binding.etUsername.addTextChangedListener(clearWatcher);
        binding.etEmail.addTextChangedListener(clearWatcher);
        binding.etPassword.addTextChangedListener(clearWatcher);
        binding.etConfirmPassword.addTextChangedListener(clearWatcher);
    }

    private void clearAllErrors() {
        binding.tilUsername.setError(null);
        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
