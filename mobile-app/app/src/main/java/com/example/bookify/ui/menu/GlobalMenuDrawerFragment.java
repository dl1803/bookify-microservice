package com.example.bookify.ui.menu;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.bookify.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GlobalMenuDrawerFragment extends DialogFragment {

    private GlobalMenuViewModel viewModel;

    public static GlobalMenuDrawerFragment newInstance() {
        return new GlobalMenuDrawerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Translucent_NoTitleBar);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_global_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(GlobalMenuViewModel.class);
        
        setupClickListeners(view);
        observeViewModel(view);
        
        viewModel.fetchProfile();
    }

    @android.annotation.SuppressLint("CheckResult")
    private void observeViewModel(View view) {
        viewModel.getProfileData().observe(getViewLifecycleOwner(), profile -> {
            if (profile != null) {
                TextView txtMenuUserName = view.findViewById(R.id.txtMenuUserName);
                TextView txtMenuEmail = view.findViewById(R.id.txtMenuEmail);
                com.google.android.material.imageview.ShapeableImageView imgMenuAvatar = view.findViewById(R.id.imgMenuAvatar);

                // Null-safe: ghép tên tránh "null" khi lastName chưa có
                String firstName = profile.getFirstName() != null ? profile.getFirstName().trim() : "";
                String lastName = profile.getLastName() != null ? profile.getLastName().trim() : "";
                String fullName = (firstName + " " + lastName).trim();
                txtMenuUserName.setText(fullName.isEmpty() ? profile.getUsername() : fullName);

                // Hiện email bên dưới tên (chủ yếu cho Google login)
                String email = profile.getEmail();
                if (email != null && !email.trim().isEmpty()) {
                    txtMenuEmail.setText(email.trim());
                    txtMenuEmail.setVisibility(android.view.View.VISIBLE);
                } else {
                    txtMenuEmail.setVisibility(android.view.View.GONE);
                }

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    com.bumptech.glide.Glide.with(this)
                            .load(com.example.bookify.utils.UrlUtils.resolveLocalUrl(profile.getAvatar()))
                            .placeholder(R.drawable.ic_menu_avatar_placeholder)
                            .error(R.drawable.ic_menu_avatar_placeholder)
                            .into(imgMenuAvatar);
                }
            }
        });


        viewModel.getLogoutSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                // Reset ngay để tránh re-trigger nếu fragment được tái sử dụng
                viewModel.resetLogoutState();
                if (getActivity() != null) {
                    showToast(getString(R.string.menu_logout) + " successful");
                    android.content.Intent intent = new android.content.Intent(getActivity(), com.example.bookify.ui.auth.AuthActivity.class);
                    intent.setFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    getActivity().finish();
                }
                dismiss();
            }
        });
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btnMenuMessages).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            if (getActivity() != null) {
                android.content.Intent intent = new android.content.Intent(getActivity(), com.example.bookify.ui.messages.MessagesActivity.class);
                startActivity(intent);
            }
            dismiss();
        });

        view.findViewById(R.id.btnMenuFriends).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showToast(getString(R.string.menu_friends));
            dismiss();
        });

        view.findViewById(R.id.btnMenuGroups).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showToast(getString(R.string.menu_groups));
            dismiss();
        });

        view.findViewById(R.id.btnMenuSettings).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            showToast(getString(R.string.menu_account_settings));
            dismiss();
        });

        view.findViewById(R.id.btnMenuLogout).setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            viewModel.logout();
        });
        
        view.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null && dialog.getWindow() != null) {
            dialog.setCanceledOnTouchOutside(true); 
            
            Window window = dialog.getWindow();
            
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            
            window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            window.setDimAmount(0.6f); 
            
            androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);

            window.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
            window.setGravity(Gravity.START | Gravity.TOP);
            
            window.setWindowAnimations(R.style.DialogAnimationSlideLeft);
        }
    }
}
