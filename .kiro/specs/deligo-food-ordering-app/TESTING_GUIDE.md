# DeliGo Testing Guide

## Firebase Configuration Verification

### ✅ Firebase Setup Checklist

1. **google-services.json** - Located at `app/google-services.json`
   - Project ID: `deligo1-app`
   - Package Name: `com.deligo.app`
   - Status: ✅ Configured

2. **Firebase Dependencies** - In `app/build.gradle`
   - Firebase BOM: `33.1.0`
   - Firebase Authentication: ✅ Included
   - Firebase Firestore: ✅ Included

3. **Google Services Plugin** - Applied in `app/build.gradle`
   - Status: ✅ Configured

### Firebase Console Setup Required

Before testing, ensure the following are configured in Firebase Console:

1. **Authentication**
   - Enable Email/Password authentication
   - Go to: Firebase Console → Authentication → Sign-in method
   - Enable "Email/Password" provider

2. **Firestore Database**
   - Create Firestore database in production mode
   - Go to: Firebase Console → Firestore Database → Create database
   - Start in production mode (security rules will be configured)

3. **Firestore Security Rules**
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       // Allow authenticated users to read/write their own data
       match /users/{userId} {
         allow read, write: if request.auth != null && request.auth.uid == userId;
       }
       
       // Allow all authenticated users to read categories and foods
       match /categories/{categoryId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
       }
       
       match /foods/{foodId} {
         allow read: if request.auth != null;
         allow write: if request.auth != null && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
       }
       
       // Cart access
       match /carts/{cartId} {
         allow read, write: if request.auth != null && resource.data.userId == request.auth.uid;
         
         match /cartItems/{itemId} {
           allow read, write: if request.auth != null;
         }
       }
       
       // Orders
       match /orders/{orderId} {
         allow read: if request.auth != null && 
           (resource.data.customerId == request.auth.uid || 
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
         allow create: if request.auth != null && request.resource.data.customerId == request.auth.uid;
         allow update: if request.auth != null && 
           get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
         
         match /orderDetails/{detailId} {
           allow read: if request.auth != null;
           allow write: if request.auth != null;
         }
       }
       
       // Reviews
       match /reviews/{reviewId} {
         allow read: if request.auth != null;
         allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
       }
       
       // Complaints
       match /complaints/{complaintId} {
         allow read: if request.auth != null && 
           (resource.data.userId == request.auth.uid || 
            get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin');
         allow create: if request.auth != null && request.resource.data.userId == request.auth.uid;
         allow update: if request.auth != null && 
           get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == 'admin';
       }
     }
   }
   ```

## End-to-End Testing Flows

### 🧪 Test Flow 1: Customer Registration and Login

**Objective**: Verify user authentication works correctly

**Steps**:
1. Launch the app
2. On the Splash screen, wait for redirect to Login
3. Click "Register" button
4. Fill in registration form:
   - Full Name: "Test Customer"
   - Email: "customer@test.com"
   - Phone: "0123456789"
   - Password: "password123"
5. Click "Register"
6. Verify redirect to Customer Main screen
7. Logout
8. Login again with same credentials
9. Verify successful login

**Expected Results**:
- ✅ User account created in Firebase Authentication
- ✅ User document created in Firestore `users` collection with role="customer"
- ✅ Successful redirect to CustomerMainActivity
- ✅ Login works with created credentials

**Verification**:
- Check Firebase Console → Authentication → Users
- Check Firestore → users collection for new document

---

### 🧪 Test Flow 2: Admin Setup and Login

**Objective**: Create admin account and verify admin access

**Steps**:
1. **Manual Setup** (One-time): Create admin user in Firebase Console
   - Go to Firebase Console → Authentication → Add user
   - Email: "admin@deligo.com"
   - Password: "admin123"
   - Then go to Firestore → users collection → Create document:
     ```json
     {
       "userId": "[auth_uid]",
       "fullName": "Admin User",
       "email": "admin@deligo.com",
       "phone": "0987654321",
       "role": "admin",
       "status": "active",
       "createdAt": [current_timestamp]
     }
     ```

2. Launch app and login with admin credentials
3. Verify redirect to Admin Main screen
4. Verify bottom navigation shows: Orders, Menu, Statistics, Complaints

**Expected Results**:
- ✅ Admin user can login
- ✅ Redirect to AdminMainActivity
- ✅ Admin navigation menu displayed

---

### 🧪 Test Flow 3: Complete Customer Flow

**Objective**: Test full customer journey from browsing to order completion

#### 3.1 Browse Menu
1. Login as customer
2. Navigate to Menu (Home tab)
3. Verify food items are displayed
4. Test search functionality:
   - Enter food name in search bar
   - Verify filtered results
5. Test category filter:
   - Click on a category chip
   - Verify only foods from that category shown

**Expected Results**:
- ✅ Food items load from Firestore
- ✅ Search filters correctly
- ✅ Category filter works
- ✅ Images load using Glide

#### 3.2 View Food Details
1. Click on a food item
2. Verify Food Detail screen shows:
   - Food image
   - Name, description, price
   - Reviews section
   - "Add to Cart" button

**Expected Results**:
- ✅ All food details displayed
- ✅ Reviews loaded (if any exist)
- ✅ Average rating calculated

#### 3.3 Add to Cart
1. On Food Detail screen, click "Add to Cart"
2. Enter quantity (e.g., 2)
3. Add optional note
4. Click "Add to Cart"
5. Navigate to Cart tab
6. Verify item appears in cart
7. Test quantity update:
   - Increase/decrease quantity
   - Verify total updates
8. Test remove item:
   - Click remove button
   - Verify item removed

**Expected Results**:
- ✅ Item added to cart in Firestore
- ✅ Cart displays correct items
- ✅ Quantity updates work
- ✅ Total amount calculated correctly
- ✅ Remove item works

#### 3.4 Checkout and Place Order
1. In Cart, click "Checkout"
2. Fill in checkout form:
   - Delivery Address: "123 Test Street, District 1"
   - Payment Method: Select "Cash on Delivery"
   - Optional note: "Please call before delivery"
3. Review order summary
4. Click "Place Order"
5. Verify order confirmation shown
6. Navigate to Orders tab
7. Verify new order appears with status "pending"

**Expected Results**:
- ✅ Order created in Firestore
- ✅ Cart cleared after order
- ✅ Order appears in order history
- ✅ Order status is "pending"
- ✅ Payment status is "pending"

#### 3.5 View Order Details
1. In Orders tab, click on the order
2. Verify Order Detail screen shows:
   - Order ID and date
   - Delivery address
   - Order items with quantities
   - Total amount
   - Order status
   - Payment status

**Expected Results**:
- ✅ All order details displayed correctly
- ✅ Order items loaded from orderDetails subcollection

#### 3.6 Submit Review (After Order Completion)
**Note**: This requires admin to mark order as completed first

1. After order is completed, go to Menu
2. Click on a food item that was in the completed order
3. Click "Write Review" button
4. Fill in review:
   - Rating: 5 stars
   - Comment: "Delicious food!"
5. Submit review
6. Verify review appears on food detail page

**Expected Results**:
- ✅ Review created in Firestore
- ✅ Review appears on food detail
- ✅ Average rating updated

#### 3.7 Submit Complaint
1. Go to Orders tab
2. Click on a completed order
3. Click "Submit Complaint" button
4. Fill in complaint:
   - Content: "Food was cold"
5. Submit complaint
6. Go to Profile → My Complaints
7. Verify complaint appears with status "pending"

**Expected Results**:
- ✅ Complaint created in Firestore
- ✅ Complaint appears in user's complaint list
- ✅ Status is "pending"

#### 3.8 Update Profile
1. Navigate to Profile tab
2. Click "Edit Profile"
3. Update information:
   - Full Name: "Updated Name"
   - Phone: "0999999999"
4. Click "Save"
5. Verify profile updated

**Expected Results**:
- ✅ User document updated in Firestore
- ✅ Updated info displayed in profile

---

### 🧪 Test Flow 4: Complete Admin Flow

**Objective**: Test full admin functionality

#### 4.1 Manage Menu - Add Category
1. Login as admin
2. Navigate to Menu tab
3. Click "Manage Categories"
4. Click "Add Category"
5. Enter category name: "Appetizers"
6. Click "Save"
7. Verify category appears in list

**Expected Results**:
- ✅ Category created in Firestore
- ✅ Category appears in category list
- ✅ Category available for food assignment

#### 4.2 Manage Menu - Add Food Item
1. In Menu tab, click "Add Food"
2. Fill in food details:
   - Name: "Spring Rolls"
   - Description: "Fresh Vietnamese spring rolls"
   - Price: 50000
   - Category: Select "Appetizers"
   - Image URL: "https://example.com/spring-rolls.jpg"
3. Click "Save"
4. Verify food appears in menu

**Expected Results**:
- ✅ Food created in Firestore
- ✅ Food appears in admin menu list
- ✅ Food visible to customers
- ✅ isAvailable set to true

#### 4.3 Manage Menu - Edit Food Item
1. In Menu tab, click edit on a food item
2. Update price: 55000
3. Click "Save"
4. Verify price updated

**Expected Results**:
- ✅ Food document updated in Firestore
- ✅ Updated price displayed

#### 4.4 Manage Menu - Toggle Availability
1. In Menu tab, click toggle availability on a food item
2. Verify food marked as unavailable
3. Check customer view - food should show as unavailable
4. Toggle back to available

**Expected Results**:
- ✅ isAvailable field updated in Firestore
- ✅ Unavailable foods not addable to cart
- ✅ Toggle works both ways

#### 4.5 Accept and Manage Orders
1. Navigate to Orders tab
2. Verify pending orders appear (from customer test)
3. Click on a pending order
4. Click "Accept Order"
5. Verify status changes to "accepted"
6. Update status to "preparing"
7. Update status to "completed"
8. Verify customer sees updated status

**Expected Results**:
- ✅ All orders displayed in real-time
- ✅ Order status updates correctly
- ✅ Status transitions validated (pending → accepted → preparing → completed)
- ✅ Customer sees updated status immediately

#### 4.6 View Statistics
1. Navigate to Statistics tab
2. Select period: "Today"
3. Verify statistics displayed:
   - Total revenue
   - Order count
   - Orders by status
   - Top selling foods
4. Change period to "This Week"
5. Verify statistics update

**Expected Results**:
- ✅ Revenue calculated correctly (sum of completed orders)
- ✅ Order count accurate
- ✅ Status breakdown correct
- ✅ Top selling foods ranked by quantity
- ✅ Period filter works

#### 4.7 Manage Complaints
1. Navigate to Complaints tab
2. Verify complaints appear (from customer test)
3. Click on a complaint
4. Update status to "resolved"
5. Verify customer sees updated status

**Expected Results**:
- ✅ All complaints displayed
- ✅ Complaint details shown
- ✅ Status update works
- ✅ Customer sees updated status

---

## Testing Checklist

### Pre-Testing Setup
- [ ] Firebase project created
- [ ] Email/Password authentication enabled
- [ ] Firestore database created
- [ ] Security rules configured
- [ ] Admin user created manually
- [ ] Test categories created (optional)
- [ ] Test food items created (optional)

### Customer Flow Testing
- [ ] Registration works
- [ ] Login works
- [ ] Menu browsing works
- [ ] Search functionality works
- [ ] Category filter works
- [ ] Food detail view works
- [ ] Add to cart works
- [ ] Cart management works (update quantity, remove)
- [ ] Checkout works
- [ ] Order placement works
- [ ] Order history displays
- [ ] Order details display
- [ ] Review submission works (after order completion)
- [ ] Complaint submission works
- [ ] Profile update works
- [ ] Logout works

### Admin Flow Testing
- [ ] Admin login works
- [ ] Add category works
- [ ] Edit category works
- [ ] Delete category works
- [ ] Add food works
- [ ] Edit food works
- [ ] Toggle food availability works
- [ ] View all orders works
- [ ] Accept order works
- [ ] Update order status works
- [ ] Order status transitions validated
- [ ] Statistics display correctly
- [ ] Period filter works in statistics
- [ ] View complaints works
- [ ] Update complaint status works

### Error Handling Testing
- [ ] Invalid login credentials show error
- [ ] Empty cart checkout prevented
- [ ] Invalid input validation works
- [ ] Network error handling works
- [ ] Loading states display correctly
- [ ] Empty states display correctly

### UI/UX Testing
- [ ] App icon displays correctly
- [ ] Brand colors applied throughout
- [ ] Navigation works smoothly
- [ ] Images load correctly with Glide
- [ ] Responsive layouts on different screen sizes
- [ ] Dark mode works (if enabled)

---

## Common Issues and Solutions

### Issue: "User not found" after registration
**Solution**: Check that user document is created in Firestore with correct userId matching Firebase Auth UID

### Issue: Foods not loading
**Solution**: 
1. Check Firestore security rules allow read access
2. Verify foods collection exists with documents
3. Check network connectivity

### Issue: Images not loading
**Solution**:
1. Verify image URLs are valid and accessible
2. Check internet permission in AndroidManifest.xml
3. Verify Glide dependency is included

### Issue: Order not appearing for admin
**Solution**:
1. Check Firestore security rules allow admin to read all orders
2. Verify admin user has role="admin" in Firestore
3. Check real-time listener is properly set up

### Issue: Cart not clearing after order
**Solution**: Check that clearCart() is called after successful order creation in OrderRepository

---

## Performance Testing

### Load Testing Scenarios
1. **Large Menu**: Test with 100+ food items
2. **Many Orders**: Test with 50+ orders in history
3. **Multiple Cart Items**: Test with 20+ items in cart
4. **Long Reviews List**: Test food with 50+ reviews

### Expected Performance
- Menu load: < 2 seconds
- Order placement: < 3 seconds
- Cart operations: < 1 second
- Image loading: Progressive with placeholders

---

## Security Testing

### Authentication Testing
- [ ] Cannot access app without login
- [ ] Session persists after app restart
- [ ] Logout clears session
- [ ] Cannot access admin features as customer
- [ ] Cannot access customer data of other users

### Data Access Testing
- [ ] Customer can only see their own orders
- [ ] Customer can only edit their own profile
- [ ] Customer cannot modify food items
- [ ] Admin can see all orders
- [ ] Admin can modify menu items

---

## Build and Deployment

### Debug Build
```bash
./gradlew assembleDebug
```

### Release Build
```bash
./gradlew assembleRelease
```

### Install on Device
```bash
./gradlew installDebug
```

---

## Test Data Setup Script

For quick testing, you can manually add test data to Firestore:

### Sample Categories
```json
{
  "categoryId": "cat1",
  "categoryName": "Main Dishes"
}
{
  "categoryId": "cat2",
  "categoryName": "Appetizers"
}
{
  "categoryId": "cat3",
  "categoryName": "Desserts"
}
{
  "categoryId": "cat4",
  "categoryName": "Beverages"
}
```

### Sample Foods
```json
{
  "foodId": "food1",
  "categoryId": "cat1",
  "name": "Phở Bò",
  "description": "Traditional Vietnamese beef noodle soup",
  "price": 65000,
  "imageUrl": "https://images.unsplash.com/photo-1582878826629-29b7ad1cdc43",
  "isAvailable": true
}
{
  "foodId": "food2",
  "categoryId": "cat1",
  "name": "Cơm Tấm",
  "description": "Broken rice with grilled pork",
  "price": 55000,
  "imageUrl": "https://images.unsplash.com/photo-1626804475297-41608ea09aeb",
  "isAvailable": true
}
{
  "foodId": "food3",
  "categoryId": "cat2",
  "name": "Gỏi Cuốn",
  "description": "Fresh spring rolls",
  "price": 45000,
  "imageUrl": "https://images.unsplash.com/photo-1559314809-0d155014e29e",
  "isAvailable": true
}
```

---

## Conclusion

This testing guide covers all major functionality of the DeliGo app. Follow the flows systematically to ensure all features work correctly before deployment. Document any issues found and verify fixes before proceeding to production.

**Testing Status**: Ready for execution
**Last Updated**: 2025-11-15
