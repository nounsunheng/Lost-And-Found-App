# Lost and Found Android App

An Android mobile application for posting and browsing lost items within a school or campus environment.

## 📋 Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation & Setup](#installation--setup)
- [App Structure](#app-structure)
- [API Integration](#api-integration)
- [Permissions](#permissions)
- [Building & Running](#building--running)
- [Contributing](#contributing)

## 🌟 Overview

LO-FO x KYLE is an Android application that simplifies the process of reporting and recovering lost items through a user-friendly mobile interface. Students and staff can easily post lost items, browse found items, and contact each other for item recovery.

## ✨ Features

### User Features
- **User Authentication**: Secure login and registration
- **Post Creation**: Create posts for lost or found items with images
- **Search & Browse**: Filter items by lost/found status
- **Contact System**: Call or message item posters directly
- **Profile Management**: View and edit user profile
- **Post Management**: View, edit, and delete own posts
- **Multi-language Support**: English and Khmer language support

### Admin Features
- **Admin Dashboard**: Comprehensive post management
- **Content Moderation**: Delete inappropriate posts
- **User Management**: Overview of all users and posts

### Technical Features
- **Offline Support**: Local data caching
- **Image Handling**: Camera capture and gallery selection
- **Modern UI**: Material Design components
- **Responsive Design**: Optimized for different screen sizes
- **Dark Mode**: Night theme support

## 🛠 Tech Stack

- **Language**: Java
- **IDE**: Android Studio
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **UI Framework**: Android Material Design
- **Networking**: Retrofit + OkHttp
- **Image Loading**: Glide
- **Architecture**: MVVM pattern
- **Data Storage**: SharedPreferences

## 📋 Prerequisites

- Android Studio Arctic Fox or newer
- Android SDK API level 24+
- Java 11+
- Device or emulator running Android 7.0+

## 🚀 Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/nounsunheng/Lost-And-Found-App.git
   cd Lost-And-Found-App
   ```

2. **Open in Android Studio**
   - Open Android Studio
   - Select "Open an existing Android Studio project"
   - Navigate to the cloned directory and select it

3. **Configure API Endpoint**
   - Update the base URL in `ApiClient.java`:
   ```java
   private static final String BASE_URL = "http://your-api-server:8080/api/";
   ```

4. **Sync Project**
   - Android Studio will automatically sync the project
   - If not, click "Sync Now" in the notification bar

5. **Run the App**
   - Connect an Android device or start an emulator
   - Click the "Run" button or press Ctrl+R

## 📁 App Structure

```
app/src/main/java/com/kyle/lostandfoundapp/
├── activity/
│   ├── AdminActivity.java               # Admin dashboard
│   ├── CreatePostActivity.java          # Create new posts
│   ├── EditPostActivity.java            # Edit existing posts
│   ├── LoginActivity.java               # User login
│   ├── MainActivity.java                # Main app screen
│   ├── MyPostsActivity.java             # User's post history
│   ├── PostDetailActivity.java          # Detailed post view
│   ├── ProfileActivity.java             # User profile
│   ├── RegisterActivity.java            # User registration
│   └── SplashActivity.java              # Splash screen
├── adapter/
│   ├── AdminPostAdapter.java            # Admin post list adapter
│   └── PostAdapter.java                 # Post list adapter
├── model/
│   ├── AuthRequest.java                 # Authentication request
│   ├── AuthResponse.java                # Authentication response
│   ├── ChangePasswordRequest.java       # Password change request
│   ├── Post.java                        # Post model
│   ├── RegisterRequest.java             # Registration request
│   └── User.java                        # User model
├── network/
│   ├── ApiClient.java                   # Retrofit client configuration
│   └── ApiService.java                  # API endpoints interface
├── utils/
│   ├── LocaleManager.java               # Language management
│   └── SharedPreferencesManager.java    # Local storage management
├── LostAndFoundApplication.java         # Application class
└── MainActivity.java                    # Main activity
```

## 🌐 API Integration

The app integrates with the Spring Boot backend API:

### Authentication
- Login: `POST /api/auth/login`
- Register: `POST /api/auth/register`

### Posts
- Get all posts: `GET /api/posts`
- Create post: `POST /api/posts`
- Update post: `PUT /api/posts/{id}`
- Delete post: `DELETE /api/posts/{id}`

### User Management
- Get profile: `GET /api/users/profile`
- Update profile: `PUT /api/users/profile`
- Change password: `POST /api/users/change-password`

## 🔐 Permissions

The app requires the following permissions:

- `INTERNET` - Network communication
- `ACCESS_NETWORK_STATE` - Check network status
- `CAMERA` - Take photos for posts
- `READ_EXTERNAL_STORAGE` - Access gallery images
- `READ_MEDIA_IMAGES` - Read media files (API 33+)
- `CALL_PHONE` - Make phone calls to contacts
- `WRITE_EXTERNAL_STORAGE` - Save images (API ≤ 32)

## 🔧 Building & Running

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Run Tests
```bash
./gradlew test
```

### Install on Device
```bash
./gradlew installDebug
```

## 🌍 Internationalization

The app supports multiple languages:
- English (default)
- Khmer (Cambodian)

Language files are located in:
- `res/values/strings.xml` (English)
- `res/values-km/strings.xml` (Khmer)

## 🎨 UI/UX Features

- **Material Design**: Following Google's Material Design guidelines
- **Dark Theme**: Automatic dark mode support
- **Responsive Layout**: Adapts to different screen sizes
- **Intuitive Navigation**: Bottom navigation and drawer menu
- **Image Handling**: Easy photo capture and selection
- **Loading States**: Progress indicators for better UX

## 📦 Dependencies

Key dependencies used in the project:

```gradle
// UI Components
implementation 'com.google.android.material:material:1.11.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'

// Networking
implementation 'com.squareup.retrofit2:retrofit:2.9.0'
implementation 'com.squareup.retrofit2:converter-gson:2.9.0'
implementation 'com.squareup.okhttp3:logging-interceptor:4.12.0'

// Image Loading
implementation 'com.github.bumptech.glide:glide:4.16.0'

// Image Picker
implementation 'com.github.dhaval2404:imagepicker:2.1'

// Permissions
implementation 'com.karumi:dexter:6.2.3'
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes (`git commit -am 'Add new feature'`)
4. Push to the branch (`git push origin feature/new-feature`)
5. Create a Pull Request

## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Noun Sunheng**
- Individual project for Lost and Found Mobile Application
- GitHub: [@nounsunheng](https://github.com/nounsunheng)
- Backend Repository: [Lost-And-Found-API](https://github.com/nounsunheng/Lost-And-Found-API)
- Frontend Repository: [Lost-And-Found-App](https://github.com/nounsunheng/Lost-And-Found-App)

---

**Note**: Make sure to update the API base URL in `ApiClient.java` to point to your backend server before running the app.
