package com.shop.theshop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shop.theshop.controller.AdressController;
import com.shop.theshop.entities.Adress;
import com.shop.theshop.services.AdressService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdressController.class)
public class AdressControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private AdressService adressService;

    @Test
    public void shouldGetAllAdresses() throws Exception {
        Adress adress1 = new Adress();
        Adress adress2 = new Adress();
        List<Adress> adressList = Arrays.asList(adress1, adress2);

        when(adressService.getAllAdresses()).thenReturn((Adress) adressList);

        mockMvc.perform(get("/adresses"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    public void shouldGetAdressById() throws Exception {
        Long adressId = 1L;
        Adress adress = new Adress();
        adress.setId(adressId);

        when(adressService.findById(adressId)).thenReturn(adress);

        mockMvc.perform(get("/adresses/{id}", adressId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(adressId));
    }

    @Test
    public void shouldCreateAdress() throws Exception {
        Adress adress = new Adress();
        when(adressService.createAdress(adress)).thenReturn(adress);

        mockMvc.perform(post("/adresses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adress)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    public void shouldUpdateAdress() throws Exception {
        Long adressId = 1L;
        Adress existingAdress = new Adress();
        existingAdress.setId(adressId);

        Adress updatedAdress = new Adress();
        updatedAdress.setStreet("456 New St");
        updatedAdress.setCity("New City");

        when(adressService.updateAdress(eq(adressId), any(Adress.class))).thenReturn(existingAdress);

        mockMvc.perform(put("/adresses/{id}", adressId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedAdress)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(adressId));
    }

    @Test
    public void shouldDeleteAdress() throws Exception {
        Long adressId = 1L;

        mockMvc.perform(delete("/adresses/{id}", adressId))
                .andExpect(status().isOk());

        verify(adressService, times(1)).deleteAdress(adressId);
    }
}