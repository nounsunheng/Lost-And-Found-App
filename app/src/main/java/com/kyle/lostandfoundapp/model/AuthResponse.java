package com.kyle.lostandfoundapp.model;

public class AuthResponse {
    private String token;
    private Integer id;
    private String username;
    private String email;
    private String role;

    // Constructors
    public AuthResponse() {}

    // Getters and setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}