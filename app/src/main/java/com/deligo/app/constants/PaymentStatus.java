package com.deligo.app.constants;

public enum PaymentStatus {
    PENDING("Chờ thanh toán"),
    COMPLETED("Đã hoàn thành"),
    CANCELLED("Bị huỷ");

    private final String vietnameseName;

    PaymentStatus(String vietnameseName) {
        this.vietnameseName = vietnameseName;
    }

    public String getVietnameseName() {
        return vietnameseName;
    }

    /**
     * Get PaymentStatus from Vietnamese name
     * @param vietnameseName Vietnamese status name
     * @return PaymentStatus enum or null if not found
     */
    public static PaymentStatus fromVietnameseName(String vietnameseName) {
        if (vietnameseName == null) return null;
        
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.vietnameseName.equalsIgnoreCase(vietnameseName)) {
                return status;
            }
        }
        return null;
    }

    /**
     * Check if a string matches this status (case-insensitive, supports partial match)
     * @param statusString Status string to check
     * @return true if matches
     */
    public boolean matches(String statusString) {
        if (statusString == null) return false;
        String lower = statusString.toLowerCase();
        return vietnameseName.toLowerCase().contains(lower) || 
               lower.contains(vietnameseName.toLowerCase());
    }

    @Override
    public String toString() {
        return vietnameseName;
    }
}
