package com.deligo.app.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.repositories.OrderRepository;
import com.deligo.app.repositories.OrderRepositoryImpl;
import com.deligo.app.utils.CurrencyUtils;

public class BankTransferActivity extends AppCompatActivity {
    private ImageView ivQRCode;
    private TextView tvAccountName, tvAccountNumber, tvBankName, tvTransferAmount;
    private Button btnTransferCompleted;
    
    private String customerName;
    private String orderId;
    private double totalAmount;
    private OrderRepository orderRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        // Get data from intent
        customerName = getIntent().getStringExtra("customerName");
        orderId = getIntent().getStringExtra("orderId");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);

        orderRepository = new OrderRepositoryImpl();

        initViews();
        setupToolbar();
        loadQRCode();
        displayBankInfo();
        setupTransferButton();
    }

    private void initViews() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvBankName = findViewById(R.id.tvBankName);
        tvTransferAmount = findViewById(R.id.tvTransferAmount);
        btnTransferCompleted = findViewById(R.id.btnTransferCompleted);
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

    private void loadQRCode() {
        // Build QR code URL
        String qrUrl = buildQRCodeUrl();
        
        // Load QR code using Glide
        Glide.with(this)
                .load(qrUrl)
                .placeholder(R.drawable.qrcode)
                .error(R.drawable.qrcode)
                .into(ivQRCode);
    }

    private String buildQRCodeUrl() {
        String bank = "Techcombank";
        String accountNumber = "9901239999";
        String template = "compact";
        int amount = (int) totalAmount;
        String description = customerName.replace(" ", "+") + "+chuyen+khoan";
        
        return String.format(
            "https://qr.sepay.vn/img?bank=%s&acc=%s&template=%s&amount=%d&des=%s",
            bank, accountNumber, template, amount, description
        );
    }

    private void displayBankInfo() {
        tvAccountName.setText("Phương Minh Tuấn");
        tvAccountNumber.setText("9901239999");
        tvBankName.setText("Techcombank");
        tvTransferAmount.setText(CurrencyUtils.formatVND(totalAmount));
    }

    private void setupTransferButton() {
        btnTransferCompleted.setOnClickListener(v -> {
            if (orderId == null || orderId.isEmpty()) {
                Toast.makeText(this, "Không tìm thấy thông tin đơn hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            // Disable button to prevent multiple clicks
            btnTransferCompleted.setEnabled(false);

            // Update payment status to "Khách hàng đã chuyển khoản"
            orderRepository.updatePaymentStatus(orderId, "Khách hàng đã chuyển khoản", new OrderRepository.ActionCallback() {
                @Override
                public void onSuccess() {
                    Toast.makeText(BankTransferActivity.this, "Đã xác nhận chuyển khoản", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(BankTransferActivity.this, "Lỗi: " + message, Toast.LENGTH_SHORT).show();
                    btnTransferCompleted.setEnabled(true);
                }
            });
        });
    }
}
