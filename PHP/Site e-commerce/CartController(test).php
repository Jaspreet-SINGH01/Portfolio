<?php

namespace App\Controller;

use App\Classe\Cart;
use App\Entity\Products;
use App\Repository\ProductsRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Annotation\Route;

class CartController extends AbstractController
{
    #[Route('/cart', name: 'app_cart')]
    public function index(SessionInterface $session, ProductsRepository $productsRepository): Response
    {
        $cart = $session->get("cart", []);

        // On "fabrique" les données
        $dataCart = [];
        $total = 0;

        foreach((array) $cart as $id => $quantity) {
            $products = $productsRepository->find($id);
            $dataCart[] = [
                "product" => $products,
                "quantity" => $quantity
            ];
            $total += $products * $quantity;
            // $total += $cart->getStock() * $cart->getProducts()->getPrice();
        }
        // $cart->setTotal($total);


        // dd($cart->get());
        
        return $this->render('cart/index.html.twig', compact("dataCart", "total"));
    }

    #[Route('/add/{id}', name: 'add_to_cart')]
    public function add(Products $products, SessionInterface $session)
    {
        // On récupère le panier actuel
        $cart = $session->get("cart", []);
        $id = $products->getId();

        if(!empty($cart[$id])) {
            $cart[$id]++;
        }

        //On sauvegarde dans la session
        $session->set("cart", $cart);
        // dd($session);


        $session->set("cart", $id);
        // dd($session->get("cart"));
        // $cart->add($id);
    
        return $this->redirectToRoute('app_cart');
    }

    #[Route('/cart/remove', name: 'remove_my_cart')]
    public function remove(Products $products, SessionInterface $session)
    {
        // On récupère le panier actuel
        $cart = $session->get("cart", []);
        $id = $products->getId();

        if(!empty($cart[$id])){
            if($cart[$id] > 1){
                $cart[$id]--;
            }else{
                unset($cart[$id]);
            }
        }

        // On sauvegarde dans la session
        $session->set("cart", $cart);

        return $this->redirectToRoute("app_cart");
    }

    #[Route('/cart/delete/{id}', name: 'delete_my_cart')]
    public function delete(Products $products, SessionInterface $session)
    {
        // On récupère le cart actuel
        $cart = $session->get("cart", []);
        $id = $products->getId();

        if(!empty($cart[$id])){
            unset($cart[$id]);
        }

        // On sauvegarde dans la session
        $session->set("cart", $cart);

        return $this->redirectToRoute("app_cart");
    }

    
    #[Route('/cart/delete', name: 'delete_all')]

    public function deleteAll(SessionInterface $session)
    {
        $session->remove("cart");

        return $this->redirectToRoute("app_cart");
    }

}





 