package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.entities.Shipping;
import com.shop.theshop.services.ShippingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ShippingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ShippingService shippingService;

    @Test
    public void shouldGetAllShipping() throws Exception {
        Shipping shipping1 = new Shipping();
        Shipping shipping2 = new Shipping();
        List<Shipping> shippingList = Arrays.asList(shipping1, shipping2);

        when(shippingService.getAllShipping()).thenReturn(shippingList);

        mockMvc.perform(get("/shipping"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldGetShippingById() throws Exception {
        Long shippingId = 1L;
        Shipping shipping = new Shipping();
        shipping.setId(shippingId);

        when(shippingService.getShippingById(shippingId)).thenReturn(shipping);

        mockMvc.perform(get("/shipping/{id}", shippingId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(shippingId));
    }

    @Test
    public void shouldCreateShipping() throws Exception {
        Shipping shipping = new Shipping();
        shipping.setTrackingNumber("ABC123");
        shipping.setShippingStatus("Shipped");

        when(shippingService.createShipping(any(Shipping.class))).thenReturn(shipping);

        mockMvc.perform(post("/shipping")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shipping)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trackingNumber").value("ABC123"));
    }

    @Test
    public void shouldUpdateShipping() throws Exception {
        Long shippingId = 1L;
        Shipping existingShipping = new Shipping();
        existingShipping.setId(shippingId);

        Shipping updatedShipping = new Shipping();
        updatedShipping.setTrackingNumber("XYZ789");
        updatedShipping.setShippingStatus("In Transit");

        when(shippingService.updateShipping(eq(shippingId), any(Shipping.class))).thenReturn(existingShipping);

        mockMvc.perform(put("/shipping/{id}", shippingId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedShipping)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.trackingNumber").value("XYZ789"));
    }

    @Test
    public void shouldDeleteShipping() throws Exception {
        Long shippingId = 1L;
        when(shippingService.deleteShipping(shippingId)).thenReturn(true);

        mockMvc.perform(delete("/shipping/{id}", shippingId))
                .andExpect(status().isOk());
    }
}