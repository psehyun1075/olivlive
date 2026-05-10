package com.olivelive.catalog.product.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ProductStatus {
    ACTIVE("active"),
    INACTIVE("inactive");

    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ProductStatus fromValue(String value) {
        for (ProductStatus s : values()) {
            if (s.value.equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown product status: " + value);
    }
}
