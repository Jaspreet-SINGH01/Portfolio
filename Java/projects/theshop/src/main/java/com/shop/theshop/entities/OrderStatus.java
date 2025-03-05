package com.shop.theshop.entities;

public enum OrderStatus {
    PENDING("Pending"),
    SHIPPED("Shipped"),
    PROCESSING("Processing"),
    COMPLETED("Completed");

    private String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

/*package com.shop.theshop.entities;

import jakarta.persistence.*;

public enum OrderStatus {
    PENDING("Pending"),
    SHIPPED("Shipped"), PROCESSING(), COMPLETED();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String displayName;

    OrderStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}*/