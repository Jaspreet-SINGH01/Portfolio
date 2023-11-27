// Package dans src/test/java
package com.shop.theshop;

import com.shop.theshop.entities.Adress;
import com.shop.theshop.repositories.AdresseRepository;
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
    private AdressService adresseService;

    @MockBean
    private AdresseRepository adresseRepository;

    @Test
    public void shouldGetAdresseById() {
        // Given
        long adresseId = 1L;
        Adress mockAdresse = new Adress("123 Main Street", "City", "12345");
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(mockAdresse));

        // When
        Adress retrievedAdresse = adresseService.getAdressById(adresseId);

        // Then
        assertThat(retrievedAdresse).isNotNull();
        assertThat(retrievedAdresse.getStreet()).isEqualTo("123 Main Street");
        assertThat(retrievedAdresse.getCity()).isEqualTo("City");
        assertThat(retrievedAdresse.getZipCode()).isEqualTo("12345");
    }

    @Test
    public void shouldCreateAdresse() {
        // Given
        Adresse newAdresse = new Adresse("456 Oak Avenue", "Town", "67890");
        when(adresseRepository.save(newAdresse)).thenReturn(newAdresse);

        // When
        Adresse createdAdresse = adresseService.createAdresse(newAdresse);

        // Then
        assertThat(createdAdresse).isNotNull();
        assertThat(createdAdresse.getStreet()).isEqualTo("456 Oak Avenue");
        assertThat(createdAdresse.getCity()).isEqualTo("Town");
        assertThat(createdAdresse.getZipCode()).isEqualTo("67890");
    }

    @Test
    public void shouldUpdateAdresse() {
        // Given
        long adresseId = 1L;
        Adresse existingAdresse = new Adresse("789 Pine Street", "Village", "54321");
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(existingAdresse));

        Adresse updatedAdresse = new Adresse("Updated Street", "Updated City", "98765");

        // When
        Adresse updatedAdresseResult = adresseService.updateAdresse(adresseId, updatedAdresse);

        // Then
        assertThat(updatedAdresseResult).isNotNull();
        assertThat(updatedAdresseResult.getStreet()).isEqualTo("Updated Street");
        assertThat(updatedAdresseResult.getCity()).isEqualTo("Updated City");
        assertThat(updatedAdresseResult.getZipCode()).isEqualTo("98765");
    }

    @Test
    public void shouldDeleteAdresse() {
        // Given
        long adresseId = 1L;
        Adresse existingAdresse = new Adresse("123 Main Street", "City", "12345");
        when(adresseRepository.findById(adresseId)).thenReturn(Optional.of(existingAdresse));

        // When
        adresseService.deleteAdresse(adresseId);

        // Then: You might want to verify that the delete method of the repository is called
    }
}
