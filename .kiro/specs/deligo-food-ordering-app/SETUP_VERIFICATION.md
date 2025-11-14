# DeliGo Setup Verification Guide

## 🔍 Pre-Build Verification

Before building the DeliGo app, verify that your development environment is properly configured.

---

## 1. System Requirements

### Required Software
- [ ] **Android Studio**: Arctic Fox (2020.3.1) or later
- [ ] **JDK**: Version 11 or later
- [ ] **Android SDK**: API 24 (Android 7.0) or later
- [ ] **Gradle**: 7.0+ (included with Android Studio)

### Verify Java Installation

#### Windows
```cmd
java -version
```

Expected output:
```
java version "11.0.x" or later
```

If not installed:
1. Download JDK 11+ from [Oracle](https://www.oracle.com/java/technologies/downloads/) or [OpenJDK](https://adoptium.net/)
2. Install and set JAVA_HOME environment variable
3. Add to PATH: `%JAVA_HOME%\bin`

#### Set JAVA_HOME (Windows)
```cmd
setx JAVA_HOME "C:\Program Files\Java\jdk-11"
setx PATH "%PATH%;%JAVA_HOME%\bin"
```

### Verify Android SDK

In Android Studio:
1. Go to **File** → **Settings** (or **Preferences** on Mac)
2. Navigate to **Appearance & Behavior** → **System Settings** → **Android SDK**
3. Verify SDK Location is set
4. Ensure API 24+ is installed

---

## 2. Project Structure Verification

### Check Required Files

```
DeliGo/
├── app/
│   ├── build.gradle ✅
│   ├── google-services.json ✅
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml ✅
│           ├── java/com/deligo/app/ ✅
│           └── res/ ✅
├── build.gradle ✅
├── settings.gradle ✅
├── gradle.properties ✅
└── gradlew ✅
```

### Verify File Existence

#### Windows PowerShell
```powershell
# Check critical files
Test-Path app\build.gradle
Test-Path app\google-services.json
Test-Path app\src\main\AndroidManifest.xml
Test-Path build.gradle
Test-Path settings.gradle
```

All should return `True`

---

## 3. Firebase Configuration Verification

### Check google-services.json

1. **Location**: `app/google-services.json`
2. **Content**: Should contain:
   ```json
   {
     "project_info": {
       "project_id": "deligo1-app",
       "project_number": "612032109560"
     },
     "client": [
       {
         "client_info": {
           "android_client_info": {
             "package_name": "com.deligo.app"
           }
         }
       }
     ]
   }
   ```

### Verify Package Name Match

In `app/build.gradle`:
```groovy
android {
    namespace 'com.deligo.app'
    defaultConfig {
        applicationId "com.deligo.app"
    }
}
```

Package name must match in:
- ✅ `google-services.json`
- ✅ `app/build.gradle`
- ✅ `AndroidManifest.xml`

---

## 4. Gradle Configuration Verification

### Check build.gradle Files

#### Root build.gradle
```groovy
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}
```

#### app/build.gradle
```groovy
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.google.gms.google.services)
}

dependencies {
    // Firebase
    implementation platform('com.google.firebase:firebase-bom:33.1.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Other dependencies...
}
```

### Verify Gradle Wrapper

```bash
# Check Gradle wrapper exists
ls gradlew
ls gradlew.bat

# Make executable (Linux/Mac)
chmod +x gradlew
```

---

## 5. Android Studio Configuration

### Open Project in Android Studio

1. **File** → **Open**
2. Select the `DeliGo` folder
3. Wait for Gradle sync to complete

### Check for Errors

After Gradle sync:
- [ ] No red errors in **Build** output
- [ ] No missing dependencies
- [ ] No SDK version conflicts

### Common Gradle Sync Issues

#### Issue: "SDK location not found"
**Fix**: Create `local.properties` in project root:
```properties
sdk.dir=C\:\\Users\\YourUsername\\AppData\\Local\\Android\\Sdk
```
(Replace with your actual SDK path)

#### Issue: "Plugin with id 'com.google.gms.google-services' not found"
**Fix**: Check `gradle/libs.versions.toml` includes:
```toml
[plugins]
google-gms-google-services = { id = "com.google.gms.google-services", version = "4.4.0" }
```

---

## 6. Build Verification

### Sync Project with Gradle Files

In Android Studio:
1. Click **File** → **Sync Project with Gradle Files**
2. Wait for sync to complete
3. Check **Build** tab for any errors

### Clean Build

```bash
# Windows
gradlew.bat clean

# Linux/Mac
./gradlew clean
```

### Build Debug APK

```bash
# Windows
gradlew.bat assembleDebug

# Linux/Mac
./gradlew assembleDebug
```

Expected output:
```
BUILD SUCCESSFUL in Xs
```

Output location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 7. Device/Emulator Verification

### Check Connected Devices

```bash
# List connected devices
adb devices
```

Expected output:
```
List of devices attached
emulator-5554   device
```

### Create Emulator (if needed)

In Android Studio:
1. **Tools** → **Device Manager**
2. Click **Create Device**
3. Select device (e.g., Pixel 5)
4. Select system image (API 24+)
5. Click **Finish**

### Verify Emulator Settings

Recommended settings:
- **API Level**: 24 or higher
- **RAM**: 2048 MB minimum
- **Internal Storage**: 2048 MB minimum
- **SD Card**: 512 MB (optional)

---

## 8. Firebase Console Verification

### Check Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: `deligo1-app`
3. Verify:
   - [ ] Project exists
   - [ ] Android app registered
   - [ ] Package name: `com.deligo.app`

### Check Authentication

1. Go to **Authentication** → **Sign-in method**
2. Verify:
   - [ ] Email/Password is **Enabled**

### Check Firestore

1. Go to **Firestore Database**
2. Verify:
   - [ ] Database is created
   - [ ] Location is set
   - [ ] Rules are configured

---

## 9. Dependency Verification

### Check All Dependencies Resolve

In Android Studio:
1. Open `app/build.gradle`
2. Check for any red underlines
3. Hover over dependencies to verify versions

### Update Dependencies (if needed)

```bash
# Check for dependency updates
gradlew.bat dependencyUpdates
```

---

## 10. Code Verification

### Check for Compilation Errors

In Android Studio:
1. **Build** → **Make Project** (Ctrl+F9)
2. Check **Build** tab for errors
3. Fix any compilation errors

### Run Lint Checks

```bash
# Run lint analysis
gradlew.bat lint
```

View report: `app/build/reports/lint-results.html`

---

## 11. Resource Verification

### Check Required Resources

- [ ] App icon: `res/mipmap-*/ic_launcher.webp`
- [ ] Colors: `res/values/colors.xml`
- [ ] Strings: `res/values/strings.xml`
- [ ] Themes: `res/values/themes.xml`
- [ ] Layouts: All activities have corresponding layouts

### Verify No Missing Resources

Build the project and check for:
- No "Resource not found" errors
- No missing drawable references
- No missing string references

---

## 12. Manifest Verification

### Check AndroidManifest.xml

Required elements:
```xml
<manifest package="com.deligo.app">
    <uses-permission android:name="android.permission.INTERNET" />
    
    <application
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.DeliGo">
        
        <!-- Launcher activity -->
        <activity
            android:name=".activities.SplashActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        
        <!-- Other activities... -->
    </application>
</manifest>
```

---

## 13. Final Verification Checklist

### Pre-Build Checklist
- [ ] JDK 11+ installed and JAVA_HOME set
- [ ] Android Studio installed
- [ ] Android SDK installed (API 24+)
- [ ] Project opens without errors
- [ ] Gradle sync successful
- [ ] google-services.json present
- [ ] Package names match
- [ ] All dependencies resolve
- [ ] No compilation errors
- [ ] Resources are complete

### Firebase Checklist
- [ ] Firebase project created
- [ ] Android app registered
- [ ] Email/Password auth enabled
- [ ] Firestore database created
- [ ] Security rules configured

### Build Checklist
- [ ] Clean build successful
- [ ] Debug APK builds successfully
- [ ] No lint errors (critical)
- [ ] Device/emulator ready

---

## 14. Test Build and Run

### Build and Install

```bash
# Build and install on connected device
gradlew.bat installDebug
```

### Launch App

1. Find "DeliGo" app on device
2. Launch the app
3. Verify splash screen appears
4. Verify redirect to login screen

### Quick Smoke Test

- [ ] App launches without crashing
- [ ] Splash screen displays
- [ ] Login screen loads
- [ ] Can navigate to register screen
- [ ] UI elements render correctly

---

## 15. Troubleshooting Common Issues

### Issue: Gradle sync fails
**Solutions**:
1. Check internet connection
2. Invalidate caches: **File** → **Invalidate Caches / Restart**
3. Delete `.gradle` folder and sync again
4. Update Gradle wrapper: `gradlew wrapper --gradle-version 8.0`

### Issue: Build fails with "Duplicate class" error
**Solution**: Check for duplicate dependencies in `build.gradle`

### Issue: "Default FirebaseApp is not initialized"
**Solution**: 
1. Ensure `google-services.json` is in `app/` folder
2. Clean and rebuild project
3. Check Firebase plugin is applied

### Issue: App crashes on launch
**Solutions**:
1. Check logcat for error messages
2. Verify Firebase configuration
3. Check internet permission in manifest
4. Verify all activities are registered in manifest

---

## 16. Performance Verification

### Check Build Time

First build: 2-5 minutes (normal)  
Incremental builds: 10-30 seconds (normal)

If builds are slow:
1. Enable Gradle daemon
2. Increase Gradle memory in `gradle.properties`:
   ```properties
   org.gradle.jvmargs=-Xmx2048m
   ```
3. Enable parallel builds:
   ```properties
   org.gradle.parallel=true
   ```

### Check APK Size

Debug APK size: ~10-20 MB (acceptable)  
Release APK size: ~5-10 MB (with ProGuard)

---

## 17. Documentation Verification

### Check Documentation Files

- [ ] README.md exists
- [ ] QUICK_START.md exists
- [ ] FIREBASE_SETUP.md exists
- [ ] TESTING_GUIDE.md exists
- [ ] DEPLOYMENT_CHECKLIST.md exists
- [ ] PROJECT_SUMMARY.md exists

### Verify Documentation Accuracy

- [ ] Setup instructions are clear
- [ ] Code examples are correct
- [ ] Links work
- [ ] Screenshots are up-to-date (if any)

---

## ✅ Verification Complete

If all checks pass, your DeliGo project is properly configured and ready for development and testing!

### Next Steps:
1. ✅ Complete Firebase setup (see FIREBASE_SETUP.md)
2. ✅ Create admin user
3. ✅ Add test data
4. ✅ Run comprehensive testing (see TESTING_GUIDE.md)

---

## 📞 Need Help?

If you encounter issues not covered in this guide:

1. Check the [QUICK_START.md](./QUICK_START.md) guide
2. Review [FIREBASE_SETUP.md](./FIREBASE_SETUP.md)
3. Search for the error message online
4. Create an issue on GitHub
5. Contact support@deligo.com

---

**Last Updated**: 2025-11-15  
**Version**: 1.0  
**Status**: Complete
