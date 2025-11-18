package com.deligo.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.deligo.app.repositories.ProfileRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;
import com.deligo.app.utils.UIHelper;
import com.deligo.app.utils.ViewModelFactory;
import com.deligo.app.viewmodels.OrderViewModel;
import com.deligo.app.viewmodels.ProfileViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

public class CheckoutActivity extends AppCompatActivity {
    private EditText etDeliveryAddress, etOrderNote, etPhoneNumber;
    private RadioGroup rgPaymentMethod;
    private TextView tvTotalAmount, tvNewAddress;
    private LinearLayout orderItemsContainer;
    private Button btnPlaceOrder;
    private ProgressBar progressBar;

    private OrderViewModel orderViewModel;
    private ProfileViewModel profileViewModel;
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
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        etDeliveryAddress = findViewById(R.id.etDeliveryAddress);
        etOrderNote = findViewById(R.id.etOrderNote);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        orderItemsContainer = findViewById(R.id.orderItemsContainer);
        tvTotalAmount = findViewById(R.id.tvTotalAmount);
        tvNewAddress = findViewById(R.id.tvNewAddress);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        ViewModelFactory factory = new ViewModelFactory();
        orderViewModel = new ViewModelProvider(this, factory).get(OrderViewModel.class);
        profileViewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
        
        // Load user profile to get default address
        profileViewModel.loadProfile();
    }

    private void getCartData() {
        // Get cart items and total from intent
        cartItems = (ArrayList<CartItem>) getIntent().getSerializableExtra("cartItems");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);

        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }

        // Display order summary
        displayOrderItems();
        tvTotalAmount.setText(CurrencyUtils.formatVND(totalAmount));
    }

    private void displayOrderItems() {
        orderItemsContainer.removeAllViews();
        
        for (CartItem item : cartItems) {
            View itemView = getLayoutInflater().inflate(R.layout.item_checkout_food, orderItemsContainer, false);
            
            ImageView foodImageView = itemView.findViewById(R.id.foodImageView);
            TextView foodNameTextView = itemView.findViewById(R.id.foodNameTextView);
            TextView quantityPriceTextView = itemView.findViewById(R.id.quantityPriceTextView);
            TextView itemTotalTextView = itemView.findViewById(R.id.itemTotalTextView);
            
            // Set food name
            foodNameTextView.setText(item.getFoodName());
            
            // Set quantity and unit price
            String quantityPrice = "x" + item.getQuantity() + " - " + CurrencyUtils.formatVND(item.getPrice());
            quantityPriceTextView.setText(quantityPrice);
            
            // Set item total
            double itemTotal = item.getPrice() * item.getQuantity();
            itemTotalTextView.setText(CurrencyUtils.formatVND(itemTotal));
            
            // Load image if available
            if (item.getFoodImageUrl() != null && !item.getFoodImageUrl().isEmpty()) {
                com.bumptech.glide.Glide.with(this)
                    .load(item.getFoodImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(foodImageView);
            }
            
            orderItemsContainer.addView(itemView);
        }
    }

    private void setupListeners() {
        btnPlaceOrder.setOnClickListener(v -> placeOrder());
        
        tvNewAddress.setOnClickListener(v -> {
            etDeliveryAddress.setText("");
            etDeliveryAddress.requestFocus();
        });
    }

    private void placeOrder() {
        String phoneNumber = etPhoneNumber.getText().toString().trim();
        String deliveryAddress = etDeliveryAddress.getText().toString().trim();
        String orderNote = etOrderNote.getText().toString().trim();

        // Validate phone number
        if (phoneNumber.isEmpty()) {
            etPhoneNumber.setError(getString(R.string.validation_phone_required));
            etPhoneNumber.requestFocus();
            return;
        }

        if (!android.util.Patterns.PHONE.matcher(phoneNumber).matches() || phoneNumber.length() < 10) {
            etPhoneNumber.setError(getString(R.string.validation_phone_invalid));
            etPhoneNumber.requestFocus();
            return;
        }

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
        // Observe profile to get default address and phone
        profileViewModel.getUserProfile().observe(this, user -> {
            if (user != null) {
                // Set default phone number
                if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                    if (etPhoneNumber.getText().toString().trim().isEmpty()) {
                        etPhoneNumber.setText(user.getPhone());
                    }
                }
                
                // Set default address
                if (user.getAddress() != null && !user.getAddress().isEmpty()) {
                    if (etDeliveryAddress.getText().toString().trim().isEmpty()) {
                        etDeliveryAddress.setText(user.getAddress());
                    }
                }
            }
        });
        
        orderViewModel.getIsLoading().observe(this, isLoading -> {
            UIHelper.showLoading(progressBar, isLoading);
            UIHelper.setButtonEnabled(btnPlaceOrder, !isLoading);
        });

        orderViewModel.getOrderPlaced().observe(this, orderPlaced -> {
            if (orderPlaced) {
                // Check if bank transfer is selected
                int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
                if (selectedPaymentId == R.id.rbBankTransfer) {
                    // Navigate to bank transfer screen
                    profileViewModel.getUserProfile().observe(this, user -> {
                        if (user != null && user.getFullName() != null) {
                            Intent intent = new Intent(CheckoutActivity.this, BankTransferActivity.class);
                            intent.putExtra("customerName", user.getFullName());
                            intent.putExtra("totalAmount", totalAmount);
                            startActivity(intent);
                        }
                    });
                }
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
