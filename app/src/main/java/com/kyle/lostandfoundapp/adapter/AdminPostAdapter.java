package com.kyle.lostandfoundapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AdminPostAdapter extends RecyclerView.Adapter<AdminPostAdapter.AdminPostViewHolder> {

    private final Context context;
    private final OnAdminPostActionListener listener;

    // Keep both lists: one for all data, one for filtered
    private final List<Post> allPosts = new ArrayList<>();
    private final List<Post> filteredPosts = new ArrayList<>();

    public interface OnAdminPostActionListener {
        void onPostClick(Post post);
        void onDeletePost(Post post);
    }

    public AdminPostAdapter(Context context, OnAdminPostActionListener listener) {
        this.context = context;
        this.listener = listener;
    }

    // Update posts from API
    public void updatePosts(List<Post> newPosts) {
        allPosts.clear();
        allPosts.addAll(newPosts);

        filteredPosts.clear();
        filteredPosts.addAll(newPosts);

        notifyDataSetChanged();
    }

    // Apply filtering
    public void filterPosts(String type) {
        filteredPosts.clear();

        if (type.equalsIgnoreCase("all")) {
            filteredPosts.addAll(allPosts);
        } else if (type.equalsIgnoreCase("lost")) {
            for (Post post : allPosts) {
                if (post.getIsLost() != null && post.getIsLost()) {
                    filteredPosts.add(post);
                }
            }
        } else if (type.equalsIgnoreCase("found")) {
            for (Post post : allPosts) {
                if (post.getIsLost() != null && !post.getIsLost()) {
                    filteredPosts.add(post);
                }
            }
        } else if (type.equalsIgnoreCase("reported")) {
            for (Post post : allPosts) {
                if ("reported".equalsIgnoreCase(post.getStatus())) {
                    filteredPosts.add(post);
                }
            }
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AdminPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_admin_post, parent, false);
        return new AdminPostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdminPostViewHolder holder, int position) {
        Post post = filteredPosts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return filteredPosts.size();
    }

    class AdminPostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage, ivType;
        private final TextView tvTitle, tvDescription, tvDate, tvUserId;
        private final MaterialButton btnDelete;

        public AdminPostViewHolder(@NonNull View itemView) {
            super(itemView);
            ivImage = itemView.findViewById(R.id.ivImage);
            ivType = itemView.findViewById(R.id.ivType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvUserId = itemView.findViewById(R.id.tvUserId);
            btnDelete = itemView.findViewById(R.id.btnDelete);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPostClick(filteredPosts.get(position));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDeletePost(filteredPosts.get(position));
                }
            });
        }

        public void bind(Post post) {
            tvTitle.setText(post.getTitle());
            tvDescription.setText(post.getDescription());
            tvUserId.setText("User ID: " + post.getUserId());

            // Set type icon and color
            if (post.getIsLost() != null && post.getIsLost()) {
                ivType.setImageResource(R.drawable.ic_lost);
                ivType.setColorFilter(context.getResources().getColor(R.color.red_500, null));
            } else {
                ivType.setImageResource(R.drawable.ic_found);
                ivType.setColorFilter(context.getResources().getColor(R.color.green_500, null));
            }

            // Format date
            if (post.getCreatedAt() != null) {
                tvDate.setText(formatDate(post.getCreatedAt()));
            }

            // Load image
            if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                ivImage.setVisibility(View.VISIBLE);
                String imageUrl = ApiClient.getImageUrl(post.getImagePath());
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(ivImage);
            } else {
                ivImage.setVisibility(View.GONE);
            }
        }

        private String formatDate(String dateString) {
            try {
                SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
                Date date = inputFormat.parse(dateString);
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                return outputFormat.format(date);
            } catch (ParseException e) {
                return dateString;
            }
        }
    }
}
