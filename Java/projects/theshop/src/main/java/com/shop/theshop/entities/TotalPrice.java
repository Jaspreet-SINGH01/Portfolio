package com.shop.theshop.entities;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public class TotalPrice {

    private BigDecimal amount;

    public TotalPrice() {
    }

    public TotalPrice(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TotalPrice that = (TotalPrice) o;

        return Objects.equals(amount, that.amount);
    }

    @Override
    public int hashCode() {
        return amount != null ? amount.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "TotalPrice{" +
                "amount=" + amount +
                '}';
    }
}
