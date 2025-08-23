package com.kyle.lostandfoundapp;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.kyle.lostandfoundapp.activity.AdminActivity;
import com.kyle.lostandfoundapp.activity.CreatePostActivity;
import com.kyle.lostandfoundapp.activity.LoginActivity;
import com.kyle.lostandfoundapp.activity.MyPostsActivity;
import com.kyle.lostandfoundapp.activity.PostDetailActivity;
import com.kyle.lostandfoundapp.activity.ProfileActivity;
import com.kyle.lostandfoundapp.adapter.PostAdapter;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.LocaleManager;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        PostAdapter.OnPostClickListener,
        NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";
    private static final int CREATE_POST_REQUEST = 100;

    // Pagination constants
    private static final int POSTS_PER_PAGE = 15;

    // Views
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private SwipeRefreshLayout swipeRefresh;
    private SearchView searchView;
    private ChipGroup chipGroup;
    private ExtendedFloatingActionButton fabAdd;
    private TextView tvEmptyState;
    private ProgressBar progressLoadMore;

    // Navigation header views
    private TextView tvNavUsername, tvNavEmail;
    private ImageView ivProfileImage;

    private SharedPreferencesManager prefsManager;
    private LocaleManager localeManager;
    private List<Post> allPosts = new ArrayList<>();
    private List<Post> filteredPosts = new ArrayList<>();
    private String currentQuery = "";
    private Boolean currentFilter = null; // null = all, true = lost, false = found

    // Pagination variables
    private int currentPage = 0;
    private boolean isLoadingMore = false;
    private boolean hasMorePages = true;

    @Override
    protected void attachBaseContext(Context newBase) {
        // Apply language before activity is created
        SharedPreferencesManager tempPrefs = SharedPreferencesManager.getInstance(newBase);
        String language = tempPrefs.getLanguage();
        Context context = LocaleManager.setLocale(newBase, language);
        super.attachBaseContext(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== MainActivity onCreate ===");

        // Initialize managers first
        prefsManager = SharedPreferencesManager.getInstance(this);
        localeManager = new LocaleManager(this);

        // Apply theme before setting content view
        applyTheme();

        try {
            setContentView(R.layout.activity_main);
            Log.d(TAG, "Layout set successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error setting layout", e);
            Toast.makeText(this, "Error loading layout: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "SharedPreferencesManager initialized");

        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            Log.d(TAG, "User not logged in, redirecting to LoginActivity");
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Log.d(TAG, "User is logged in: " + prefsManager.getEmail());

        initViews();
        setupToolbar();
        setupNavigationDrawer();
        setupRecyclerView();
        setupSwipeRefresh();
        setupSearch();
        setupFilters();
        setupPagination();
        updateNavigationHeader();
        updateNavigationMenuState();

        // Load posts after all setup is complete
        Log.d(TAG, "Starting to load posts...");
        loadPosts(true); // true = reset pagination
    }

    private void applyTheme() {
        int themeMode = prefsManager.getThemeMode();
        Log.d(TAG, "Applying theme mode: " + themeMode);

        // Map custom theme modes to AppCompat modes
        int appCompatMode;
        switch (themeMode) {
            case SharedPreferencesManager.THEME_MODE_LIGHT:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case SharedPreferencesManager.THEME_MODE_DARK:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            case SharedPreferencesManager.THEME_MODE_SYSTEM:
            default:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }

        AppCompatDelegate.setDefaultNightMode(appCompatMode);
    }

    private void initViews() {
        Log.d(TAG, "=== Initializing Views ===");

        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        recyclerView = findViewById(R.id.recyclerView);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        searchView = findViewById(R.id.searchView);
        chipGroup = findViewById(R.id.chipGroup);
        fabAdd = findViewById(R.id.fabAdd);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressLoadMore = findViewById(R.id.progressLoadMore);

        // Get navigation header views
        View headerView = navigationView.getHeaderView(0);
        tvNavUsername = headerView.findViewById(R.id.tvNavUsername);
        tvNavEmail = headerView.findViewById(R.id.tvNavEmail);
        ivProfileImage = headerView.findViewById(R.id.ivProfileImage);

        // Update search hint text
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.search_hint));
        }

        // Log which views were found
        Log.d(TAG, "View status:");
        Log.d(TAG, "  - drawerLayout: " + (drawerLayout != null));
        Log.d(TAG, "  - navigationView: " + (navigationView != null));
        Log.d(TAG, "  - recyclerView: " + (recyclerView != null));
        Log.d(TAG, "  - swipeRefresh: " + (swipeRefresh != null));
        Log.d(TAG, "  - searchView: " + (searchView != null));
        Log.d(TAG, "  - chipGroup: " + (chipGroup != null));
        Log.d(TAG, "  - fabAdd: " + (fabAdd != null));
        Log.d(TAG, "  - tvEmptyState: " + (tvEmptyState != null));
        Log.d(TAG, "  - progressLoadMore: " + (progressLoadMore != null));

        if (fabAdd != null) {
            fabAdd.setOnClickListener(v -> {
                Log.d(TAG, "FAB clicked - starting CreatePostActivity");
                try {
                    Intent intent = new Intent(this, CreatePostActivity.class);
                    startActivityForResult(intent, CREATE_POST_REQUEST);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting CreatePostActivity", e);
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void setupToolbar() {
        Log.d(TAG, "=== Setting up Toolbar ===");
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(getString(R.string.app_name));
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
            }
            Log.d(TAG, "Toolbar set successfully");
        } else {
            Log.w(TAG, "Toolbar not found in layout");
        }
    }

    private void setupNavigationDrawer() {
        Log.d(TAG, "=== Setting up Navigation Drawer ===");

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);

        // Show/hide admin menu item
        MenuItem adminItem = navigationView.getMenu().findItem(R.id.nav_admin);
        if (adminItem != null) {
            adminItem.setVisible(prefsManager.isAdmin());
        }

        Log.d(TAG, "Navigation drawer setup completed");
    }

    private void updateNavigationMenuState() {
        if (navigationView == null) return;

        // Update theme selection
        int currentTheme = prefsManager.getThemeMode();
        switch (currentTheme) {
            case SharedPreferencesManager.THEME_MODE_LIGHT:
                navigationView.getMenu().findItem(R.id.nav_theme_light).setChecked(true);
                break;
            case SharedPreferencesManager.THEME_MODE_DARK:
                navigationView.getMenu().findItem(R.id.nav_theme_dark).setChecked(true);
                break;
            case SharedPreferencesManager.THEME_MODE_SYSTEM:
            default:
                navigationView.getMenu().findItem(R.id.nav_theme_system).setChecked(true);
                break;
        }

        // Update language selection - English is default
        String currentLanguage = prefsManager.getLanguage();
        if (SharedPreferencesManager.LANGUAGE_KHMER.equals(currentLanguage)) {
            navigationView.getMenu().findItem(R.id.nav_lang_khmer).setChecked(true);
        } else {
            // Default to English (covers both explicit "en" and null/default cases)
            navigationView.getMenu().findItem(R.id.nav_lang_english).setChecked(true);
        }

        Log.d(TAG, "Navigation menu state updated - Theme: " + currentTheme + ", Language: " + currentLanguage);
    }

    private void setupRecyclerView() {
        Log.d(TAG, "=== Setting up RecyclerView ===");

        if (recyclerView != null) {
            try {
                postAdapter = new PostAdapter(this, this);
                LinearLayoutManager layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.setAdapter(postAdapter);
                recyclerView.setHasFixedSize(true);
                Log.d(TAG, "RecyclerView setup completed successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error setting up RecyclerView", e);
                Toast.makeText(this, "RecyclerView error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e(TAG, "RecyclerView is null - cannot setup");
        }
    }

    private void setupSwipeRefresh() {
        if (swipeRefresh != null) {
            swipeRefresh.setOnRefreshListener(() -> {
                Log.d(TAG, "Swipe refresh triggered");
                loadPosts(true); // Reset pagination on refresh
            });
            swipeRefresh.setColorSchemeResources(
                    android.R.color.holo_blue_bright,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light
            );
            Log.d(TAG, "SwipeRefresh setup completed");
        }
    }

    private void setupSearch() {
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d(TAG, "Search submitted: " + query);
                    currentQuery = query.trim();
                    performSearch();
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    currentQuery = newText.trim();
                    if (currentQuery.isEmpty()) {
                        applyFilters();
                    } else {
                        performSearch();
                    }
                    return true;
                }
            });
            Log.d(TAG, "Search setup completed");
        }
    }

    private void setupFilters() {
        if (chipGroup != null) {
            chipGroup.check(R.id.chipAll);
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty()) {
                    currentFilter = null;
                    chipGroup.check(R.id.chipAll);
                } else {
                    int checkedId = checkedIds.get(0);
                    if (checkedId == R.id.chipLost) {
                        currentFilter = true;
                    } else if (checkedId == R.id.chipFound) {
                        currentFilter = false;
                    } else {
                        currentFilter = null;
                    }
                }

                if (currentQuery.isEmpty()) {
                    applyFilters();
                } else {
                    performSearch();
                }
            });
            Log.d(TAG, "Filters setup completed");
        }
    }

    private void setupPagination() {
        if (recyclerView != null) {
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                    if (layoutManager != null && hasMorePages && !isLoadingMore) {
                        int visibleItemCount = layoutManager.getChildCount();
                        int totalItemCount = layoutManager.getItemCount();
                        int pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

                        if ((visibleItemCount + pastVisibleItems) >= totalItemCount - 5) {
                            loadMorePosts();
                        }
                    }
                }
            });
            Log.d(TAG, "Pagination setup completed");
        }
    }

    private void updateNavigationHeader() {
        if (tvNavUsername != null) {
            tvNavUsername.setText(prefsManager.getUsername());
        }
        if (tvNavEmail != null) {
            tvNavEmail.setText(prefsManager.getEmail());
        }
        Log.d(TAG, "Navigation header updated");
    }

    private void loadPosts(boolean resetPagination) {
        Log.d(TAG, "=== Loading Posts (Reset: " + resetPagination + ") ===");

        if (resetPagination) {
            currentPage = 0;
            hasMorePages = true;
            allPosts.clear();
            filteredPosts.clear();
            if (postAdapter != null) {
                postAdapter.updatePosts(new ArrayList<>());
            }
        }

        if (swipeRefresh != null && currentPage == 0) {
            swipeRefresh.setRefreshing(true);
        }

        // Debug API client
        Log.d(TAG, "Auth Token: " + (prefsManager.getToken() != null ? "Present" : "Null"));
        Log.d(TAG, "Auth Header: " + prefsManager.getAuthHeader());

        try {
            Call<List<Post>> call = ApiClient.getApiService().getAllPosts(prefsManager.getAuthHeader());
            Log.d(TAG, "API call created, executing...");

            call.enqueue(new Callback<List<Post>>() {
                @Override
                public void onResponse(Call<List<Post>> call, Response<List<Post>> response) {
                    Log.d(TAG, "=== API Response Received ===");
                    Log.d(TAG, "Response Code: " + response.code());
                    Log.d(TAG, "Response Success: " + response.isSuccessful());

                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    isLoadingMore = false;
                    if (progressLoadMore != null) {
                        progressLoadMore.setVisibility(View.GONE);
                    }

                    if (response.isSuccessful() && response.body() != null) {
                        List<Post> newPosts = response.body();
                        Log.d(TAG, "Posts loaded successfully: " + newPosts.size() + " posts");

                        // Sort posts by date (newest first)
                        sortPostsByDate(newPosts);

                        if (resetPagination) {
                            allPosts = newPosts;
                        } else {
                            allPosts.addAll(newPosts);
                        }

                        // Apply pagination
                        applyPagination();

                        if (currentQuery.isEmpty()) {
                            applyFilters();
                        } else {
                            performSearch();
                        }
                    } else {
                        String errorMsg = getString(R.string.failed_to_load_posts) + ". Code: " + response.code();
                        Log.e(TAG, errorMsg);
                        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        updatePostsList(new ArrayList<>());
                    }
                }

                @Override
                public void onFailure(Call<List<Post>> call, Throwable t) {
                    Log.e(TAG, "=== API Call Failed ===", t);

                    if (swipeRefresh != null) {
                        swipeRefresh.setRefreshing(false);
                    }
                    isLoadingMore = false;
                    if (progressLoadMore != null) {
                        progressLoadMore.setVisibility(View.GONE);
                    }

                    String errorMsg = getString(R.string.network_error) + ": " + t.getMessage();
                    Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    updatePostsList(new ArrayList<>());
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error creating API call", e);
            if (swipeRefresh != null) {
                swipeRefresh.setRefreshing(false);
            }
            isLoadingMore = false;
            Toast.makeText(this, getString(R.string.error) + ": " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void loadMorePosts() {
        if (isLoadingMore || !hasMorePages) return;

        Log.d(TAG, "Loading more posts...");
        isLoadingMore = true;
        currentPage++;

        if (progressLoadMore != null) {
            progressLoadMore.setVisibility(View.VISIBLE);
        }

        // Simulate loading more posts from existing data
        applyPagination();

        // Hide loading indicator after a short delay
        if (progressLoadMore != null) {
            progressLoadMore.postDelayed(() -> {
                progressLoadMore.setVisibility(View.GONE);
                isLoadingMore = false;
            }, 1000);
        }
    }

    private void applyPagination() {
        int startIndex = 0;
        int endIndex = Math.min((currentPage + 1) * POSTS_PER_PAGE, allPosts.size());

        hasMorePages = endIndex < allPosts.size();

        Log.d(TAG, "Pagination: showing " + endIndex + " of " + allPosts.size() + " posts, hasMore: " + hasMorePages);
        isLoadingMore = false;
    }

    private void sortPostsByDate(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return;

        Collections.sort(posts, new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                // Sort by newest first (descending order)
                if (p1.getCreatedAt() == null && p2.getCreatedAt() == null) return 0;
                if (p1.getCreatedAt() == null) return 1;
                if (p2.getCreatedAt() == null) return -1;
                return p2.getCreatedAt().compareTo(p1.getCreatedAt());
            }
        });
    }

    private void performSearch() {
        Log.d(TAG, "Performing search: '" + currentQuery + "', filter: " + currentFilter);

        if (currentQuery.isEmpty()) {
            applyFilters();
            return;
        }

        List<Post> results = new ArrayList<>();
        String query = currentQuery.toLowerCase();

        // Apply pagination to search results
        int maxItems = (currentPage + 1) * POSTS_PER_PAGE;

        for (Post post : allPosts) {
            if (results.size() >= maxItems) break;

            // Apply type filter first if set
            if (currentFilter != null && !post.getIsLost().equals(currentFilter)) {
                continue;
            }

            // Search in title, description, and contact
            boolean matchFound = false;
            if (post.getTitle() != null && post.getTitle().toLowerCase().contains(query)) {
                matchFound = true;
            }
            if (!matchFound && post.getDescription() != null && post.getDescription().toLowerCase().contains(query)) {
                matchFound = true;
            }
            if (!matchFound && post.getContact() != null && post.getContact().toLowerCase().contains(query)) {
                matchFound = true;
            }

            if (matchFound) {
                results.add(post);
            }
        }

        Log.d(TAG, "Search found " + results.size() + " results");
        updatePostsList(results);
    }

    private void applyFilters() {
        Log.d(TAG, "Applying filters. Filter: " + currentFilter + ", Total posts: " + allPosts.size());

        List<Post> results = new ArrayList<>();
        int maxItems = (currentPage + 1) * POSTS_PER_PAGE;

        for (Post post : allPosts) {
            if (results.size() >= maxItems) break;

            if (currentFilter == null || post.getIsLost().equals(currentFilter)) {
                results.add(post);
            }
        }

        Log.d(TAG, "Filter applied. Showing " + results.size() + " / " + allPosts.size() + " posts");
        updatePostsList(results);
    }

    private void updatePostsList(List<Post> posts) {
        Log.d(TAG, "=== Updating Posts List ===");
        Log.d(TAG, "Updating with " + (posts != null ? posts.size() : 0) + " posts");

        filteredPosts = posts != null ? posts : new ArrayList<>();

        // Show/hide empty state
        if (tvEmptyState != null) {
            if (filteredPosts.isEmpty()) {
                tvEmptyState.setVisibility(View.VISIBLE);
                Log.d(TAG, "Showing empty state");
            } else {
                tvEmptyState.setVisibility(View.GONE);
                Log.d(TAG, "Hiding empty state");
            }
        }

        if (postAdapter != null) {
            postAdapter.updatePosts(filteredPosts);
            Log.d(TAG, "Adapter updated successfully");

            // Scroll to top only on refresh
            if (recyclerView != null && !filteredPosts.isEmpty() && currentPage == 0) {
                recyclerView.scrollToPosition(0);
            }
        } else {
            Log.e(TAG, "PostAdapter is null - cannot update!");
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        Log.d(TAG, "Navigation item selected: " + id);

        try {
            if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
            } else if (id == R.id.nav_my_posts) {
                startActivity(new Intent(this, MyPostsActivity.class));
            } else if (id == R.id.nav_theme_light) {
                changeTheme(SharedPreferencesManager.THEME_MODE_LIGHT);
            } else if (id == R.id.nav_theme_dark) {
                changeTheme(SharedPreferencesManager.THEME_MODE_DARK);
            } else if (id == R.id.nav_theme_system) {
                changeTheme(SharedPreferencesManager.THEME_MODE_SYSTEM);
            } else if (id == R.id.nav_lang_english) {
                changeLanguage(SharedPreferencesManager.LANGUAGE_ENGLISH);
            } else if (id == R.id.nav_lang_khmer) {
                changeLanguage(SharedPreferencesManager.LANGUAGE_KHMER);
            } else if (id == R.id.nav_admin) {
                if (prefsManager.isAdmin()) {
                    startActivity(new Intent(this, AdminActivity.class));
                }
            } else if (id == R.id.nav_logout) {
                logout();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling navigation item", e);
            Toast.makeText(this, getString(R.string.error) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void changeTheme(int themeMode) {
        Log.d(TAG, "Changing theme to: " + themeMode);

        // Save theme preference
        prefsManager.setThemeMode(themeMode);

        // Apply theme immediately
        int appCompatMode;
        switch (themeMode) {
            case SharedPreferencesManager.THEME_MODE_LIGHT:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_NO;
                Toast.makeText(this, getString(R.string.theme_light), Toast.LENGTH_SHORT).show();
                break;
            case SharedPreferencesManager.THEME_MODE_DARK:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_YES;
                Toast.makeText(this, getString(R.string.theme_dark), Toast.LENGTH_SHORT).show();
                break;
            case SharedPreferencesManager.THEME_MODE_SYSTEM:
            default:
                appCompatMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                Toast.makeText(this, getString(R.string.theme_system), Toast.LENGTH_SHORT).show();
                break;
        }

        AppCompatDelegate.setDefaultNightMode(appCompatMode);

        // Update menu state
        updateNavigationMenuState();

        Log.d(TAG, "Theme changed successfully to: " + themeMode);
    }

    private void changeLanguage(String languageCode) {
        Log.d(TAG, "Changing language to: " + languageCode);

        // Don't change if already selected
        if (languageCode.equals(prefsManager.getLanguage())) {
            Log.d(TAG, "Language already selected: " + languageCode);
            return;
        }

        // Save language preference
        prefsManager.setLanguage(languageCode);

        // Show appropriate message before language change
        String message = languageCode.equals(SharedPreferencesManager.LANGUAGE_KHMER) ?
                "ការផ្លាស់ប្តូរភាសាខ្មែរ..." : "Language changed to English...";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Apply language change using LocaleManager
        try {
            localeManager.setNewLocale(this, languageCode);
        } catch (Exception e) {
            Log.e(TAG, "Error changing language", e);
            // Fallback: recreate activity manually
            recreate();
        }
    }

    private void logout() {
        Log.d(TAG, "Logging out user");
        prefsManager.clearUserData();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onPostClick(Post post) {
        Log.d(TAG, "Post clicked: " + (post != null ? post.getId() : "null"));
        try {
            Intent intent = new Intent(this, PostDetailActivity.class);
            intent.putExtra("post_id", post.getId());
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening post detail", e);
            Toast.makeText(this, getString(R.string.error) + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_POST_REQUEST && resultCode == RESULT_OK) {
            Log.d(TAG, "Post created successfully, refreshing posts");
            loadPosts(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Activity resumed, updating UI");

        // Update navigation menu state in case preferences changed
        updateNavigationMenuState();

        // Reload posts
        loadPosts(true);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Activity destroyed");
    }
}