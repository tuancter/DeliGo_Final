package com.deligo.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.deligo.app.R;
import com.deligo.app.activities.CheckoutActivity;
import com.deligo.app.adapters.CartAdapter;
import com.deligo.app.models.CartItem;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.CartViewModel;

/**
 * Fragment displaying the shopping cart
 */
public class CartFragment extends Fragment implements CartAdapter.OnCartItemActionListener {
    
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private RecyclerView cartRecyclerView;
    private TextView totalAmountTextView;
    private Button checkoutButton;
    private ProgressBar progressBar;
    private View emptyCartLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cart, container, false);
        
        initViews(view);
        setupViewModel();
        setupRecyclerView();
        setupCheckoutButton();
        observeViewModel();

        // Load cart data
        cartViewModel.loadCart();
        
        return view;
    }

    private void initViews(View view) {
        cartRecyclerView = view.findViewById(R.id.cartRecyclerView);
        totalAmountTextView = view.findViewById(R.id.totalAmountTextView);
        checkoutButton = view.findViewById(R.id.checkoutButton);
        progressBar = view.findViewById(R.id.progressBar);
        emptyCartLayout = view.findViewById(R.id.emptyCartLayout);
    }

    private void setupViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        cartViewModel = new ViewModelProvider(this, factory).get(CartViewModel.class);
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        cartRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        cartRecyclerView.setAdapter(cartAdapter);
    }

    private void setupCheckoutButton() {
        checkoutButton.setOnClickListener(v -> {
            // Navigate to checkout activity
            Intent intent = new Intent(getActivity(), CheckoutActivity.class);
            intent.putExtra("cartItems", (java.io.Serializable) cartViewModel.getCartItems().getValue());
            intent.putExtra("totalAmount", cartViewModel.getCartTotal().getValue());
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems != null) {
                cartAdapter.setCartItems(cartItems);
                
                // Show/hide empty state
                boolean isEmpty = cartItems.isEmpty();
                emptyCartLayout.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
                cartRecyclerView.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
                checkoutButton.setEnabled(!isEmpty);
            }
        });

        cartViewModel.getCartTotal().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                totalAmountTextView.setText(CurrencyUtils.formatVND(total));
            }
        });

        cartViewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
        });

        cartViewModel.getErrorMessage().observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty() && getView() != null) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(getView(), 
                    friendlyMessage, v -> cartViewModel.loadCart());
            }
        });

        cartViewModel.getSuccessMessage().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty() && getView() != null) {
                UIHelper.showSuccessSnackbar(getView(), successMessage);
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
    public void onResume() {
        super.onResume();
        // Reload cart when fragment becomes visible
        if (cartViewModel != null) {
            cartViewModel.loadCart();
        }
    }
}
