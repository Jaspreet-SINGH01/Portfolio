package com.shop.theshop;

import com.shop.theshop.entities.Shipping;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ShippingTest {

    @Test
    public void shouldCreateShipping() {
        // Given
        Shipping shipping = new Shipping();
        shipping.setTrackingNumber("ABC123");
        shipping.setShippingStatus("Shipped");

        // When
        String trackingNumber = shipping.getTrackingNumber();
        String shippingStatus = shipping.getShippingStatus();

        // Then
        assertThat(trackingNumber).isEqualTo("ABC123");
        assertThat(shippingStatus).isEqualTo("Shipped");
    }
}
