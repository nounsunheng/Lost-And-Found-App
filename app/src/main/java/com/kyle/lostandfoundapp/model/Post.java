package com.kyle.lostandfoundapp.model;

public class Post {
    private Integer id;
    private String title;
    private String description;
    private Boolean isLost;
    private String imagePath;
    private String contact;
    private String createdAt;
    private Integer userId;
    private String status;

    public Post() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getIsLost() { return isLost; }
    public void setIsLost(Boolean isLost) { this.isLost = isLost; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
