package com.example.bookify.utils;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.NestedScrollView;

import com.example.bookify.R;
import com.example.bookify.data.model.PostModel;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;

public class BookifyDialogHelper {

    public interface OnProfileUpdateListener {
        void onProfileUpdated(String newFirstName, String newLastName, String newDob, String newCity, String newBio);
    }

    public interface OnBookSelectedListener {
        void onBookSelected(String title, String author, int coverResId);
    }

    public static void showBookDetailBottomSheet(Context context, String title, String author, int coverResId, String rating) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_book_detail_bottom_sheet, null);
        dialog.setContentView(view);

        ImageView imgCover = view.findViewById(R.id.dialogImgBookCover);
        TextView tvTitle = view.findViewById(R.id.dialogTvBookTitle);
        TextView tvAuthor = view.findViewById(R.id.dialogTvBookAuthor);
        TextView tvRating = view.findViewById(R.id.dialogTvRating);
        MaterialButton btnSaveBook = view.findViewById(R.id.dialogBtnSaveBook);
        MaterialButton btnShareBook = view.findViewById(R.id.dialogBtnShareBook);

        imgCover.setImageResource(coverResId);
        tvTitle.setText(title);
        tvAuthor.setText(author);
        tvRating.setText(rating != null ? rating.replace("⭐", "").trim() : "4.8");

        btnSaveBook.setOnClickListener(v -> {
            btnSaveBook.setText("Saved ✓");
            btnSaveBook.setEnabled(false);
            Toast.makeText(context, "Added '" + title + "' to your Saved Books library!", Toast.LENGTH_SHORT).show();
        });

        btnShareBook.setOnClickListener(v -> {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this amazing book on Bookify: '" + title + "' by " + author);
            sendIntent.setType("text/plain");
            context.startActivity(Intent.createChooser(sendIntent, "Share Book via"));
            dialog.dismiss();
        });

        dialog.show();
    }

    // ==========================================
    // POST MORE OPTIONS & CONFIRMATION DIALOGS
    // ==========================================

    public static void showConfirmDeletePostDialog(Context context, Runnable onConfirmDelete) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_confirm_delete_post, null);
        builder.setView(view);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        View btnCancel = view.findViewById(R.id.btn_cancel_delete);
        View btnConfirm = view.findViewById(R.id.btn_confirm_delete);

        if (btnCancel != null) {
            btnCancel.setOnClickListener(v -> dialog.dismiss());
        }

        if (btnConfirm != null) {
            btnConfirm.setOnClickListener(v -> {
                dialog.dismiss();
                if (onConfirmDelete != null) {
                    onConfirmDelete.run();
                }
                Toast.makeText(context, "Post deleted successfully", Toast.LENGTH_SHORT).show();
            });
        }

        dialog.show();
    }

    public static void showMyPostOptionsBottomSheet(
            Context context,
            PostModel post,
            Runnable onEditPost,
            Runnable onDeletePost) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_post_options, null);
        dialog.setContentView(view);

        View itemEdit = view.findViewById(R.id.item_edit_post);
        View dividerEdit = view.findViewById(R.id.divider_edit_post);
        View itemSave = view.findViewById(R.id.item_save_post);
        View dividerSave = view.findViewById(R.id.divider_save_post);
        View itemViewOriginal = view.findViewById(R.id.item_view_original);
        View dividerViewOriginal = view.findViewById(R.id.divider_view_original);
        View itemCopyContent = view.findViewById(R.id.item_copy_content);
        View dividerCopyContent = view.findViewById(R.id.divider_copy_content);
        View itemCopyLink = view.findViewById(R.id.item_copy_link);
        View dividerCopyLink = view.findViewById(R.id.divider_copy_link);
        View itemSharePost = view.findViewById(R.id.item_share_post);
        View dividerSharePost = view.findViewById(R.id.divider_share_post);
        View itemDeletePost = view.findViewById(R.id.item_delete_post);
        View dividerDeletePost = view.findViewById(R.id.divider_delete_post);
        View itemReportPost = view.findViewById(R.id.item_report_post);

        // My Posts: Hiện Edit Post, Copy Link, Share Post, Delete Post
        if (itemEdit != null) {
            itemEdit.setVisibility(View.VISIBLE);
            itemEdit.setOnClickListener(v -> {
                dialog.dismiss();
                if (onEditPost != null) {
                    onEditPost.run();
                } else {
                    Toast.makeText(context, "Edit post will be available in a future update", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerEdit != null) dividerEdit.setVisibility(View.VISIBLE);

        if (itemSave != null) itemSave.setVisibility(View.GONE);
        if (dividerSave != null) dividerSave.setVisibility(View.GONE);
        if (itemViewOriginal != null) itemViewOriginal.setVisibility(View.GONE);
        if (dividerViewOriginal != null) dividerViewOriginal.setVisibility(View.GONE);
        if (itemCopyContent != null) itemCopyContent.setVisibility(View.GONE);
        if (dividerCopyContent != null) dividerCopyContent.setVisibility(View.GONE);

        if (itemCopyLink != null) {
            itemCopyLink.setVisibility(View.VISIBLE);
            itemCopyLink.setOnClickListener(v -> {
                dialog.dismiss();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Post Link", "https://bookify.app/posts/" + (post != null ? post.getUserName() : "me"));
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Post link copied to clipboard!", Toast.LENGTH_SHORT).show();
            });
        }
        if (dividerCopyLink != null) dividerCopyLink.setVisibility(View.VISIBLE);

        if (itemSharePost != null) {
            itemSharePost.setVisibility(View.VISIBLE);
            itemSharePost.setOnClickListener(v -> {
                dialog.dismiss();
                if (post != null) {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out my post on Bookify:\n\"" + post.getPostContent() + "\"");
                    sendIntent.setType("text/plain");
                    context.startActivity(Intent.createChooser(sendIntent, "Share Post via"));
                }
            });
        }
        if (dividerSharePost != null) dividerSharePost.setVisibility(View.VISIBLE);

        if (itemDeletePost != null) {
            itemDeletePost.setVisibility(View.VISIBLE);
            itemDeletePost.setOnClickListener(v -> {
                dialog.dismiss();
                showConfirmDeletePostDialog(context, onDeletePost);
            });
        }
        if (dividerDeletePost != null) dividerDeletePost.setVisibility(View.GONE);

        if (itemReportPost != null) itemReportPost.setVisibility(View.GONE);

        dialog.show();
    }

    public static void showMyPostOptionsBottomSheet(
            Context context,
            PostModel post,
            Runnable onDeletePost) {
        showMyPostOptionsBottomSheet(context, post, null, onDeletePost);
    }

    public static void showSavedPostOptionsBottomSheet(
            Context context,
            PostModel post,
            Runnable onRemoveSaved,
            Runnable onViewOriginalPost,
            Runnable onCopyContent,
            Runnable onSharePost) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_post_options, null);
        dialog.setContentView(view);

        View itemEdit = view.findViewById(R.id.item_edit_post);
        View dividerEdit = view.findViewById(R.id.divider_edit_post);
        View itemSave = view.findViewById(R.id.item_save_post);
        TextView tvSaveTitle = view.findViewById(R.id.tv_save_title);
        ImageView imgSaveIcon = view.findViewById(R.id.img_save_icon);
        View dividerSave = view.findViewById(R.id.divider_save_post);
        View itemViewOriginal = view.findViewById(R.id.item_view_original);
        View dividerViewOriginal = view.findViewById(R.id.divider_view_original);
        View itemCopyContent = view.findViewById(R.id.item_copy_content);
        View dividerCopyContent = view.findViewById(R.id.divider_copy_content);
        View itemCopyLink = view.findViewById(R.id.item_copy_link);
        View dividerCopyLink = view.findViewById(R.id.divider_copy_link);
        View itemSharePost = view.findViewById(R.id.item_share_post);
        View dividerSharePost = view.findViewById(R.id.divider_share_post);
        View itemDeletePost = view.findViewById(R.id.item_delete_post);
        View dividerDeletePost = view.findViewById(R.id.divider_delete_post);
        View itemReportPost = view.findViewById(R.id.item_report_post);

        // Saved: Ẩn Edit Post, Hiện Remove from Saved, View Original Post, Copy Quote/Text, Copy Link, Share Post
        if (itemEdit != null) itemEdit.setVisibility(View.GONE);
        if (dividerEdit != null) dividerEdit.setVisibility(View.GONE);
        if (itemSave != null) {
            itemSave.setVisibility(View.VISIBLE);
            if (tvSaveTitle != null) tvSaveTitle.setText("Remove from Saved");
            if (imgSaveIcon != null) imgSaveIcon.setImageResource(R.drawable.ic_bookmark_remove);
            itemSave.setOnClickListener(v -> {
                dialog.dismiss();
                if (onRemoveSaved != null) {
                    onRemoveSaved.run();
                } else {
                    Toast.makeText(context, "Removed from saved posts", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerSave != null) dividerSave.setVisibility(View.VISIBLE);

        if (itemViewOriginal != null) {
            itemViewOriginal.setVisibility(View.VISIBLE);
            itemViewOriginal.setOnClickListener(v -> {
                dialog.dismiss();
                if (onViewOriginalPost != null) {
                    onViewOriginalPost.run();
                } else {
                    String author = post != null ? post.getUserName() : "author";
                    Toast.makeText(context, "Viewing original post by " + author, Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerViewOriginal != null) dividerViewOriginal.setVisibility(View.VISIBLE);

        if (itemCopyContent != null) {
            itemCopyContent.setVisibility(View.VISIBLE);
            itemCopyContent.setOnClickListener(v -> {
                dialog.dismiss();
                if (onCopyContent != null) {
                    onCopyContent.run();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    String textToCopy = post != null ? post.getPostContent() : "";
                    ClipData clip = ClipData.newPlainText("Quote/Text", textToCopy);
                    if (clipboard != null) clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Quote / text copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerCopyContent != null) dividerCopyContent.setVisibility(View.VISIBLE);

        if (itemCopyLink != null) {
            itemCopyLink.setVisibility(View.VISIBLE);
            itemCopyLink.setOnClickListener(v -> {
                dialog.dismiss();
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Post Link", "https://bookify.app/posts/" + (post != null ? post.getUserName() : "saved"));
                if (clipboard != null) clipboard.setPrimaryClip(clip);
                Toast.makeText(context, "Post link copied to clipboard!", Toast.LENGTH_SHORT).show();
            });
        }
        if (dividerCopyLink != null) dividerCopyLink.setVisibility(View.VISIBLE);

        if (itemSharePost != null) {
            itemSharePost.setVisibility(View.VISIBLE);
            itemSharePost.setOnClickListener(v -> {
                dialog.dismiss();
                if (onSharePost != null) {
                    onSharePost.run();
                } else {
                    if (post != null) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out this quote on Bookify:\n\"" + post.getPostContent() + "\"\n— " + post.getBookTitle() + " by " + post.getBookAuthor());
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, "Share Post via"));
                    }
                }
            });
        }
        if (dividerSharePost != null) dividerSharePost.setVisibility(View.GONE);

        if (itemDeletePost != null) itemDeletePost.setVisibility(View.GONE);
        if (dividerDeletePost != null) dividerDeletePost.setVisibility(View.GONE);
        if (itemReportPost != null) itemReportPost.setVisibility(View.GONE);

        dialog.show();
    }

    public static void showFeedOtherPostOptionsBottomSheet(
            Context context,
            PostModel post,
            Runnable onToggleSave,
            Runnable onCopyLink,
            Runnable onSharePost,
            Runnable onReportPost) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_post_options, null);
        dialog.setContentView(view);

        View itemEdit = view.findViewById(R.id.item_edit_post);
        View dividerEdit = view.findViewById(R.id.divider_edit_post);
        View itemSave = view.findViewById(R.id.item_save_post);
        TextView tvSaveTitle = view.findViewById(R.id.tv_save_title);
        ImageView imgSaveIcon = view.findViewById(R.id.img_save_icon);
        View dividerSave = view.findViewById(R.id.divider_save_post);
        View itemViewOriginal = view.findViewById(R.id.item_view_original);
        View dividerViewOriginal = view.findViewById(R.id.divider_view_original);
        View itemCopyContent = view.findViewById(R.id.item_copy_content);
        View dividerCopyContent = view.findViewById(R.id.divider_copy_content);
        View itemCopyLink = view.findViewById(R.id.item_copy_link);
        View dividerCopyLink = view.findViewById(R.id.divider_copy_link);
        View itemSharePost = view.findViewById(R.id.item_share_post);
        View dividerSharePost = view.findViewById(R.id.divider_share_post);
        View itemDeletePost = view.findViewById(R.id.item_delete_post);
        View dividerDeletePost = view.findViewById(R.id.divider_delete_post);
        View itemReportPost = view.findViewById(R.id.item_report_post);

        // Feed/Other: Ẩn Edit Post, Hiện Save/Unsave, Copy Link, Share Post, Report Post
        if (itemEdit != null) itemEdit.setVisibility(View.GONE);
        if (dividerEdit != null) dividerEdit.setVisibility(View.GONE);
        if (itemSave != null) {
            itemSave.setVisibility(View.VISIBLE);
            boolean isAlreadySaved = post != null && post.isSaved();
            if (tvSaveTitle != null) tvSaveTitle.setText(isAlreadySaved ? "Remove from Saved" : "Save Post");
            if (imgSaveIcon != null) imgSaveIcon.setImageResource(isAlreadySaved ? R.drawable.ic_bookmark_remove : R.drawable.ic_bookmark);
            itemSave.setOnClickListener(v -> {
                dialog.dismiss();
                if (onToggleSave != null) {
                    onToggleSave.run();
                } else {
                    if (post != null) post.setSaved(!isAlreadySaved);
                    Toast.makeText(context, isAlreadySaved ? "Removed from saved" : "Saved post to your library!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerSave != null) dividerSave.setVisibility(View.VISIBLE);

        if (itemViewOriginal != null) itemViewOriginal.setVisibility(View.GONE);
        if (dividerViewOriginal != null) dividerViewOriginal.setVisibility(View.GONE);
        if (itemCopyContent != null) itemCopyContent.setVisibility(View.GONE);
        if (dividerCopyContent != null) dividerCopyContent.setVisibility(View.GONE);

        if (itemCopyLink != null) {
            itemCopyLink.setVisibility(View.VISIBLE);
            itemCopyLink.setOnClickListener(v -> {
                dialog.dismiss();
                if (onCopyLink != null) {
                    onCopyLink.run();
                } else {
                    ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Post Link", "https://bookify.app/posts/" + (post != null ? post.getUserName() : ""));
                    if (clipboard != null) clipboard.setPrimaryClip(clip);
                    Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show();
                }
            });
        }
        if (dividerCopyLink != null) dividerCopyLink.setVisibility(View.VISIBLE);

        if (itemSharePost != null) {
            itemSharePost.setVisibility(View.VISIBLE);
            itemSharePost.setOnClickListener(v -> {
                dialog.dismiss();
                if (onSharePost != null) {
                    onSharePost.run();
                } else {
                    if (post != null) {
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, "Check out " + post.getUserName() + "'s post on Bookify: \"" + post.getPostContent() + "\"");
                        sendIntent.setType("text/plain");
                        context.startActivity(Intent.createChooser(sendIntent, "Share Post via"));
                    }
                }
            });
        }
        if (dividerSharePost != null) dividerSharePost.setVisibility(View.VISIBLE);

        if (itemDeletePost != null) itemDeletePost.setVisibility(View.GONE);
        if (dividerDeletePost != null) dividerDeletePost.setVisibility(View.GONE);

        if (itemReportPost != null) {
            itemReportPost.setVisibility(View.VISIBLE);
            itemReportPost.setOnClickListener(v -> {
                dialog.dismiss();
                if (onReportPost != null) {
                    onReportPost.run();
                } else {
                    Toast.makeText(context, "Post reported. Thank you for your feedback.", Toast.LENGTH_SHORT).show();
                }
            });
        }

        dialog.show();
    }

    public static void showPostMoreOptionsBottomSheet(Context context, PostModel post) {
        showFeedOtherPostOptionsBottomSheet(context, post, null, null, null, null);
    }

    public static void showPostMoreOptionsBottomSheet(
            Context context,
            boolean isAuthor,
            boolean isSaved,
            Runnable onToggleSave,
            Runnable onCopyLink,
            Runnable onSharePost,
            Runnable onDeletePost,
            Runnable onReportPost) {
        if (isAuthor) {
            showMyPostOptionsBottomSheet(context, null, onDeletePost);
        } else if (isSaved) {
            showSavedPostOptionsBottomSheet(context, null, onToggleSave, null, null, onSharePost);
        } else {
            showFeedOtherPostOptionsBottomSheet(context, null, onToggleSave, onCopyLink, onSharePost, onReportPost);
        }
    }

    public static void showPostMoreOptionsBottomSheet(
            Context context,
            boolean isAuthor,
            boolean isSaved,
            Runnable onToggleSave,
            Runnable onCopyLink,
            Runnable onSharePost,
            Runnable onEditPost,
            Runnable onDeletePost,
            Runnable onReportPost) {
        if (isAuthor) {
            showMyPostOptionsBottomSheet(context, null, onEditPost, onDeletePost);
        } else if (isSaved) {
            showSavedPostOptionsBottomSheet(context, null, onToggleSave, null, null, onSharePost);
        } else {
            showFeedOtherPostOptionsBottomSheet(context, null, onToggleSave, onCopyLink, onSharePost, onReportPost);
        }
    }

    public static void showProfileMoreOptionsBottomSheet(Context context, Runnable onEditProfile, Runnable onShareProfile) {
        showProfileMoreOptionsBottomSheet(context, true, false, onEditProfile, onShareProfile, null, null, null);
    }

    public static void showProfileMoreOptionsBottomSheet(
            Context context,
            boolean isSelf,
            Runnable onEditProfile,
            Runnable onShareProfile,
            Runnable onBlockUser,
            Runnable onReportUser) {
        showProfileMoreOptionsBottomSheet(context, isSelf, false, onEditProfile, onShareProfile, null, onBlockUser, onReportUser);
    }

    public static void showProfileMoreOptionsBottomSheet(
            Context context,
            boolean isSelf,
            boolean isFriend,
            Runnable onEditProfile,
            Runnable onShareProfile,
            Runnable onUnfriend,
            Runnable onBlockUser,
            Runnable onReportUser) {

        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_profile_options, null);
        dialog.setContentView(view);

        View itemEdit = view.findViewById(R.id.item_edit_profile);
        View dividerEdit = view.findViewById(R.id.divider_edit);
        View itemUnfriend = view.findViewById(R.id.item_unfriend);
        View dividerUnfriend = view.findViewById(R.id.divider_unfriend);
        View itemShare = view.findViewById(R.id.item_share_profile);
        View dividerShare = view.findViewById(R.id.divider_share);
        View itemBlock = view.findViewById(R.id.item_block_user);
        View dividerBlock = view.findViewById(R.id.divider_block);
        View itemReport = view.findViewById(R.id.item_report_user);

        if (isSelf) {
            // Khi ở Profile của chính mình (My Profile): Edit Profile + Share Profile
            if (itemEdit != null) {
                itemEdit.setVisibility(View.VISIBLE);
                itemEdit.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (onEditProfile != null) onEditProfile.run();
                });
            }
            if (dividerEdit != null) dividerEdit.setVisibility(View.VISIBLE);

            if (itemUnfriend != null) itemUnfriend.setVisibility(View.GONE);
            if (dividerUnfriend != null) dividerUnfriend.setVisibility(View.GONE);

            if (itemShare != null) {
                itemShare.setVisibility(View.VISIBLE);
                itemShare.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (onShareProfile != null) onShareProfile.run();
                });
            }
            if (dividerShare != null) dividerShare.setVisibility(View.GONE);

            if (itemBlock != null) itemBlock.setVisibility(View.GONE);
            if (dividerBlock != null) dividerBlock.setVisibility(View.GONE);
            if (itemReport != null) itemReport.setVisibility(View.GONE);
        } else {
            // Khi ở Profile của người khác: Unfriend (nếu là bạn) + Share Profile + Block User + Report
            if (itemEdit != null) itemEdit.setVisibility(View.GONE);
            if (dividerEdit != null) dividerEdit.setVisibility(View.GONE);

            if (isFriend) {
                if (itemUnfriend != null) {
                    itemUnfriend.setVisibility(View.VISIBLE);
                    itemUnfriend.setOnClickListener(v -> {
                        dialog.dismiss();
                        if (onUnfriend != null) onUnfriend.run();
                    });
                }
                if (dividerUnfriend != null) dividerUnfriend.setVisibility(View.VISIBLE);
            } else {
                if (itemUnfriend != null) itemUnfriend.setVisibility(View.GONE);
                if (dividerUnfriend != null) dividerUnfriend.setVisibility(View.GONE);
            }

            if (itemShare != null) {
                itemShare.setVisibility(View.VISIBLE);
                itemShare.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (onShareProfile != null) onShareProfile.run();
                });
            }
            if (dividerShare != null) dividerShare.setVisibility(View.VISIBLE);

            if (itemBlock != null) {
                itemBlock.setVisibility(View.VISIBLE);
                itemBlock.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (onBlockUser != null) onBlockUser.run();
                });
            }
            if (dividerBlock != null) dividerBlock.setVisibility(View.VISIBLE);

            if (itemReport != null) {
                itemReport.setVisibility(View.VISIBLE);
                itemReport.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (onReportUser != null) onReportUser.run();
                });
            }
        }

        dialog.show();
    }

    public static void showCommentsBottomSheet(Context context, PostModel post) {
        BottomSheetDialog dialog = new BottomSheetDialog(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_comments_bottom_sheet, null);
        dialog.setContentView(view);

        TextView tvHeader = view.findViewById(R.id.dialogTvCommentsHeader);
        EditText etCommentInput = view.findViewById(R.id.dialogEtCommentInput);
        ImageView btnSendComment = view.findViewById(R.id.dialogBtnSendComment);
        LinearLayout layoutCommentsList = view.findViewById(R.id.layoutCommentsList);
        NestedScrollView scrollComments = view.findViewById(R.id.dialogScrollComments);

        final int[] commentsCount = {post.getCommentsCount()};
        tvHeader.setText("Comments (" + commentsCount[0] + ")");

        btnSendComment.setOnClickListener(v -> {
            String commentText = etCommentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(context, "Please enter a comment!", Toast.LENGTH_SHORT).show();
                return;
            }

            View commentLineView = LayoutInflater.from(context).inflate(R.layout.item_comment_line, layoutCommentsList, false);
            TextView tvAuthor = commentLineView.findViewById(R.id.tvCommentAuthor);
            TextView tvTime = commentLineView.findViewById(R.id.tvCommentTime);
            TextView tvText = commentLineView.findViewById(R.id.tvCommentText);

            tvAuthor.setText("Emily Reader");
            tvTime.setText("Just now");
            tvText.setText(commentText);

            layoutCommentsList.addView(commentLineView);
            commentsCount[0]++;
            tvHeader.setText("Comments (" + commentsCount[0] + ")");
            etCommentInput.setText("");

            if (scrollComments != null) {
                scrollComments.post(() -> scrollComments.fullScroll(View.FOCUS_DOWN));
            }
        });

        dialog.show();
    }

    public static void showEditProfileDialog(Context context, com.example.bookify.data.model.UserModel currentUser, String currentBio, OnProfileUpdateListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_edit_profile, null);
        builder.setView(view);

        EditText etUsername = view.findViewById(R.id.dialogEtUsername);
        EditText etEmail = view.findViewById(R.id.dialogEtEmail);
        EditText etFirstName = view.findViewById(R.id.dialogEtFirstName);
        EditText etLastName = view.findViewById(R.id.dialogEtLastName);
        EditText etDob = view.findViewById(R.id.dialogEtDob);
        EditText etCity = view.findViewById(R.id.dialogEtCity);
        EditText etBio = view.findViewById(R.id.dialogEtBio);
        MaterialButton btnSave = view.findViewById(R.id.dialogBtnSaveProfile);
        MaterialButton btnCancel = view.findViewById(R.id.dialogBtnCancelProfile);

        if (currentUser != null) {
            etUsername.setText(currentUser.getUsername());
            etEmail.setText(currentUser.getEmail());
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
            etDob.setText(currentUser.getDob());
            etCity.setText(currentUser.getCity());
        }
        etBio.setText(currentBio);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        btnSave.setOnClickListener(v -> {
            String newFirstName = etFirstName.getText().toString().trim();
            String newLastName = etLastName.getText().toString().trim();
            String newDob = etDob.getText().toString().trim();
            String newCity = etCity.getText().toString().trim();
            String newBio = etBio.getText().toString().trim();

            if (newFirstName.isEmpty()) {
                Toast.makeText(context, "First Name cannot be empty!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (listener != null) {
                listener.onProfileUpdated(newFirstName, newLastName, newDob, newCity, newBio);
            }
            Toast.makeText(context, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showAttachBookDialog(Context context, OnBookSelectedListener listener) {
        String[] bookTitles = {"The Silent Echo", "Roots of Thought", "Currents", "Lost Time"};
        String[] bookAuthors = {"Arthur Pendelton", "Elena Vance", "Marcus Thorne", "Sarah Jenkins"};
        int[] coverResIds = {R.drawable.book_cover_1, R.drawable.book_cover_2, R.drawable.book_cover_3, R.drawable.book_cover_4};

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select a Book to Attach");
        builder.setItems(bookTitles, (dialog, which) -> {
            if (listener != null) {
                listener.onBookSelected(bookTitles[which], bookAuthors[which], coverResIds[which]);
            }
            Toast.makeText(context, "Attached '" + bookTitles[which] + "' to your post!", Toast.LENGTH_SHORT).show();
        });
        builder.show();
    }
}
