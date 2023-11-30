package com.shop.theshop;

import com.shop.theshop.entities.Shipping;
import com.shop.theshop.repositories.ShippingRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ShippingRepositoryTest {

    @Autowired
    private ShippingRepository shippingRepository;

    @Test
    public void shouldSaveAndRetrieveShipping() {
        // Given
        Shipping shipping = new Shipping();
        shipping.setTrackingNumber("ABC123");
        shipping.setShippingStatus("Shipped");

        // When
        shippingRepository.save(shipping);

        // Then
        Optional<Shipping> retrievedShippingOptional = shippingRepository.findById(shipping.getId());
        assertThat(retrievedShippingOptional).isPresent();

        Shipping retrievedShipping = retrievedShippingOptional.get();
        assertThat(retrievedShipping.getTrackingNumber()).isEqualTo("ABC123");
        assertThat(retrievedShipping.getShippingStatus()).isEqualTo("Shipped");
        // Add more assertions as needed for other properties
    }

    @Test
    public void shouldDeleteShipping() {
        // Given
        Shipping shipping = new Shipping();
        shipping.setTrackingNumber("XYZ789");
        shipping.setShippingStatus("In Transit");
        shippingRepository.save(shipping);

        // When
        shippingRepository.deleteById(shipping.getId());

        // Then
        Optional<Shipping> retrievedShippingOptional = shippingRepository.findById(shipping.getId());
        assertThat(retrievedShippingOptional).isEmpty();
    }
}
