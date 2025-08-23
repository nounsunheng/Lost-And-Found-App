package com.kyle.lostandfoundapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.adapter.PostAdapter;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPostsActivity extends AppCompatActivity implements PostAdapter.OnPostClickListener {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private LinearLayout layoutEmpty;
    private TextView tvEmpty, tvMyLostCount, tvMyFoundCount;

    private ChipGroup chipGroup;
    private Chip chipAll, chipMyLost, chipMyFound;

    private ExtendedFloatingActionButton fabAdd;

    private SharedPreferencesManager prefsManager;
    private List<Post> allPosts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);

        prefsManager = SharedPreferencesManager.getInstance(this);

        initViews();
        setupRecyclerView();
        setupSwipeRefresh();
        setupChipGroup();

        loadMyPosts();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Posts");
        }
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmpty = findViewById(R.id.tvEmpty);
        tvMyLostCount = findViewById(R.id.tvMyLostCount);
        tvMyFoundCount = findViewById(R.id.tvMyFoundCount);

        chipGroup = findViewById(R.id.chipGroup);
        chipAll = findViewById(R.id.chipAll);
        chipMyLost = findViewById(R.id.chipMyLost);
        chipMyFound = findViewById(R.id.chipMyFound);

        fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(MyPostsActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btnCreateFirst).setOnClickListener(v -> {
            Intent intent = new Intent(MyPostsActivity.this, CreatePostActivity.class);
            startActivity(intent);
        });
    }

    private void setupRecyclerView() {
        postAdapter = new PostAdapter(this, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(postAdapter);
    }

    private void setupSwipeRefresh() {
        swipeRefresh.setOnRefreshListener(this::loadMyPosts);
    }

    private void setupChipGroup() {
        chipGroup.setOnCheckedChangeListener((group, checkedId) -> filterPosts(checkedId));
    }

    private void loadMyPosts() {
        swipeRefresh.setRefreshing(true);

        Call<List<Post>> call = ApiClient.getApiService().getMyPosts(prefsManager.getAuthHeader());
        call.enqueue(new Callback<List<Post>>() {
            @Override
            public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                swipeRefresh.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allPosts = response.body();

                    int lostCount = 0, foundCount = 0;
                    for (Post post : allPosts) {
                        if (post.getIsLost() != null && post.getIsLost()) lostCount++;
                        else foundCount++;
                    }
                    tvMyLostCount.setText(String.valueOf(lostCount));
                    tvMyFoundCount.setText(String.valueOf(foundCount));

                    filterPosts(chipGroup.getCheckedChipId());
                } else {
                    Toast.makeText(MyPostsActivity.this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<Post>> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Toast.makeText(MyPostsActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyState();
            }
        });
    }

    private void filterPosts(int checkedId) {
        List<Post> filtered = new ArrayList<>();

        if (checkedId == R.id.chipAll) {
            filtered.addAll(allPosts);
        } else if (checkedId == R.id.chipMyLost) {
            for (Post lostPost : allPosts) {
                if (lostPost.getIsLost() != null && lostPost.getIsLost()) filtered.add(lostPost);
            }
        } else if (checkedId == R.id.chipMyFound) {
            for (Post foundPost : allPosts) {
                if (foundPost.getIsLost() != null && !foundPost.getIsLost()) filtered.add(foundPost);
            }
        }

        if (filtered.isEmpty()) showEmptyState();
        else showPosts(filtered);
    }

    private void showEmptyState() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showPosts(List<Post> posts) {
        layoutEmpty.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        postAdapter.updatePosts(posts);
    }

    @Override
    public void onPostClick(Post post) {
        Intent intent = new Intent(this, PostDetailActivity.class);
        intent.putExtra("post_id", post.getId());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMyPosts();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
