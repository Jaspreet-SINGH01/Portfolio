package com.shop.theshop.controller;

import com.shop.theshop.entities.Adress;
import com.shop.theshop.services.AdressService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class AdressController {

    private final AdressService adressService;
    private Long adressId;

    public AdressController(AdressService adressService) {
        this.adressService = adressService;
    }

    @GetMapping("/adresses")
    public List<Adress> getAllAdresses() {
        return (List<Adress>) adressService.getAllAdresses(adressId);
    }

    @GetMapping("/adresses/{id}")
    public Adress getAdress(@PathVariable Long id) {
        return adressService.findById(id);
    }

    @PostMapping("/adresses")
    public Adress createAdress(@RequestBody Adress adress) {
        return adressService.createAdress(adress);
    }

    @PutMapping("/adresses/{id}")
    public Adress updateAdress(@PathVariable Long id, @RequestBody Adress adress) {
        return adressService.updateAdress(id, adress);
    }

    @DeleteMapping("/adresses/{id}")
    public void deleteAdress(@PathVariable Long id) {
        adressService.deleteAdress(id);
    }
}

