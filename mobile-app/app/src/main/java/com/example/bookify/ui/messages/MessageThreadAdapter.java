package com.example.bookify.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.HapticFeedbackConstants;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bookify.R;
import com.example.bookify.data.model.MessageThread;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import android.graphics.Color;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.ViewHolder> {

    private final List<MessageThread> threadList;

    public MessageThreadAdapter(List<MessageThread> threadList) {
        this.threadList = threadList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_thread, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        MessageThread thread = threadList.get(position);
        holder.bind(thread);
        
        holder.itemView.setOnClickListener(v -> {
            v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
            String avatarUrl = (thread.getAvatarUrls() != null && !thread.getAvatarUrls().isEmpty()) ? thread.getAvatarUrls().get(0) : "";
            android.content.Intent intent = ChatActivity.newIntent(v.getContext(), thread.getTitle(), avatarUrl);
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return threadList == null ? 0 : threadList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView ivSingleAvatar, ivGroupAvatar1, ivGroupAvatar2, ivGroupAvatar3;
        ConstraintLayout clGroupAvatars;
        TextView tvName, tvTime, tvLastMessage, tvUnreadBadge, tvUnreadBadgeGroup;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivSingleAvatar = itemView.findViewById(R.id.ivSingleAvatar);
            ivGroupAvatar1 = itemView.findViewById(R.id.ivGroupAvatar1);
            ivGroupAvatar2 = itemView.findViewById(R.id.ivGroupAvatar2);
            ivGroupAvatar3 = itemView.findViewById(R.id.ivGroupAvatar3);
            clGroupAvatars = itemView.findViewById(R.id.clGroupAvatars);
            
            tvName = itemView.findViewById(R.id.tvName);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
            tvUnreadBadge = itemView.findViewById(R.id.tvUnreadBadge);
            tvUnreadBadgeGroup = itemView.findViewById(R.id.tvUnreadBadgeGroup);
        }

        public void bind(MessageThread thread) {
            tvName.setText(thread.getTitle());
            tvTime.setText(thread.getTime());
            tvLastMessage.setText(thread.getLastMessage());

            // Unread styling
            itemView.setBackgroundColor(Color.TRANSPARENT);
            
            if (thread.getUnreadCount() > 0) {
                tvName.setTypeface(null, Typeface.BOLD);
                tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface));
                tvLastMessage.setTypeface(null, Typeface.BOLD);
                tvLastMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface));
                tvTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.primary));
            } else {
                tvName.setTypeface(null, Typeface.NORMAL);
                tvName.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface));
                tvLastMessage.setTypeface(null, Typeface.NORMAL);
                tvLastMessage.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface_variant));
                tvTime.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.on_surface_variant));
            }

            // Avatar Logic
            if (thread.isGroupChat() && thread.getAvatarUrls() != null && thread.getAvatarUrls().size() >= 3) {
                ivSingleAvatar.setVisibility(View.GONE);
                tvUnreadBadge.setVisibility(View.GONE);
                
                clGroupAvatars.setVisibility(View.VISIBLE);
                if (thread.getUnreadCount() > 0) {
                    tvUnreadBadgeGroup.setVisibility(View.VISIBLE);
                    tvUnreadBadgeGroup.setText("+" + thread.getUnreadCount());
                } else {
                    tvUnreadBadgeGroup.setVisibility(View.GONE);
                }
                
                Glide.with(itemView.getContext()).load(thread.getAvatarUrls().get(0)).centerCrop().into(ivGroupAvatar1);
                Glide.with(itemView.getContext()).load(thread.getAvatarUrls().get(1)).centerCrop().into(ivGroupAvatar2);
                Glide.with(itemView.getContext()).load(thread.getAvatarUrls().get(2)).centerCrop().into(ivGroupAvatar3);
            } else {
                clGroupAvatars.setVisibility(View.GONE);
                
                ivSingleAvatar.setVisibility(View.VISIBLE);
                if (thread.getUnreadCount() > 0) {
                    tvUnreadBadge.setVisibility(View.VISIBLE);
                    tvUnreadBadge.setText("+" + thread.getUnreadCount());
                } else {
                    tvUnreadBadge.setVisibility(View.GONE);
                }
                
                String url = (thread.getAvatarUrls() != null && !thread.getAvatarUrls().isEmpty()) ? thread.getAvatarUrls().get(0) : "";
                Glide.with(itemView.getContext()).load(url).centerCrop().into(ivSingleAvatar);
            }
        }
    }
}
