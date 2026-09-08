package com.example.bookify.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.example.bookify.R;
import com.example.bookify.ui.alerts.NotificationsActivity;
import com.example.bookify.ui.home.HomeActivity;
import com.example.bookify.ui.profile.ProfileActivity;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class BookifyBottomNavHelper {

    public enum NavTab {
        HOME,
        EXPLORE,
        SHELF,
        ALERTS,
        PROFILE
    }

    private static float dpToPx(Context context, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    private static boolean isNavigating = false;

    // Bottom Navigation Setup
    public static void setup(Activity activity, NavTab currentTab) {
        if (activity == null) return;
        isNavigating = false;

        BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
        if (bottomNav == null) return;

        bottomNav.setClipChildren(false);
        bottomNav.setClipToPadding(false);
        if (bottomNav.getChildAt(0) instanceof ViewGroup) {
            ViewGroup menuView = (ViewGroup) bottomNav.getChildAt(0);
            menuView.setClipChildren(false);
            menuView.setClipToPadding(false);
        }

        // Notification Badge
        setupAlertBadge(activity, bottomNav);

        int targetItemId = getMenuIdForTab(currentTab);
        if (targetItemId != 0) {
            bottomNav.setSelectedItemId(targetItemId);
        }
        final BottomNavigationView finalNav = bottomNav;
        applyItemElevation(finalNav, targetItemId, false);
        bottomNav.post(() -> applyItemElevation(finalNav, targetItemId, false));

        // Shelf Button
        FloatingActionButton fabShelf = activity.findViewById(R.id.fab_nav_shelf);
        if (fabShelf != null) {
            fabShelf.setOnClickListener(v -> {
                animateShelf(activity, fabShelf);
                Toast.makeText(activity, "Shelf feature coming soon!", Toast.LENGTH_SHORT).show();
            });
        }

        // Tab Selection
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                if (currentTab == NavTab.HOME) {
                    bounceActiveItem(finalNav, itemId);
                    View rvFeed = activity.findViewById(R.id.rvFeed);
                    if (rvFeed instanceof androidx.recyclerview.widget.RecyclerView) {
                        ((androidx.recyclerview.widget.RecyclerView) rvFeed).smoothScrollToPosition(0);
                    }
                    return true;
                } else {
                    if (isNavigating) return false;
                    isNavigating = true;
                    applyItemElevation(finalNav, itemId, true);
                    finalNav.postDelayed(() -> {
                        Intent intent = new Intent(activity, HomeActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                        activity.finish();
                    }, 120);
                    return true;
                }
            } else if (itemId == R.id.nav_explore) {
                View exploreView = finalNav.findViewById(R.id.nav_explore);
                if (exploreView != null) {
                    exploreView.animate().cancel();
                    exploreView.animate()
                        .translationY(-dpToPx(activity, 4f))
                        .setDuration(90)
                        .setInterpolator(new DecelerateInterpolator())
                        .withEndAction(() -> exploreView.animate()
                            .translationY(0f)
                            .setDuration(90)
                            .setInterpolator(new DecelerateInterpolator())
                            .start())
                        .start();
                }
                Toast.makeText(activity, "Explore feature coming soon!", Toast.LENGTH_SHORT).show();
                return false;
            } else if (itemId == R.id.nav_alerts) {
                if (currentTab == NavTab.ALERTS) {
                    bounceActiveItem(finalNav, itemId);
                    View scroll = activity.findViewById(R.id.scrollViewContent);
                    if (scroll != null) scroll.scrollTo(0, 0);
                    return true;
                } else {
                    if (isNavigating) return false;
                    isNavigating = true;
                    applyItemElevation(finalNav, itemId, true);
                    finalNav.postDelayed(() -> {
                        Intent intent = new Intent(activity, NotificationsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                        activity.finish();
                    }, 120);
                    return true;
                }
            } else if (itemId == R.id.nav_profile) {
                if (currentTab == NavTab.PROFILE) {
                    bounceActiveItem(finalNav, itemId);
                    View scroll = activity.findViewById(R.id.scroll_view_content);
                    if (scroll != null) scroll.scrollTo(0, 0);
                    return true;
                } else {
                    if (isNavigating) return false;
                    isNavigating = true;
                    applyItemElevation(finalNav, itemId, true);
                    finalNav.postDelayed(() -> {
                        Intent intent = new Intent(activity, ProfileActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        activity.startActivity(intent);
                        activity.overridePendingTransition(0, 0);
                        activity.finish();
                    }, 120);
                    return true;
                }
            }
            return false;
        });
    }

    private static int getMenuIdForTab(NavTab tab) {
        if (tab == null) return 0;
        switch (tab) {
            case HOME:
                return R.id.nav_home;
            case EXPLORE:
                return R.id.nav_explore;
            case ALERTS:
                return R.id.nav_alerts;
            case PROFILE:
                return R.id.nav_profile;
            default:
                return 0;
        }
    }

    // Item Elevation Animation
    private static void applyItemElevation(BottomNavigationView bottomNav, int selectedItemId, boolean animate) {
        if (bottomNav == null) return;
        Context context = bottomNav.getContext();
        float elevatedY = -dpToPx(context, 7f);

        int[] itemIds = { R.id.nav_home, R.id.nav_explore, R.id.nav_alerts, R.id.nav_profile };
        for (int id : itemIds) {
            View itemView = bottomNav.findViewById(id);
            if (itemView == null) continue;

            if (itemView.getParent() instanceof ViewGroup) {
                ((ViewGroup) itemView.getParent()).setClipChildren(false);
                ((ViewGroup) itemView.getParent()).setClipToPadding(false);
            }

            boolean isSelected = (id == selectedItemId);
            float targetY = isSelected ? elevatedY : 0f;

            if (animate) {
                if (Math.abs(itemView.getTranslationY() - targetY) > 0.5f) {
                    itemView.animate().cancel();
                    itemView.animate()
                        .translationY(targetY)
                        .setDuration(120)
                        .setInterpolator(new DecelerateInterpolator())
                        .withLayer()
                        .start();
                }
            } else {
                itemView.animate().cancel();
                itemView.setTranslationY(targetY);
            }
        }
    }

    // Active Item Bounce Animation
    private static void bounceActiveItem(BottomNavigationView bottomNav, int itemId) {
        if (bottomNav == null) return;
        View itemView = bottomNav.findViewById(itemId);
        if (itemView == null) return;

        itemView.animate().cancel();
        itemView.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(80)
            .setInterpolator(new DecelerateInterpolator())
            .withLayer()
            .withEndAction(() -> itemView.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(100)
                .setInterpolator(new DecelerateInterpolator())
                .withLayer()
                .start())
            .start();
    }

    // Shelf Button Animation
    private static void animateShelf(Context context, View shelfFab) {
        if (shelfFab == null) return;
        shelfFab.animate().cancel();
        float popY = -dpToPx(context, 5f);
        shelfFab.animate()
            .scaleX(1.10f)
            .scaleY(1.10f)
            .translationY(popY)
            .setDuration(120)
            .setInterpolator(new DecelerateInterpolator())
            .withEndAction(() -> shelfFab.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .translationY(0f)
                .setDuration(120)
                .setInterpolator(new DecelerateInterpolator())
                .start())
            .start();
    }

    // Alert Badge
    public static void setupAlertBadge(Activity activity, BottomNavigationView bottomNav) {
        if (bottomNav == null || activity == null) return;
        try {
            BadgeDrawable badge = bottomNav.getOrCreateBadge(R.id.nav_alerts);
            badge.setBackgroundColor(ContextCompat.getColor(activity, R.color.badge_error));
            badge.setVisible(true);
            badge.clearNumber();
            badge.setVerticalOffset((int) dpToPx(activity, 4f));
            badge.setHorizontalOffset((int) dpToPx(activity, 4f));
        } catch (Exception ignored) {
        }
    }

    public static void setAlertBadgeVisible(Activity activity, boolean visible) {
        if (activity == null) return;
        BottomNavigationView bottomNav = activity.findViewById(R.id.bottom_navigation);
        if (bottomNav != null) {
            try {
                BadgeDrawable badge = bottomNav.getBadge(R.id.nav_alerts);
                if (badge != null) {
                    badge.setVisible(visible);
                }
            } catch (Exception ignored) {}
        }
    }
}
