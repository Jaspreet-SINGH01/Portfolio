package com.produits.usersmicroservices.entities;

@Projection(name = "nomProd", types = { Produit.class })
public interface ProduitProjection {
    public String getNomProduit();
}
