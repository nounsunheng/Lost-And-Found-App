package com.kyle.lostandfoundapp.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.kyle.lostandfoundapp.R;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.network.ApiClient;
import com.kyle.lostandfoundapp.network.ApiService;
import com.kyle.lostandfoundapp.utils.SharedPreferencesManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CreatePostActivity extends AppCompatActivity {

    private static final String TAG = "CreatePostActivity";
    private static final int CAMERA_PERMISSION_REQUEST = 100;
    private static final int STORAGE_PERMISSION_REQUEST = 101;

    private EditText etTitle, etDescription, etContact;
    private RadioButton rbLost, rbFound;
    private ImageView ivImage, btnRemoveImage;
    private MaterialButton btnSelectImage, btnTakePhoto, btnSubmit;
    private ProgressBar progressBar;
    private CardView cardLost, cardFound, cvImagePreview;
    private Toolbar toolbar;

    private SharedPreferencesManager prefsManager;
    private Uri selectedImageUri;
    private Uri cameraImageUri;

    private ActivityResultLauncher<Intent> galleryLauncher;
    private ActivityResultLauncher<Intent> cameraLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        Log.d(TAG, "CreatePostActivity started");

        prefsManager = SharedPreferencesManager.getInstance(this);

        initViews();
        setupToolbar();
        setupClickListeners();
        setupActivityResultLaunchers();
        setupCardSelection();
    }

    private void initViews() {
        Log.d(TAG, "Initializing views");

        toolbar = findViewById(R.id.toolbar);
        etTitle = findViewById(R.id.etTitle);
        etDescription = findViewById(R.id.etDescription);
        etContact = findViewById(R.id.etContact);

        rbLost = findViewById(R.id.rbLost);
        rbFound = findViewById(R.id.rbFound);

        cardLost = findViewById(R.id.cardLost);
        cardFound = findViewById(R.id.cardFound);

        ivImage = findViewById(R.id.ivImage);
        cvImagePreview = findViewById(R.id.cvImagePreview);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Check which views were found
        Log.d(TAG, "Views initialized - " +
                "etTitle: " + (etTitle != null) +
                ", etDescription: " + (etDescription != null) +
                ", etContact: " + (etContact != null) +
                ", rbLost: " + (rbLost != null) +
                ", rbFound: " + (rbFound != null) +
                ", btnSubmit: " + (btnSubmit != null) +
                ", toolbar: " + (toolbar != null));

        // Set default selection
        if (rbLost != null) {
            rbLost.setChecked(true);
        }

        // Pre-fill contact with user's email
        String userContact = prefsManager.getEmail();
        if (userContact != null && etContact != null) {
            etContact.setText(userContact);
            Log.d(TAG, "Pre-filled contact with: " + userContact);
        }
    }

    private void setupToolbar() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setTitle("Create Post");
            }

            // Handle navigation back
            toolbar.setNavigationOnClickListener(v -> {
                onBackPressed();
            });
        }
    }

    private void setupCardSelection() {
        Log.d(TAG, "Setting up card selection");

        // Setup card click listeners
        if (cardLost != null && rbLost != null && rbFound != null) {
            cardLost.setOnClickListener(v -> {
                Log.d(TAG, "Lost card clicked");
                rbLost.setChecked(true);
                rbFound.setChecked(false);
                updateCardAppearance();
            });
        }

        if (cardFound != null && rbLost != null && rbFound != null) {
            cardFound.setOnClickListener(v -> {
                Log.d(TAG, "Found card clicked");
                rbFound.setChecked(true);
                rbLost.setChecked(false);
                updateCardAppearance();
            });
        }

        // Setup individual RadioButton listeners since there's no RadioGroup
        if (rbLost != null) {
            rbLost.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && rbFound != null) {
                    rbFound.setChecked(false);
                    updateCardAppearance();
                }
            });
        }

        if (rbFound != null) {
            rbFound.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && rbLost != null) {
                    rbLost.setChecked(false);
                    updateCardAppearance();
                }
            });
        }

        updateCardAppearance();
    }

    private void updateCardAppearance() {
        // Update card appearance based on selection
        if (cardLost != null && rbLost != null) {
            if (rbLost.isChecked()) {
                cardLost.setCardElevation(8f);
                cardLost.setAlpha(1.0f);
            } else {
                cardLost.setCardElevation(2f);
                cardLost.setAlpha(0.7f);
            }
        }

        if (cardFound != null && rbFound != null) {
            if (rbFound.isChecked()) {
                cardFound.setCardElevation(8f);
                cardFound.setAlpha(1.0f);
            } else {
                cardFound.setCardElevation(2f);
                cardFound.setAlpha(0.7f);
            }
        }
    }

    private void setupClickListeners() {
        if (btnSelectImage != null) {
            btnSelectImage.setOnClickListener(v -> selectImageFromGallery());
        }
        if (btnTakePhoto != null) {
            btnTakePhoto.setOnClickListener(v -> takePhoto());
        }
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> submitPost());
        }
        if (btnRemoveImage != null) {
            btnRemoveImage.setOnClickListener(v -> removeImage());
        }
    }

    private void removeImage() {
        Log.d(TAG, "Removing selected image");
        selectedImageUri = null;
        if (cvImagePreview != null) {
            cvImagePreview.setVisibility(View.GONE);
        }
        if (ivImage != null) {
            ivImage.setImageDrawable(null);
        }
    }

    private void setupActivityResultLaunchers() {
        galleryLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Gallery result: " + result.getResultCode());
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        Log.d(TAG, "Image selected: " + selectedImageUri);
                        displaySelectedImage();
                    }
                }
        );

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    Log.d(TAG, "Camera result: " + result.getResultCode());
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        selectedImageUri = cameraImageUri;
                        Log.d(TAG, "Photo taken: " + selectedImageUri);
                        displaySelectedImage();
                    }
                }
        );
    }

    private void selectImageFromGallery() {
        Log.d(TAG, "Select image from gallery");

        // For Android 13+ (API 33+), we need READ_MEDIA_IMAGES permission
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_IMAGES
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, STORAGE_PERMISSION_REQUEST);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        galleryLauncher.launch(intent);
    }

    private void takePhoto() {
        Log.d(TAG, "Take photo");
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            try {
                File photoFile = new File(getExternalFilesDir(null), "photo_" + System.currentTimeMillis() + ".jpg");
                cameraImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", photoFile);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);

                // Grant temporary permission to camera app
                intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                cameraLauncher.launch(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error creating camera intent", e);
                Toast.makeText(this, "Error starting camera: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySelectedImage() {
        Log.d(TAG, "Display selected image");
        if (selectedImageUri != null && ivImage != null) {
            if (cvImagePreview != null) {
                cvImagePreview.setVisibility(View.VISIBLE);
            }
            ivImage.setVisibility(View.VISIBLE);
            Glide.with(this)
                    .load(selectedImageUri)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(ivImage);
        }
    }

    private void submitPost() {
        Log.d(TAG, "=== Submit Post ===");

        if (etTitle == null || etDescription == null || etContact == null) {
            Toast.makeText(this, "Required views not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String title = etTitle.getText().toString().trim();
        String description = etDescription.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        Log.d(TAG, "Form data - Title: '" + title + "', Description length: " + description.length() +
                ", Contact: '" + contact + "'");

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

        if (contact.isEmpty()) {
            etContact.setError("Contact information is required");
            etContact.requestFocus();
            return;
        }

        // Determine if it's a lost item
        boolean isLost = true; // default
        if (rbLost != null && rbFound != null) {
            isLost = rbLost.isChecked();
        }

        Log.d(TAG, "Submitting post - Title: " + title + ", IsLost: " + isLost +
                ", HasImage: " + (selectedImageUri != null));
        Log.d(TAG, "Auth Header: " + prefsManager.getAuthHeader());

        setLoading(true);

        try {
            if (selectedImageUri != null) {
                submitPostWithImage(title, description, isLost, contact);
            } else {
                submitPostWithoutImage(title, description, isLost, contact);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in submitPost", e);
            setLoading(false);
            Toast.makeText(this, "Error submitting post: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitPostWithImage(String title, String description, boolean isLost, String contact) {
        Log.d(TAG, "Submit post with image");
        try {
            File file = createFileFromUri(selectedImageUri);
            if (file == null) {
                Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                setLoading(false);
                return;
            }

            Log.d(TAG, "Image file created: " + file.getAbsolutePath() + ", Size: " + file.length() + " bytes");

            // Create RequestBody instances
            RequestBody titleBody = RequestBody.create(MediaType.parse("text/plain"), title);
            RequestBody descBody = RequestBody.create(MediaType.parse("text/plain"), description);
            RequestBody isLostBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(isLost));
            RequestBody contactBody = RequestBody.create(MediaType.parse("text/plain"), contact);

            // Create image part
            RequestBody fileBody = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData("image", file.getName(), fileBody);

            Log.d(TAG, "Making API call with image...");
            Call<Post> call = ApiClient.getApiService().createPost(
                    prefsManager.getAuthHeader(),
                    titleBody,
                    descBody,
                    isLostBody,
                    contactBody,
                    imagePart
            );

            call.enqueue(postCallback);

        } catch (Exception e) {
            Log.e(TAG, "Error submitting post with image", e);
            setLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void submitPostWithoutImage(String title, String description, boolean isLost, String contact) {
        Log.d(TAG, "Submit post without image");

        try {
            ApiService.CreatePostRequest request = new ApiService.CreatePostRequest(title, description, isLost, contact);

            Log.d(TAG, "Making API call without image...");
            Call<Post> call = ApiClient.getApiService().createPostWithoutImage(prefsManager.getAuthHeader(), request);
            call.enqueue(postCallback);
        } catch (Exception e) {
            Log.e(TAG, "Error submitting post without image", e);
            setLoading(false);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Callback<Post> postCallback = new Callback<Post>() {
        @Override
        public void onResponse(Call<Post> call, Response<Post> response) {
            Log.d(TAG, "=== Post Response Received ===");
            Log.d(TAG, "Response Code: " + response.code());
            Log.d(TAG, "Response Success: " + response.isSuccessful());
            Log.d(TAG, "Response Message: " + response.message());

            setLoading(false);

            if (response.isSuccessful() && response.body() != null) {
                Post createdPost = response.body();
                Log.d(TAG, "Post created successfully with ID: " + createdPost.getId());
                Toast.makeText(CreatePostActivity.this, "Post created successfully!", Toast.LENGTH_SHORT).show();

                // Set result and finish
                setResult(RESULT_OK);
                finish();
            } else {
                String errorMsg = "Failed to create post. Response code: " + response.code();

                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e(TAG, "Error body: " + errorBody);

                        // Try to parse error message for user-friendly display
                        if (errorBody.contains("validation") || errorBody.contains("required")) {
                            errorMsg = "Please check all required fields";
                        } else if (errorBody.contains("unauthorized") || errorBody.contains("401")) {
                            errorMsg = "Session expired. Please login again";
                        } else if (errorBody.contains("image") || errorBody.contains("file")) {
                            errorMsg = "Error processing image. Please try again";
                        } else {
                            errorMsg = "Error: " + errorBody;
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }

                Log.e(TAG, errorMsg);
                Toast.makeText(CreatePostActivity.this, errorMsg, Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void onFailure(Call<Post> call, Throwable t) {
            Log.e(TAG, "=== Post Creation Failed ===");
            Log.e(TAG, "Error: " + t.getMessage(), t);

            setLoading(false);
            String errorMsg;
            if (t.getMessage() != null) {
                if (t.getMessage().contains("timeout")) {
                    errorMsg = "Request timeout. Please check your connection and try again";
                } else if (t.getMessage().contains("Unable to resolve host")) {
                    errorMsg = "Cannot connect to server. Please check your internet connection";
                } else {
                    errorMsg = "Network error: " + t.getMessage();
                }
            } else {
                errorMsg = "Unknown network error occurred";
            }
            Toast.makeText(CreatePostActivity.this, errorMsg, Toast.LENGTH_LONG).show();
        }
    };

    private File createFileFromUri(Uri uri) {
        try {
            Log.d(TAG, "Creating file from URI: " + uri);

            InputStream inputStream = getContentResolver().openInputStream(uri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream");
                return null;
            }

            String fileName = getFileName(uri);
            if (fileName == null) {
                fileName = "image_" + System.currentTimeMillis() + ".jpg";
            }

            // Ensure the file has a proper extension
            if (!fileName.toLowerCase().endsWith(".jpg") &&
                    !fileName.toLowerCase().endsWith(".jpeg") &&
                    !fileName.toLowerCase().endsWith(".png")) {
                fileName += ".jpg";
            }

            File file = new File(getCacheDir(), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);

            byte[] buffer = new byte[8192]; // Larger buffer for better performance
            int length;
            long totalBytes = 0;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
                totalBytes += length;
            }

            outputStream.close();
            inputStream.close();

            Log.d(TAG, "File created successfully: " + file.getAbsolutePath() +
                    ", Size: " + totalBytes + " bytes");

            return file;
        } catch (IOException e) {
            Log.e(TAG, "Error creating file from URI", e);
            return null;
        }
    }

    private String getFileName(Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index >= 0) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                Log.w(TAG, "Error getting file name from cursor", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            }
        }
        return result;
    }

    private void setLoading(boolean loading) {
        Log.d(TAG, "Set loading: " + loading);

        if (progressBar != null) {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
        if (btnSubmit != null) {
            btnSubmit.setEnabled(!loading);
            btnSubmit.setText(loading ? "Creating..." : "Create Post");
        }
        if (btnSelectImage != null) {
            btnSelectImage.setEnabled(!loading);
        }
        if (btnTakePhoto != null) {
            btnTakePhoto.setEnabled(!loading);
        }

        // Disable form fields during loading
        if (etTitle != null) etTitle.setEnabled(!loading);
        if (etDescription != null) etDescription.setEnabled(!loading);
        if (etContact != null) etContact.setEnabled(!loading);
        if (rbLost != null) rbLost.setEnabled(!loading);
        if (rbFound != null) rbFound.setEnabled(!loading);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "Permission result: " + requestCode + ", Results: " +
                (grantResults.length > 0 ? grantResults[0] : "empty"));

        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                takePhoto();
            } else {
                Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImageFromGallery();
            } else {
                Toast.makeText(this, "Storage permission is required to select images", Toast.LENGTH_SHORT).show();
            }
        }
    }
}