package com.example.bookify.ui.profile.adapter;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.data.model.PostModel;

import java.util.List;

public class ProfilePostAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int TYPE_REVIEW = 0;
    public static final int TYPE_QUOTE = 1;

    public interface OnPostItemListener {
        void onMoreOptionsClick(PostModel post, int position, View anchorView);
        void onLikeClick(PostModel post, int position);
        void onCommentClick(PostModel post, int position);
        void onBookmarkClick(PostModel post, int position);
        void onShareClick(PostModel post, int position);
    }

    private List<PostModel> posts;
    private OnPostItemListener listener;

    public ProfilePostAdapter(List<PostModel> posts) {
        this.posts = posts;
    }

    public void setOnPostItemListener(OnPostItemListener listener) {
        this.listener = listener;
    }

    public void setPosts(List<PostModel> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (posts != null && posts.get(position).isQuote()) ? TYPE_QUOTE : TYPE_REVIEW;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_QUOTE) {
            View view = inflater.inflate(R.layout.item_post_quote, parent, false);
            return new QuoteViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_post_review, parent, false);
            return new ReviewViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostModel post = posts.get(position);
        if (holder instanceof QuoteViewHolder) {
            ((QuoteViewHolder) holder).bind(post, position, listener);
        } else if (holder instanceof ReviewViewHolder) {
            ((ReviewViewHolder) holder).bind(post, position, listener);
        }
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public static class QuoteViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnMore, btnLike, btnComment, btnBookmark, btnShare;
        TextView tvAuthorName, tvPostTime, tvQuoteContent, tvQuoteSource, tvLikeCount, tvCommentCount;

        public QuoteViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_author_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            btnMore = itemView.findViewById(R.id.btn_post_more);
            tvQuoteContent = itemView.findViewById(R.id.tv_quote_content);
            tvQuoteSource = itemView.findViewById(R.id.tv_quote_source);
            btnLike = itemView.findViewById(R.id.btn_like);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnComment = itemView.findViewById(R.id.btn_comment);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            btnBookmark = itemView.findViewById(R.id.btn_bookmark);
            btnShare = itemView.findViewById(R.id.btn_share);
        }

        public void bind(PostModel post, int position, OnPostItemListener listener) {
            Context context = itemView.getContext();
            tvAuthorName.setText(post.getUserName());
            tvPostTime.setText(post.getPostTime());
            tvQuoteContent.setText(post.getPostContent());
            if (post.getQuoteSource() != null && !post.getQuoteSource().isEmpty()) {
                tvQuoteSource.setText(post.getQuoteSource());
                tvQuoteSource.setVisibility(View.VISIBLE);
            } else if (post.getBookTitle() != null) {
                tvQuoteSource.setText("— " + post.getBookTitle() + ", " + post.getBookAuthor());
                tvQuoteSource.setVisibility(View.VISIBLE);
            } else {
                tvQuoteSource.setVisibility(View.GONE);
            }

            if (post.getUserAvatarResId() != 0) {
                imgAvatar.setImageResource(post.getUserAvatarResId());
            }

            tvLikeCount.setText(String.valueOf(post.getLikesCount()));
            tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

            int redColor = ContextCompat.getColor(context, R.color.like_red);
            int normalTint = ContextCompat.getColor(context, R.color.on_surface_variant);
            int primaryTint = ContextCompat.getColor(context, R.color.primary);

            btnLike.setColorFilter(post.isLiked() ? redColor : normalTint);
            if (btnBookmark != null) {
                btnBookmark.setColorFilter(post.isSaved() ? primaryTint : normalTint);
            }

            btnMore.setOnClickListener(v -> {
                if (listener != null) listener.onMoreOptionsClick(post, position, btnMore);
            });

            btnLike.setOnClickListener(v -> {
                post.setLiked(!post.isLiked());
                post.setLikesCount(post.isLiked() ? post.getLikesCount() + 1 : Math.max(0, post.getLikesCount() - 1));
                tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                btnLike.setColorFilter(post.isLiked() ? redColor : normalTint);
                animatePop(btnLike);
                if (listener != null) listener.onLikeClick(post, position);
            });

            if (btnComment != null) {
                btnComment.setOnClickListener(v -> {
                    if (listener != null) listener.onCommentClick(post, position);
                });
            }

            if (btnBookmark != null) {
                btnBookmark.setOnClickListener(v -> {
                    post.setSaved(!post.isSaved());
                    btnBookmark.setColorFilter(post.isSaved() ? primaryTint : normalTint);
                    animatePop(btnBookmark);
                    if (listener != null) listener.onBookmarkClick(post, position);
                });
            }

            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    if (listener != null) listener.onShareClick(post, position);
                });
            }
        }
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar, btnMore, imgCover, btnLike, btnComment, btnBookmark, btnShare;
        TextView tvAuthorName, tvPostTime, tvReviewContent, tvLikeCount, tvCommentCount;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_author_avatar);
            tvAuthorName = itemView.findViewById(R.id.tv_author_name);
            tvPostTime = itemView.findViewById(R.id.tv_post_time);
            btnMore = itemView.findViewById(R.id.btn_post_more);
            imgCover = itemView.findViewById(R.id.img_book_cover);
            tvReviewContent = itemView.findViewById(R.id.tv_review_content);
            btnLike = itemView.findViewById(R.id.btn_like);
            tvLikeCount = itemView.findViewById(R.id.tv_like_count);
            btnComment = itemView.findViewById(R.id.btn_comment);
            tvCommentCount = itemView.findViewById(R.id.tv_comment_count);
            btnBookmark = itemView.findViewById(R.id.btn_bookmark);
            btnShare = itemView.findViewById(R.id.btn_share);
        }

        public void bind(PostModel post, int position, OnPostItemListener listener) {
            Context context = itemView.getContext();
            tvAuthorName.setText(post.getUserName());
            tvPostTime.setText(post.getPostTime());
            tvReviewContent.setText(post.getPostContent());

            if (post.getBookCoverResId() != 0) {
                imgCover.setImageResource(post.getBookCoverResId());
                imgCover.setVisibility(View.VISIBLE);
            } else {
                imgCover.setVisibility(View.GONE);
            }

            if (post.getUserAvatarResId() != 0) {
                imgAvatar.setImageResource(post.getUserAvatarResId());
            }

            tvLikeCount.setText(String.valueOf(post.getLikesCount()));
            tvCommentCount.setText(String.valueOf(post.getCommentsCount()));

            int redColor = ContextCompat.getColor(context, R.color.like_red);
            int normalTint = ContextCompat.getColor(context, R.color.on_surface_variant);
            int primaryTint = ContextCompat.getColor(context, R.color.primary);

            btnLike.setColorFilter(post.isLiked() ? redColor : normalTint);
            if (btnBookmark != null) {
                btnBookmark.setColorFilter(post.isSaved() ? primaryTint : normalTint);
            }

            btnMore.setOnClickListener(v -> {
                if (listener != null) listener.onMoreOptionsClick(post, position, btnMore);
            });

            btnLike.setOnClickListener(v -> {
                post.setLiked(!post.isLiked());
                post.setLikesCount(post.isLiked() ? post.getLikesCount() + 1 : Math.max(0, post.getLikesCount() - 1));
                tvLikeCount.setText(String.valueOf(post.getLikesCount()));
                btnLike.setColorFilter(post.isLiked() ? redColor : normalTint);
                animatePop(btnLike);
                if (listener != null) listener.onLikeClick(post, position);
            });

            if (btnComment != null) {
                btnComment.setOnClickListener(v -> {
                    if (listener != null) listener.onCommentClick(post, position);
                });
            }

            if (btnBookmark != null) {
                btnBookmark.setOnClickListener(v -> {
                    post.setSaved(!post.isSaved());
                    btnBookmark.setColorFilter(post.isSaved() ? primaryTint : normalTint);
                    animatePop(btnBookmark);
                    if (listener != null) listener.onBookmarkClick(post, position);
                });
            }

            if (btnShare != null) {
                btnShare.setOnClickListener(v -> {
                    if (listener != null) listener.onShareClick(post, position);
                });
            }
        }
    }

    private static void animatePop(View target) {
        ObjectAnimator scaleXUp = ObjectAnimator.ofFloat(target, "scaleX", 1.0f, 1.35f);
        ObjectAnimator scaleYUp = ObjectAnimator.ofFloat(target, "scaleY", 1.0f, 1.35f);
        scaleXUp.setDuration(150);
        scaleYUp.setDuration(150);

        ObjectAnimator scaleXDown = ObjectAnimator.ofFloat(target, "scaleX", 1.35f, 1.0f);
        ObjectAnimator scaleYDown = ObjectAnimator.ofFloat(target, "scaleY", 1.35f, 1.0f);
        scaleXDown.setDuration(150);
        scaleYDown.setDuration(150);

        AnimatorSet scaleUpSet = new AnimatorSet();
        scaleUpSet.playTogether(scaleXUp, scaleYUp);

        AnimatorSet scaleDownSet = new AnimatorSet();
        scaleDownSet.playTogether(scaleXDown, scaleYDown);

        AnimatorSet fullAnimation = new AnimatorSet();
        fullAnimation.playSequentially(scaleUpSet, scaleDownSet);
        fullAnimation.start();
    }
}
