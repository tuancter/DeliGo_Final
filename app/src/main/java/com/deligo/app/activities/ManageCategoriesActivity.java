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
import androidx.appcompat.widget.Toolbar;
import android.widget.ImageButton;

public class ManageCategoriesActivity extends AppCompatActivity 
        implements CategoryAdapter.OnCategoryActionListener {
    
    private AdminMenuViewModel viewModel;
    private CategoryAdapter categoryAdapter;
    
    private Toolbar toolbar;
    private RecyclerView categoryRecyclerView;
    private ImageButton fabAddCategory;
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
                .setTitle(R.string.dialog_delete_category)
                .setMessage(getString(R.string.dialog_delete_category_message, category.getCategoryName()))
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteCategory(category.getCategoryId());
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showAddCategoryDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(R.string.hint_category_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_add_category)
                .setView(input)
                .setPositiveButton(R.string.action_add, (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        viewModel.addCategory(categoryName);
                    } else {
                        Toast.makeText(this, getString(R.string.toast_category_empty), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void showEditCategoryDialog(Category category) {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setText(category.getCategoryName());
        input.setHint(R.string.hint_category_name);

        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_edit_category)
                .setView(input)
                .setPositiveButton(R.string.action_save, (dialog, which) -> {
                    String categoryName = input.getText().toString().trim();
                    if (!categoryName.isEmpty()) {
                        viewModel.updateCategory(category.getCategoryId(), categoryName);
                    } else {
                        Toast.makeText(this, getString(R.string.toast_category_empty), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }
}
