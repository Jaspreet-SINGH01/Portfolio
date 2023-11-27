package com.shop.theshop.services;

import com.shop.theshop.entities.Adress;
import com.shop.theshop.repositories.AdressRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdressService {

    private final AdressRepository adressRepository;

    public AdressService(AdressRepository adressRepository) {
        this.adressRepository = adressRepository;
    }

    public List<Adress> getAllAdresses() {
        return adressRepository.findAll();
    }

    public Adress findById(Long id) {
        return adressRepository.findById(id).orElse(null);
    }

    public Adress createAdress(Adress adress) {
        return adressRepository.save(adress);
    }

    public Adress updateAdress(Long id, Adress adress) {
        Adress existingAdress = adressRepository.findById(id).orElse(null);
        if (existingAdress == null) {
            return null;
        }

        existingAdress.setStreet(adress.getStreet());
        existingAdress.setCity(adress.getCity());
        existingAdress.setState(adress.getState());
        existingAdress.setZipCode(adress.getZipCode());

        return adressRepository.save(existingAdress);
    }

    public void deleteAdress(Long id) {
        adressRepository.deleteById(id);
    }
}
