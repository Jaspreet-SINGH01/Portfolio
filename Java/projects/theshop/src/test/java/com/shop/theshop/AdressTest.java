package com.shop.theshop;

import com.shop.theshop.entities.Adress;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class AdressTest {

    @Test
    public void shouldCreateAdress() {
        String street = "123 Main St";
        String city = "City";
        String state = "State";
        String zipcode = "12345";
        String country = "Country";

        Adress adress = new Adress();
        adress.setStreet(street);
        adress.setCity(city);
        adress.setState(state);
        adress.setZipcode(zipcode);
        adress.setCountry(country);

        assertThat(adress).isNotNull();
        assertThat(adress.getStreet()).isEqualTo(street);
        assertThat(adress.getCity()).isEqualTo(city);
        assertThat(adress.getState()).isEqualTo(state);
        assertThat(adress.getZipcode()).isEqualTo(zipcode);
        assertThat(adress.getCountry()).isEqualTo(country);
    }

    @Test
    public void shouldUpdateAdress() {
        String street = "123 Main St";
        String city = "City";
        String state = "State";
        String zipcode = "12345";
        String country = "Country";

        Adress adress = new Adress();
        adress.setStreet(street);
        adress.setCity(city);
        adress.setState(state);
        adress.setZipcode(zipcode);
        adress.setCountry(country);

        String updatedStreet = "456 New St";
        String updatedCity = "New City";

        adress.setStreet(updatedStreet);
        adress.setCity(updatedCity);

        assertThat(adress.getStreet()).isEqualTo(updatedStreet);
        assertThat(adress.getCity()).isEqualTo(updatedCity);
    }
}
