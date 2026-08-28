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

    private void observeViewModel() {
        //  Loading
        authViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.btnRegister.setEnabled(!isLoading);
            binding.btnRegister.setText(isLoading ? "Registering..." : "Register");
        });

        //  Field Errors
        authViewModel.getRegUsernameError().observe(getViewLifecycleOwner(), error -> binding.tilUsername.setError(error));
        authViewModel.getRegEmailError().observe(getViewLifecycleOwner(), error -> binding.tilEmail.setError(error));
        authViewModel.getRegFirstNameError().observe(getViewLifecycleOwner(), error -> binding.tilFirstName.setError(error));
        authViewModel.getRegLastNameError().observe(getViewLifecycleOwner(), error -> binding.tilLastName.setError(error));
        authViewModel.getRegDobError().observe(getViewLifecycleOwner(), error -> binding.tilDob.setError(error));
        authViewModel.getRegCityError().observe(getViewLifecycleOwner(), error -> binding.tilCity.setError(error));
        authViewModel.getRegPasswordError().observe(getViewLifecycleOwner(), error -> binding.tilPassword.setError(error));
        authViewModel.getRegConfirmPasswordError().observe(getViewLifecycleOwner(), error -> binding.tilConfirmPassword.setError(error));

        //  API Error (Toast thông báo hệ thống / kết nối)
        authViewModel.getApiError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        //  Success State
        authViewModel.getRegisterSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            if (isSuccess) {
                authViewModel.resetRegisterState();
                clearAllErrors();
                Toast.makeText(requireContext(), "Registration successful! Please verify your email.", Toast.LENGTH_SHORT).show();
                Navigation.findNavController(requireView()).navigate(R.id.action_registerFragment_to_otpVerifyFragment);
            }
        });

        //  TextWatchers xóa lỗi từng ô khi người dùng gõ
        binding.etUsername.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilUsername.setError(null)));
        binding.etEmail.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilEmail.setError(null)));
        binding.etFirstName.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilFirstName.setError(null)));
        binding.etLastName.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilLastName.setError(null)));
        binding.etDob.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilDob.setError(null)));
        binding.etCity.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilCity.setError(null)));
        binding.etPassword.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilPassword.setError(null)));
        binding.etConfirmPassword.addTextChangedListener(new SimpleTextWatcher(() -> binding.tilConfirmPassword.setError(null)));
    }

    private void clearAllErrors() {
        binding.tilUsername.setError(null);
        binding.tilFirstName.setError(null);
        binding.tilLastName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilDob.setError(null);
        binding.tilCity.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    private static class SimpleTextWatcher implements android.text.TextWatcher {
        private final Runnable onTextChangedRunnable;
        public SimpleTextWatcher(Runnable runnable) { this.onTextChangedRunnable = runnable; }
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (onTextChangedRunnable != null) onTextChangedRunnable.run();
        }
        @Override public void afterTextChanged(android.text.Editable s) {}
    }

    @Override
    public void onDestroyView() {
        binding = null;
        super.onDestroyView();
    }
}
