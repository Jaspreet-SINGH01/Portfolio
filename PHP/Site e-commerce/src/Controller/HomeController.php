<?php

namespace App\Controller;

use App\Entity\Products;
use App\Repository\CategoriesRepository;
use App\Repository\ProductsRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;

class HomeController extends AbstractController
{
    #[Route('/', name: 'app_home')]
    public function index(CategoriesRepository $categoriesRepository, ProductsRepository $productsRepository): Response
    {
        $categories = $categoriesRepository->findAll();
        $products = $productsRepository->findAll();

        //dd([$categories]);
        //dd([$products]);

        return $this->render('home/index.html.twig', [
           'controller_name' => 'HomeController',
           'products' => $products,
           'categories' => $categoriesRepository->findBy([],
           ['categoryOrder' => 'asc'])
        ]);
    }

    #[Route('/products/{slug}', name: 'products_details')]

    public function show(?Products $products): Response{
        if(!$products){
            return $this->redirectToRoute('home');
        }

        return $this->render('home/single_product.html.twig',[
            'product' =>$products
        ]);
    }
}
