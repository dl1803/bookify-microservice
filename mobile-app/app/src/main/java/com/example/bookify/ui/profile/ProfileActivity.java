package com.example.bookify.ui.profile;

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

import com.example.bookify.R;
import com.example.bookify.ui.home.HomeActivity;
import com.example.bookify.utils.BookifyDialogHelper;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

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
    private TextView tvProfileLocation;
    private TextView tvProfileBio;
    private View layoutBooksGrid;
    private ShapeableImageView imgAvatar;

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
        tvProfileLocation = findViewById(R.id.tv_profile_location);
        tvProfileBio = findViewById(R.id.tv_profile_bio);
        layoutBooksGrid = findViewById(R.id.layout_books_grid);
        imgAvatar = findViewById(R.id.img_avatar);

        setupAvatarPicker();
        setupViewModel();
        setupProfileInteractions();
        setupTabs();
        setupBookCards();
        setupBottomNavigationAndFab();
    }

    private void setupAvatarPicker() {
        avatarPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null && imgAvatar != null) {
                        try {
                            File file = FileUtils.getFileFromUri(this, uri);
                            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(okhttp3.MediaType.parse("image/*"), file);
                            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData("file", file.getName(), requestFile);
                            viewModel.uploadAvatar(body);
                        } catch (Exception e) {
                            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        
        viewModel.fetchMyProfile();

        viewModel.getProfileData().observe(this, profile -> {
            if (profile != null) {

                String firstName = profile.getFirstName() != null ? profile.getFirstName() : "";
                String lastName = profile.getLastName() != null ? profile.getLastName() : "";
                tvProfileName.setText(firstName +""+ lastName);
                
                String locationInfo = profile.getCity() != null ? profile.getCity() : "Hanoi";
                if (profile.getDob() != null) {
                    locationInfo = locationInfo + " • " + profile.getDob();
                }
                tvProfileLocation.setText(locationInfo);

                if (profile.getBio() != null && !profile.getBio().isEmpty()) {
                    tvProfileBio.setText(profile.getBio());
                    tvProfileBio.setVisibility(View.VISIBLE);
                } else {
                    tvProfileBio.setText("");
                    tvProfileBio.setVisibility(View.GONE);
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
            if (layoutActionButtons == null || btnFriendPrimary == null || btnFriendChat == null) return;

            if (state == ProfileViewModel.FriendState.SELF) {
                layoutActionButtons.setVisibility(View.GONE);
                return;
            }

            layoutActionButtons.setVisibility(View.VISIBLE);

            switch (state) {
                case NOT_FRIEND:
                    btnFriendPrimary.setText("Add Friend");
                    btnFriendPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.primary)));
                    btnFriendPrimary.setTextColor(ContextCompat.getColor(this, R.color.white));
                    btnFriendChat.setVisibility(View.GONE);
                    break;
                case REQUEST_SENT:
                    btnFriendPrimary.setText("Cancel");
                    btnFriendPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container)));
                    btnFriendPrimary.setTextColor(ContextCompat.getColor(this, R.color.primary));
                    btnFriendChat.setVisibility(View.GONE);
                    break;
                case FRIEND:
                    btnFriendPrimary.setText("Friend ✓");
                    btnFriendPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_container)));
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
            btnFriendChat.setOnClickListener(v -> 
                Toast.makeText(this, "Opening chat with Emily...", Toast.LENGTH_SHORT).show()
            );
        }

        findViewById(R.id.btn_more).setOnClickListener(v -> 
            BookifyDialogHelper.showProfileMoreOptionsBottomSheet(
                    this,
                    () -> {
                        UserProfileResponse currentProfile = viewModel.getProfileData().getValue();
                        com.example.bookify.data.model.UserModel user;
                        String currentBio = "";
                        if (currentProfile != null) {
                            user = new com.example.bookify.data.model.UserModel(currentProfile.getUsername(), currentProfile.getEmail(), currentProfile.getFirstName(), currentProfile.getLastName(), currentProfile.getDob(), currentProfile.getCity(), currentProfile.getBio());
                            currentBio = currentProfile.getBio() != null ? currentProfile.getBio() : "";
                        } else {
                            user = new com.example.bookify.data.model.UserModel("emily", "emily@example.com", "Emily", "Reader", "15/08/1998", "Hanoi", "Lover of books");
                            currentBio = "Lover of books";
                        }
                        BookifyDialogHelper.showEditProfileDialog(
                                this,
                                user,
                                currentBio,
                                (newFirstName, newLastName, newDob, newCity, newBio) -> {
                                    UpdateProfileRequest req = new UpdateProfileRequest(user.getEmail(), newFirstName, newLastName, newDob, newCity, newBio);
                                    viewModel.updateProfile(req);
                                }
                        );
                    },
                    () -> {
                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out Emily Reader's profile on Bookify!");
                        sendIntent.setType("text/plain");
                        startActivity(Intent.createChooser(sendIntent, "Share Profile via"));
                    }
            )
        );

    }

    private void setupTabs() {
        TabLayout tabLayout = findViewById(R.id.tab_layout_profile);
        if (tabLayout != null) {
            TabLayout.Tab savedBooksTab = tabLayout.getTabAt(2);
            if (savedBooksTab != null) {
                savedBooksTab.select();
            }

            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    int position = tab.getPosition();
                    viewModel.selectTab(position);
                    if (layoutBooksGrid != null) {
                        layoutBooksGrid.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {}

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                    if (scrollViewContent != null) {
                        scrollViewContent.smoothScrollTo(0, 0);
                    }
                }
            });
        }
    }

    private void setupBookCards() {
        findViewById(R.id.card_book_1).setOnClickListener(v -> 
            BookifyDialogHelper.showBookDetailBottomSheet(this, "The Silent Echo", "Arthur Pendelton", R.drawable.book_cover_1, "4.8")
        );

        findViewById(R.id.card_book_2).setOnClickListener(v -> 
            BookifyDialogHelper.showBookDetailBottomSheet(this, "Roots of Thought", "Elena Vance", R.drawable.book_cover_2, "4.5")
        );

        findViewById(R.id.card_book_3).setOnClickListener(v -> 
            BookifyDialogHelper.showBookDetailBottomSheet(this, "Currents", "Marcus Thorne", R.drawable.book_cover_3, "4.9")
        );

        findViewById(R.id.card_book_4).setOnClickListener(v -> 
            BookifyDialogHelper.showBookDetailBottomSheet(this, "Lost Time", "Sarah Jenkins", R.drawable.book_cover_4, "4.2")
        );

        findViewById(R.id.card_add_new).setOnClickListener(v -> 
            BookifyDialogHelper.showAttachBookDialog(this, (title, author, coverResId) -> {
                Toast.makeText(this, "Added '" + title + "' to your Saved Books!", Toast.LENGTH_SHORT).show();
            })
        );
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
