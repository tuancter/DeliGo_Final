# Implementation Plan

- [x] 1. Tạo custom navigation components và models





  - Tạo class NavigationItem model với id, iconResId, title, isSelected
  - Tạo class NavigationConfiguration với static methods getCustomerNavigationItems() và getAdminNavigationItems()
  - Tạo class CustomBottomNavigation extends LinearLayout với methods setItems(), setSelectedItem(), setOnNavigationItemSelectedListener()
  - Tạo class NavigationManager để quản lý Fragment transactions với fragmentCache
  - _Requirements: 1.1, 1.2, 2.1, 7.1, 7.2_

- [x] 2. Tạo layout resources cho custom bottom navigation


  - Tạo layout view_custom_bottom_navigation.xml với LinearLayout horizontal orientation
  - Tạo layout item_navigation.xml với LinearLayout vertical chứa ImageView và TextView
  - _Requirements: 1.1, 1.2, 3.4_

- [x] 3. Tạo drawable resources cho navigation


  - Tạo navigation_item_background.xml selector với states selected, pressed, default
  - Tạo bottom_nav_background.xml layer-list với shadow và background
  - Tạo các icon drawables: ic_home.xml, ic_cart.xml, ic_orders.xml, ic_profile.xml, ic_menu.xml, ic_statistics.xml, ic_complaints.xml
  - _Requirements: 1.3, 3.3, 3.5, 6.1, 6.2_

- [x] 4. Tạo color resources và selectors


  - Thêm colors mới vào colors.xml: nav_selected, nav_unselected, primary_light_transparent, ripple_color, shadow_color
  - Tạo nav_icon_color.xml selector cho icon colors
  - Tạo nav_text_color.xml selector cho text colors
  - _Requirements: 3.1, 3.2, 3.3, 5.2_

- [x] 5. Migrate themes từ Material3 sang AppCompat



  - Cập nhật themes.xml: thay Theme.Material3 bằng Theme.AppCompat.Light.NoActionBar
  - Xóa tất cả Material-specific attributes và styles
  - Tạo Widget.DeliGo.Button styles với AppCompat parent
  - Tạo TextAppearance styles không dùng Material
  - _Requirements: 4.4, 5.1, 5.3, 5.4, 5.5_

- [x] 6. Tạo button và card drawable replacements
  - Tạo button_primary.xml selector với states pressed, disabled, default
  - Tạo button_outlined.xml selector với stroke và transparent background
  - Tạo card_background.xml shape với corners và stroke
  - _Requirements: 5.5_

- [x] 7. Update activity layouts để sử dụng CustomBottomNavigation
  - Cập nhật activity_customer_main.xml: thay BottomNavigationView bằng include view_custom_bottom_navigation.xml
  - Thêm FrameLayout fragmentContainer vào activity_customer_main.xml
  - Cập nhật activity_admin_main.xml: thay BottomNavigationView bằng include view_custom_bottom_navigation.xml
  - Thêm FrameLayout fragmentContainer vào activity_admin_main.xml
  - _Requirements: 1.1, 1.2, 2.1_

- [x] 8. Implement CustomBottomNavigation logic
  - Implement constructor và inflate item_navigation.xml cho mỗi NavigationItem
  - Implement setItems() để add navigation items vào LinearLayout
  - Implement setOnClickListener cho mỗi item để trigger OnNavigationItemSelectedListener
  - Implement updateItemStates() để update icon và text colors based on selected state
  - Implement setSelectedItem() để programmatically select item
  - _Requirements: 2.1, 2.3, 3.1, 3.2, 3.3, 6.1, 6.3_

- [x] 9. Implement NavigationManager logic
  - Implement navigateToFragment() với FragmentTransaction replace
  - Implement fragment caching trong Map để reuse fragments
  - Implement saveState() và restoreState() cho configuration changes
  - Handle back stack management
  - _Requirements: 2.1, 2.2, 2.4, 2.5, 7.1_

- [x] 10. Update CustomerMainActivity để sử dụng custom navigation
  - Remove Material BottomNavigationView imports và references
  - Initialize CustomBottomNavigation từ layout
  - Setup NavigationConfiguration.getCustomerNavigationItems()
  - Initialize NavigationManager với fragmentContainer
  - Implement OnNavigationItemSelectedListener để call NavigationManager.navigateToFragment()
  - Set default selected item (Home)
  - _Requirements: 1.3, 2.1, 2.3, 4.4, 7.5_

- [x] 11. Update AdminMainActivity để sử dụng custom navigation
  - Remove Material BottomNavigationView imports và references
  - Initialize CustomBottomNavigation từ layout
  - Setup NavigationConfiguration.getAdminNavigationItems()
  - Initialize NavigationManager với fragmentContainer
  - Implement OnNavigationItemSelectedListener để call NavigationManager.navigateToFragment()
  - Set default selected item (Orders)
  - _Requirements: 1.3, 2.1, 2.3, 4.4, 7.5_

- [x] 12. Remove Material dependency và clean up
  - Remove "com.google.android.material:material" dependency từ app/build.gradle
  - Search và remove tất cả Material imports trong Java files
  - Delete menu files: customer_bottom_menu.xml, admin_bottom_menu.xml
  - Delete color file: bottom_nav_color.xml (replaced by nav_icon_color.xml và nav_text_color.xml)
  - Clean và rebuild project
  - _Requirements: 4.1, 4.2, 4.3, 4.4, 4.5_

- [x] 13. Handle configuration changes và state restoration
  - Implement onSaveInstanceState() trong Activities để save selected navigation position
  - Implement onRestoreInstanceState() để restore selected position
  - Test rotation và process death scenarios
  - Ensure fragments maintain state across configuration changes
  - _Requirements: 2.4, 2.5_

- [x] 14. Add visual feedback và animations
  - Verify ripple effect hoạt động với navigation_item_background.xml selector
  - Add transition animations cho fragment changes (optional fade in/out)
  - Implement debouncing cho rapid clicks (prevent double navigation)
  - Test pressed states và visual feedback timing
  - _Requirements: 6.1, 6.2, 6.3, 6.4, 6.5_

- [ ] 15. Verify compatibility và testing
  - Test trên device với API 24 (minimum SDK)
  - Test trên device với API 34 (target SDK)
  - Test trên different screen sizes (phone, tablet)
  - Verify không có InflateException khi inflate layouts
  - Verify APK size đã giảm so với version có Material
  - Test navigation flow: click items, switch fragments, back button
  - Test rapid navigation clicks
  - _Requirements: 1.4, 1.5, 4.5, 5.4, 6.5_
