package com.example.Produits.services;

import com.example.Produits.dto.ProduitDTO;
import com.example.Produits.entities.Categorie;
import com.example.Produits.entities.Produit;
import com.example.Produits.repositories.ProduitRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;


@Service
    public class ProduitServiceImpl implements ProduitService {
        @Autowired
        ProduitRepository produitRepository;
        @Autowired
        ModelMapper modelMapper;
        @Override
        public ProduitDTO saveProduit(ProduitDTO p) {
            return convertEntityToDto(produitRepository.save(convertDtoToEntity(p)));
        }
        @Override
        public ProduitDTO updateProduit(ProduitDTO p) {
            return produitRepository.save(convertEntityToDto(p));
        }
        @Override
        public void deleteProduit(Produit p) {
            produitRepository.delete(p);
        }
        @Override
        public void deleteProduitById(Long id) {
            produitRepository.deleteById(id);
        }
        @Override
        public ProduitDTO getProduit(Long id) {
            return convertEntityToDto(produitRepository.findById(id).get());
        }
        @Override
        public List<ProduitDTO> getAllProduits() {
            return produitRepository.findAll().stream().map(this::convertEntityToDto).collect(Collectors.toList());
        }

        @Override
        public List<Produit> findByNomProduit(String nom) {
            return produitRepository.findByNomProduit(nom);
        }

        @Override
        public List<Produit> findByNomProduitContains(String nom) {
            return produitRepository.findByNomProduitContains(nom);
        }

        @Override
        public List<Produit> findByNomPrix(String nom, Double prix) {
            return produitRepository.findByNomPrix(nom, prix);
        }

        @Override
        public List<Produit> findByCategorie(Categorie categorie) {
            return produitRepository.findByCategorie(categorie);
        }

        @Override
        public List<Produit> findByCategorieIdCat(Long id) {
            return produitRepository.findByCategorieIdCat(id);
        }

        @Override
        public List<Produit> findByOrderByNomProduitAsc() {
            return produitRepository.findByOrderByNomProduitAsc();
        }

        @Override
        public List<Produit> trierProduitsNomsPrix() {
            return produitRepository.trierProduitsNomsPrix();
        }

        public ProduitDTO convertEntityToDto(ProduitDTO p) {
            /*ProduitDTO produitDTO = new ProduitDTO();

            produitDTO.setIdProduit(p.getIdProduit());
            produitDTO.setNomProduit(p.getNomProduit());
            produitDTO.setPrixProduit(p.getPrixProduit());
            produitDTO.setCategorie(p.getCategorie());

            return produitDTO;*/

            /*return ProduitDTO.builder()
                    .idProduit(p.getIdProduit())
                    .nomProduit(p.getNomProduit())
                    .prixProduit(p.getPrixProduit())
                    .dateCreation(p.getDateCreation())
                    .nomCat(p.getCategorie().getNomCat())
                    .categorie(p.getCategorie())
                    .build();*/

            modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.LOOSE);
            ProduitDTO produitDTO= modelMapper.map(p, ProduitDTO.class);
            return produitDTO;
        }

    @Override
    public Produit convertDtoToEntity(ProduitDTO produitDto) {
            /*Produit produit = new Produit();
            produit.setIdProduit(produitDto.getIdProduit());
            produit.setNomProduit(produitDto.getNomProduit());
            produit.setPrixProduit(produit.getPrixProduit());
            produit.setDateCreation(produit.getDateCreation());
            produit.setCategorie(produit.getCategorie());
        return produit;*/

        Produit produit = new Produit(null, null, null);
        produit = modelMapper.map(produitDto, Produit.class);
        return produit;
    }
    @Override
    public ProduitDTO convertEntityToDto(Produit p) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'convertEntityToDto'");
    }
}
