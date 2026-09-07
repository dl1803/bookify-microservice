package com.example.bookify.ui.alerts;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.data.model.NotificationModel;
import com.example.bookify.ui.alerts.adapter.NotificationAdapter;
import com.example.bookify.ui.home.HomeActivity;
import com.example.bookify.ui.menu.GlobalMenuDrawerFragment;
import com.example.bookify.ui.profile.ProfileActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class NotificationsActivity extends AppCompatActivity {

    private RecyclerView rvNotifications;
    private NotificationAdapter adapter;
    private NestedScrollView scrollViewContent;
    private List<NotificationModel> notificationList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_notifications);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_alerts_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        initViews();
        setupTopAppBar();
        setupRecyclerView();
        setupBottomNavigationAndFab();
    }

    private void initViews() {
        rvNotifications = findViewById(R.id.rvNotifications);
        scrollViewContent = findViewById(R.id.scrollViewContent);
    }

    private void setupTopAppBar() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> {
                GlobalMenuDrawerFragment.newInstance()
                        .show(getSupportFragmentManager(), "GlobalMenu");
            });
        }
    }

    private void setupRecyclerView() {
        notificationList = generateMockNotifications();
        adapter = new NotificationAdapter(notificationList);

        adapter.setOnNotificationClickListener(new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(NotificationModel item, int position) {
                Toast.makeText(NotificationsActivity.this, "Opened notification: " + item.getUserName(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNotificationLongClick(NotificationModel item, int position) {
                Toast.makeText(NotificationsActivity.this, "Notification Options for " + item.getUserName(),
                        Toast.LENGTH_SHORT).show();
            }
        });

        rvNotifications.setLayoutManager(new LinearLayoutManager(this));
        rvNotifications.setAdapter(adapter);
    }

    private List<NotificationModel> generateMockNotifications() {
        List<NotificationModel> list = new ArrayList<>();

        list.add(new NotificationModel("TODAY"));
        list.add(new NotificationModel(
                "n1",
                "Alex Rivera",
                "<b>Alex Rivera</b> liked your review of <font color='#93452D'><i>The Secret History</i></font>.",
                "2h ago",
                R.drawable.profile_avatar,
                NotificationModel.NotificationType.LIKE,
                true,
                false));

        list.add(new NotificationModel(
                "n2",
                "Sarah Jenkins",
                "<b>Sarah Jenkins</b> started following you.",
                "4h ago",
                R.drawable.profile_avatar,
                NotificationModel.NotificationType.FOLLOW,
                true,
                false));

        list.add(new NotificationModel(
                "n3",
                "Bookify Goal",
                "Your reading goal for this month is 80% complete! Keep going.",
                "5h ago",
                R.drawable.ic_book,
                NotificationModel.NotificationType.GOAL,
                false,
                false));

        list.add(new NotificationModel("YESTERDAY"));
        list.add(new NotificationModel(
                "n4",
                "David Chen",
                "<b>David Chen</b> commented on your post: \"I completely agree with your take on chapter 4!\"",
                "1d ago",
                R.drawable.profile_avatar,
                NotificationModel.NotificationType.COMMENT,
                false,
                false));

        list.add(new NotificationModel(
                "n5",
                "Classic Literature Club",
                "New discussion added in <b><font color='#74554B'>Classic Literature Club</font></b>.",
                "1d ago",
                R.drawable.book_cover_1,
                NotificationModel.NotificationType.CLUB,
                false,
                false));

        return list;
    }

    private void setupBottomNavigationAndFab() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_alerts);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(NotificationsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_alerts) {
                    if (scrollViewContent != null) {
                        scrollViewContent.smoothScrollTo(0, 0);
                    }
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    Toast.makeText(this, "Explore feature coming soon!", Toast.LENGTH_SHORT).show();
                    return false;
                }
                return false;
            });
        }

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> {
                Intent intent = new Intent(NotificationsActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            });
        }
    }
}
