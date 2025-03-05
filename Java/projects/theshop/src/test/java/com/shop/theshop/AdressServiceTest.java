package com.shop.theshop;

import com.shop.theshop.entities.Adress;
import com.shop.theshop.repositories.AdressRepository;
import com.shop.theshop.services.AdressService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
public class AdressServiceTest {

    @Autowired
    private AdressService adressService;

    @MockBean
    private AdressRepository adressRepository;

    @Test
    public void shouldGetAdressById() {
        long adressId = 1L;
        Adress mockAdress = new Adress("123 Main Street", "City", "12345");
        when(adressRepository.findById(adressId)).thenReturn(Optional.of(mockAdress));

        Adress retrievedAdress = adressService.getAllAdresses(adressId);

        assertThat(retrievedAdress).isNotNull();
        assertThat(retrievedAdress.getStreet()).isEqualTo("123 Main Street");
        assertThat(retrievedAdress.getCity()).isEqualTo("City");
        assertThat(retrievedAdress.getZipcode()).isEqualTo("12345");
    }

    @Test
    public void shouldCreateAdress() {
        Adress newAdress = new Adress("456 Oak Avenue", "Town", "67890");
        when(adressRepository.save(newAdress)).thenReturn(newAdress);

        Adress createdAdresse = adressService.createAdress(newAdress);

        assertThat(createdAdresse).isNotNull();
        assertThat(createdAdresse.getStreet()).isEqualTo("456 Oak Avenue");
        assertThat(createdAdresse.getCity()).isEqualTo("Town");
        assertThat(createdAdresse.getZipcode()).isEqualTo("67890");
    }

    @Test
    public void shouldUpdateAdress() {
        long adressId = 1L;
        Adress existingAdress = new Adress("789 Pine Street", "Village", "54321");
        when(adressRepository.findById(adressId)).thenReturn(Optional.of(existingAdress));

        Adress updatedAdress = new Adress("Updated Street", "Updated City", "98765");

        Adress updatedAdressResult = adressService.updateAdress(adressId, updatedAdress);

        assertThat(updatedAdressResult).isNotNull();
        assertThat(updatedAdressResult.getStreet()).isEqualTo("Updated Street");
        assertThat(updatedAdressResult.getCity()).isEqualTo("Updated City");
        assertThat(updatedAdressResult.getZipcode()).isEqualTo("98765");
    }

    @Test
    public void shouldDeleteAdress() {
        long adressId = 1L;
        Adress existingAdress = new Adress("123 Main Street", "City", "12345");
        when(adressRepository.findById(adressId)).thenReturn(Optional.of(existingAdress));

        adressService.deleteAdress(adressId);

    }
}
