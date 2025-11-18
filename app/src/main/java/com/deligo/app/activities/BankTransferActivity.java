package com.deligo.app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.deligo.app.R;
import com.deligo.app.utils.CurrencyUtils;

public class BankTransferActivity extends AppCompatActivity {
    private ImageView ivQRCode;
    private TextView tvAccountName, tvAccountNumber, tvBankName, tvTransferAmount;
    
    private String customerName;
    private double totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bank_transfer);

        // Get data from intent
        customerName = getIntent().getStringExtra("customerName");
        totalAmount = getIntent().getDoubleExtra("totalAmount", 0.0);

        initViews();
        setupToolbar();
        loadQRCode();
        displayBankInfo();
    }

    private void initViews() {
        ivQRCode = findViewById(R.id.ivQRCode);
        tvAccountName = findViewById(R.id.tvAccountName);
        tvAccountNumber = findViewById(R.id.tvAccountNumber);
        tvBankName = findViewById(R.id.tvBankName);
        tvTransferAmount = findViewById(R.id.tvTransferAmount);
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
}
