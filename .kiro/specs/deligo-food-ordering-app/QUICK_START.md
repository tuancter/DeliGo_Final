# DeliGo Quick Start Guide

## 🚀 Get Started in 5 Minutes

This guide will help you get the DeliGo app running on your local machine quickly.

---

## Prerequisites

✅ Android Studio installed  
✅ JDK 11 or later  
✅ Android device or emulator (API 24+)  
✅ Firebase account (free tier is sufficient)

---

## Step 1: Clone and Open Project (1 min)

```bash
# Clone the repository
git clone https://github.com/yourusername/deligo-app.git

# Open in Android Studio
# File → Open → Select the deligo-app folder
```

---

## Step 2: Firebase Setup (2 mins)

### Option A: Use Existing Configuration (Fastest)
The project already includes `google-services.json` configured for project `deligo1-app`.

**Just enable these in Firebase Console:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `deligo1-app`
3. Enable **Email/Password** authentication
4. Create **Firestore Database** (production mode)

### Option B: Use Your Own Firebase Project
1. Create new Firebase project
2. Add Android app with package: `com.deligo.app`
3. Download `google-services.json` → place in `app/` folder
4. Enable Email/Password authentication
5. Create Firestore database

**Detailed instructions**: See [FIREBASE_SETUP.md](./FIREBASE_SETUP.md)

---

## Step 3: Create Admin User (1 min)

### In Firebase Console:

1. **Authentication** → **Users** → **Add user**
   - Email: `admin@deligo.com`
   - Password: `admin123456`
   - Copy the User UID

2. **Firestore Database** → **Start collection**
   - Collection ID: `users`
   - Document ID: [paste User UID]
   - Add fields:
     ```
     userId: [paste User UID]
     fullName: "Admin User"
     email: "admin@deligo.com"
     phone: "0987654321"
     role: "admin"
     status: "active"
     createdAt: [current timestamp]
     ```

---

## Step 4: Build and Run (1 min)

### In Android Studio:

1. Click **Build** → **Rebuild Project**
2. Connect Android device or start emulator
3. Click **Run** (green play button)

### Or use command line:

```bash
# Build
./gradlew build

# Install on device
./gradlew installDebug
```

---

## Step 5: Test the App

### Login as Admin
- Email: `admin@deligo.com`
- Password: `admin123456`

### Create Customer Account
1. Click "Register" on login screen
2. Fill in details
3. Login with new account

---

## 🎯 What to Test First

### As Admin:
1. ✅ Add a category (e.g., "Main Dishes")
2. ✅ Add a food item with image URL
3. ✅ View the menu

### As Customer:
1. ✅ Register new account
2. ✅ Browse menu
3. ✅ Add item to cart
4. ✅ Place an order

### Back to Admin:
1. ✅ View new order
2. ✅ Accept and update order status

---

## 📝 Sample Data

### Sample Image URLs for Food Items:
```
Phở Bò: https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43
Cơm Tấm: https://images.unsplash.com/photo-1626804475297-41608ea09aeb
Gỏi Cuốn: https://images.unsplash.com/photo-1559314809-0d155014e29e
Bánh Mì: https://images.unsplash.com/photo-1591047139829-d91aecb6caea
Bún Chả: https://images.unsplash.com/photo-1559314809-0d155014e29e
```

---

## 🐛 Common Issues

### Issue: "Default FirebaseApp is not initialized"
**Fix**: 
1. Ensure `google-services.json` is in `app/` folder
2. Rebuild project: **Build** → **Clean Project** → **Rebuild Project**

### Issue: "Permission denied" in Firestore
**Fix**: Configure security rules in Firebase Console → Firestore → Rules:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
  }
}
```
(For testing only - use proper rules for production)

### Issue: Images not loading
**Fix**: Check internet permission in `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### Issue: Build fails
**Fix**:
```bash
# Clean and rebuild
./gradlew clean
./gradlew build
```

---

## 📚 Next Steps

Once you have the app running:

1. **Read the docs**:
   - [Testing Guide](./TESTING_GUIDE.md) - Comprehensive testing flows
   - [Firebase Setup](./FIREBASE_SETUP.md) - Detailed Firebase configuration
   - [Design Document](./design.md) - Architecture and design decisions

2. **Add test data**:
   - Create categories in admin panel
   - Add food items with images
   - Test customer ordering flow

3. **Explore the code**:
   - Check out the MVVM architecture
   - Review repository implementations
   - Understand the data flow

4. **Customize**:
   - Update colors in `res/values/colors.xml`
   - Modify app name in `res/values/strings.xml`
   - Change app icon in `res/mipmap-*` folders

---

## 🎨 Quick Customization

### Change App Colors
Edit `app/src/main/res/values/colors.xml`:
```xml
<color name="primary">#FF6B35</color>      <!-- Main brand color -->
<color name="accent">#FFC107</color>       <!-- Accent color -->
```

### Change App Name
Edit `app/src/main/res/values/strings.xml`:
```xml
<string name="app_name">Your App Name</string>
```

### Update App Icon
Replace files in:
- `app/src/main/res/mipmap-hdpi/`
- `app/src/main/res/mipmap-mdpi/`
- `app/src/main/res/mipmap-xhdpi/`
- `app/src/main/res/mipmap-xxhdpi/`
- `app/src/main/res/mipmap-xxxhdpi/`

---

## 💡 Pro Tips

1. **Use Firebase Emulator** for local testing without affecting production data
2. **Enable Firestore offline persistence** for better user experience
3. **Monitor Firebase usage** to stay within free tier limits
4. **Use Android Studio Profiler** to check app performance
5. **Enable ProGuard** for release builds to reduce APK size

---

## 🆘 Need Help?

- **Documentation**: Check the `docs/` folder
- **Issues**: Create an issue on GitHub
- **Email**: support@deligo.com
- **Firebase Docs**: https://firebase.google.com/docs

---

## ✅ Checklist

Before you start developing:

- [ ] Project opens in Android Studio without errors
- [ ] Firebase project created and configured
- [ ] Email/Password authentication enabled
- [ ] Firestore database created
- [ ] Admin user created
- [ ] App builds successfully
- [ ] App runs on device/emulator
- [ ] Can login as admin
- [ ] Can register as customer
- [ ] Can add food items (admin)
- [ ] Can place orders (customer)

---

## 🎉 You're Ready!

If you've completed all the steps above, you're ready to start developing with DeliGo!

**Happy Coding! 🚀**

---

**Estimated Setup Time**: 5-10 minutes  
**Difficulty**: Beginner-friendly  
**Last Updated**: 2025-11-15
