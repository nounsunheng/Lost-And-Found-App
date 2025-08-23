package com.kyle.lostandfoundapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostDetailActivity extends AppCompatActivity {

    private static final String TAG = "PostDetailActivity";

    private ImageView ivHeroImage, ivType;
    private TextView tvTitle, tvDescription, tvDate, tvContact, tvType;
    private MaterialButton btnCall, btnMessage;
    private Toolbar toolbar;

    private SharedPreferencesManager prefsManager;
    private Post currentPost;
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        Log.d(TAG, "PostDetailActivity started");

        prefsManager = SharedPreferencesManager.getInstance(this);
        postId = getIntent().getIntExtra("post_id", -1);

        if (postId == -1) {
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d(TAG, "Loading post with ID: " + postId);

        initViews();
        setupToolbar();
        loadPostDetails();
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");

        toolbar = findViewById(R.id.toolbar);
        ivHeroImage = findViewById(R.id.ivHeroImage);
        ivType = findViewById(R.id.ivType);
        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);
        tvDate = findViewById(R.id.tvDate);
        tvContact = findViewById(R.id.tvContact);
        tvType = findViewById(R.id.tvType);
        btnCall = findViewById(R.id.btnCall);
        btnMessage = findViewById(R.id.btnMessage);

        // Set click listeners
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> makeCall());
        }
        if (btnMessage != null) {
            btnMessage.setOnClickListener(v -> sendMessage());
        }

        Log.d(TAG, "Views initialized - " +
                "toolbar: " + (toolbar != null) +
                ", tvTitle: " + (tvTitle != null) +
                ", btnCall: " + (btnCall != null));
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Post Details");
            }

            // Handle navigation back
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
            });
        }
    }

    private void loadPostDetails() {
        Log.d(TAG, "Loading post details");

        // Since the API doesn't have a specific endpoint for single post,
        // we'll get all posts and find the one we need
        Call<List<Post>> call = ApiClient.getApiService().getAllPosts(prefsManager.getAuthHeader());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                Log.d(TAG, "Posts loaded for detail view. Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    Log.d(TAG, "Searching through " + posts.size() + " posts for ID: " + postId);

                    for (Post post : posts) {
                        if (post != null && post.getId() != null && post.getId() == postId) {
                            Log.d(TAG, "Found matching post: " + post.getTitle());
                            currentPost = post;
                            displayPostDetails();
                            return;
                        }
                    }

                    Log.w(TAG, "Post not found with ID: " + postId);
                    Toast.makeText(PostDetailActivity.this, "Post not found", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to load posts. Code: " + response.code());
                    Toast.makeText(PostDetailActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, "Network error loading post details", t);
                Toast.makeText(PostDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void displayPostDetails() {
        if (currentPost == null) {
            Log.e(TAG, "Cannot display details - currentPost is null");
            return;
        }

        Log.d(TAG, "Displaying post details for: " + currentPost.getTitle());

        // Title
        if (tvTitle != null) {
            tvTitle.setText(currentPost.getTitle() != null ? currentPost.getTitle() : "No Title");
        }

        // Description
        if (tvDescription != null) {
            tvDescription.setText(currentPost.getDescription() != null ? currentPost.getDescription() : "No description available");
        }

        // Type and status
        if (tvType != null && ivType != null) {
            boolean isLost = currentPost.getIsLost() != null && currentPost.getIsLost();
            tvType.setText(isLost ? "LOST ITEM" : "FOUND ITEM");
            ivType.setImageResource(isLost ? R.drawable.ic_lost : R.drawable.ic_found);
        }

        // Date
        if (tvDate != null) {
            String formattedDate = formatDate(currentPost.getCreatedAt());
            tvDate.setText(formattedDate != null ? formattedDate : "Date unknown");
        }

        // Contact info
        if (tvContact != null) {
            String contact = currentPost.getContact();
            if (contact != null && !contact.trim().isEmpty()) {
                tvContact.setText(contact);
                tvContact.setVisibility(View.VISIBLE);
            } else {
                tvContact.setText("No contact information provided");
                tvContact.setVisibility(View.VISIBLE);
            }
        }

        // Contact buttons
        boolean hasContact = currentPost.getContact() != null && !currentPost.getContact().trim().isEmpty();
        if (btnCall != null) {
            btnCall.setVisibility(hasContact ? View.VISIBLE : View.GONE);
        }
        if (btnMessage != null) {
            btnMessage.setVisibility(hasContact ? View.VISIBLE : View.GONE);
        }

        // Hero image with authentication
        if (ivHeroImage != null) {
            if (currentPost.getImagePath() != null && !currentPost.getImagePath().isEmpty()) {
                String imageUrl = ApiClient.getImageUrl(currentPost.getImagePath());
                Log.d(TAG, "Loading image: " + imageUrl);

                if (imageUrl != null) {
                    // Create GlideUrl with auth headers
                    GlideUrl glideUrl = new GlideUrl(imageUrl, new LazyHeaders.Builder()
                            .addHeader("Authorization", prefsManager.getAuthHeader())
                            .build());

                    Glide.with(this)
                            .load(glideUrl)
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.placeholder_image)
                            .into(ivHeroImage);
                } else {
                    ivHeroImage.setImageResource(R.drawable.placeholder_image);
                }
            } else {
                Log.d(TAG, "No image path provided, using placeholder");
                ivHeroImage.setImageResource(R.drawable.placeholder_image);
            }
        }

        // Invalidate menu to update edit/delete button visibility
        invalidateOptionsMenu();
        Log.d(TAG, "Post details displayed successfully");
    }

    private String formatDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(dateString);
            if (date != null) {
                SimpleDateFormat outputFormat = new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault());
                return "Posted on " + outputFormat.format(date);
            }
        } catch (ParseException e) {
            Log.w(TAG, "Error parsing date: " + dateString, e);
        }

        return "Posted on " + dateString;
    }

    private void makeCall() {
        if (currentPost != null && currentPost.getContact() != null) {
            String contact = currentPost.getContact().trim();
            Log.d(TAG, "Attempting to make call to: " + contact);

            // Extract phone number if contact contains both email and phone
            String phoneNumber = extractPhoneNumber(contact);

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + phoneNumber));

                try {
                    startActivity(callIntent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting call intent", e);
                    Toast.makeText(this, "Unable to make call", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "No phone number available", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void sendMessage() {
        if (currentPost != null && currentPost.getContact() != null) {
            String contact = currentPost.getContact().trim();
            Log.d(TAG, "Attempting to send message to: " + contact);

            // Extract phone number if contact contains both email and phone
            String phoneNumber = extractPhoneNumber(contact);

            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                Intent smsIntent = new Intent(Intent.ACTION_SENDTO);
                smsIntent.setData(Uri.parse("smsto:" + phoneNumber));
                smsIntent.putExtra("sms_body", "Hi, I saw your post about: " + currentPost.getTitle());

                try {
                    if (smsIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(smsIntent);
                    } else {
                        Toast.makeText(this, "No SMS app found", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error starting SMS intent", e);
                    Toast.makeText(this, "Unable to send message", Toast.LENGTH_SHORT).show();
                }
            } else {
                // If no phone number, try to send email
                String email = extractEmail(contact);
                if (email != null && !email.isEmpty()) {
                    Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                    emailIntent.setData(Uri.parse("mailto:" + email));
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Regarding: " + currentPost.getTitle());
                    emailIntent.putExtra(Intent.EXTRA_TEXT, "Hi, I saw your post about: " + currentPost.getTitle());

                    try {
                        if (emailIntent.resolveActivity(getPackageManager()) != null) {
                            startActivity(emailIntent);
                        } else {
                            Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error starting email intent", e);
                        Toast.makeText(this, "Unable to send email", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "No contact method available", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private String extractPhoneNumber(String contact) {
        // Look for phone number patterns
        if (contact.contains("•")) {
            String[] parts = contact.split("•");
            for (String part : parts) {
                part = part.trim();
                if (part.matches(".*\\+?[0-9\\(\\)\\-\\s]{10,}.*")) {
                    return part.replaceAll("[^\\+0-9]", "");
                }
            }
        } else if (contact.matches(".*\\+?[0-9\\(\\)\\-\\s]{10,}.*")) {
            return contact.replaceAll("[^\\+0-9]", "");
        }
        return null;
    }

    private String extractEmail(String contact) {
        // Look for email patterns
        if (contact.contains("@")) {
            if (contact.contains("•")) {
                String[] parts = contact.split("•");
                for (String part : parts) {
                    part = part.trim();
                    if (part.contains("@") && part.matches(".*[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}.*")) {
                        return part;
                    }
                }
            } else {
                return contact.trim();
            }
        }
        return null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_detail_menu, menu);

        MenuItem deleteItem = menu.findItem(R.id.menu_delete);
        MenuItem editItem = menu.findItem(R.id.menu_edit);

        if (currentPost != null) {
            boolean isOwner = currentPost.getUserId() != null &&
                    currentPost.getUserId().equals(prefsManager.getUserId());
            boolean isAdmin = prefsManager.isAdmin();

            Log.d(TAG, "Menu visibility - IsOwner: " + isOwner + ", IsAdmin: " + isAdmin +
                    ", CurrentUserId: " + prefsManager.getUserId() +
                    ", PostUserId: " + currentPost.getUserId());

            if (deleteItem != null) {
                deleteItem.setVisible(isOwner || isAdmin);
            }
            if (editItem != null) {
                editItem.setVisible(isOwner);
            }
        } else {
            Log.w(TAG, "currentPost is null when creating menu");
            if (deleteItem != null) deleteItem.setVisible(false);
            if (editItem != null) editItem.setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "Menu item selected: " + id);

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_delete) {
            showDeleteConfirmationDialog();
            return true;
        } else if (id == R.id.menu_edit) {
            if (currentPost != null && currentPost.getId() != null) {
                Intent intent = new Intent(this, EditPostActivity.class);
                intent.putExtra("post_id", currentPost.getId());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid post. Cannot edit.", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.menu_share) {
            sharePost();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void sharePost() {
        if (currentPost != null) {
            String shareText = "Check out this " +
                    (currentPost.getIsLost() ? "lost" : "found") +
                    " item: " + currentPost.getTitle();

            if (currentPost.getDescription() != null) {
                shareText += "\n\nDescription: " + currentPost.getDescription();
            }

            if (currentPost.getContact() != null) {
                shareText += "\n\nContact: " + currentPost.getContact();
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Lost & Found: " + currentPost.getTitle());

            try {
                startActivity(Intent.createChooser(shareIntent, "Share post"));
            } catch (Exception e) {
                Log.e(TAG, "Error sharing post", e);
                Toast.makeText(this, "Unable to share post", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePost())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost() {
        Log.d(TAG, "Deleting post with ID: " + postId);

        Call<ResponseBody> call = ApiClient.getApiService().deletePost(prefsManager.getAuthHeader(), postId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Delete response code: " + response.code());

                if (response.isSuccessful()) {
                    Toast.makeText(PostDetailActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK); // Notify calling activity
                    finish();
                } else {
                    String errorMsg = "Failed to delete post. Code: " + response.code();
                    Log.e(TAG, errorMsg);
                    Toast.makeText(PostDetailActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Delete request failed", t);
                Toast.makeText(PostDetailActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}