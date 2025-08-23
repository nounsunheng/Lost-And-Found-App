package com.kyle.lostandfoundapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.adapter.AdminPostAdapter;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends AppCompatActivity implements AdminPostAdapter.OnAdminPostActionListener {

    private RecyclerView recyclerView;
    private AdminPostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private TextView tvEmpty;
    private TextView tvTotalPosts; // Total Posts TextView
    private ChipGroup chipGroup;

    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        prefsManager = SharedPreferencesManager.getInstance(this);

        // Check if user is admin
        if (!prefsManager.isAdmin()) {
            Toast.makeText(this, "Access denied. Admin privileges required.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupChipGroup();
        loadAllPosts();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Admin Panel");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        tvEmpty = findViewById(R.id.tvEmpty);
        chipGroup = findViewById(R.id.chipGroup);
        tvTotalPosts = findViewById(R.id.tvTotalPosts); // link XML TextView
    }

    private void setupRecyclerView() {
        postAdapter = new AdminPostAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadAllPosts);
    }

    private void setupChipGroup() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                postAdapter.filterPosts("all");
            } else if (checkedId == R.id.chipLost) {
                postAdapter.filterPosts("lost");
            } else if (checkedId == R.id.chipFound) {
                postAdapter.filterPosts("found");
            } else if (checkedId == R.id.chipReported) {
                postAdapter.filterPosts("reported");
            }
        });
    }

    private void loadAllPosts() {
        swipeRefresh.setRefreshing(true);

        Call<List<Post>> call = ApiClient.getApiService().getAllPosts(prefsManager.getAuthHeader());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    List<Post> posts = response.body();

                    // Update total posts count
                    tvTotalPosts.setText(String.valueOf(posts.size()));

                    if (posts.isEmpty()) {
                        tvEmpty.setVisibility(TextView.VISIBLE);
                        recyclerView.setVisibility(RecyclerView.GONE);
                    } else {
                        tvEmpty.setVisibility(TextView.GONE);
                        recyclerView.setVisibility(RecyclerView.VISIBLE);
                        postAdapter.updatePosts(posts);

                        // Apply current filter again after refresh
                        int checkedId = chipGroup.getCheckedChipId();
                        if (checkedId != -1) {
                            chipGroup.check(checkedId);
                        }
                    }
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(AdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("post_id", post.getId());
        startActivity(intent);
    }

    @Override
    public void onDeletePost(Post post) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post?\n\nTitle: " + post.getTitle())
                .setPositiveButton("Delete", (dialog, which) -> deletePost(post))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePost(Post post) {
        Call<ResponseBody> call = ApiClient.getApiService().deletePost(prefsManager.getAuthHeader(), post.getId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminActivity.this, "Post deleted successfully", Toast.LENGTH_SHORT).show();
                    loadAllPosts(); // Refresh the list
                } else {
                    Toast.makeText(AdminActivity.this, "Failed to delete post", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(AdminActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAllPosts(); // Refresh when returning
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
