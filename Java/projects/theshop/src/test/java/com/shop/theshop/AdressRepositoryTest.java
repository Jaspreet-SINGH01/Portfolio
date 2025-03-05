package com.shop.theshop;

import com.shop.theshop.entities.Adress;
import com.shop.theshop.repositories.AdressRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class AdressRepositoryTest {

    @Autowired
    private AdressRepository adressRepository;

    @Test
    public void shouldSaveAndRetrieveAdress() {
        Adress adress = new Adress();
        adress.setStreet("123 Main St");
        adress.setCity("City");
        adress.setState("State");
        adress.setZipcode("12345");
        adress.setCountry("Country");

        adressRepository.save(adress);

        Adress retrievedAdress = adressRepository.findById(adress.getId()).orElse(null);
        assertThat(retrievedAdress).isNotNull();
        assertThat(retrievedAdress.getStreet()).isEqualTo("123 Main St");
        assertThat(retrievedAdress.getCity()).isEqualTo("City");
        assertThat(retrievedAdress.getState()).isEqualTo("State");
        assertThat(retrievedAdress.getZipcode()).isEqualTo("12345");
        assertThat(retrievedAdress.getCountry()).isEqualTo("Country");
    }
}