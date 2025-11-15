package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.FoodAdapter;
import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.repositories.CategoryRepositoryImpl;
import com.deligo.app.repositories.FoodRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.MenuViewModel;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import java.util.List;

public class MenuActivity extends AppCompatActivity implements FoodAdapter.OnFoodClickListener {
    private MenuViewModel menuViewModel;
    private FoodAdapter foodAdapter;
    private RecyclerView foodRecyclerView;
    private SearchView searchView;
    private HorizontalScrollView categoryChipGroup;
    private ProgressBar progressBar;
    private TextView emptyTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        initViews();
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        observeViewModel();

        // Load initial data
        menuViewModel.loadFoods();
        menuViewModel.loadCategories();
    }

    private void initViews() {
        foodRecyclerView = findViewById(R.id.foodRecyclerView);
        searchView = findViewById(R.id.searchView);
        categoryChipGroup = findViewById(R.id.categoryChipGroup);
        progressBar = findViewById(R.id.progressBar);
        emptyTextView = findViewById(R.id.emptyTextView);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        menuViewModel = new ViewModelProvider(this, factory).get(MenuViewModel.class);
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(this);
        foodRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        foodRecyclerView.setAdapter(foodAdapter);
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                menuViewModel.searchFoods(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.isEmpty()) {
                    menuViewModel.loadFoods();
                }
                return true;
            }
        });
    }

    private void observeViewModel() {
        menuViewModel.getFoodList().observe(this, foods -> {
            if (foods != null) {
                foodAdapter.setFoodList(foods);
                emptyTextView.setVisibility(foods.isEmpty() ? View.VISIBLE : View.GONE);
                foodRecyclerView.setVisibility(foods.isEmpty() ? View.GONE : View.VISIBLE);
            }
        });

        menuViewModel.getCategories().observe(this, categories -> {
            if (categories != null) {
                setupCategoryChips(categories);
            }
        });

        menuViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        menuViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> menuViewModel.loadFoods());
            }
        });
    }

    private void setupCategoryChips(List<Category> categories) {
        // Keep the "All" button
        Button chipAll = categoryChipGroup.findViewById(R.id.chipAll);
        if (chipAll != null) {
            chipAll.setOnClickListener(v -> {
                menuViewModel.filterByCategory(null);
            });
        }

        // Note: Dynamic category buttons are not supported with HorizontalScrollView
        // Categories should be predefined in XML or use a different approach
    }

    @Override
    public void onFoodClick(Food food) {
        Intent intent = new Intent(this, FoodDetailActivity.class);
        intent.putExtra("foodId", food.getFoodId());
        startActivity(intent);
    }
}
