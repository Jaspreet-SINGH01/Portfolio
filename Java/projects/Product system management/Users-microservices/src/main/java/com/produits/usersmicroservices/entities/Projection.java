package com.produits.usersmicroservices.entities;

public @interface Projection {

    String name();

    Class<Produit>[] types();

}
