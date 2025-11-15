package com.deligo.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.AdminMenuViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddEditFoodActivity extends AppCompatActivity {
    private AdminMenuViewModel viewModel;
    
    private Toolbar toolbar;
    private EditText nameEditText;
    private EditText descriptionEditText;
    private EditText priceEditText;
    private Spinner categorySpinner;
    private EditText imageUrlEditText;
    private Switch availableSwitch;
    private Button saveButton;
    private ProgressBar progressBar;
    
    private List<Category> categories = new ArrayList<>();
    private Map<String, String> categoryNameToIdMap = new HashMap<>();
    private boolean isEditMode = false;
    private String foodId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_food);

        initViews();
        setupViewModel();
        loadCategories();
        checkEditMode();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        nameEditText = findViewById(R.id.nameEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        priceEditText = findViewById(R.id.priceEditText);
        categorySpinner = findViewById(R.id.categorySpinner);
        imageUrlEditText = findViewById(R.id.imageUrlEditText);
        availableSwitch = findViewById(R.id.availableSwitch);
        saveButton = findViewById(R.id.saveButton);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        viewModel = new ViewModelProvider(this, factory).get(AdminMenuViewModel.class);
    }

    private void loadCategories() {
        viewModel.loadCategories();
    }

    private void checkEditMode() {
        // Check if we're in edit mode
        foodId = getIntent().getStringExtra("foodId");
        if (foodId != null) {
            isEditMode = true;
            toolbar.setTitle(R.string.edit_food);
            
            // Populate fields with existing data
            nameEditText.setText(getIntent().getStringExtra("foodName"));
            descriptionEditText.setText(getIntent().getStringExtra("foodDescription"));
            priceEditText.setText(String.valueOf(getIntent().getDoubleExtra("foodPrice", 0.0)));
            imageUrlEditText.setText(getIntent().getStringExtra("foodImageUrl"));
            availableSwitch.setChecked(getIntent().getBooleanExtra("foodIsAvailable", true));
            
            // Category will be set after categories are loaded
        } else {
            isEditMode = false;
            toolbar.setTitle(R.string.add_food);
        }
    }

    private void setupListeners() {
        saveButton.setOnClickListener(v -> saveFood());
    }

    private void observeViewModel() {
        viewModel.getCategories().observe(this, categoryList -> {
            if (categoryList != null && !categoryList.isEmpty()) {
                categories = categoryList;
                setupCategoryDropdown();
                
                // If in edit mode, set the selected category
                if (isEditMode) {
                    String categoryId = getIntent().getStringExtra("foodCategoryId");
                    for (int i = 0; i < categories.size(); i++) {
                        if (categories.get(i).getCategoryId().equals(categoryId)) {
                            categorySpinner.setSelection(i);
                            break;
                        }
                    }
                }
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            saveButton.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
                finish(); // Close activity on success
            }
        });
    }

    private void setupCategoryDropdown() {
        List<String> categoryNames = new ArrayList<>();
        categoryNameToIdMap.clear();
        
        for (Category category : categories) {
            categoryNames.add(category.getCategoryName());
            categoryNameToIdMap.put(category.getCategoryName(), category.getCategoryId());
        }
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void saveFood() {
        // Validate inputs
        String name = nameEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();
        String priceStr = priceEditText.getText().toString().trim();
        String categoryName = categorySpinner.getSelectedItem() != null ? categorySpinner.getSelectedItem().toString() : "";
        String imageUrl = imageUrlEditText.getText().toString().trim();
        boolean isAvailable = availableSwitch.isChecked();

        if (name.isEmpty()) {
            nameEditText.setError(getString(R.string.validation_name_required));
            return;
        }

        if (description.isEmpty()) {
            descriptionEditText.setError(getString(R.string.validation_description_required));
            return;
        }

        if (priceStr.isEmpty()) {
            priceEditText.setError(getString(R.string.validation_price_required));
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
            if (price <= 0) {
                priceEditText.setError(getString(R.string.validation_price_positive));
                return;
            }
        } catch (NumberFormatException e) {
            priceEditText.setError(getString(R.string.validation_price_invalid));
            return;
        }

        if (categoryName.isEmpty()) {
            Toast.makeText(this, getString(R.string.toast_select_category), Toast.LENGTH_SHORT).show();
            return;
        }

        String categoryId = categoryNameToIdMap.get(categoryName);
        if (categoryId == null) {
            Toast.makeText(this, getString(R.string.toast_invalid_category), Toast.LENGTH_SHORT).show();
            return;
        }

        // Create Food object
        Food food = new Food();
        food.setName(name);
        food.setDescription(description);
        food.setPrice(price);
        food.setCategoryId(categoryId);
        food.setImageUrl(imageUrl);
        food.setAvailable(isAvailable);

        // Save or update
        if (isEditMode) {
            viewModel.updateFood(foodId, food);
        } else {
            viewModel.addFood(food);
        }
    }
}
