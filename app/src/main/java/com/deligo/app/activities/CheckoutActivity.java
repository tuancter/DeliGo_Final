package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.deligo.app.R;
import com.deligo.app.models.CartItem;
import com.deligo.app.repositories.CartRepositoryImpl;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etDeliveryAddress, etOrderNote;
    private RadioGroup rgPaymentMethod;
    private TextView tvItemCount, tvTotalAmount;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private OrderViewModel orderViewModel;
    private List<CartItem> cartItems;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        initViews();
        initViewModel();
        getCartData();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        etOrderNote = findViewById(R.id.etOrderNote);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvItemCount = findViewById(R.id.tvItemCount);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
    }

    private void getCartData() {
        // Get cart items and total from intent
        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Display order summary
        tvItemCount.setText(String.valueOf(cartItems.size()));
        tvTotalAmount.setText(CurrencyUtils.formatVND(totalAmount));
    }

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
    }

    private void placeOrder() {
        String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        String orderNote = etOrderNote.getText().toString().trim();

        // Validate delivery address
        if (deliveryAddress.isEmpty()) {
            etDeliveryAddress.setError(getString(R.string.validation_address_required));
            etDeliveryAddress.requestFocus();
            return;
        }

        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        RadioButton selectedPaymentButton = findViewById(selectedPaymentId);
        String paymentMethod = selectedPaymentButton.getText().toString();

        // Place order
        orderViewModel.placeOrder(deliveryAddress, paymentMethod, orderNote, cartItems);
    }

    private void observeViewModel() {
        orderViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
            UIHelper.setButtonEnabled(btnPlaceOrder, !isLoading);
        });

        orderViewModel.getOrderPlaced().observe(this, orderPlaced -> {
            if (orderPlaced) {
                showOrderConfirmation();
            }
        });

        orderViewModel.getErrorMessage().observe(this, errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                String friendlyMessage = UIHelper.getFirestoreErrorMessage(errorMessage);
                UIHelper.showErrorSnackbar(findViewById(android.R.id.content), 
                    friendlyMessage, v -> placeOrder());
            }
        });
    }

    private void showOrderConfirmation() {
        new AlertDialog.Builder(this, R.style.Theme_DeliGo_Dialog_Alert)
                .setTitle(R.string.dialog_order_success)
                .setMessage(R.string.dialog_order_success_message)
                .setPositiveButton(R.string.button_view_orders, (dialog, which) -> {
                    Intent intent = new Intent(CheckoutActivity.this, OrderHistoryActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton(R.string.button_continue_shopping, (dialog, which) -> {
                    Intent intent = new Intent(CheckoutActivity.this, CustomerMainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }
}
