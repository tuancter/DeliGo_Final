package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.AdminFoodAdapter;
import com.deligo.app.adapters.CategoryAdapter;
import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminMenuViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

public class AdminMenuActivity extends AppCompatActivity 
        implements AdminFoodAdapter.OnFoodActionListener, CategoryAdapter.OnCategoryActionListener {
    
    private AdminMenuViewModel viewModel;
    private AdminFoodAdapter foodAdapter;
    private CategoryAdapter categoryAdapter;
    
    private MaterialToolbar toolbar;
    private TabLayout tabLayout;
    private RecyclerView foodRecyclerView;
    private RecyclerView categoryRecyclerView;
    private FloatingActionButton fabAddFood;
    private FloatingActionButton fabAddCategory;
    private ProgressBar progressBar;
    
    private View foodsContent;
    private View categoriesContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_menu);

        initViews();
        setupViewModel();
        setupRecyclerViews();
        setupTabLayout();
        setupFABs();
        observeViewModel();

        // Load initial data
        viewModel.loadFoods();
        viewModel.loadCategories();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tabLayout = findViewById(R.id.tabLayout);
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        fabAddFood = findViewById(R.id.fabAddFood);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        progressBar = findViewById(R.id.progressBar);
        foodsContent = findViewById(R.id.foodsContent);
        categoriesContent = findViewById(R.id.categoriesContent);

        setSupportActionBar(toolbar);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminMenuViewModel.class);
    }

    private void setupRecyclerViews() {
        // Foods RecyclerView
        foodAdapter = new AdminFoodAdapter(this);
        foodRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        foodRecyclerView.setAdapter(foodAdapter);

        // Categories RecyclerView
        categoryAdapter = new CategoryAdapter(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupTabLayout() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    // Foods tab
                    foodsContent.setVisibility(View.VISIBLE);
                    categoriesContent.setVisibility(View.GONE);
                } else {
                    // Categories tab
                    foodsContent.setVisibility(View.GONE);
                    categoriesContent.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Show foods tab by default
        foodsContent.setVisibility(View.VISIBLE);
        categoriesContent.setVisibility(View.GONE);
    }

    private void setupFABs() {
        fabAddFood.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditFoodActivity.class);
            startActivity(intent);
        });

        fabAddCategory.setOnClickListener(v -> {
            showAddCategoryDialog();
        });
    }

    private void observeViewModel() {
        viewModel.getFoods().observe(this, foods -> {
            if (foods != null) {
                foodAdapter.setFoodList(foods);
            }
        });

        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoryAdapter.setCategoryList(categories);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> {
                        viewModel.loadFoods();
                        viewModel.loadCategories();
                    });
            }
        });

        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                UIHelper.showSuccessSnackbar(findViewById(android.R.id.content), successMessage);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload data when returning from add/edit screens
        viewModel.loadFoods();
        viewModel.loadCategories();
    }

    // AdminFoodAdapter.OnFoodActionListener implementation
    @Override
    public void onEditFood(Food food) {
        Intent intent = new Intent(this, AddEditFoodActivity.class);
        intent.putExtra("foodId", food.getFoodId());
        intent.putExtra("foodName", food.getName());
        intent.putExtra("foodDescription", food.getDescription());
        intent.putExtra("foodPrice", food.getPrice());
        intent.putExtra("foodCategoryId", food.getCategoryId());
        intent.putExtra("foodImageUrl", food.getImageUrl());
        intent.putExtra("foodIsAvailable", food.isAvailable());
        startActivity(intent);
    }

    @Override
    public void onDeleteFood(Food food) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Food")
                .setMessage("Are you sure you want to delete " + food.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteFood(food.getFoodId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onToggleAvailability(Food food) {
        viewModel.toggleAvailability(food.getFoodId(), !food.isAvailable());
    }

    // CategoryAdapter.OnCategoryActionListener implementation
    @Override
    public void onEditCategory(Category category) {
        showEditCategoryDialog(category);
    }

    @Override
    public void onDeleteCategory(Category category) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Category")
                .setMessage("Are you sure you want to delete " + category.getCategoryName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    viewModel.deleteCategory(category.getCategoryId());
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddCategoryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Category Name");

        new AlertDialog.Builder(this)
                .setTitle("Add Category")
                .setView(input)
                .setPositiveButton("Add", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        viewModel.addCategory(categoryName);
                    } else {
                        UIHelper.showErrorToast(this, "Category name cannot be empty");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(category.getCategoryName());
        input.setHint("Category Name");

        new AlertDialog.Builder(this)
                .setTitle("Edit Category")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        viewModel.updateCategory(category.getCategoryId(), categoryName);
                    } else {
                        UIHelper.showErrorToast(this, "Category name cannot be empty");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
