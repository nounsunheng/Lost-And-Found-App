package com.kyle.lostandfoundapp.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditPostActivity extends AppCompatActivity {

    private static final String TAG = "EditPostActivity";

    private TextInputEditText etTitle, etDescription;
    private RadioGroup rgStatus;
    private RadioButton rbLost, rbFound;
    private MaterialButton btnSave;
    private Toolbar toolbar;

    private SharedPreferencesManager prefsManager;
    private Post currentPost;
    private int postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);

        Log.d(TAG, "EditPostActivity started");

        prefsManager = SharedPreferencesManager.getInstance(this);
        postId = getIntent().getIntExtra("post_id", -1);

        Log.d(TAG, "Post ID: " + postId);

        if (postId == -1) {
            Log.e(TAG, "Invalid post ID");
            Toast.makeText(this, "Invalid post", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupToolbar();
        loadPostDetails();
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");

        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        rgStatus = findViewById(R.id.rgStatus);
        rbLost = findViewById(R.id.rbLost);
        rbFound = findViewById(R.id.rbFound);
        btnSave = findViewById(R.id.btnSave);

        // Log which views were found
        Log.d(TAG, "Views initialized - " +
                "etTitle: " + (etTitle != null) +
                ", etDescription: " + (etDescription != null) +
                ", rgStatus: " + (rgStatus != null) +
                ", rbLost: " + (rbLost != null) +
                ", rbFound: " + (rbFound != null) +
                ", btnSave: " + (btnSave != null) +
                ", toolbar: " + (toolbar != null));

        if (btnSave != null) {
            btnSave.setOnClickListener(v -> savePost());
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Edit Post");
            }
        }
    }

    private void loadPostDetails() {
        Log.d(TAG, "Loading post details for ID: " + postId);

        // Try to get the specific post first
        Call<Post> postCall = ApiClient.getApiService().getPost(prefsManager.getAuthHeader(), postId);
        postCall.enqueue(new Callback<Post>() {
            @Override
            public void onResponse(Call<Post> call, Response<Post> response) {
                Log.d(TAG, "Get post response - Code: " + response.code() + ", Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    currentPost = response.body();
                    Log.d(TAG, "Post loaded directly: " + currentPost.getTitle());
                    populateFields();
                } else {
                    Log.d(TAG, "Direct post fetch failed, trying to find in user's posts");
                    // Fallback: try to find it in user's posts
                    loadFromMyPosts();
                }
            }

            @Override
            public void onFailure(Call<Post> call, Throwable t) {
                Log.e(TAG, "Direct post fetch failed: " + t.getMessage());
                // Fallback: try to find it in user's posts
                loadFromMyPosts();
            }
        });
    }

    private void loadFromMyPosts() {
        Log.d(TAG, "Loading from user's posts");

        Call<List<Post>> call = ApiClient.getApiService().getMyPosts(prefsManager.getAuthHeader());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                Log.d(TAG, "My posts response - Code: " + response.code() + ", Success: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();
                    Log.d(TAG, "Found " + posts.size() + " user posts, searching for ID: " + postId);

                    for (Post post : posts) {
                        Log.d(TAG, "Checking post ID: " + post.getId() + " vs " + postId);
                        if (post.getId() != null && post.getId().equals(postId)) {
                            currentPost = post;
                            Log.d(TAG, "Found matching post: " + post.getTitle());
                            populateFields();
                            return;
                        }
                    }

                    Log.w(TAG, "Post not found in user's posts");
                    Toast.makeText(EditPostActivity.this, "Post not found or you don't have permission to edit it", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Log.e(TAG, "Failed to load user posts. Code: " + response.code());
                    Toast.makeText(EditPostActivity.this, "Failed to load post details", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                Log.e(TAG, "Failed to load user posts: " + t.getMessage(), t);
                Toast.makeText(EditPostActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void populateFields() {
        Log.d(TAG, "Populating fields with post data");

        if (currentPost == null) {
            Log.e(TAG, "Current post is null, cannot populate");
            return;
        }

        try {
            // Populate title
            if (etTitle != null) {
                String title = currentPost.getTitle() != null ? currentPost.getTitle() : "";
                etTitle.setText(title);
                Log.d(TAG, "Set title: " + title);
            }

            // Populate description
            if (etDescription != null) {
                String description = currentPost.getDescription() != null ? currentPost.getDescription() : "";
                etDescription.setText(description);
                Log.d(TAG, "Set description length: " + description.length());
            }

            // Set status radio button
            if (rgStatus != null && rbLost != null && rbFound != null) {
                if (currentPost.getIsLost() != null && currentPost.getIsLost()) {
                    rgStatus.check(R.id.rbLost);
                    Log.d(TAG, "Set status: LOST");
                } else {
                    rgStatus.check(R.id.rbFound);
                    Log.d(TAG, "Set status: FOUND");
                }
            } else {
                Log.w(TAG, "Radio button views not found");
            }

            Log.d(TAG, "Fields populated successfully");

        } catch (Exception e) {
            Log.e(TAG, "Error populating fields", e);
            Toast.makeText(this, "Error loading post data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void savePost() {
        Log.d(TAG, "=== Save Post ===");

        if (currentPost == null) {
            Log.e(TAG, "Current post is null, cannot save");
            Toast.makeText(this, "Error: Post data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        if (etTitle == null || etDescription == null || rgStatus == null) {
            Log.e(TAG, "Required views are null");
            Toast.makeText(this, "Error: Form fields not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        boolean isLost = rgStatus.getCheckedRadioButtonId() == R.id.rbLost;

        Log.d(TAG, "Form data - Title: '" + title + "', Description length: " + description.length() +
                ", IsLost: " + isLost);

        // Validation
        if (title.isEmpty()) {
            etTitle.setError("Title is required");
            etTitle.requestFocus();
            return;
        }

        if (description.isEmpty()) {
            etDescription.setError("Description is required");
            etDescription.requestFocus();
            return;
        }

        // Update post object
        currentPost.setTitle(title);
        currentPost.setDescription(description);
        currentPost.setIsLost(isLost);

        Log.d(TAG, "Updating post ID: " + postId);
        Log.d(TAG, "Auth Header: " + prefsManager.getAuthHeader());

        // Disable button during save
        if (btnSave != null) {
            btnSave.setEnabled(false);
            btnSave.setText("Saving...");
        }

        Call<ResponseBody> call = ApiClient.getApiService().updatePost(
                prefsManager.getAuthHeader(),
                postId,
                currentPost
        );

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "=== Update Response ===");
                Log.d(TAG, "Response Code: " + response.code());
                Log.d(TAG, "Response Success: " + response.isSuccessful());
                Log.d(TAG, "Response Message: " + response.message());

                // Re-enable button
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                }

                if (response.isSuccessful()) {
                    Log.d(TAG, "Post updated successfully");
                    Toast.makeText(EditPostActivity.this, "Post updated successfully", Toast.LENGTH_SHORT).show();

                    // Set result and finish
                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMsg = "Failed to update post. Code: " + response.code();

                    if (response.errorBody() != null) {
                        try {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);

                            // Parse error for user-friendly message
                            if (errorBody.contains("unauthorized") || response.code() == 401) {
                                errorMsg = "Session expired. Please login again";
                            } else if (errorBody.contains("forbidden") || response.code() == 403) {
                                errorMsg = "You don't have permission to edit this post";
                            } else if (errorBody.contains("not found") || response.code() == 404) {
                                errorMsg = "Post not found";
                            } else {
                                errorMsg = "Update failed: " + errorBody;
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body", e);
                        }
                    }

                    Log.e(TAG, errorMsg);
                    Toast.makeText(EditPostActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "=== Update Failed ===");
                Log.e(TAG, "Error: " + t.getMessage(), t);

                // Re-enable button
                if (btnSave != null) {
                    btnSave.setEnabled(true);
                    btnSave.setText("Save Changes");
                }

                String errorMsg;
                if (t.getMessage() != null) {
                    if (t.getMessage().contains("timeout")) {
                        errorMsg = "Request timeout. Please try again";
                    } else if (t.getMessage().contains("Unable to resolve host")) {
                        errorMsg = "Cannot connect to server. Please check your internet connection";
                    } else {
                        errorMsg = "Network error: " + t.getMessage();
                    }
                } else {
                    errorMsg = "Unknown network error occurred";
                }

                Toast.makeText(EditPostActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}