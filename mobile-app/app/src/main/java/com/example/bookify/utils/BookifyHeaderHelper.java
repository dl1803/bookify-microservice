package com.example.bookify.utils;

import android.view.View;
import android.widget.ImageView;

import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bookify.R;
import com.example.bookify.ui.menu.GlobalMenuDrawerFragment;

public class BookifyHeaderHelper {

    // Header Setup
    public static void setup(AppCompatActivity activity) {
        setup(activity, R.drawable.ic_menu, v -> {
            GlobalMenuDrawerFragment.newInstance()
                    .show(activity.getSupportFragmentManager(), "GlobalMenu");
        });
    }

    // Header Setup with Left Action
    public static void setup(
            AppCompatActivity activity,
            @DrawableRes int leftIconRes,
            View.OnClickListener onLeftClick) {
        if (activity == null) return;

        ImageView btnLeft = activity.findViewById(R.id.btn_header_left);
        if (btnLeft != null) {
            btnLeft.setImageResource(leftIconRes);
            btnLeft.setVisibility(View.VISIBLE);
            if (onLeftClick != null) {
                btnLeft.setOnClickListener(onLeftClick);
            } else {
                btnLeft.setOnClickListener(v -> {
                    GlobalMenuDrawerFragment.newInstance()
                            .show(activity.getSupportFragmentManager(), "GlobalMenu");
                });
            }
        }
    }

    // Left Action Configuration
    public static void setLeftAction(AppCompatActivity activity, @DrawableRes int iconRes, View.OnClickListener listener) {
        if (activity == null) return;
        ImageView btnLeft = activity.findViewById(R.id.btn_header_left);
        if (btnLeft != null) {
            btnLeft.setImageResource(iconRes);
            btnLeft.setVisibility(View.VISIBLE);
            btnLeft.setOnClickListener(listener);
        }
    }
}
