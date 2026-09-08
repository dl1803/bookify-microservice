package com.example.bookify.ui.connections.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bookify.R;
import com.example.bookify.ui.connections.model.ConnectionUserModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class ConnectionsAdapter extends RecyclerView.Adapter<ConnectionsAdapter.ConnectionViewHolder> {

    public interface OnConnectionActionListener {
        void onPrimaryActionClick(ConnectionUserModel user, int position);
        void onSecondaryActionClick(ConnectionUserModel user, int position);
    }

    private List<ConnectionUserModel> userList;
    private OnConnectionActionListener listener;

    public ConnectionsAdapter(List<ConnectionUserModel> userList) {
        this.userList = userList;
    }

    public void setOnConnectionActionListener(OnConnectionActionListener listener) {
        this.listener = listener;
    }

    public void updateList(List<ConnectionUserModel> newList) {
        this.userList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ConnectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_connection_user, parent, false);
        return new ConnectionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ConnectionViewHolder holder, int position) {
        ConnectionUserModel user = userList.get(position);
        Context context = holder.itemView.getContext();

        holder.imgAvatar.setImageResource(user.getAvatarResId() != 0 ? user.getAvatarResId() : R.drawable.profile_avatar);
        holder.tvName.setText(user.getName());

        if (user.getTabType() == ConnectionUserModel.TabType.YOUR_FRIENDS) {
            // Tab Your Friends: Ẩn subtitle, ẩn nút secondary
            holder.tvSubtitle.setVisibility(View.GONE);
            holder.btnSecondary.setVisibility(View.GONE);

            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText(user.getPrimaryActionText());
            if ("Message".equalsIgnoreCase(user.getPrimaryActionText())) {
                holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_container_high)));
                holder.btnPrimary.setTextColor(ContextCompat.getColor(context, R.color.on_surface));
                holder.btnPrimary.setStrokeWidth(0);
            } else {
                holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
                holder.btnPrimary.setTextColor(ContextCompat.getColor(context, R.color.white));
                holder.btnPrimary.setStrokeWidth(0);
            }
        } else if (user.getTabType() == ConnectionUserModel.TabType.REQUESTS) {
            // Tab Requests: Hiện subtitle "X mutual friends • Sent Y ago", hiện nút đôi Delete + Confirm
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(user.getSubtitle());

            holder.btnSecondary.setVisibility(View.VISIBLE);
            holder.btnSecondary.setText("Delete");

            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText("Confirm");
            holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.primary)));
            holder.btnPrimary.setTextColor(ContextCompat.getColor(context, R.color.white));
            holder.btnPrimary.setStrokeWidth(0);
        } else {
            // Tab Sent: Hiện subtitle "Sent X ago", ẩn nút secondary, nút Cancel
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(user.getSubtitle());

            holder.btnSecondary.setVisibility(View.GONE);

            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText("Cancel");
            holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.surface_container_lowest)));
            holder.btnPrimary.setTextColor(ContextCompat.getColor(context, R.color.on_surface_variant));
            holder.btnPrimary.setStrokeColor(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.outline_variant)));
            holder.btnPrimary.setStrokeWidth(2);
        }

        holder.btnPrimary.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onPrimaryActionClick(user, pos);
            }
        });

        holder.btnSecondary.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION && listener != null) {
                listener.onSecondaryActionClick(user, pos);
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class ConnectionViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvSubtitle;
        MaterialButton btnPrimary, btnSecondary;

        public ConnectionViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.img_user_avatar);
            tvName = itemView.findViewById(R.id.tv_user_name);
            tvSubtitle = itemView.findViewById(R.id.tv_user_subtitle);
            btnPrimary = itemView.findViewById(R.id.btn_action_primary);
            btnSecondary = itemView.findViewById(R.id.btn_action_secondary);
        }
    }
}
