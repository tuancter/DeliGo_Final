package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.CategoryAdapter;
import com.deligo.app.models.Category;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminMenuViewModel;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class ManageCategoriesActivity extends AppCompatActivity 
        implements CategoryAdapter.OnCategoryActionListener {
    
    private AdminMenuViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    
    private MaterialToolbar toolbar;
    private RecyclerView categoryRecyclerView;
    private FloatingActionButton fabAddCategory;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_categories);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupFAB();
        observeViewModel();

        // Load categories
        viewModel.loadCategories();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        categoryRecyclerView = findViewById(R.id.categoryRecyclerView);
        fabAddCategory = findViewById(R.id.fabAddCategory);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminMenuViewModel.class);
    }

    private void setupRecyclerView() {
        categoryAdapter = new CategoryAdapter(this);
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    private void setupFAB() {
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                categoryAdapter.setCategoryList(categories);
                emptyTextView.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                categoryRecyclerView.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

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
                        Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(this, "Category name cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
