# Tính năng Biểu đồ Thống kê Admin

## Tổng quan
Đã thêm biểu đồ trực quan vào màn hình thống kê admin để hiển thị dữ liệu một cách sinh động và dễ hiểu hơn.

## Các biểu đồ đã thêm

### 1. Biểu đồ Tròn (Pie Chart) - Đơn hàng theo trạng thái
- **Vị trí**: Trong card "Đơn hàng theo trạng thái"
- **Mục đích**: Hiển thị tỷ lệ phần trăm các đơn hàng theo từng trạng thái
- **Màu sắc**: Mỗi trạng thái có màu riêng biệt
  - Pending (Chờ xử lý) - Vàng
  - Confirmed (Đã xác nhận) - Xanh dương
  - Preparing (Đang chuẩn bị) - Tím
  - Ready (Sẵn sàng) - Cam
  - Delivering (Đang giao) - Xanh nhạt
  - Delivered (Đã giao) - Xanh lá
  - Cancelled (Đã hủy) - Đỏ
- **Tính năng**: 
  - Hiển thị phần trăm
  - Có thể xoay biểu đồ
  - Nhấn vào từng phần để xem chi tiết

### 2. Biểu đồ Cột (Bar Chart) - Món ăn bán chạy
- **Vị trí**: Trong card "Món ăn bán chạy"
- **Mục đích**: So sánh số lượng bán của các món ăn
- **Hiển thị**: Top 10 món ăn bán chạy nhất
- **Tính năng**:
  - Hiển thị số lượng đã bán trên mỗi cột
  - Tên món ăn được rút gọn nếu quá dài
  - Trục X nghiêng 45 độ để dễ đọc

### 3. Biểu đồ Đường (Line Chart) - Xu hướng doanh thu
- **Vị trí**: Card mới "Xu hướng doanh thu"
- **Mục đích**: Hiển thị xu hướng doanh thu theo thời gian
- **Hiển thị**: Doanh thu 7 ngày gần nhất (hiện tại là dữ liệu mẫu)
- **Tính năng**:
  - Đường cong mượt mà
  - Có thể zoom và kéo
  - Hiển thị giá trị tại mỗi điểm
  - Vùng dưới đường được tô màu nhạt

## Thư viện sử dụng
**MPAndroidChart v3.1.0** - Thư viện biểu đồ mạnh mẽ và phổ biến cho Android

## Cài đặt
Đã thêm vào `app/build.gradle`:
```gradle
implementation 'com.github.PhilJay:MPAndroidChart:v3.1.0'
```

Đã thêm repository vào `settings.gradle`:
```gradle
maven { url 'https://jitpack.io' }
```

## Cách sử dụng
1. Mở ứng dụng với tài khoản Admin
2. Vào menu "Thống kê"
3. Chọn khoảng thời gian (Hôm nay / Tuần này / Tháng này)
4. Xem các biểu đồ tự động cập nhật

## Lưu ý
- Biểu đồ đường hiện đang sử dụng dữ liệu mẫu
- Để hiển thị dữ liệu thực, cần cập nhật `StatisticsViewModel` để cung cấp dữ liệu xu hướng theo thời gian
- Tất cả biểu đồ có animation khi hiển thị (1 giây)

## Tùy chỉnh
Có thể tùy chỉnh màu sắc, kiểu hiển thị trong các phương thức:
- `setupPieChart()` - Cấu hình biểu đồ tròn
- `setupBarChart()` - Cấu hình biểu đồ cột
- `setupLineChart()` - Cấu hình biểu đồ đường
