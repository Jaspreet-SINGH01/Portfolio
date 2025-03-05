<?php

namespace App\Controller;

use App\Classe\Cart;
use App\Entity\Products;
use App\Repository\ProductsRepository;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\HttpFoundation\Session\SessionInterface;
use Symfony\Component\Routing\Annotation\Route;
use Doctrine\ORM\EntityManagerInterface;

class CartController extends AbstractController
{
    private $entityManager;

    public function __construct(EntityManagerInterface $entityManager)
    {
        $this->entityManager = $entityManager;
    }


    #[Route('/cart', name: 'app_cart')]
    public function index(Cart $cart): Response
    {
        return $this->render('cart/index.html.twig', [
            'cart' => $cart->getFull()
        ]);

    }

    #[Route('/add/{id}', name: 'add_to_cart')]
    public function add(Cart $cart, $id)
    {
        
        $cart->add($id);
    
        return $this->redirectToRoute('app_cart');
    }

    #[Route('/cart/remove', name: 'remove_my_cart')]
    public function remove(Cart $cart)
    {
        $cart->remove();

        return $this->redirectToRoute("app_cart");
    }

    #[Route('/cart/delete/{id}', name: 'delete_my_cart')]
    public function delete(Cart $cart, $id)
    {
        $cart->delete($id);

        return $this->redirectToRoute("app_cart");
    }

    
    #[Route('/cart/delete', name: 'delete_all')]

    public function deleteAll(Cart $cart, $id)
    {
        $cart->remove("cart");

        return $this->redirectToRoute("app_cart");
    }

}





 