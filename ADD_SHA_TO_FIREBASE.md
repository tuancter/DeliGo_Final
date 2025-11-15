# Thêm SHA Fingerprint vào Firebase

## ⚠️ Lỗi hiện tại
```
java.lang.SecurityException: Unknown calling package name 'com.google.android.gms'
```

Nguyên nhân: App debug chưa được đăng ký SHA fingerprint trên Firebase Console.

## 🔑 SHA Fingerprints của bạn

**SHA-1:**
```
28:B4:75:37:81:A6:63:09:4C:C9:DC:74:BE:FB:A0:CD:92:5E:E4:58
```

**SHA-256:**
```
8B:60:C1:B1:E1:E1:E6:59:31:03:85:D2:76:27:74:82:24:F2:D1:5D:82:AE:35:EF:F0:33:E4:87:10:7B:8D:93
```

## 📝 Hướng dẫn thêm vào Firebase

### Bước 1: Mở Firebase Console
1. Truy cập: https://console.firebase.google.com
2. Chọn project **deligo1-app**

### Bước 2: Vào Settings
1. Click vào **⚙️ Project settings** (góc trên bên trái, bên cạnh Project Overview)
2. Scroll xuống phần **"Your apps"**
3. Tìm app Android có package name: `com.deligo.app`
4. Click vào app đó để mở rộng

### Bước 3: Thêm SHA fingerprints
1. Scroll xuống đến section **"SHA certificate fingerprints"**
2. Click nút **"Add fingerprint"**
3. Paste SHA-1:
   ```
   28:B4:75:37:81:A6:63:09:4C:C9:DC:74:BE:FB:A0:CD:92:5E:E4:58
   ```
4. Click **"Add fingerprint"** một lần nữa
5. Paste SHA-256:
   ```
   8B:60:C1:B1:E1:E1:E6:59:31:03:85:D2:76:27:74:82:24:F2:D1:5D:82:AE:35:EF:F0:33:E4:87:10:7B:8D:93
   ```
6. Click **"Save"** ở cuối trang

### Bước 4: Download google-services.json mới
1. Vẫn ở trang settings, scroll lên phần app
2. Click **"Download google-services.json"**
3. **Replace** file cũ ở `d:\Product\DeliGo\app\google-services.json`

### Bước 5: Rebuild app
```powershell
# Clean và rebuild project
.\gradlew clean
.\gradlew assembleDebug

# Hoặc trong Android Studio: Build > Clean Project > Rebuild Project
```

### Bước 6: Uninstall và Install lại app
```powershell
# Uninstall app cũ trên emulator/device
adb uninstall com.deligo.app

# Run lại từ Android Studio
```

## ✅ Kiểm tra thành công

Sau khi làm xong các bước trên:
1. Mở app DeliGo
2. Đăng nhập
3. Vào MenuFragment
4. Nếu **không còn lỗi SecurityException** trong Logcat → Thành công! ✅
5. Danh sách món ăn sẽ hiển thị bình thường

## 🔍 Debug

Nếu vẫn lỗi, kiểm tra:

### 1. Xác nhận SHA đã được thêm
- Vào Firebase Console > Project Settings
- Scroll xuống "Your apps" > Android app
- Kiểm tra section "SHA certificate fingerprints"
- Phải thấy 2 fingerprints đã thêm

### 2. Kiểm tra google-services.json
- Đảm bảo đã download và replace file mới
- File phải nằm ở: `d:\Product\DeliGo\app\google-services.json`
- Package name trong file phải là: `com.deligo.app`

### 3. Rebuild & reinstall
- Phải clean build và rebuild lại
- Phải uninstall app cũ trước khi cài app mới

### 4. Xem Logcat
```
# Filter Firestore logs
adb logcat | Select-String "Firestore"

# Filter all errors
adb logcat *:E
```

## 📌 Lưu ý quan trọng

- **Debug keystore** thay đổi nếu reinstall Android Studio → Phải lấy SHA mới
- **Release keystore** cần SHA khác cho production
- Mỗi máy dev khác nhau có debug keystore khác nhau
- SHA phải khớp với keystore thực tế sign app

## 🚀 Cho môi trường Production

Khi release app, cần:
1. Tạo release keystore
2. Lấy SHA từ release keystore
3. Thêm SHA vào Firebase
4. Download google-services.json mới
5. Build release APK/AAB

---

**Tóm tắt:** Copy 2 SHA ở trên → Firebase Console → Thêm vào app Android → Download google-services.json mới → Rebuild app
