# Constants Package

Package này chứa các enum và constants được sử dụng trong toàn bộ ứng dụng.

## OrderStatus

Enum quản lý các trạng thái đơn hàng với tên tiếng Việt.

### Các trạng thái:

| Enum | Tên tiếng Việt | Mô tả |
|------|----------------|-------|
| `PENDING` | "Chờ xác nhận" | Đơn hàng mới được tạo, chờ admin xác nhận |
| `ACCEPTED` | "Đã nhận" | Admin đã chấp nhận đơn hàng |
| `PREPARING` | "Đang chuẩn bị" | Đơn hàng đang được chuẩn bị |
| `COMPLETED` | "Đã hoàn thành" | Đơn hàng đã hoàn thành |
| `CANCELLED` | "Bị huỷ" | Đơn hàng bị huỷ |

### Cách sử dụng:

```java
// Lấy tên tiếng Việt
String statusName = OrderStatus.COMPLETED.getVietnameseName(); // "Đã hoàn thành"

// Chuyển từ tên tiếng Việt sang enum
OrderStatus status = OrderStatus.fromVietnameseName("Đã hoàn thành");

// Kiểm tra xem một string có match với status không
if (OrderStatus.COMPLETED.matches(orderStatusString)) {
    // Do something
}

// Sử dụng trong Firestore query
firestore.collection("orders")
    .whereEqualTo("orderStatus", OrderStatus.COMPLETED.getVietnameseName())
    .get();
```

## PaymentStatus

Enum quản lý các trạng thái thanh toán với tên tiếng Việt.

### Các trạng thái:

| Enum | Tên tiếng Việt | Mô tả |
|------|----------------|-------|
| `PENDING` | "Chờ thanh toán" | Chờ thanh toán |
| `COMPLETED` | "Đã hoàn thành" | Đã thanh toán thành công |
| `CANCELLED` | "Bị huỷ" | Thanh toán bị huỷ |

### Cách sử dụng:

```java
// Lấy tên tiếng Việt
String statusName = PaymentStatus.COMPLETED.getVietnameseName(); // "Đã hoàn thành"

// Chuyển từ tên tiếng Việt sang enum
PaymentStatus status = PaymentStatus.fromVietnameseName("Đã hoàn thành");

// Kiểm tra xem một string có match với status không
if (PaymentStatus.PENDING.matches(paymentStatusString)) {
    // Do something
}
```

## Lợi ích của việc sử dụng Constants

1. **Tránh lỗi typo**: Không cần gõ string literal nhiều lần
2. **Dễ refactor**: Thay đổi một chỗ, áp dụng toàn bộ app
3. **Type-safe**: Compiler sẽ báo lỗi nếu sử dụng sai
4. **Autocomplete**: IDE sẽ gợi ý các giá trị có sẵn
5. **Dễ maintain**: Tất cả constants ở một nơi, dễ quản lý
