package com.produits.usersmicroservices.restcontrollers;

import java.util.List;

import com.produits.usersmicroservices.services.ProduitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.produits.usersmicroservices.entities.Produit;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ProduitRESTController {
    @Autowired
    ProduitService produitService;

    @GetMapping("all")
    public List<Produit> getAllProduits() {
        return produitService.getAllProduits();
    }

    @GetMapping("/getbyid/{id}")
    //@GetMapping("/getbyid/{id}")
    public Produit getProduitById(@PathVariable Long id) {
        return produitService.getProduit(id);
    }

    @PostMapping("/addprod")
    //@PostMapping("/addprod")
    public Produit createProduit(@RequestBody Produit produit) {
        return produitService.saveProduit(produit);
    }

    @PutMapping("/updateprod")
    //@PutMapping("/updateprod")
    public Produit updateProduit(@RequestBody Produit produit) {
        return produitService.updateProduit(produit);
    }

    @DeleteMapping("/delprod/{id}")
    //@DeleteMapping("/delprod/{id}")
    public void deleteProduit(@PathVariable Long id)
    {
        produitService.deleteProduitById(id);
    }

    @GetMapping("/prodscat/{idCat}")
    public List<Produit> getProduitsByCatId(@PathVariable Long idCat) {
        return produitService.findByCategorieIdCat(idCat);
    }


}
