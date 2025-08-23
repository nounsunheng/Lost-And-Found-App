package com.kyle.lostandfoundapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.ChangePasswordRequest;
import com.kyle.lostandfoundapp.model.User;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileActivity extends AppCompatActivity {

    private TextView tvUsername, tvEmail, tvPhone, tvRole;
    private EditText etOldPassword, etNewPassword, etConfirmPassword;
    private MaterialButton btnChangePassword, btnMyPosts, btnLogout;
    private ProgressBar progressBar;

    private SharedPreferencesManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        prefsManager = SharedPreferencesManager.getInstance(this);

        initViews();
        loadUserProfile();
        setupClickListeners();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }
    }

    private void initViews() {
        tvUsername = findViewById(R.id.tvUsername);
        tvEmail = findViewById(R.id.tvEmail);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);

        etOldPassword = findViewById(R.id.etOldPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnMyPosts = findViewById(R.id.btnMyPosts);
        btnLogout = findViewById(R.id.btnLogout);

        progressBar = findViewById(R.id.progressBar);
    }

    private void setupClickListeners() {
        btnChangePassword.setOnClickListener(v -> changePassword());

        btnMyPosts.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyPostsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> {
            prefsManager.clearUserData();
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void loadUserProfile() {
        progressBar.setVisibility(View.VISIBLE);
        Call<User> call = ApiClient.getApiService().getProfile(prefsManager.getAuthHeader());
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    displayUserInfo(response.body());
                } else {
                    Toast.makeText(ProfileActivity.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayUserInfo(User user) {
        tvUsername.setText(user.getUsername());
        tvEmail.setText(user.getEmail());
        tvPhone.setText(user.getPhone());
        tvRole.setText(user.getRole());

        // Update role color
        if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            tvRole.setTextColor(getResources().getColor(R.color.purple_500, null));
        } else {
            tvRole.setTextColor(getResources().getColor(R.color.blue_500, null));
        }
    }

    private void changePassword() {
        String oldPass = etOldPassword.getText().toString().trim();
        String newPass = etNewPassword.getText().toString().trim();
        String confirmPass = etConfirmPassword.getText().toString().trim();

        // Validation
        if (oldPass.isEmpty()) {
            etOldPassword.setError("Current password is required");
            etOldPassword.requestFocus();
            return;
        }
        if (newPass.isEmpty()) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }
        if (newPass.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }
        if (!newPass.equals(confirmPass)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        setLoading(true);

        ChangePasswordRequest request = new ChangePasswordRequest(oldPass, newPass);
        Call<ResponseBody> call = ApiClient.getApiService().changePassword(prefsManager.getAuthHeader(), request);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                setLoading(false);
                if (response.isSuccessful()) {
                    Toast.makeText(ProfileActivity.this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    etOldPassword.setText("");
                    etNewPassword.setText("");
                    etConfirmPassword.setText("");
                } else {
                    if (response.code() == 400) {
                        Toast.makeText(ProfileActivity.this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ProfileActivity.this, "Failed to change password", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                setLoading(false);
                Toast.makeText(ProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnChangePassword.setEnabled(!loading);
        etOldPassword.setEnabled(!loading);
        etNewPassword.setEnabled(!loading);
        etConfirmPassword.setEnabled(!loading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
