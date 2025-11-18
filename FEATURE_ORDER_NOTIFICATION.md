# Tính năng Thông báo Đơn hàng mới cho Admin

## Mô tả
Tính năng này cho phép Admin nhận thông báo real-time khi có đơn hàng mới và hiển thị badge số lượng đơn hàng chờ xác nhận trên màn hình Admin Dashboard.

## Các thành phần đã thêm

### 1. BadgeView (Custom View)
- **File**: `app/src/main/java/com/deligo/app/views/BadgeView.java`
- **Chức năng**: Hiển thị badge màu đỏ với số đơn hàng chờ xác nhận
- **Vị trí**: Góc trên bên phải của button "Quản lý đơn hàng"

### 2. OrderRepository - Phương thức mới
- **File**: `app/src/main/java/com/deligo/app/repositories/OrderRepository.java`
- **Phương thức mới**:
  - `getPendingOrdersCount()`: Lấy số lượng đơn hàng chờ xác nhận
  - `listenToPendingOrders()`: Lắng nghe thay đổi đơn hàng real-time
  - `removeOrderListener()`: Xóa listener khi không cần thiết

### 3. OrderRepositoryImpl - Triển khai
- **File**: `app/src/main/java/com/deligo/app/repositories/OrderRepositoryImpl.java`
- **Công nghệ**: Firebase Firestore Snapshot Listener
- **Chức năng**: Lắng nghe thay đổi trong collection "orders" với status "pending"

### 4. AdminMainActivity - Cập nhật
- **File**: `app/src/main/java/com/deligo/app/activities/AdminMainActivity.java`
- **Tính năng mới**:
  - Hiển thị badge số đơn hàng chờ
  - Gửi notification khi có đơn hàng mới
  - Request permission POST_NOTIFICATIONS cho Android 13+
  - Tự động cập nhật badge khi có thay đổi

### 5. Layout cập nhật
- **File**: `app/src/main/res/layout/activity_admin_main.xml`
- **Thay đổi**: Thêm BadgeView vào FrameLayout bao quanh button "Quản lý đơn hàng"

### 6. Icon notification
- **File**: `app/src/main/res/drawable/ic_notification.xml`
- **Chức năng**: Icon hiển thị trong notification

### 7. Permission
- **File**: `app/src/main/AndroidManifest.xml`
- **Thêm**: `<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />`

## Cách hoạt động

1. **Khi Admin mở app**:
   - AdminMainActivity khởi tạo và request permission notification (Android 13+)
   - Tạo notification channel "admin_orders_channel"
   - Bắt đầu lắng nghe đơn hàng pending từ Firebase

2. **Khi có đơn hàng mới**:
   - Firebase Snapshot Listener phát hiện thay đổi
   - Badge cập nhật số lượng đơn hàng chờ
   - Nếu số lượng tăng, gửi notification với nội dung: "Bạn có X đơn hàng chờ xác nhận"

3. **Khi Admin click notification**:
   - Mở màn hình AdminOrdersActivity để xem danh sách đơn hàng

4. **Khi Admin quay lại màn hình**:
   - onResume() refresh lại số lượng đơn hàng chờ

5. **Khi Admin thoát app**:
   - onDestroy() xóa listener để tránh memory leak

## Lưu ý kỹ thuật

- Badge chỉ hiển thị khi có đơn hàng chờ (count > 0)
- Badge hiển thị tối đa "99+", nếu số lượng > 99
- Notification chỉ hiển thị khi số lượng đơn hàng tăng (không hiển thị khi giảm)
- Sử dụng real-time listener nên không cần refresh thủ công
- Permission notification được request tự động cho Android 13+

## Test

Để test tính năng:
1. Đăng nhập với tài khoản Admin
2. Mở màn hình Admin Dashboard
3. Từ một thiết bị khác hoặc emulator khác, đăng nhập với tài khoản Customer và đặt hàng
4. Quan sát badge và notification xuất hiện trên màn hình Admin

## Màu sắc

- Badge background: `@color/error` (#EA4335 - màu đỏ)
- Badge text: `@color/white` (#FFFFFF)
- Badge size: 24dp x 24dp
- Text size: 10sp (bold)
