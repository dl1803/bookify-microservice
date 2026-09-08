package com.example.bookify.ui.profile;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.bumptech.glide.request.RequestOptions;
import com.example.bookify.data.remote.dto.UpdateProfileRequest;
import com.example.bookify.data.remote.dto.UserProfileResponse;
import com.example.bookify.ui.alerts.NotificationsActivity;
import com.example.bookify.utils.FileUtils;
import com.example.bookify.utils.UrlUtils;

import java.io.File;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.ui.connections.ConnectionsActivity;
import com.example.bookify.data.model.PostModel;
import com.example.bookify.ui.home.HomeActivity;
import com.example.bookify.ui.profile.adapter.ProfilePostAdapter;
import com.example.bookify.utils.BookifyDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ProfileActivity extends AppCompatActivity {

    @Inject
    com.example.bookify.data.repository.AuthRepository authRepository;

    private ProfileViewModel viewModel;
    private MaterialButton btnFriendPrimary;
    private MaterialButton btnFriendChat;
    private View layoutActionButtons;
    private NestedScrollView scrollViewContent;
    private TextView tvProfileName;
    private TextView tvProfileBio;
    private ShapeableImageView imgAvatar;

    private RecyclerView recyclerProfileFeed;
    private ProfilePostAdapter postAdapter;
    private final List<PostModel> myPosts = new ArrayList<>();
    private final List<PostModel> savedPosts = new ArrayList<>();
    private int currentTabPosition = 0;

    private ActivityResultLauncher<String> avatarPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_profile_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        scrollViewContent = findViewById(R.id.scroll_view_content);
        btnFriendPrimary = findViewById(R.id.btn_friend_primary);
        btnFriendChat = findViewById(R.id.btn_friend_chat);
        layoutActionButtons = findViewById(R.id.layout_action_buttons);
        tvProfileName = findViewById(R.id.tv_profile_name);
        tvProfileBio = findViewById(R.id.tv_profile_bio);
        imgAvatar = findViewById(R.id.img_avatar);

        setupAvatarPicker();
        setupViewModel();
        setupProfileInteractions();
        setupTabs();
        setupProfileFeed();
        setupBottomNavigationAndFab();
    }

    private void setupAvatarPicker() {
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && imgAvatar != null) {
                        try {
                            File file = FileUtils.getFileFromUri(this, uri);
                            okhttp3.RequestBody requestFile = okhttp3.RequestBody
                                    .create(okhttp3.MediaType.parse("image/*"), file);
                            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file",
                                    file.getName(), requestFile);
                            viewModel.uploadAvatar(body);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        viewModel.fetchMyProfile();

        viewModel.getProfileData().observe(this, profile -> {
            if (profile != null) {

                String firstName = profile.getFirstName() != null ? profile.getFirstName() : "";
                String lastName = profile.getLastName() != null ? profile.getLastName() : "";
                tvProfileName.setText((firstName + " " + lastName).trim());

                StringBuilder bioBuilder = new StringBuilder();
                if (profile.getUsername() != null && !profile.getUsername().isEmpty()) {
                    bioBuilder.append("@").append(profile.getUsername());
                }
                if (profile.getCity() != null && !profile.getCity().isEmpty()) {
                    if (bioBuilder.length() > 0)
                        bioBuilder.append(" | ");
                    bioBuilder.append(profile.getCity());
                }
                if (profile.getBio() != null && !profile.getBio().isEmpty()) {
                    if (bioBuilder.length() > 0)
                        bioBuilder.append(" | ");
                    bioBuilder.append(profile.getBio());
                }

                if (tvProfileBio != null) {
                    tvProfileBio.setText(bioBuilder.toString());
                    tvProfileBio.setVisibility(bioBuilder.length() > 0 ? View.VISIBLE : View.GONE);
                }

                if (profile.getAvatar() != null && !profile.getAvatar().isEmpty()) {
                    String url = UrlUtils.resolveLocalUrl(profile.getAvatar());
                    Glide.with(this)
                            .load(url)
                            .apply(RequestOptions.bitmapTransform(new CircleCrop()))
                            .placeholder(R.drawable.profile_avatar)
                            .error(R.drawable.profile_avatar)
                            .into(imgAvatar);
                }
            }
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getFriendState().observe(this, state -> {
            if (layoutActionButtons == null || btnFriendPrimary == null || btnFriendChat == null)
                return;

            if (state == ProfileViewModel.FriendState.SELF) {
                layoutActionButtons.setVisibility(View.GONE);
                return;
            }

            layoutActionButtons.setVisibility(View.VISIBLE);

            switch (state) {
                case NOT_FRIEND:
                    btnFriendPrimary.setText("Add Friend");
                    btnFriendPrimary.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                    btnFriendPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
                    btnFriendChat.setVisibility(View.GONE);
                    break;
                case REQUEST_SENT:
                    btnFriendPrimary.setText("Cancel");
                    btnFriendPrimary.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container)));
                    btnFriendPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary));
                    btnFriendChat.setVisibility(View.GONE);
                    break;
                case FRIEND:
                    btnFriendPrimary.setText("Friend ✓");
                    btnFriendPrimary.setBackgroundTintList(
                            ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container)));
                    btnFriendPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary));
                    btnFriendChat.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }
        });
    }

    private void setupProfileInteractions() {
        if (imgAvatar != null) {
            imgAvatar.setOnClickListener(v -> {
                avatarPickerLauncher.launch("image/*");
            });
        }

        if (btnFriendPrimary != null) {
            btnFriendPrimary.setOnClickListener(v -> {
                viewModel.cycleFriendState();
                ProfileViewModel.FriendState state = viewModel.getFriendState().getValue();
                if (state == ProfileViewModel.FriendState.REQUEST_SENT) {
                    Toast.makeText(this, "Friend request sent to Emily Reader!", Toast.LENGTH_SHORT).show();
                } else if (state == ProfileViewModel.FriendState.FRIEND) {
                    Toast.makeText(this, "You are now friends with Emily Reader!", Toast.LENGTH_SHORT).show();
                } else if (state == ProfileViewModel.FriendState.NOT_FRIEND) {
                    Toast.makeText(this, "Canceled request/Unfriended Emily Reader", Toast.LENGTH_SHORT).show();
                }
            });
        }

        if (btnFriendChat != null) {
            btnFriendChat.setOnClickListener(
                    v -> Toast.makeText(this, "Opening chat with Emily...", Toast.LENGTH_SHORT).show());
        }

        View badgeFriends = findViewById(R.id.layout_badge_friends);
        if (badgeFriends != null) {
            badgeFriends.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, ConnectionsActivity.class);
                startActivity(intent);
            });
        }

        findViewById(R.id.btn_more).setOnClickListener(v -> {
            ProfileViewModel.FriendState friendState = viewModel.getFriendState().getValue();
            boolean isSelf = (friendState == ProfileViewModel.FriendState.SELF);
            boolean isFriend = (friendState == ProfileViewModel.FriendState.FRIEND);

            BookifyDialogHelper.showProfileMoreOptionsBottomSheet(
                    this,
                    isSelf,
                    isFriend,
                    () -> {
                        UserProfileResponse currentProfile = viewModel.getProfileData().getValue();
                        com.example.bookify.data.model.UserModel user;
                        String currentBio = "";
                        if (currentProfile != null) {
                            user = new com.example.bookify.data.model.UserModel(currentProfile.getUsername(),
                                    currentProfile.getEmail(), currentProfile.getFirstName(),
                                    currentProfile.getLastName(), currentProfile.getDob(), currentProfile.getCity(),
                                    currentProfile.getBio());
                            currentBio = currentProfile.getBio() != null ? currentProfile.getBio() : "";
                        } else {
                            user = new com.example.bookify.data.model.UserModel("loitran", "loitran@example.com", "Duc",
                                    "Loi", "15/08/1998", "Hanoi", "Read every day | Love mystery stories");
                            currentBio = "Read every day | Love mystery stories";
                        }
                        BookifyDialogHelper.showEditProfileDialog(
                                this,
                                user,
                                currentBio,
                                (newFirstName, newLastName, newDob, newCity, newBio) -> {
                                    UpdateProfileRequest req = new UpdateProfileRequest(user.getEmail(), newFirstName,
                                            newLastName, newDob, newCity, newBio);
                                    viewModel.updateProfile(req);
                                });
                    },
                    () -> {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT,
                                "Check out " + (tvProfileName != null ? tvProfileName.getText() : "this")
                                        + "'s profile on Bookify!");
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Share Profile via"));
                    },
                    () -> {
                        viewModel.cycleFriendState();
                        Toast.makeText(this, "Unfriended " + (tvProfileName != null ? tvProfileName.getText() : "user"),
                                Toast.LENGTH_SHORT).show();
                    },
                    () -> Toast.makeText(this, "User blocked", Toast.LENGTH_SHORT).show(),
                    () -> Toast.makeText(this, "User reported. Thank you.", Toast.LENGTH_SHORT).show());
        });

        View goalAction = findViewById(R.id.tv_goal_action);
        if (goalAction != null) {
            goalAction.setOnClickListener(v -> {
                com.google.android.material.bottomsheet.BottomSheetDialog goalSheet = new com.google.android.material.bottomsheet.BottomSheetDialog(
                        this);
                View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_set_reading_goal, null);
                goalSheet.setContentView(sheetView);

                final int[] count = { 20 };
                TextView tvCount = sheetView.findViewById(R.id.tv_goal_count);
                View btnDecrease = sheetView.findViewById(R.id.btn_decrease_goal);
                View btnIncrease = sheetView.findViewById(R.id.btn_increase_goal);
                View btnClose = sheetView.findViewById(R.id.btn_close);
                View btnUpdate = sheetView.findViewById(R.id.btn_update_goal);

                if (btnDecrease != null) {
                    btnDecrease.setOnClickListener(b -> {
                        if (count[0] > 1) {
                            count[0]--;
                            if (tvCount != null)
                                tvCount.setText(String.valueOf(count[0]));
                        }
                    });
                }
                if (btnIncrease != null) {
                    btnIncrease.setOnClickListener(b -> {
                        count[0]++;
                        if (tvCount != null)
                            tvCount.setText(String.valueOf(count[0]));
                    });
                }
                if (btnClose != null) {
                    btnClose.setOnClickListener(b -> goalSheet.dismiss());
                }
                if (btnUpdate != null) {
                    btnUpdate.setOnClickListener(b -> {
                        Toast.makeText(this, "Goal updated to " + count[0] + " books for 2026!", Toast.LENGTH_SHORT)
                                .show();
                        goalSheet.dismiss();
                    });
                }
                goalSheet.show();
            });
        }
    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_layout_profile);
        if (tabLayout != null) {
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    currentTabPosition = position;
                    viewModel.selectTab(position);
                    if (postAdapter != null) {
                        postAdapter.setPosts(position == 0 ? myPosts : savedPosts);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (scrollViewContent != null) {
                        scrollViewContent.smoothScrollTo(0, 0);
                    }
                }
            });
        }
    }

    private void setupProfileFeed() {
        recyclerProfileFeed = findViewById(R.id.recycler_profile_feed);
        if (recyclerProfileFeed == null)
            return;

        recyclerProfileFeed.setLayoutManager(new LinearLayoutManager(this));
        recyclerProfileFeed.setNestedScrollingEnabled(false);

        initMockPosts();

        postAdapter = new ProfilePostAdapter(myPosts);
        postAdapter.setOnPostItemListener(new ProfilePostAdapter.OnPostItemListener() {
            @Override
            public void onMoreOptionsClick(PostModel post, int position, View anchorView) {
                if (currentTabPosition == 0) {
                    BookifyDialogHelper.showMyPostOptionsBottomSheet(
                            ProfileActivity.this,
                            post,
                            () -> {
                                myPosts.remove(position);
                                postAdapter.notifyItemRemoved(position);
                            });
                } else {
                    BookifyDialogHelper.showSavedPostOptionsBottomSheet(
                            ProfileActivity.this,
                            post,
                            () -> {
                                savedPosts.remove(position);
                                postAdapter.notifyItemRemoved(position);
                                Toast.makeText(ProfileActivity.this, "Removed from saved posts", Toast.LENGTH_SHORT)
                                        .show();
                            },
                            () -> Toast.makeText(ProfileActivity.this, "Viewing original post by " + post.getUserName(),
                                    Toast.LENGTH_SHORT).show(),
                            () -> {
                                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(
                                        Context.CLIPBOARD_SERVICE);
                                android.content.ClipData clip = android.content.ClipData.newPlainText("Post Quote",
                                        post.getPostContent());
                                if (clipboard != null)
                                    clipboard.setPrimaryClip(clip);
                                Toast.makeText(ProfileActivity.this, "Quote copied to clipboard!", Toast.LENGTH_SHORT)
                                        .show();
                            },
                            () -> {
                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                                sendIntent.putExtra(Intent.EXTRA_TEXT,
                                        "Check out this excerpt on Bookify:\n\"" + post.getPostContent() + "\"\n— "
                                                + post.getBookTitle() + " (" + post.getBookAuthor() + ")");
                                sendIntent.setType("text/plain");
                                startActivity(Intent.createChooser(sendIntent, "Share via"));
                            });
                }
            }

            @Override
            public void onLikeClick(PostModel post, int position) {
            }

            @Override
            public void onCommentClick(PostModel post, int position) {
                Toast.makeText(ProfileActivity.this, "Comments: " + post.getCommentsCount(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onBookmarkClick(PostModel post, int position) {
                Toast.makeText(ProfileActivity.this, post.isSaved() ? "Saved to library" : "Removed from library",
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onShareClick(PostModel post, int position) {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,
                        "Check out this post on Bookify:\n\"" + post.getPostContent() + "\"");
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, "Share via"));
            }
        });

        recyclerProfileFeed.setAdapter(postAdapter);
    }

    private void initMockPosts() {
        myPosts.clear();
        PostModel myReview = new PostModel(
                "Duc Loi",
                "2h ago",
                "Just finished reading this book, the plot is really engaging from the first pages. A great combination of psychology and detective elements.",
                "The Silent Echo",
                "Arthur Pendelton",
                R.drawable.profile_avatar,
                R.drawable.book_cover_1,
                24,
                5);
        myReview.setQuote(false);

        PostModel myQuote = new PostModel(
                "Duc Loi",
                "2h ago",
                "“The greatest enemy of truth is very often not the lie, but the myth.”",
                "Midnight Call",
                "Arthur Pendelton",
                R.drawable.profile_avatar,
                0,
                48,
                12);
        myQuote.setQuote(true);
        myQuote.setQuoteSource("— Midnight Call, Page 142");

        myPosts.add(myReview);
        myPosts.add(myQuote);

        savedPosts.clear();
        PostModel savedReview = new PostModel(
                "Donna Tartt",
                "1d ago",
                "Tartt's debut remains a masterclass in atmospheric tension. The prose is lush, almost suffocatingly so, mirroring the insular world of her classical scholars...",
                "The Secret History",
                "Donna Tartt",
                R.drawable.profile_avatar,
                R.drawable.book_cover_2,
                124,
                18);
        savedReview.setQuote(false);
        savedReview.setSaved(true);

        PostModel savedQuote = new PostModel(
                "Stephen Hawking",
                "3d ago",
                "“The greatest enemy of knowledge is not ignorance, it is the illusion of knowledge.”",
                "A Brief History of Time",
                "Stephen Hawking",
                R.drawable.profile_avatar,
                0,
                342,
                45);
        savedQuote.setQuote(true);
        savedQuote.setQuoteSource("— Stephen Hawking, Page 42");
        savedQuote.setSaved(true);

        savedPosts.add(savedReview);
        savedPosts.add(savedQuote);
    }

    private void setupBottomNavigationAndFab() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav != null) {
            bottomNav.setSelectedItemId(R.id.nav_profile);
            bottomNav.setOnItemSelectedListener(item -> {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    if (scrollViewContent != null) {
                        scrollViewContent.smoothScrollTo(0, 0);
                    }
                    return true;
                } else if (itemId == R.id.nav_explore) {
                    Toast.makeText(this, "Explore feature coming soon!", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (itemId == R.id.nav_alerts) {
                    Intent intent = new Intent(ProfileActivity.this, NotificationsActivity.class);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                    return true;
                }
                return false;
            });
        }

        FloatingActionButton fabCreate = findViewById(R.id.fabCreate);
        if (fabCreate != null) {
            fabCreate.setOnClickListener(v -> {
                Intent intent = new Intent(ProfileActivity.this, HomeActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                finish();
            });
        }
    }
}
