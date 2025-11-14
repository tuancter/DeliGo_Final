package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.adapters.CartAdapter;
import com.deligo.app.models.CartItem;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.CartViewModel;
import com.google.android.material.button.MaterialButton;

public class CartActivity extends AppCompatActivity implements CartAdapter.OnCartItemActionListener {
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private RecyclerView cartRecyclerView;
    private TextView totalAmountTextView;
    private MaterialButton checkoutButton;
    private ProgressBar progressBar;
    private View emptyCartLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        initViews();
        setupToolbar();
        setupViewModel();
        setupRecyclerView();
        setupCheckoutButton();
        observeViewModel();

        // Load cart data
        cartViewModel.loadCart();
    }

    private void initViews() {
        cartRecyclerView = findViewById(R.id.cartRecyclerView);
        totalAmountTextView = findViewById(R.id.totalAmountTextView);
        checkoutButton = findViewById(R.id.checkoutButton);
        progressBar = findViewById(R.id.progressBar);
        emptyCartLayout = findViewById(R.id.emptyCartLayout);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory(
                null,
                null,
                null,
                new CartRepositoryImpl()
        );
        cartViewModel = new ViewModelProvider(this, factory).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            // Navigate to checkout activity
            Intent intent = new Intent(CartActivity.this, CheckoutActivity.class);
            intent.putExtra("cartItems", (java.io.Serializable) cartViewModel.getCartItems().getValue());
            intent.putExtra("totalAmount", cartViewModel.getCartTotal().getValue());
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(this, cartItems -> {
            if (cartItems != null) {
                cartAdapter.setCartItems(cartItems);
                
                // Show/hide empty state
                boolean isEmpty = cartItems.isEmpty();
                emptyCartLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                cartRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                checkoutButton.setEnabled(!isEmpty);
            }
        });

        cartViewModel.getCartTotal().observe(this, total -> {
            if (total != null) {
                totalAmountTextView.setText(String.format("$%.2f", total));
            }
        });

        cartViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        cartViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> cartViewModel.loadCart());
            }
        });

        cartViewModel.getSuccessMessage().observe(this, successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                UIHelper.showSuccessSnackbar(findViewById(android.R.id.content), successMessage);
            }
        });
    }

    @Override
    public void onQuantityChanged(CartItem cartItem, int newQuantity) {
        cartViewModel.updateQuantity(cartItem.getCartItemId(), newQuantity);
    }

    @Override
    public void onRemoveItem(CartItem cartItem) {
        cartViewModel.removeItem(cartItem.getCartItemId());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Reload cart when returning to this activity
        cartViewModel.loadCart();
    }
}
