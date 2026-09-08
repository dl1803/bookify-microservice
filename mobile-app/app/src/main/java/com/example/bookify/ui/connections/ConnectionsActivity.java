package com.example.bookify.ui.connections;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.ui.connections.adapter.ConnectionsAdapter;
import com.example.bookify.ui.connections.model.ConnectionUserModel;
import com.example.bookify.ui.messages.ChatActivity;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

public class ConnectionsActivity extends AppCompatActivity {

    private TabLayout tabLayoutConnections;
    private TextView tvSectionTitle;
    private TextView tvSectionAction;
    private RecyclerView recyclerConnections;
    private View layoutEmptyState;
    private EditText etSearch;
    private ImageView btnBack;

    private ConnectionsAdapter adapter;
    private final List<ConnectionUserModel> friendsList = new ArrayList<>();
    private final List<ConnectionUserModel> requestsList = new ArrayList<>();
    private final List<ConnectionUserModel> sentList = new ArrayList<>();
    private int currentTab = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_connections);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_connections_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        initMockData();
        setupRecyclerView();
        setupTabs();
        setupSearch();
    }

    private void initViews() {
        btnBack = findViewById(R.id.btn_back);
        tabLayoutConnections = findViewById(R.id.tab_layout_connections);
        tvSectionTitle = findViewById(R.id.tv_section_title);
        tvSectionAction = findViewById(R.id.tv_section_action);
        recyclerConnections = findViewById(R.id.recycler_connections);
        layoutEmptyState = findViewById(R.id.layout_empty_state);
        etSearch = findViewById(R.id.et_search);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }

        if (tvSectionAction != null) {
            tvSectionAction.setOnClickListener(v -> {
                if (currentTab == 1) {
                    // Accept All in Requests
                    Toast.makeText(this, "Accepted all requests!", Toast.LENGTH_SHORT).show();
                    requestsList.clear();
                    adapter.updateList(requestsList);
                    updateSectionHeader();
                }
            });
        }
    }

    private void initMockData() {
        // Tab 0: Your Friends
        friendsList.clear();
        friendsList.add(new ConnectionUserModel("Eleanor Vance", "", R.drawable.profile_avatar, ConnectionUserModel.TabType.YOUR_FRIENDS, "Message", false));
        friendsList.add(new ConnectionUserModel("Arthur Pendelton", "", R.drawable.profile_avatar, ConnectionUserModel.TabType.YOUR_FRIENDS, "Message", false));
        friendsList.add(new ConnectionUserModel("Marcus Thorne", "", R.drawable.profile_avatar, ConnectionUserModel.TabType.YOUR_FRIENDS, "Message", false));
        friendsList.add(new ConnectionUserModel("Sarah Jenkins", "", R.drawable.profile_avatar, ConnectionUserModel.TabType.YOUR_FRIENDS, "Message", false));

        // Tab 1: Requests
        requestsList.clear();
        requestsList.add(new ConnectionUserModel("Clara Bow", "4 mutual friends • Sent 2d ago", R.drawable.profile_avatar, ConnectionUserModel.TabType.REQUESTS, "Confirm", true));
        requestsList.add(new ConnectionUserModel("Henry James", "1 mutual friend • Sent 3d ago", R.drawable.profile_avatar, ConnectionUserModel.TabType.REQUESTS, "Confirm", true));
        requestsList.add(new ConnectionUserModel("Emily Bronte", "2 mutual friends • Sent 5d ago", R.drawable.profile_avatar, ConnectionUserModel.TabType.REQUESTS, "Confirm", true));

        // Tab 2: Sent
        sentList.clear();
        sentList.add(new ConnectionUserModel("George Eliot", "Sent 1d ago", R.drawable.profile_avatar, ConnectionUserModel.TabType.SENT, "Cancel", false));
        sentList.add(new ConnectionUserModel("Virginia Woolf", "Sent 4d ago", R.drawable.profile_avatar, ConnectionUserModel.TabType.SENT, "Cancel", false));
    }

    private void setupRecyclerView() {
        recyclerConnections.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ConnectionsAdapter(friendsList);
        adapter.setOnConnectionActionListener(new ConnectionsAdapter.OnConnectionActionListener() {
            @Override
            public void onPrimaryActionClick(ConnectionUserModel user, int position) {
                if (user.getTabType() == ConnectionUserModel.TabType.YOUR_FRIENDS) {
                    Intent chatIntent = new Intent(ConnectionsActivity.this, ChatActivity.class);
                    chatIntent.putExtra("chat_title", user.getName());
                    startActivity(chatIntent);
                } else if (user.getTabType() == ConnectionUserModel.TabType.REQUESTS) {
                    Toast.makeText(ConnectionsActivity.this, "Accepted friend request from " + user.getName(), Toast.LENGTH_SHORT).show();
                    requestsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateSectionHeader();
                } else {
                    Toast.makeText(ConnectionsActivity.this, "Canceled friend request to " + user.getName(), Toast.LENGTH_SHORT).show();
                    sentList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateSectionHeader();
                }
            }

            @Override
            public void onSecondaryActionClick(ConnectionUserModel user, int position) {
                if (user.getTabType() == ConnectionUserModel.TabType.REQUESTS) {
                    Toast.makeText(ConnectionsActivity.this, "Deleted request from " + user.getName(), Toast.LENGTH_SHORT).show();
                    requestsList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateSectionHeader();
                }
            }
        });
        recyclerConnections.setAdapter(adapter);
    }

    private void setupTabs() {
        if (tabLayoutConnections == null) return;

        tabLayoutConnections.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                switchTabContent(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        switchTabContent(0);
    }

    private void switchTabContent(int tabIndex) {
        if (etSearch != null) etSearch.setText("");

        if (tabIndex == 0) {
            adapter.updateList(friendsList);
        } else if (tabIndex == 1) {
            adapter.updateList(requestsList);
        } else {
            adapter.updateList(sentList);
        }

        updateSectionHeader();
    }

    private void updateSectionHeader() {
        if (currentTab == 0) {
            tvSectionTitle.setText("ALL FRIENDS (" + friendsList.size() + ")");
            tvSectionAction.setVisibility(View.GONE);
            layoutEmptyState.setVisibility(friendsList.isEmpty() ? View.VISIBLE : View.GONE);
        } else if (currentTab == 1) {
            tvSectionTitle.setText("RECEIVED REQUESTS (" + requestsList.size() + ")");
            tvSectionAction.setVisibility(requestsList.isEmpty() ? View.GONE : View.VISIBLE);
            tvSectionAction.setText("Accept All");
            layoutEmptyState.setVisibility(requestsList.isEmpty() ? View.VISIBLE : View.GONE);
        } else {
            tvSectionTitle.setText("PENDING REQUESTS (" + sentList.size() + ")");
            tvSectionAction.setVisibility(View.VISIBLE);
            tvSectionAction.setText("Auto-expires in 30d");
            layoutEmptyState.setVisibility(sentList.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private void setupSearch() {
        if (etSearch == null) return;

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterConnections(s.toString().trim());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterConnections(String query) {
        List<ConnectionUserModel> activeList = (currentTab == 0) ? friendsList : (currentTab == 1) ? requestsList : sentList;
        if (query.isEmpty()) {
            adapter.updateList(activeList);
            layoutEmptyState.setVisibility(activeList.isEmpty() ? View.VISIBLE : View.GONE);
            return;
        }

        List<ConnectionUserModel> filtered = new ArrayList<>();
        for (ConnectionUserModel item : activeList) {
            if (item.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(item);
            }
        }
        adapter.updateList(filtered);
        layoutEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
