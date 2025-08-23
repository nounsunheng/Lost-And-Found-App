package com.kyle.lostandfoundapp.network;

import com.kyle.lostandfoundapp.model.AuthRequest;
import com.kyle.lostandfoundapp.model.AuthResponse;
import com.kyle.lostandfoundapp.model.ChangePasswordRequest;
import com.kyle.lostandfoundapp.model.Post;
import com.kyle.lostandfoundapp.model.RegisterRequest;
import com.kyle.lostandfoundapp.model.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    // Authentication
    @POST("api/auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("api/auth/login")
    Call<AuthResponse> login(@Body AuthRequest request);

    // User
    @GET("api/users/me")
    Call<User> getProfile(@Header("Authorization") String token);

    @PUT("api/users/me/password")
    Call<ResponseBody> changePassword(@Header("Authorization") String token, @Body ChangePasswordRequest request);

    // Posts
    @GET("api/posts")
    Call<List<Post>> getAllPosts(@Header("Authorization") String token);

    @GET("api/posts/search")
    Call<List<Post>> searchPosts(@Header("Authorization") String token,
                                 @Query("q") String query,
                                 @Query("isLost") Boolean isLost);

    @GET("api/posts/user/me")
    Call<List<Post>> getMyPosts(@Header("Authorization") String token);

    @GET("api/posts/{id}")
    Call<Post> getPost(
            @Header("Authorization") String token,
            @Path("id") int id
    );

    @PUT("api/posts/{id}")
    Call<ResponseBody> updatePost(
            @Header("Authorization") String token,
            @Path("id") int postId,
            @Body Post post
    );

    @Multipart
    @POST("api/posts")
    Call<Post> createPost(@Header("Authorization") String token,
                          @Part("title") RequestBody title,
                          @Part("description") RequestBody description,
                          @Part("isLost") RequestBody isLost,
                          @Part("contact") RequestBody contact,
                          @Part MultipartBody.Part image);

    @POST("api/posts")
    Call<Post> createPostWithoutImage(@Header("Authorization") String token,
                                      @Body CreatePostRequest request);

    @DELETE("api/posts/{id}")
    Call<ResponseBody> deletePost(@Header("Authorization") String token, @Path("id") int id);

    // Inner class for post creation without image
    class CreatePostRequest {
        private String title;
        private String description;
        private boolean isLost;
        private String contact;

        public CreatePostRequest(String title, String description, boolean isLost, String contact) {
            this.title = title;
            this.description = description;
            this.isLost = isLost;
            this.contact = contact;
        }

        // Getters and setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isLost() { return isLost; }
        public void setLost(boolean lost) { isLost = lost; }

        public String getContact() { return contact; }
        public void setContact(String contact) { this.contact = contact; }
    }
}