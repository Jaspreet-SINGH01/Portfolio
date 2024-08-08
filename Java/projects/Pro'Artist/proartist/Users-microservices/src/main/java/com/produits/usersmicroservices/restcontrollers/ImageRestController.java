package com.produits.usersmicroservices.restcontrollers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.produits.usersmicroservices.entities.Image;
import com.produits.usersmicroservices.entities.Produit;
import com.produits.usersmicroservices.services.ProduitService;
import com.produits.usersmicroservices.services.ImageService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/image")
@CrossOrigin(origins = "*")
public class ImageRestController {
    @Autowired
    ImageService imageService ;

    @Autowired
    ProduitService produitService;


    @PostMapping("/uploadFS/{id}")
    public void uploadImageFS(@RequestParam("image") MultipartFile
                                      file,@PathVariable Long id) throws IOException {
        Produit p = produitService.getProduit(id);
        p.setImagePath(id+".jpg");

        Files.write(Paths.get(System.getProperty("user.home")+"/images/"+p.getImagePath())
                , file.getBytes());
        produitService.saveProduit(p);

    }
    @GetMapping(value = "/loadfromFS/{id}" ,
            produces = MediaType.IMAGE_JPEG_VALUE)
    public byte[] getImageFS(@PathVariable Long id) throws IOException {

        Produit p = produitService.getProduit(id);
        return	 Files.readAllBytes(Paths.get(System.getProperty("user.home")+"/images/"+p.getImagePath()));
    }








    @PostMapping("/upload")
    public Image uploadImage(@RequestParam("image")MultipartFile file) throws IOException {
        return imageService.uplaodImage(file);
    }

    @PostMapping(value = "/uplaodImageProd/{idProd}" )
    public Image uploadMultiImages(@RequestParam("image")MultipartFile file,
                                   @PathVariable Long idProd)
            throws IOException {
        return imageService.uplaodImageProd(file,idProd);
    }

    @GetMapping("/getImagesProd/{idProd}")
    public List<Image> getImagesProd(@PathVariable Long idProd)
            throws IOException {
        return imageService.getImagesParProd(idProd);
    }




    @GetMapping("/get/info/{id}")
    public Image getImageDetails(@PathVariable Long id) throws IOException {
        return imageService.getImageDetails(id) ;
    }


    @GetMapping("/load/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) throws IOException
    {
        return imageService.getImage(id);
    }


    @DeleteMapping("/delete/{id}")
    public void deleteImage(@PathVariable Long id){
        imageService.deleteImage(id);
    }



    @PutMapping("/update")
    public Image UpdateImage(@RequestParam("image")MultipartFile file) throws IOException {
        return imageService.uplaodImage(file);
    }
}
