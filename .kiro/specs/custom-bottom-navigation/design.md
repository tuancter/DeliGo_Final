# Design Document - Custom Bottom Navigation

## Overview

Thiết kế này mô tả cách thay thế hoàn toàn Material Design Components khỏi ứng dụng DeliGo, tập trung vào việc tạo một hệ thống bottom navigation tùy chỉnh sử dụng LinearLayout và các Android components cơ bản. Giải pháp này loại bỏ dependency `com.google.android.material:material`, giảm kích thước APK, và đảm bảo tương thích 100% trên mọi thiết bị Android từ API 24+.

### Goals
- Loại bỏ hoàn toàn Material Design Components dependency
- Thay thế BottomNavigationView bằng LinearLayout tùy chỉnh
- Chuyển đổi theme từ Material3 sang AppCompat
- Duy trì hoặc cải thiện trải nghiệm người dùng
- Giảm kích thước APK
- Tránh InflateException và các vấn đề tương thích

### Non-Goals
- Không thay đổi business logic hoặc data layer
- Không thay đổi Firebase integration
- Không thay đổi navigation flow hiện tại

## Architecture

### Component Hierarchy

```
Activity (Customer/Admin)
├── ConstraintLayout (Root)
│   ├── FrameLayout (Fragment Container)
│   │   └── Current Fragment
│   └── CustomBottomNavigation (LinearLayout)
│       ├── NavigationItem 1 (LinearLayout)
│       │   ├── ImageView (Icon)
│       │   └── TextView (Label)
│       ├── NavigationItem 2
│       ├── NavigationItem 3
│       └── NavigationItem 4
```

### Key Components

#### 1. CustomBottomNavigation
- **Type**: Custom ViewGroup extending LinearLayout
- **Responsibility**: Container cho các navigation items, quản lý selection state
- **Location**: `com.deligo.app.views.CustomBottomNavigation`

#### 2. NavigationItem
- **Type**: LinearLayout với ImageView + TextView
- **Responsibility**: Hiển thị một tab với icon và label
- **State**: Selected/Unselected với color changes

#### 3. NavigationManager
- **Type**: Helper class
- **Responsibility**: Quản lý Fragment transactions và navigation state
- **Location**: `com.deligo.app.utils.NavigationManager`

## Components and Interfaces

### 1. CustomBottomNavigation View

```java
public class CustomBottomNavigation extends LinearLayout {
    
    private List<NavigationItem> items;
    private int selectedPosition = 0;
    private OnNavigationItemSelectedListener listener;
    
    // Constructor và initialization
    public CustomBottomNavigation(Context context, AttributeSet attrs);
    
    // Configuration
    public void setItems(List<NavigationItem> items);
    public void setSelectedItem(int position);
    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener);
    
    // Internal methods
    private void setupNavigationItems();
    private void updateItemStates();
    private void onItemClicked(int position);
    
    // Listener interface
    public interface OnNavigationItemSelectedListener {
        boolean onNavigationItemSelected(int position, NavigationItem item);
    }
}
```

### 2. NavigationItem Model

```java
public class NavigationItem {
    private int id;
    private int iconResId;
    private String title;
    private boolean isSelected;
    
    public NavigationItem(int id, int iconResId, String title);
    
    // Getters and setters
    public int getId();
    public int getIconResId();
    public String getTitle();
    public boolean isSelected();
    public void setSelected(boolean selected);
}
```

### 3. NavigationManager

```java
public class NavigationManager {
    
    private FragmentManager fragmentManager;
    private int containerId;
    private Map<Integer, Fragment> fragmentCache;
    
    public NavigationManager(FragmentManager fragmentManager, int containerId);
    
    // Fragment management
    public void navigateToFragment(int position, Fragment fragment);
    public void navigateToFragment(int position, Class<? extends Fragment> fragmentClass);
    
    // State management
    public void saveState(Bundle outState);
    public void restoreState(Bundle savedState);
    
    // Cache management
    private Fragment getOrCreateFragment(Class<? extends Fragment> fragmentClass);
}
```

## Data Models

### NavigationConfiguration

```java
public class NavigationConfiguration {
    
    public static List<NavigationItem> getCustomerNavigationItems(Context context) {
        List<NavigationItem> items = new ArrayList<>();
        items.add(new NavigationItem(R.id.nav_home, R.drawable.ic_home, 
            context.getString(R.string.nav_home)));
        items.add(new NavigationItem(R.id.nav_cart, R.drawable.ic_cart, 
            context.getString(R.string.nav_cart)));
        items.add(new NavigationItem(R.id.nav_orders, R.drawable.ic_orders, 
            context.getString(R.string.nav_orders)));
        items.add(new NavigationItem(R.id.nav_profile, R.drawable.ic_profile, 
            context.getString(R.string.nav_profile)));
        return items;
    }
    
    public static List<NavigationItem> getAdminNavigationItems(Context context) {
        List<NavigationItem> items = new ArrayList<>();
        items.add(new NavigationItem(R.id.nav_orders, R.drawable.ic_orders, 
            context.getString(R.string.nav_orders)));
        items.add(new NavigationItem(R.id.nav_menu, R.drawable.ic_menu, 
            context.getString(R.string.nav_menu)));
        items.add(new NavigationItem(R.id.nav_statistics, R.drawable.ic_statistics, 
            context.getString(R.string.nav_statistics)));
        items.add(new NavigationItem(R.id.nav_complaints, R.drawable.ic_complaints, 
            context.getString(R.string.nav_complaints)));
        return items;
    }
}
```

## Layout Design

### 1. Custom Bottom Navigation Layout

**File**: `res/layout/view_custom_bottom_navigation.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:id="@+id/customBottomNavigation"
    android:layout_width="match_parent"
    android:layout_height="56dp"
    android:orientation="horizontal"
    android:background="@drawable/bottom_nav_background"
    android:elevation="8dp"
    android:paddingTop="4dp"
    android:paddingBottom="4dp">
    
    <!-- Navigation items will be added programmatically -->
    
</LinearLayout>
```

### 2. Navigation Item Layout

**File**: `res/layout/item_navigation.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="0dp"
    android:layout_height="match_parent"
    android:layout_weight="1"
    android:orientation="vertical"
    android:gravity="center"
    android:background="@drawable/navigation_item_background"
    android:clickable="true"
    android:focusable="true">
    
    <ImageView
        android:id="@+id/navIcon"
        android:layout_width="24dp"
        android:layout_height="24dp"
        android:scaleType="fitCenter"
        android:contentDescription="@string/nav_icon_description" />
    
    <TextView
        android:id="@+id/navLabel"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="2dp"
        android:textSize="12sp"
        android:maxLines="1"
        android:ellipsize="end" />
    
</LinearLayout>
```

### 3. Updated Activity Layouts

**File**: `res/layout/activity_customer_main.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <FrameLayout
        android:id="@+id/fragmentContainer"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toTopOf="@id/bottomNavigation"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <include
        android:id="@+id/bottomNavigation"
        layout="@layout/view_custom_bottom_navigation"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

## Drawable Resources

### 1. Navigation Item Background

**File**: `res/drawable/navigation_item_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_selected="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/primary_light_transparent" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/ripple_color" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@android:color/transparent" />
        </shape>
    </item>
</selector>
```

### 2. Bottom Navigation Background

**File**: `res/drawable/bottom_nav_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<layer-list xmlns:android="http://schemas.android.com/apk/res/android">
    <!-- Shadow -->
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/shadow_color" />
        </shape>
    </item>
    <!-- Background -->
    <item android:top="1dp">
        <shape android:shape="rectangle">
            <solid android:color="@color/white" />
        </shape>
    </item>
</layer-list>
```

### 3. Navigation Icons

Tạo các icon drawable cho navigation:
- `ic_home.xml` - Home icon
- `ic_cart.xml` - Shopping cart icon
- `ic_orders.xml` - Orders/history icon
- `ic_profile.xml` - User profile icon
- `ic_menu.xml` - Menu/food icon
- `ic_statistics.xml` - Statistics/chart icon
- `ic_complaints.xml` - Complaints/feedback icon

**Example**: `res/drawable/ic_home.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/nav_icon_color"
        android:pathData="M10,20v-6h4v6h5v-8h3L12,3 2,12h3v8z"/>
</vector>
```

## Color Resources

### Updated colors.xml

**File**: `res/values/colors.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- Existing colors -->
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    
    <!-- DeliGo Brand Colors -->
    <color name="primary">#FF6B35</color>
    <color name="primary_dark">#E85A2A</color>
    <color name="primary_light">#FF8C5F</color>
    <color name="accent">#FFC107</color>
    <color name="accent_dark">#FFA000</color>
    
    <!-- Background Colors -->
    <color name="background">#FFFFFF</color>
    <color name="background_light">#F5F5F5</color>
    <color name="surface">#FFFFFF</color>
    
    <!-- Text Colors -->
    <color name="text_primary">#212121</color>
    <color name="text_secondary">#757575</color>
    <color name="text_hint">#BDBDBD</color>
    
    <!-- Status Colors -->
    <color name="success">#4CAF50</color>
    <color name="error">#F44336</color>
    <color name="warning">#FF9800</color>
    <color name="info">#2196F3</color>
    
    <!-- Order Status Colors -->
    <color name="status_pending">#FFC107</color>
    <color name="status_accepted">#2196F3</color>
    <color name="status_preparing">#FF9800</color>
    <color name="status_completed">#4CAF50</color>
    <color name="status_cancelled">#F44336</color>
    
    <!-- New: Navigation Colors -->
    <color name="nav_selected">#FF6B35</color>
    <color name="nav_unselected">#757575</color>
    <color name="primary_light_transparent">#1AFF6B35</color>
    <color name="ripple_color">#1A000000</color>
    <color name="shadow_color">#1A000000</color>
</resources>
```

### Navigation Icon Color Selector

**File**: `res/color/nav_icon_color.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/nav_selected" android:state_selected="true" />
    <item android:color="@color/nav_unselected" />
</selector>
```

### Navigation Text Color Selector

**File**: `res/color/nav_text_color.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:color="@color/nav_selected" android:state_selected="true" />
    <item android:color="@color/nav_unselected" />
</selector>
```

## Theme Migration

### Updated themes.xml

**File**: `res/values/themes.xml`

```xml
<resources xmlns:tools="http://schemas.android.com/tools">
    <!-- Base application theme using AppCompat -->
    <style name="Base.Theme.DeliGo" parent="Theme.AppCompat.Light.NoActionBar">
        <!-- Primary brand color -->
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        
        <!-- Background colors -->
        <item name="android:colorBackground">@color/background</item>
        <item name="android:windowBackground">@color/background</item>
        
        <!-- Status bar color -->
        <item name="android:statusBarColor">@color/primary_dark</item>
        
        <!-- Text colors -->
        <item name="android:textColorPrimary">@color/text_primary</item>
        <item name="android:textColorSecondary">@color/text_secondary</item>
        <item name="android:textColorHint">@color/text_hint</item>
        
        <!-- Button style -->
        <item name="buttonStyle">@style/Widget.DeliGo.Button</item>
    </style>

    <style name="Theme.DeliGo" parent="Base.Theme.DeliGo" />
    
    <!-- Button Styles -->
    <style name="Widget.DeliGo.Button" parent="Widget.AppCompat.Button">
        <item name="android:textColor">@color/white</item>
        <item name="android:background">@drawable/button_primary</item>
        <item name="android:paddingStart">24dp</item>
        <item name="android:paddingEnd">24dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:textAllCaps">false</item>
    </style>
    
    <style name="Widget.DeliGo.Button.Outlined" parent="Widget.AppCompat.Button.Borderless">
        <item name="android:textColor">@color/primary</item>
        <item name="android:background">@drawable/button_outlined</item>
        <item name="android:paddingStart">24dp</item>
        <item name="android:paddingEnd">24dp</item>
        <item name="android:paddingTop">12dp</item>
        <item name="android:paddingBottom">12dp</item>
        <item name="android:textAllCaps">false</item>
    </style>
    
    <!-- Card Style -->
    <style name="Widget.DeliGo.Card">
        <item name="android:background">@drawable/card_background</item>
        <item name="android:padding">16dp</item>
    </style>
    
    <!-- Text Styles -->
    <style name="TextAppearance.DeliGo.Headline">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">24sp</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="TextAppearance.DeliGo.Title">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">18sp</item>
        <item name="android:textStyle">bold</item>
    </style>
    
    <style name="TextAppearance.DeliGo.Body">
        <item name="android:textColor">@color/text_primary</item>
        <item name="android:textSize">14sp</item>
    </style>
    
    <style name="TextAppearance.DeliGo.Caption">
        <item name="android:textColor">@color/text_secondary</item>
        <item name="android:textSize">12sp</item>
    </style>
    
    <!-- Splash Screen Theme -->
    <style name="Theme.DeliGo.Splash" parent="Theme.DeliGo">
        <item name="android:windowBackground">@color/primary</item>
        <item name="android:statusBarColor">@color/primary</item>
    </style>
</resources>
```

### Button Drawables

**File**: `res/drawable/button_primary.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/primary_dark" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item android:state_enabled="false">
        <shape android:shape="rectangle">
            <solid android:color="@color/text_hint" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@color/primary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>
```

**File**: `res/drawable/button_outlined.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<selector xmlns:android="http://schemas.android.com/apk/res/android">
    <item android:state_pressed="true">
        <shape android:shape="rectangle">
            <solid android:color="@color/primary_light_transparent" />
            <stroke android:width="2dp" android:color="@color/primary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
    <item>
        <shape android:shape="rectangle">
            <solid android:color="@android:color/transparent" />
            <stroke android:width="2dp" android:color="@color/primary" />
            <corners android:radius="8dp" />
        </shape>
    </item>
</selector>
```

**File**: `res/drawable/card_background.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle">
    <solid android:color="@color/white" />
    <corners android:radius="12dp" />
    <stroke android:width="1dp" android:color="@color/text_hint" />
</shape>
```

## Implementation Flow

### Phase 1: Create Custom Components
1. Create `CustomBottomNavigation` class
2. Create `NavigationItem` model
3. Create `NavigationManager` helper
4. Create `NavigationConfiguration` utility

### Phase 2: Create Resources
1. Create layout files (view_custom_bottom_navigation.xml, item_navigation.xml)
2. Create drawable resources (backgrounds, selectors, icons)
3. Create color resources and selectors
4. Update themes.xml to use AppCompat

### Phase 3: Update Activities
1. Update `CustomerMainActivity` to use CustomBottomNavigation
2. Update `AdminMainActivity` to use CustomBottomNavigation
3. Remove Material BottomNavigationView references
4. Implement NavigationManager integration

### Phase 4: Remove Material Dependency
1. Remove Material dependency from build.gradle
2. Remove Material imports from all Java files
3. Update all Material widgets to AppCompat equivalents
4. Test compilation

### Phase 5: Testing & Refinement
1. Test on multiple devices and API levels
2. Verify no InflateException occurs
3. Test navigation flow
4. Verify visual consistency
5. Performance testing

## Error Handling

### Potential Issues and Solutions

1. **Fragment Transaction Errors**
   - Use `commitAllowingStateLoss()` when appropriate
   - Check `isAdded()` before fragment operations
   - Handle configuration changes properly

2. **Memory Leaks**
   - Clear fragment cache when activity is destroyed
   - Remove listeners in `onDestroy()`
   - Use WeakReference for callbacks if needed

3. **State Restoration**
   - Save selected position in `onSaveInstanceState()`
   - Restore fragment state properly
   - Handle process death scenarios

4. **Visual Glitches**
   - Use `post()` for UI updates after layout
   - Ensure proper view recycling
   - Handle rapid clicks with debouncing

## Testing Strategy

### Unit Tests
- NavigationItem model tests
- NavigationConfiguration tests
- NavigationManager logic tests

### UI Tests
- Navigation item click tests
- Fragment switching tests
- State restoration tests
- Visual regression tests

### Integration Tests
- Full navigation flow tests
- Activity lifecycle tests
- Configuration change tests

### Manual Testing Checklist
- [ ] Test on API 24 device
- [ ] Test on API 34 device
- [ ] Test on different screen sizes
- [ ] Test rotation handling
- [ ] Test back button behavior
- [ ] Test rapid navigation clicks
- [ ] Test app backgrounding/foregrounding
- [ ] Verify no InflateException
- [ ] Verify APK size reduction
- [ ] Verify smooth animations

## Performance Considerations

1. **View Recycling**: Reuse navigation item views instead of recreating
2. **Fragment Caching**: Cache fragments to avoid recreation
3. **Lazy Loading**: Load fragments only when needed
4. **Memory Management**: Clear unused resources
5. **Animation Performance**: Use hardware acceleration

## Accessibility

1. **Content Descriptions**: Add proper contentDescription for icons
2. **Touch Targets**: Ensure minimum 48dp touch target size
3. **Color Contrast**: Maintain WCAG AA contrast ratios
4. **Screen Reader**: Test with TalkBack
5. **Focus Management**: Proper focus order for keyboard navigation

## Migration Path

### Before Migration
- App uses Material BottomNavigationView
- Dependency: `com.google.android.material:material:1.12.0`
- Theme: `Theme.Material3.DayNight.NoActionBar`

### After Migration
- App uses CustomBottomNavigation (LinearLayout)
- No Material dependency
- Theme: `Theme.AppCompat.Light.NoActionBar`
- Reduced APK size
- 100% compatibility, no InflateException

## Maintenance and Extensibility

### Adding New Navigation Items
1. Add icon drawable
2. Add string resource
3. Update `NavigationConfiguration`
4. Create corresponding Fragment
5. Update Activity to handle new item

### Customization Options
- Colors via themes and color resources
- Icon sizes via dimens
- Layout spacing via dimens
- Animation duration via integer resources
- Custom item layouts via layout inflation

## Dependencies After Migration

```gradle
dependencies {
    // Firebase (unchanged)
    implementation platform('com.google.firebase:firebase-bom:33.1.0')
    implementation 'com.google.firebase:firebase-auth'
    implementation 'com.google.firebase:firebase-firestore'
    
    // Jetpack (unchanged)
    implementation 'androidx.lifecycle:lifecycle-viewmodel:2.6.2'
    implementation 'androidx.lifecycle:lifecycle-livedata:2.6.2'
    
    // Image Loading (unchanged)
    implementation 'com.github.bumptech.glide:glide:4.16.0'
    annotationProcessor 'com.github.bumptech.glide:compiler:4.16.0'
    
    // UI - UPDATED: No Material dependency
    implementation libs.appcompat
    implementation libs.activity
    implementation libs.constraintlayout
    implementation 'androidx.recyclerview:recyclerview:1.3.2'
    implementation 'androidx.cardview:cardview:1.0.0'
    // REMOVED: implementation "com.google.android.material:material:1.12.0"
    
    // Testing (unchanged)
    testImplementation libs.junit
    androidTestImplementation libs.ext.junit
    androidTestImplementation libs.espresso.core
}
```

## Summary

Thiết kế này cung cấp một giải pháp hoàn chỉnh để thay thế Material Design Components bằng các Android components cơ bản. CustomBottomNavigation sử dụng LinearLayout với ImageView và TextView, hoàn toàn tùy chỉnh được và không phụ thuộc vào thư viện bên ngoài. Giải pháp này đảm bảo:

- ✅ Không có Material dependency
- ✅ Không có InflateException
- ✅ Tương thích 100% từ API 24+
- ✅ Giảm kích thước APK
- ✅ Dễ maintain và mở rộng
- ✅ Hiệu suất tốt
- ✅ Trải nghiệm người dùng mượt mà
