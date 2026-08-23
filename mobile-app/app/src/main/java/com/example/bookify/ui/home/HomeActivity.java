package com.example.bookify.ui.home;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.data.model.PostModel;
import com.example.bookify.ui.alerts.NotificationsActivity;
import com.example.bookify.ui.home.adapter.PostAdapter;
import com.example.bookify.ui.profile.ProfileActivity;
import com.example.bookify.utils.BookifyDialogHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class HomeActivity extends AppCompatActivity {

    @Inject
    com.example.bookify.data.repository.AuthRepository authRepository;

    private HomeViewModel viewModel;
    private PostAdapter adapter;
    private RecyclerView rvFeed;
    private EditText etPostInput;

    private String selectedBookTitle = "The Silent Echo";
    private String selectedBookAuthor = "Arthur Pendelton";
    private int selectedBookCoverResId = R.drawable.book_cover_1;

    private ActivityResultLauncher<String> imagePickerLauncher;
    private ActivityResultLauncher<String> pdfPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_home_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        setupPickers();
        setupTopAppBar();
        setupCreatePostSection();
        setupRecyclerView();
        setupViewModel();
        setupBottomNavigationAndFab();
    }

    private void setupPickers() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String fileName = uri.getLastPathSegment();
                        Toast.makeText(this, "Selected Image: " + (fileName != null ? fileName : "photo.jpg"), Toast.LENGTH_SHORT).show();
                    }
                }
        );

        pdfPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        String fileName = uri.getLastPathSegment();
                        selectedBookTitle = fileName != null ? fileName : "Uploaded_Book.pdf";
                        selectedBookAuthor = "PDF Document";
                        Toast.makeText(this, "Attached PDF Book: " + selectedBookTitle, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void setupTopAppBar() {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar != null) {
            topAppBar.setNavigationOnClickListener(v -> {
                com.example.bookify.ui.menu.GlobalMenuDrawerFragment.newInstance()
                        .show(getSupportFragmentManager(), "GlobalMenu");
            });
        }
    }

    private void performLogout() {
        authRepository.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(HomeActivity.this, com.example.bookify.ui.auth.AuthViewModel.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupCreatePostSection() {
        etPostInput = findViewById(R.id.etPostInput);

        findViewById(R.id.btnAttachImage).setOnClickListener(v -> {
            try {
                imagePickerLauncher.launch("image/*");
            } catch (Exception e) {
                BookifyDialogHelper.showAttachBookDialog(this, (title, author, coverResId) -> {
                    selectedBookTitle = title;
                    selectedBookAuthor = author;
                    selectedBookCoverResId = coverResId;
                });
            }
        });

        findViewById(R.id.btnAttachBook).setOnClickListener(v -> {
            try {
                pdfPickerLauncher.launch("application/pdf");
            } catch (Exception e) {
                BookifyDialogHelper.showAttachBookDialog(this, (title, author, coverResId) -> {
                    selectedBookTitle = title;
                    selectedBookAuthor = author;
                    selectedBookCoverResId = coverResId;
                });
            }
        });

        findViewById(R.id.btnPost).setOnClickListener(v -> {
            if (etPostInput == null) return;
            String content = etPostInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter your thoughts before posting!", Toast.LENGTH_SHORT).show();
                return;
            }

            viewModel.createNewPost(content, selectedBookTitle, selectedBookAuthor, selectedBookCoverResId);
            
            etPostInput.setText("");
            etPostInput.clearFocus();
            hideKeyboard();
            Toast.makeText(this, "Post published successfully!", Toast.LENGTH_SHORT).show();

            if (rvFeed != null) {
                rvFeed.smoothScrollToPosition(0);
            }
        });
    }

    private void setupRecyclerView() {
        rvFeed = findViewById(R.id.rvFeed);
        if (rvFeed != null) {
            rvFeed.setLayoutManager(new LinearLayoutManager(this));
            adapter = new PostAdapter(new ArrayList<>());
            adapter.setOnPostInteractionListener(new PostAdapter.OnPostInteractionListener() {
                @Override
                public void onLikeClick(int position, View likeImageView) {
                    viewModel.toggleLike(position);
                }

                @Override
                public void onCommentClick(int position) {
                    List<PostModel> currentPosts = viewModel.getPosts().getValue();
                    if (currentPosts != null && position < currentPosts.size()) {
                        BookifyDialogHelper.showCommentsBottomSheet(HomeActivity.this, currentPosts.get(position));
                    }
                }

                @Override
                public void onShareClick(int position) {
                    List<PostModel> currentPosts = viewModel.getPosts().getValue();
                    if (currentPosts != null && position < currentPosts.size()) {
                        PostModel post = currentPosts.get(position);
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + post.getUserName() + "'s post on Bookify: \"" + post.getPostContent() + "\"");
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Share Post via"));
                    }
                }

                @Override
                public void onMoreClick(int position, View anchorView) {
                    List<PostModel> currentPosts = viewModel.getPosts().getValue();
                    if (currentPosts != null && position < currentPosts.size()) {
                        BookifyDialogHelper.showPostMoreOptionsBottomSheet(HomeActivity.this, currentPosts.get(position));
                    }
                }

                @Override
                public void onBookClick(int position) {
                    List<PostModel> currentPosts = viewModel.getPosts().getValue();
                    if (currentPosts != null && position < currentPosts.size()) {
                        PostModel post = currentPosts.get(position);
                        BookifyDialogHelper.showBookDetailBottomSheet(
                                HomeActivity.this,
                                post.getBookTitle(),
                                post.getBookAuthor(),
                                post.getBookCoverResId(),
                                "4.8"
                        );
                    }
                }
            });
            rvFeed.setAdapter(adapter);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        viewModel.getPosts().observe(this, posts -> {
            if (adapter != null && posts != null) {
                adapter.updatePosts(posts);
            }
        });
    }

    private void setupBottomNavigationAndFab() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_home);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    if (rvFeed != null) {
                        rvFeed.smoothScrollToPosition(0);
                    }
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    Toast.makeText(this, "Explore section coming soon!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (itemId == R.id.nav_alerts) {
                    Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    return true;
                }
                return false;
            });
        }

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> {
                if (etPostInput != null) {
                    etPostInput.requestFocus();
                    showKeyboard();
                }
            });
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    private void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && etPostInput != null) {
            imm.showSoftInput(etPostInput, InputMethodManager.SHOW_IMPLICIT);
        }
    }
}
