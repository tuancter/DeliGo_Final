# Requirements Document

## Introduction

Thay thế Material BottomNavigationView bằng một giải pháp điều hướng tùy chỉnh sử dụng LinearLayout với ImageButton/TextView. Giải pháp này loại bỏ phụ thuộc vào Material Design Components, giảm thiểu rủi ro InflateException, và đảm bảo tương thích với mọi thiết bị Android từ API 24 trở lên.

## Glossary

- **CustomBottomNavigation**: Hệ thống điều hướng tùy chỉnh thay thế BottomNavigationView
- **NavigationItem**: Một mục điều hướng bao gồm icon và label
- **FragmentContainer**: Container chứa các Fragment được hiển thị khi người dùng chọn NavigationItem
- **NavigationManager**: Class quản lý việc chuyển đổi Fragment
- **InflateException**: Lỗi xảy ra khi Android không thể inflate layout XML
- **Material Components**: Thư viện UI components của Google Material Design

## Requirements

### Requirement 1

**User Story:** Là một developer, tôi muốn thay thế BottomNavigationView bằng LinearLayout tùy chỉnh, để ứng dụng không phụ thuộc vào Material Components và tránh InflateException

#### Acceptance Criteria

1. THE CustomBottomNavigation SHALL sử dụng LinearLayout với orientation horizontal làm container chính
2. WHEN người dùng khởi động ứng dụng, THE CustomBottomNavigation SHALL hiển thị 3-4 NavigationItem với icon và label
3. THE CustomBottomNavigation SHALL không sử dụng bất kỳ component nào từ thư viện Material Design
4. THE CustomBottomNavigation SHALL hoạt động trên mọi thiết bị Android từ API level 24 trở lên
5. WHEN layout được inflate, THE CustomBottomNavigation SHALL không gây ra InflateException

### Requirement 2

**User Story:** Là một người dùng, tôi muốn nhấn vào các tab điều hướng để chuyển đổi giữa các màn hình, để dễ dàng truy cập các chức năng khác nhau

#### Acceptance Criteria

1. WHEN người dùng nhấn vào một NavigationItem, THE NavigationManager SHALL thay thế Fragment hiện tại bằng Fragment tương ứng
2. THE NavigationManager SHALL sử dụng FragmentTransaction để quản lý việc chuyển đổi Fragment
3. WHEN Fragment được thay đổi, THE CustomBottomNavigation SHALL cập nhật trạng thái selected của NavigationItem tương ứng
4. THE NavigationManager SHALL duy trì trạng thái của Fragment khi người dùng quay lại tab đã xem trước đó
5. WHEN người dùng nhấn nút back, THE NavigationManager SHALL xử lý navigation stack một cách hợp lý

### Requirement 3

**User Story:** Là một developer, tôi muốn tùy chỉnh giao diện của bottom navigation, để phù hợp với thiết kế của ứng dụng mà không bị giới hạn bởi Material Design

#### Acceptance Criteria

1. THE CustomBottomNavigation SHALL cho phép tùy chỉnh màu sắc của icon và text thông qua XML attributes hoặc programmatically
2. WHEN một NavigationItem được selected, THE CustomBottomNavigation SHALL thay đổi màu icon và text sang màu primary
3. WHEN một NavigationItem không được selected, THE CustomBottomNavigation SHALL hiển thị màu gray hoặc màu mặc định
4. THE CustomBottomNavigation SHALL cho phép tùy chỉnh kích thước icon và text size
5. THE CustomBottomNavigation SHALL hỗ trợ custom drawable cho mỗi NavigationItem

### Requirement 4

**User Story:** Là một developer, tôi muốn loại bỏ dependency Material Components khỏi project, để giảm kích thước APK và tránh xung đột thư viện

#### Acceptance Criteria

1. THE build.gradle file SHALL không chứa dependency "com.google.android.material:material"
2. THE ứng dụng SHALL compile và build thành công sau khi loại bỏ Material dependency
3. THE ứng dụng SHALL chạy bình thường trên thiết bị thực và emulator mà không có Material Components
4. WHEN Material dependency được loại bỏ, THE ứng dụng SHALL không có bất kỳ import statement nào tham chiếu đến Material Components
5. THE APK size SHALL giảm so với version sử dụng Material Components

### Requirement 5

**User Story:** Là một developer, tôi muốn cập nhật toàn bộ theme và color scheme, để đảm bảo giao diện nhất quán sau khi loại bỏ Material Design

#### Acceptance Criteria

1. THE themes.xml SHALL sử dụng Theme.AppCompat thay vì Theme.MaterialComponents
2. THE colors.xml SHALL định nghĩa các màu primary, primaryDark, accent cho ứng dụng
3. THE ứng dụng SHALL áp dụng theme nhất quán cho tất cả Activity và Fragment
4. WHEN theme được thay đổi, THE ứng dụng SHALL không có lỗi về missing attributes hoặc styles
5. THE giao diện SHALL duy trì tính thẩm mỹ và trải nghiệm người dùng tốt sau khi thay đổi theme

### Requirement 6

**User Story:** Là một người dùng, tôi muốn thấy hiệu ứng visual feedback khi nhấn vào tab, để biết rằng hành động của tôi đã được nhận diện

#### Acceptance Criteria

1. WHEN người dùng nhấn vào NavigationItem, THE CustomBottomNavigation SHALL hiển thị ripple effect hoặc background color change
2. THE ripple effect SHALL sử dụng android:background với selector drawable thay vì Material ripple
3. WHEN người dùng giữ ngón tay trên NavigationItem, THE CustomBottomNavigation SHALL hiển thị pressed state
4. THE visual feedback SHALL hoàn thành trong vòng 200ms
5. THE visual feedback SHALL hoạt động mượt mà trên mọi thiết bị

### Requirement 7

**User Story:** Là một developer, tôi muốn code dễ maintain và mở rộng, để có thể thêm hoặc bớt tab điều hướng trong tương lai

#### Acceptance Criteria

1. THE NavigationManager SHALL sử dụng interface hoặc abstract class để định nghĩa contract
2. THE CustomBottomNavigation SHALL cho phép thêm NavigationItem thông qua configuration array hoặc method
3. THE code SHALL tuân theo SOLID principles và separation of concerns
4. THE CustomBottomNavigation SHALL có documentation rõ ràng về cách sử dụng và tùy chỉnh
5. WHEN developer muốn thêm tab mới, THE developer SHALL chỉ cần thêm configuration và Fragment tương ứng mà không cần sửa logic core
