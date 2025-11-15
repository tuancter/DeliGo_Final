# Debug MenuFragment - Food List Not Displaying

## 🔍 Vấn đề
Danh sách món ăn không hiển thị trong MenuFragment

## 🐛 Lỗi hiện tại
```
SecurityException: Unknown calling package name 'com.google.android.gms'
```
**Lưu ý**: Đây chỉ là WARNING, không ngăn Firestore hoạt động.

## ✅ Đã thêm logging

Đã thêm chi tiết logging vào:
- `MenuViewModel.loadFoods()`
- `MenuViewModel.loadCategories()`
- `FoodRepositoryImpl.getAvailableFoods()`

## 📋 Các bước debug

### 1. Rebuild app
```powershell
# Trong Android Studio
Build > Clean Project
Build > Rebuild Project
```

### 2. Run app và check Logcat

Filter theo TAG để xem logs:
```
MenuViewModel
FoodRepositoryImpl
MenuFragment
```

### 3. Kiểm tra logs theo thứ tự

**A. MenuFragment onCreate:**
```
MenuFragment: onCreateView: Creating MenuFragment view
MenuFragment: onCreateView: Loading foods and categories
```

**B. MenuViewModel loadFoods:**
```
MenuViewModel: loadFoods: Starting to load foods
```

**C. FoodRepositoryImpl query:**
```
FoodRepositoryImpl: getAvailableFoods: Starting to fetch foods from Firestore
FoodRepositoryImpl: getAvailableFoods: Query successful, documents count: X
FoodRepositoryImpl: getAvailableFoods: Food loaded - Pizza ($10.99)
FoodRepositoryImpl: getAvailableFoods: Total foods loaded: X
```

**D. MenuViewModel callback:**
```
MenuViewModel: loadFoods: Success - received X foods
```

**E. MenuFragment observer:**
```
MenuFragment: observeViewModel: Food list updated, size: X
```

### 4. Các trường hợp lỗi

#### Trường hợp 1: Không có log từ FoodRepositoryImpl
→ **Firestore chưa kết nối được**

**Giải pháp:**
- Kiểm tra `google-services.json` có đúng package name không
- Kiểm tra Internet permission trong AndroidManifest.xml
- Thử tắt/bật WiFi trên emulator

#### Trường hợp 2: Query successful nhưng documents count = 0
→ **Không có dữ liệu trong Firestore**

**Giải pháp:**
1. Vào Firebase Console → Firestore Database
2. Kiểm tra collection `foods` có documents không
3. Kiểm tra field `isAvailable` = `true` trong documents

**Tạo dữ liệu mẫu:**
- Vào Firestore Console
- Collection: `foods`
- Add document với fields:
```
{
  "name": "Pizza",
  "description": "Delicious pizza",
  "price": 10.99,
  "imageUrl": "https://example.com/pizza.jpg",
  "categoryId": "cat1",
  "isAvailable": true,
  "createdAt": 1700000000000
}
```

#### Trường hợp 3: Query successful, có documents, nhưng không hiển thị
→ **Vấn đề UI/RecyclerView**

**Giải pháp:**
- Kiểm tra `FoodAdapter.setFoodList()` có được gọi không
- Kiểm tra layout `fragment_menu.xml`
- Kiểm tra `item_food.xml`

#### Trường hợp 4: Có lỗi "Permission denied"
→ **Firestore rules chặn**

**Giải pháp:**
Vào Firestore Console → Rules, đảm bảo:
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

### 5. Kiểm tra Firestore connection

Thêm test code vào `MenuViewModel.loadFoods()`:
```java
Log.d(TAG, "Firestore instance: " + firestore);
Log.d(TAG, "Firestore app: " + firestore.getApp().getName());
```

### 6. SecurityException warning

Nếu chỉ có lỗi SecurityException nhưng app vẫn hoạt động bình thường:
- Bỏ qua lỗi này (chỉ là warning từ Google Play Services)
- Hoặc thử chạy trên **emulator có Google Play** thay vì emulator thường

### 7. Test trực tiếp Firestore

Tạo test đơn giản trong `MenuFragment.onCreateView()`:
```java
FirebaseFirestore.getInstance()
    .collection("foods")
    .get()
    .addOnSuccessListener(snap -> {
        Log.d(TAG, "TEST: Direct query successful, size: " + snap.size());
    })
    .addOnFailureListener(e -> {
        Log.e(TAG, "TEST: Direct query failed", e);
    });
```

## 🎯 Checklist debug

- [ ] Clean + Rebuild project
- [ ] Run app và mở Logcat
- [ ] Filter Logcat theo "MenuViewModel"
- [ ] Kiểm tra log "Starting to load foods"
- [ ] Kiểm tra log "Query successful"
- [ ] Kiểm tra "documents count"
- [ ] Kiểm tra "Success - received X foods"
- [ ] Kiểm tra "Food list updated"
- [ ] Nếu không có log → Kiểm tra Firestore connection
- [ ] Nếu documents count = 0 → Thêm dữ liệu vào Firestore
- [ ] Nếu có foods nhưng không hiển thị → Kiểm tra RecyclerView

## 📊 Kiểm tra dữ liệu trong Firestore

### Cách 1: Firebase Console
1. https://console.firebase.google.com
2. Project: deligo1-app
3. Firestore Database
4. Collection: `foods`
5. Đảm bảo có ít nhất 1 document với `isAvailable: true`

### Cách 2: Thêm dữ liệu qua code (chỉ để test)
```java
// TEMPORARY TEST CODE - Xóa sau khi test xong
Map<String, Object> testFood = new HashMap<>();
testFood.put("name", "Test Pizza");
testFood.put("description", "Test description");
testFood.put("price", 9.99);
testFood.put("imageUrl", "");
testFood.put("categoryId", "test");
testFood.put("isAvailable", true);
testFood.put("createdAt", System.currentTimeMillis());

FirebaseFirestore.getInstance()
    .collection("foods")
    .add(testFood)
    .addOnSuccessListener(ref -> Log.d(TAG, "Test food added: " + ref.getId()))
    .addOnFailureListener(e -> Log.e(TAG, "Failed to add test food", e));
```

## 🔥 Nếu vẫn không work

Báo cho tôi kết quả của:
1. Log từ MenuViewModel (có bắt đầu load không?)
2. Log từ FoodRepositoryImpl (query có thành công không?)
3. Documents count (có bao nhiêu documents?)
4. Screenshot Firestore Console (collection foods)

Khi đó tôi sẽ biết chính xác vấn đề ở đâu!
