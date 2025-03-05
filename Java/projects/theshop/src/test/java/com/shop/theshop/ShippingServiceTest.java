package com.shop.theshop;

import com.shop.theshop.entities.Shipping;
import com.shop.theshop.repositories.ShippingRepository;
import com.shop.theshop.services.ShippingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class ShippingServiceTest {

    @Autowired
    private ShippingService shippingService;

    @MockBean
    private ShippingRepository shippingRepository;

    @Test
    public void shouldGetAllShipping() {
        Shipping shipping1 = new Shipping();
        Shipping shipping2 = new Shipping();
        List<Shipping> shippingList = Arrays.asList(shipping1, shipping2);

        when(shippingRepository.findAll()).thenReturn(shippingList);

        List<Shipping> result = shippingService.getAllShipping();

        assertThat(result).hasSize(2);
        verify(shippingRepository, times(1)).findAll();
    }

    @Test
    public void shouldGetShippingById() {
        Long shippingId = 1L;
        Shipping shipping = new Shipping();
        shipping.setId(shippingId);

        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(shipping));

        Shipping result = shippingService.getShippingById(shippingId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shippingId);
        verify(shippingRepository, times(1)).findById(shippingId);
    }

    @Test
    public void shouldCreateShipping() {
        Shipping shipping = new Shipping();

        when(shippingRepository.save(shipping)).thenReturn(shipping);

        Shipping result = shippingService.createShipping(shipping);

        assertThat(result).isNotNull();
        verify(shippingRepository, times(1)).save(shipping);
    }

    @Test
    public void shouldUpdateShipping() {
        Long shippingId = 1L;
        Shipping existingShipping = new Shipping();
        existingShipping.setId(shippingId);

        Shipping updatedShipping = new Shipping();
        updatedShipping.setTrackingNumber("ABC123");
        updatedShipping.setShippingStatus("Shipped");

        when(shippingRepository.findById(shippingId)).thenReturn(Optional.of(existingShipping));
        when(shippingRepository.save(existingShipping)).thenReturn(existingShipping);

        Shipping result = shippingService.updateShipping(shippingId, updatedShipping);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(shippingId);
        assertThat(result.getTrackingNumber()).isEqualTo("ABC123");
        assertThat(result.getShippingStatus()).isEqualTo("Shipped");
        verify(shippingRepository, times(1)).findById(shippingId);
        verify(shippingRepository, times(1)).save(existingShipping);
    }

    @Test
    public void shouldDeleteShipping() {
        Long shippingId = 1L;
        when(shippingRepository.existsById(shippingId)).thenReturn(true);

        boolean result = shippingService.deleteShipping(shippingId);

        assertThat(result).isTrue();
        verify(shippingRepository, times(1)).existsById(shippingId);
        verify(shippingRepository, times(1)).deleteById(shippingId);
    }
}