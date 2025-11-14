# Firebase Setup Guide for DeliGo

## Overview

This guide walks you through setting up Firebase for the DeliGo food ordering application. The app uses Firebase Authentication and Firestore Database.

---

## Prerequisites

- Google account
- Android Studio installed
- DeliGo project opened in Android Studio

---

## Step 1: Verify Firebase Project

Your Firebase project is already configured:
- **Project ID**: `deligo1-app`
- **Package Name**: `com.deligo.app`
- **Configuration File**: `app/google-services.json` ✅

---

## Step 2: Enable Firebase Authentication

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: `deligo1-app`
3. In the left sidebar, click **Authentication**
4. Click **Get Started** (if not already enabled)
5. Go to **Sign-in method** tab
6. Click on **Email/Password**
7. Enable the **Email/Password** toggle
8. Click **Save**

**Status**: ✅ Email/Password authentication enabled

---

## Step 3: Create Firestore Database

1. In Firebase Console, click **Firestore Database** in the left sidebar
2. Click **Create database**
3. Select **Start in production mode**
4. Choose a location (select closest to your users, e.g., `asia-southeast1` for Vietnam)
5. Click **Enable**

**Status**: ✅ Firestore database created

---

## Step 4: Configure Firestore Security Rules

1. In Firestore Database, go to the **Rules** tab
2. Replace the default rules with the following:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    // Helper function to check if user is admin
    function isAdmin() {
      return request.auth != null && 
             get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
    }
    
    // Helper function to check if user is authenticated
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Helper function to check if user owns the resource
    function isOwner(userId) {
      return request.auth != null && request.auth.uid == userId;
    }
    
    // Users collection
    match /users/{userId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && request.auth.uid == userId;
      allow update: if isOwner(userId) || isAdmin();
      allow delete: if isAdmin();
    }
    
    // Categories collection
    match /categories/{categoryId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }
    
    // Foods collection
    match /foods/{foodId} {
      allow read: if isAuthenticated();
      allow write: if isAdmin();
    }
    
    // Carts collection
    match /carts/{cartId} {
      allow read: if isAuthenticated() && resource.data.userId == request.auth.uid;
      allow create: if isAuthenticated() && request.resource.data.userId == request.auth.uid;
      allow update, delete: if isAuthenticated() && resource.data.userId == request.auth.uid;
      
      // Cart items subcollection
      match /cartItems/{itemId} {
        allow read, write: if isAuthenticated();
      }
    }
    
    // Orders collection
    match /orders/{orderId} {
      allow read: if isAuthenticated() && 
                     (resource.data.customerId == request.auth.uid || isAdmin());
      allow create: if isAuthenticated() && 
                       request.resource.data.customerId == request.auth.uid;
      allow update: if isAdmin();
      allow delete: if isAdmin();
      
      // Order details subcollection
      match /orderDetails/{detailId} {
        allow read: if isAuthenticated();
        allow write: if isAuthenticated();
      }
    }
    
    // Reviews collection
    match /reviews/{reviewId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      allow update: if isOwner(resource.data.userId);
      allow delete: if isOwner(resource.data.userId) || isAdmin();
    }
    
    // Complaints collection
    match /complaints/{complaintId} {
      allow read: if isAuthenticated() && 
                     (resource.data.userId == request.auth.uid || isAdmin());
      allow create: if isAuthenticated() && 
                       request.resource.data.userId == request.auth.uid;
      allow update: if isAdmin();
      allow delete: if isAdmin();
    }
  }
}
```

3. Click **Publish**

**Status**: ✅ Security rules configured

---

## Step 5: Create Admin User

Since the app doesn't have an admin registration flow, you need to manually create an admin user:

### 5.1 Create User in Authentication

1. Go to **Authentication** → **Users** tab
2. Click **Add user**
3. Enter:
   - **Email**: `admin@deligo.com` (or your preferred admin email)
   - **Password**: `admin123456` (or your preferred secure password)
4. Click **Add user**
5. **Copy the User UID** (you'll need this in the next step)

### 5.2 Create User Document in Firestore

1. Go to **Firestore Database**
2. Click **Start collection**
3. Collection ID: `users`
4. Click **Next**
5. Document ID: Paste the **User UID** from step 5.1
6. Add the following fields:

| Field | Type | Value |
|-------|------|-------|
| userId | string | [paste the User UID] |
| fullName | string | Admin User |
| email | string | admin@deligo.com |
| phone | string | 0987654321 |
| role | string | admin |
| status | string | active |
| createdAt | timestamp | [click "Set to current time"] |

7. Click **Save**

**Status**: ✅ Admin user created

---

## Step 6: Create Initial Test Data (Optional)

To make testing easier, you can add some initial categories and food items:

### 6.1 Create Categories

1. In Firestore, click **Start collection**
2. Collection ID: `categories`
3. Add the following documents:

**Document 1:**
- Document ID: Auto-ID
- Fields:
  - `categoryName` (string): "Main Dishes"

**Document 2:**
- Document ID: Auto-ID
- Fields:
  - `categoryName` (string): "Appetizers"

**Document 3:**
- Document ID: Auto-ID
- Fields:
  - `categoryName` (string): "Desserts"

**Document 4:**
- Document ID: Auto-ID
- Fields:
  - `categoryName` (string): "Beverages"

### 6.2 Create Sample Foods

1. In Firestore, click **Start collection**
2. Collection ID: `foods`
3. Add sample food items (copy the category IDs from step 6.1):

**Sample Food 1:**
- Document ID: Auto-ID
- Fields:
  - `categoryId` (string): [paste Main Dishes category ID]
  - `name` (string): Phở Bò
  - `description` (string): Traditional Vietnamese beef noodle soup
  - `price` (number): 65000
  - `imageUrl` (string): https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43
  - `isAvailable` (boolean): true

**Sample Food 2:**
- Document ID: Auto-ID
- Fields:
  - `categoryId` (string): [paste Main Dishes category ID]
  - `name` (string): Cơm Tấm
  - `description` (string): Broken rice with grilled pork
  - `price` (number): 55000
  - `imageUrl` (string): https://images.unsplash.com/photo-1626804475297-41608ea09aeb
  - `isAvailable` (boolean): true

**Sample Food 3:**
- Document ID: Auto-ID
- Fields:
  - `categoryId` (string): [paste Appetizers category ID]
  - `name` (string): Gỏi Cuốn
  - `description` (string): Fresh spring rolls
  - `price` (number): 45000
  - `imageUrl` (string): https://images.unsplash.com/photo-1559314809-0d155014e29e
  - `isAvailable` (boolean): true

**Status**: ✅ Test data created (optional)

---

## Step 7: Verify Configuration

### 7.1 Check google-services.json

Ensure the file `app/google-services.json` exists and contains:
```json
{
  "project_info": {
    "project_number": "612032109560",
    "project_id": "deligo1-app",
    "storage_bucket": "deligo1-app.firebasestorage.app"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "1:612032109560:android:cf9138ee2c9dce4395540d",
        "android_client_info": {
          "package_name": "com.deligo.app"
        }
      }
    }
  ]
}
```

**Status**: ✅ Configuration file verified

### 7.2 Check Build Configuration

Verify `app/build.gradle` includes:
```groovy
plugins {
    alias(libs.plugins.google.gms.google.services)
}

dependencies {
    implementation platform('com.google.firebase:firebase-bom:33.1.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
}
```

**Status**: ✅ Build configuration verified

---

## Step 8: Build and Test

1. In Android Studio, click **Build** → **Rebuild Project**
2. Wait for the build to complete
3. Connect an Android device or start an emulator
4. Click **Run** → **Run 'app'**
5. Test the following:
   - App launches successfully
   - Can register a new customer account
   - Can login with admin credentials
   - Can view menu (if test data added)

---

## Firestore Collections Structure

After setup, your Firestore should have the following structure:

```
deligo1-app (Firestore Database)
├── users/
│   └── [userId]/
│       ├── userId: string
│       ├── fullName: string
│       ├── email: string
│       ├── phone: string
│       ├── role: string ("customer" | "admin")
│       ├── status: string ("active" | "inactive")
│       └── createdAt: timestamp
│
├── categories/
│   └── [categoryId]/
│       └── categoryName: string
│
├── foods/
│   └── [foodId]/
│       ├── categoryId: string
│       ├── name: string
│       ├── description: string
│       ├── price: number
│       ├── imageUrl: string
│       └── isAvailable: boolean
│
├── carts/
│   └── [cartId]/
│       ├── userId: string
│       ├── createdAt: timestamp
│       ├── updatedAt: timestamp
│       └── cartItems/ (subcollection)
│           └── [cartItemId]/
│               ├── foodId: string
│               ├── quantity: number
│               ├── price: number
│               └── note: string (optional)
│
├── orders/
│   └── [orderId]/
│       ├── customerId: string
│       ├── deliveryAddress: string
│       ├── totalAmount: number
│       ├── paymentMethod: string
│       ├── paymentStatus: string
│       ├── orderStatus: string
│       ├── note: string (optional)
│       ├── createdAt: timestamp
│       └── orderDetails/ (subcollection)
│           └── [orderDetailId]/
│               ├── foodId: string
│               ├── quantity: number
│               └── unitPrice: number
│
├── reviews/
│   └── [reviewId]/
│       ├── userId: string
│       ├── foodId: string
│       ├── rating: number (1-5)
│       ├── comment: string (optional)
│       └── createdAt: timestamp
│
└── complaints/
    └── [complaintId]/
        ├── userId: string
        ├── orderId: string
        ├── content: string
        ├── status: string ("pending" | "resolved" | "rejected")
        └── createdAt: timestamp
```

---

## Troubleshooting

### Issue: "Default FirebaseApp is not initialized"
**Solution**: 
1. Ensure `google-services.json` is in the `app/` directory
2. Rebuild the project
3. Clean and rebuild: **Build** → **Clean Project** → **Rebuild Project**

### Issue: "Permission denied" errors in Firestore
**Solution**:
1. Check that security rules are published
2. Verify user is authenticated
3. Check that user document has correct `role` field for admin operations

### Issue: Authentication not working
**Solution**:
1. Verify Email/Password authentication is enabled in Firebase Console
2. Check internet connectivity
3. Verify API key in `google-services.json` is correct

### Issue: Images not loading
**Solution**:
1. Check internet permission in `AndroidManifest.xml`:
   ```xml
   <uses-permission android:name="android.permission.INTERNET" />
   ```
2. Verify image URLs are valid and accessible
3. Check Glide dependency is included

---

## Security Best Practices

1. **Never commit sensitive data**: Don't commit `google-services.json` to public repositories
2. **Use environment-specific configs**: Use different Firebase projects for development and production
3. **Regularly review security rules**: Audit Firestore security rules periodically
4. **Enable App Check**: Consider enabling Firebase App Check for production
5. **Monitor usage**: Set up Firebase usage alerts to detect unusual activity

---

## Next Steps

After completing this setup:

1. ✅ Firebase is fully configured
2. ✅ Admin user is created
3. ✅ Test data is available (optional)
4. 📱 Ready to test the app
5. 📋 Follow the [TESTING_GUIDE.md](./TESTING_GUIDE.md) for comprehensive testing

---

## Support Resources

- [Firebase Documentation](https://firebase.google.com/docs)
- [Firebase Console](https://console.firebase.google.com/)
- [Firestore Security Rules](https://firebase.google.com/docs/firestore/security/get-started)
- [Firebase Authentication](https://firebase.google.com/docs/auth)

---

**Setup Status**: ✅ Complete
**Last Updated**: 2025-11-15
