# Requirements Document - DeliGo Food Ordering App

## Introduction

DeliGo là ứng dụng đặt đồ ăn di động dành cho một nhà hàng duy nhất, cho phép nhiều khách hàng đặt hàng trực tuyến. Hệ thống hỗ trợ hai vai trò người dùng: Admin (chủ cửa hàng) quản lý menu, danh mục, đơn hàng và xem thống kê; Customer (khách hàng) có thể duyệt menu, đặt hàng, thanh toán và quản lý hồ sơ cá nhân. Ứng dụng sử dụng Firebase Firestore để lưu trữ dữ liệu trực tuyến.

## Glossary

- **DeliGo System**: Ứng dụng Android đặt đồ ăn bao gồm giao diện người dùng và tích hợp Firebase Firestore
- **Customer**: Người dùng cuối có thể đặt hàng đồ ăn từ nhà hàng
- **Admin**: Chủ cửa hàng quản lý menu, đơn hàng và xem thống kê
- **Food Item**: Món ăn có sẵn trong menu với thông tin chi tiết
- **Cart**: Giỏ hàng chứa các món ăn mà Customer đã chọn trước khi đặt hàng
- **Order**: Đơn hàng được tạo sau khi Customer xác nhận thanh toán
- **Category**: Danh mục phân loại các món ăn
- **Firebase Firestore**: Cơ sở dữ liệu NoSQL cloud để lưu trữ dữ liệu ứng dụng
- **Payment Status**: Trạng thái thanh toán của đơn hàng (pending, completed, failed)
- **Order Status**: Trạng thái đơn hàng (pending, accepted, preparing, completed, cancelled)

## Requirements

### Requirement 1: User Authentication

**User Story:** As a Customer, I want to register, login, and logout, so that I can access my personal account and order history

#### Acceptance Criteria

1. WHEN a new user provides valid registration information (full name, email, phone, password), THE DeliGo System SHALL create a new Customer account in Firebase Firestore with Role set to "customer" and Status set to "active"

2. WHEN a registered user provides valid email and password credentials, THE DeliGo System SHALL authenticate the user and grant access to the appropriate interface based on their Role

3. WHEN an authenticated user requests to logout, THE DeliGo System SHALL terminate the user session and redirect to the login screen

4. IF a user provides invalid credentials during login, THEN THE DeliGo System SHALL display an error message and deny access

5. THE DeliGo System SHALL store user passwords in hashed format in Firebase Firestore

### Requirement 2: Customer Menu Browsing

**User Story:** As a Customer, I want to view and search the menu, so that I can find food items I want to order

#### Acceptance Criteria

1. WHEN a Customer accesses the menu screen, THE DeliGo System SHALL retrieve and display all available Food Items from Firebase Firestore with their name, description, price, and image

2. WHEN a Customer enters a search term in the search field, THE DeliGo System SHALL filter and display Food Items whose names contain the search term

3. WHEN a Customer selects a category filter, THE DeliGo System SHALL display only Food Items belonging to the selected Category

4. WHILE a Food Item has IsAvailable set to false, THE DeliGo System SHALL display the item as unavailable and prevent it from being added to Cart

5. WHEN a Customer selects a Food Item, THE DeliGo System SHALL display detailed information including description, price, image, and customer reviews

### Requirement 3: Shopping Cart Management

**User Story:** As a Customer, I want to add items to my cart and modify quantities, so that I can prepare my order before checkout

#### Acceptance Criteria

1. WHEN a Customer adds a Food Item to Cart, THE DeliGo System SHALL create or update a CartItem in Firebase Firestore with the selected FoodID, quantity, price, and optional note

2. WHEN a Customer modifies the quantity of a CartItem, THE DeliGo System SHALL update the CartItem quantity and recalculate the total cart amount

3. WHEN a Customer removes a CartItem, THE DeliGo System SHALL delete the CartItem from Firebase Firestore and update the cart total

4. THE DeliGo System SHALL display the current cart contents with item names, quantities, unit prices, and total amount

5. WHEN a Customer views their Cart, THE DeliGo System SHALL retrieve all CartItems associated with the Customer's CartID from Firebase Firestore

### Requirement 4: Order Placement and Payment

**User Story:** As a Customer, I want to place orders and make payments, so that I can receive my food

#### Acceptance Criteria

1. WHEN a Customer confirms checkout with a non-empty Cart, THE DeliGo System SHALL create an Order in Firebase Firestore with OrderStatus set to "pending" and PaymentStatus set to "pending"

2. WHEN an Order is created, THE DeliGo System SHALL transfer all CartItems to OrderDetails with the current quantity and unit price, then clear the Cart

3. WHEN a Customer provides delivery address and payment method, THE DeliGo System SHALL store this information in the Order record

4. WHEN a Customer completes payment, THE DeliGo System SHALL update the Order PaymentStatus to "completed" and record the payment timestamp

5. IF payment processing fails, THEN THE DeliGo System SHALL update PaymentStatus to "failed" and notify the Customer

6. THE DeliGo System SHALL calculate TotalAmount as the sum of all OrderDetails (Quantity × UnitPrice) for the Order

### Requirement 5: Customer Profile Management

**User Story:** As a Customer, I want to view and update my profile information, so that I can keep my account details current

#### Acceptance Criteria

1. WHEN a Customer accesses their profile screen, THE DeliGo System SHALL display the Customer's FullName, Email, Phone, and order history

2. WHEN a Customer updates their profile information, THE DeliGo System SHALL validate the new data and update the User record in Firebase Firestore

3. WHEN a Customer views order history, THE DeliGo System SHALL retrieve and display all Orders associated with the Customer's UserID sorted by CreatedAt in descending order

4. WHEN a Customer selects a past Order, THE DeliGo System SHALL display the Order details including OrderDetails, TotalAmount, OrderStatus, and PaymentStatus

### Requirement 6: Admin Menu Management

**User Story:** As an Admin, I want to manage food items and categories, so that I can keep the menu up-to-date

#### Acceptance Criteria

1. WHEN an Admin adds a new Food Item with valid information (name, description, price, CategoryID, image), THE DeliGo System SHALL create the Food Item in Firebase Firestore with IsAvailable set to true

2. WHEN an Admin updates a Food Item, THE DeliGo System SHALL validate the changes and update the Food record in Firebase Firestore

3. WHEN an Admin deletes a Food Item, THE DeliGo System SHALL set IsAvailable to false rather than removing the record from Firebase Firestore

4. WHEN an Admin adds a new Category with a valid CategoryName, THE DeliGo System SHALL create the Category in Firebase Firestore

5. WHEN an Admin updates or deletes a Category, THE DeliGo System SHALL modify the Category record in Firebase Firestore

6. THE DeliGo System SHALL allow Admin to upload and store Food Item images in Firebase Storage and save the ImageUrl in Firestore

### Requirement 7: Admin Order Management

**User Story:** As an Admin, I want to view and manage customer orders, so that I can fulfill them efficiently

#### Acceptance Criteria

1. WHEN an Admin accesses the order management screen, THE DeliGo System SHALL retrieve and display all Orders from Firebase Firestore with their OrderID, CustomerID, TotalAmount, OrderStatus, and CreatedAt

2. WHEN a new Order is placed by a Customer, THE DeliGo System SHALL notify the Admin interface in real-time

3. WHEN an Admin accepts an Order, THE DeliGo System SHALL update the OrderStatus to "accepted" and record the timestamp

4. WHEN an Admin updates an Order status, THE DeliGo System SHALL validate the status transition and update the Order record in Firebase Firestore

5. WHEN an Admin views Order details, THE DeliGo System SHALL display the Customer information, delivery address, OrderDetails with Food Item names and quantities, and payment information

6. THE DeliGo System SHALL allow Admin to update OrderStatus through the following valid transitions: pending → accepted → preparing → completed, or pending → cancelled

### Requirement 8: Admin Statistics and Reporting

**User Story:** As an Admin, I want to view sales statistics by day, week, and month, so that I can track business performance

#### Acceptance Criteria

1. WHEN an Admin selects a date range filter (day, week, or month), THE DeliGo System SHALL retrieve all Orders with CreatedAt within the selected period from Firebase Firestore

2. THE DeliGo System SHALL calculate and display total revenue as the sum of TotalAmount for all completed Orders in the selected period

3. THE DeliGo System SHALL calculate and display the total number of Orders in the selected period

4. THE DeliGo System SHALL display the number of Orders grouped by OrderStatus for the selected period

5. THE DeliGo System SHALL calculate and display the most popular Food Items based on quantity sold in OrderDetails for the selected period

### Requirement 9: Review and Rating System

**User Story:** As a Customer, I want to rate and review food items, so that I can share my experience with others

#### Acceptance Criteria

1. WHEN a Customer has a completed Order containing a Food Item, THE DeliGo System SHALL allow the Customer to submit a Review with Rating (1-5) and optional Comment

2. WHEN a Customer submits a Review, THE DeliGo System SHALL create a Review record in Firebase Firestore with the UserID, FoodID, Rating, Comment, and CreatedAt timestamp

3. WHEN a Customer or Admin views a Food Item, THE DeliGo System SHALL display all Reviews associated with the FoodID sorted by CreatedAt in descending order

4. THE DeliGo System SHALL calculate and display the average Rating for each Food Item based on all associated Reviews

5. THE DeliGo System SHALL prevent a Customer from submitting multiple Reviews for the same Food Item in a single Order

### Requirement 10: Complaint Management

**User Story:** As a Customer, I want to submit complaints about orders, so that issues can be addressed

#### Acceptance Criteria

1. WHEN a Customer has a completed Order, THE DeliGo System SHALL allow the Customer to submit a Complaint with Content describing the issue

2. WHEN a Customer submits a Complaint, THE DeliGo System SHALL create a Complaint record in Firebase Firestore with UserID, OrderID, Content, Status set to "pending", and CreatedAt timestamp

3. WHEN an Admin views the complaint management screen, THE DeliGo System SHALL retrieve and display all Complaints from Firebase Firestore with their ComplaintID, UserID, OrderID, Status, and CreatedAt

4. WHEN an Admin updates a Complaint Status, THE DeliGo System SHALL modify the Complaint record in Firebase Firestore

5. WHEN a Customer views their Complaints, THE DeliGo System SHALL retrieve and display all Complaints associated with the Customer's UserID
