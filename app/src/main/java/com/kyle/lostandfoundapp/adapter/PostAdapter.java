package com.kyle.lostandfoundapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import com.bumptech.glide.Glide;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private static final String TAG = "PostAdapter";

    public interface OnPostClickListener {
        void onPostClick(Post post);
    }

    private final Context context;
    private final List<Post> posts = new ArrayList<>();
    private final OnPostClickListener listener;

    public PostAdapter(Context context, OnPostClickListener listener) {
        this.context = context;
        this.listener = listener;
        Log.d(TAG, "PostAdapter created");
    }

    public void updatePosts(List<Post> newPosts) {
        Log.d(TAG, "updatePosts called with " + (newPosts != null ? newPosts.size() : 0) + " posts");

        posts.clear();
        if (newPosts != null) {
            posts.addAll(newPosts);
        }
        notifyDataSetChanged();

        Log.d(TAG, "Posts updated. Total posts: " + posts.size());
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder called");
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder called for position: " + position);
        if (position >= 0 && position < posts.size() && posts.get(position) != null) {
            holder.bind(posts.get(position));
        }
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + posts.size());
        return posts.size();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivImage, ivType;
        private final TextView tvTitle, tvDescription, tvDate, tvContact, tvType;
        private final CardView cvImage, cvStatusBadge;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);

            // Initialize views based on the layout
            ivImage = itemView.findViewById(R.id.ivImage);
            ivType = itemView.findViewById(R.id.ivType);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvContact = itemView.findViewById(R.id.tvContact);
            tvType = itemView.findViewById(R.id.tvType);
            cvImage = itemView.findViewById(R.id.cvImage);
            cvStatusBadge = itemView.findViewById(R.id.cvStatusBadge);

            // Log which views were found/not found
            Log.d(TAG, "Views initialized - " +
                    "ivImage: " + (ivImage != null) +
                    ", tvTitle: " + (tvTitle != null) +
                    ", tvDescription: " + (tvDescription != null) +
                    ", tvDate: " + (tvDate != null) +
                    ", tvContact: " + (tvContact != null));

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null && position < posts.size()) {
                    Log.d(TAG, "Post clicked at position: " + position);
                    listener.onPostClick(posts.get(position));
                }
            });
        }

        public void bind(Post post) {
            Log.d(TAG, "Binding post: " + (post != null ? post.getTitle() : "null"));

            if (post == null) {
                Log.w(TAG, "Post is null, cannot bind");
                return;
            }

            // Title & description
            if (tvTitle != null) {
                tvTitle.setText(post.getTitle() != null ? post.getTitle() : "No Title");
            }
            if (tvDescription != null) {
                tvDescription.setText(post.getDescription() != null ? post.getDescription() : "");
            }

            // Type icon and text - use safe color access
            if (post.getIsLost() != null && post.getIsLost()) {
                if (ivType != null) {
                    // Use system icons if custom icons don't exist
                    ivType.setImageResource(android.R.drawable.ic_dialog_alert);
                    ivType.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
                if (tvType != null) {
                    tvType.setText("LOST");
                    tvType.setTextColor(ContextCompat.getColor(context, android.R.color.holo_red_dark));
                }
                if (cvStatusBadge != null) {
                    cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
                }
            } else {
                if (ivType != null) {
                    // Use system icons if custom icons don't exist
                    ivType.setImageResource(android.R.drawable.ic_dialog_info);
                    ivType.setColorFilter(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                }
                if (tvType != null) {
                    tvType.setText("FOUND");
                    tvType.setTextColor(ContextCompat.getColor(context, android.R.color.holo_green_dark));
                }
                if (cvStatusBadge != null) {
                    cvStatusBadge.setCardBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
                }
            }

            // Date
            if (tvDate != null) {
                if (post.getCreatedAt() != null) {
                    tvDate.setText(formatDate(post.getCreatedAt()));
                } else {
                    tvDate.setText("Unknown date");
                }
            }

            // Contact
            if (tvContact != null) {
                if (post.getContact() != null && !post.getContact().isEmpty()) {
                    tvContact.setText("Contact: " + post.getContact());
                    tvContact.setVisibility(View.VISIBLE);
                } else {
                    tvContact.setText("No contact info");
                    tvContact.setVisibility(View.VISIBLE);
                }
            }

            // Image
            // Image loading with authentication headers
            if (post.getImagePath() != null && !post.getImagePath().isEmpty()) {
                if (ivImage != null && cvImage != null) {
                    cvImage.setVisibility(View.VISIBLE);
                    ivImage.setVisibility(View.VISIBLE);

                    String imageUrl = ApiClient.getImageUrl(post.getImagePath());
                    Log.d(TAG, "Loading image for post: " + post.getTitle() + ", URL: " + imageUrl);

                    if (imageUrl != null) {
                        // Get authentication token
                        SharedPreferencesManager prefsManager = SharedPreferencesManager.getInstance(context);
                        String authHeader = prefsManager.getAuthHeader();

                        GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                                .addHeader("Authorization", authHeader)
                                .build());

                        Glide.with(context)
                                .load(glideUrl)
                                .placeholder(R.drawable.placeholder_image)
                                .error(R.drawable.placeholder_image)
                                .centerCrop()
                                .listener(new RequestListener<Drawable>() {
                                    @Override
                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                        Log.e(TAG, "Failed to load image: " + imageUrl, e);
                                        return false;
                                    }

                                    @Override
                                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                        Log.d(TAG, "Image loaded successfully: " + imageUrl);
                                        return false;
                                    }
                                })
                                .into(ivImage);
                    } else {
                        Log.w(TAG, "Image URL is null for post: " + post.getTitle());
                        ivImage.setImageResource(R.drawable.placeholder_image);
                    }
                }
            } else {
                Log.d(TAG, "No image path for post: " + post.getTitle());
                if (cvImage != null) {
                    cvImage.setVisibility(View.GONE);
                }
                if (ivImage != null) {
                    ivImage.setVisibility(View.GONE);
                }
            }
        }

        private String formatDate(String dateString) {
            if (dateString == null) return "Unknown";
            try {
                // Try multiple date formats
                SimpleDateFormat[] inputFormats = {
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()),
                        new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                };

                Date date = null;
                for (SimpleDateFormat format : inputFormats) {
                    try {
                        date = format.parse(dateString);
                        break;
                    } catch (ParseException e) {
                        // Try next format
                    }
                }

                if (date != null) {
                    long diff = System.currentTimeMillis() - date.getTime();
                    long hours = diff / (1000 * 60 * 60);
                    long days = hours / 24;

                    if (hours < 1) {
                        return "Just now";
                    } else if (hours < 24) {
                        return hours + " hours ago";
                    } else if (days < 7) {
                        return days + " days ago";
                    } else {
                        SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                        return outputFormat.format(date);
                    }
                }
                return dateString;
            } catch (Exception e) {
                Log.w(TAG, "Error parsing date: " + dateString, e);
                return dateString;
            }
        }
    }
}