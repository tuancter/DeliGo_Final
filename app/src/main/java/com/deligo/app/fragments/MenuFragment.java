package com.deligo.app.fragments;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.activities.FoodDetailActivity;
import com.deligo.app.adapters.FoodAdapter;
import com.deligo.app.models.Category;
import com.deligo.app.models.Food;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.MenuViewModel;

import java.util.List;

/**
 * Fragment displaying the menu/home screen
 */
public class MenuFragment extends Fragment implements FoodAdapter.OnFoodClickListener {
    private static final String TAG = "MenuFragment";
    
    private MenuViewModel menuViewModel;
    private FoodAdapter foodAdapter;
    private RecyclerView foodRecyclerView;
    private SearchView searchView;
    private LinearLayout categoryChipGroup;
    private ProgressBar progressBar;
    private TextView emptyTextView;
    private String selectedCategoryId = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Creating MenuFragment view");
        View view = inflater.inflate(R.layout.fragment_menu, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupSearchView();
        observeViewModel();

        // Load initial data
        Log.d(TAG, "onCreateView: Loading foods and categories");
        menuViewModel.loadFoods();
        menuViewModel.loadCategories();
        
        return view;
    }

    private void initViews(View view) {
        foodRecyclerView = view.findViewById(R.id.foodRecyclerView);
        searchView = view.findViewById(R.id.searchView);
        categoryChipGroup = view.findViewById(R.id.categoryChipGroup);
        progressBar = view.findViewById(R.id.progressBar);
        emptyTextView = view.findViewById(R.id.emptyTextView);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        menuViewModel = new ViewModelProvider(this, factory).get(MenuViewModel.class);
    }

    private void setupRecyclerView() {
        foodAdapter = new FoodAdapter(this);
        foodRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
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
        menuViewModel.getFoodList().observe(getViewLifecycleOwner(), foods -> {
            if (foods != null) {
                Log.d(TAG, "observeViewModel: Food list updated, size: " + foods.size());
                foodAdapter.setFoodList(foods);
                emptyTextView.setVisibility(foods.isEmpty() ? View.VISIBLE : View.GONE);
                foodRecyclerView.setVisibility(foods.isEmpty() ? View.GONE : View.VISIBLE);
            } else {
                Log.w(TAG, "observeViewModel: Food list is null");
            }
        });

        menuViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                setupCategoryChips(categories);
            }
        });

        menuViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        menuViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getView() != null) {
                Log.e(TAG, "observeViewModel: Error occurred - " + errorMessage);
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(getView(), 
                    friendlyMessage, v -> menuViewModel.loadFoods());
            }
        });
    }

    private void setupCategoryChips(List<Category> categories) {
        if (categoryChipGroup == null || categories == null) {
            return;
        }

        categoryChipGroup.removeAllViews();

        // Add "All" button
        Button allButton = createCategoryButton("Tất cả", null, true);
        categoryChipGroup.addView(allButton);

        // Add category buttons
        for (Category category : categories) {
            Button categoryButton = createCategoryButton(category.getCategoryName(), category.getCategoryId(), false);
            categoryChipGroup.addView(categoryButton);
        }
    }

    private Button createCategoryButton(String name, String categoryId, boolean isSelected) {
        Button button = new Button(getContext());
        
        // Set layout params
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 16, 0);
        button.setLayoutParams(params);
        
        // Set text
        button.setText(name);
        button.setTextSize(14);
        button.setPadding(32, 16, 32, 16);
        
        // Set style based on selection
        updateButtonStyle(button, isSelected);
        
        // Set click listener
        button.setOnClickListener(v -> {
            selectedCategoryId = categoryId;
            menuViewModel.filterByCategory(categoryId);
            updateAllCategoryButtons();
        });
        
        return button;
    }

    private void updateButtonStyle(Button button, boolean isSelected) {
        if (isSelected) {
            button.setBackgroundResource(R.drawable.category_button_selected);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_primary));
        } else {
            button.setBackgroundResource(R.drawable.category_button_background);
            button.setTextColor(ContextCompat.getColor(requireContext(), R.color.text_secondary));
        }
    }

    private void updateAllCategoryButtons() {
        for (int i = 0; i < categoryChipGroup.getChildCount(); i++) {
            View child = categoryChipGroup.getChildAt(i);
            if (child instanceof Button) {
                Button button = (Button) child;
                boolean isSelected;
                
                if (i == 0) {
                    // "All" button
                    isSelected = (selectedCategoryId == null);
                } else {
                    isSelected = false;
                }
                
                updateButtonStyle(button, isSelected);
            }
        }
    }

    @Override
    public void onFoodClick(Food food) {
        Intent intent = new Intent(getActivity(), FoodDetailActivity.class);
        intent.putExtra("foodId", food.getFoodId());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Reload data when fragment becomes visible
        if (menuViewModel != null) {
            menuViewModel.loadFoods();
        }
    }
}
