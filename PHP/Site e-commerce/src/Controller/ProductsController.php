<?php

namespace App\Controller;
use App\Entity\Products;
use App\Form\SearchType as FormSearchType;
use App\Repository\ProductsRepository;
use Doctrine\ORM\EntityManagerInterface;
use Symfony\Bundle\FrameworkBundle\Controller\AbstractController;
use Symfony\Component\HttpFoundation\Request;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use Symfony\Component\Form\Extension\Core\Type\SearchType;
use App\Classe\Search;
use App\Entity\Comments;
use App\Form\CommentsType;
use DateTimeImmutable;
use Doctrine\Common\Collections\ArrayCollection;
use Symfony\Component\Form\FormTypeInterface;
use Symfony\Component\Form\FormView;
use Doctrine\Persistence\ManagerRegistry;
use mysqli;
use Symfony\Component\Mailer\MailerInterface;

class ProductsController extends AbstractController
{
    private $entityManager;

    public function __construct(EntityManagerInterface $entityManager, private ManagerRegistry $doctrine)
    {
        $this->entityManager = $entityManager;
        $this->getUsers = new ArrayCollection();
    }

    #[Route('/products', name: 'products')]
    public function index(Request $request)
    {
        $products = $this->entityManager->getRepository(Products::class)->findAll();

         $search = new Search;
         $form = $this->createForm(FormSearchType::class, $search);
        
         $form->handleRequest($request);
        
         if ($form->isSubmitted() && $form->isValid()) {
            $products = $this->entityManager->getRepository(Products::class)->findWithSearch($search);
        } else {
            $products = $this->entityManager->getRepository(Products::class)->findAll();
        }

        //  dd($products);
        
        return $this->render('products/index.html.twig', [
            'products' => $products,
             'form' => $form->createView()
        ]);
    }

    #[Route('/produits/{slug}', name: 'produits')]
    public function show($slug, Request $request, MailerInterface $mail)
    {
        $product = $this->entityManager->getRepository(Products::class)->findOneBySlug($slug);
        $products = $this->entityManager->getRepository(Products::class)->findAll();
        

        if (!$product) {
            return $this->redirectToRoute('products');
        }

        $form = $this->createForm(CommentsType::class);

        $contact = $form->handleRequest($request);

        if($form->isSubmitted() && $form->isValid()){
            $context = [
                'product' => $products,
                'mail' => $contact->get('email')->getData(),
                'content' => $contact->get('content')->getData()
            ];

            $mail->send($contact->get('email')->getData(), $products->getUsers()->getEmail(), 'Contact au sujet de votre commentaire "' . $products->getTitle() . '"', 'contact_products', $context);
            // On confirme et on redirige
            $this->addFlash('message', 'Votre e-mail a bien été envoyé');
            return $this->redirectToRoute('products_produits', ['slug' => $products->getSlug()]);
        }


        // Formulaire :

        $comments = new Comments($products);

        $commentsForm = $this->createForm(CommentsType::class, $comments);

        $commentsForm->handleRequest($request);

            // Traitement du formulaire
        if($commentsForm->isSubmitted() && $commentsForm->isValid()){
            $comments->setCreatedAt(new DateTimeImmutable());
            $comments->setProducts($products);

            // On récupère le contenu du champ parentid
            $parentid = $commentsForm["comments"]->get("parentid")->getData();

            // $username = $form["username"]->getData();
            // $username = $form["user"]["username"]->getData();

            // On va chercher le commentaire correspondant
            $entityManager = $this->doctrine->getManager();

            if($parentid != null){
                $parent = $entityManager->getRepository(Comments::class)->find($parentid);
            }

            // On définit le parent
            $comments->setParent($parent ?? null);

            $entityManager->persist($comments);
            $entityManager->flush();

            $this->addFlash('message', 'Votre commentaire a bien été envoyé');
            return $this->redirectToRoute('products_produits', ['slug' => $products->getSlug()]);
        }
        


        return $this->render('products/show.html.twig', [
            'product' => $product,
            'products' => $products,
            'commentsForm' => $commentsForm->createView()
        ]);



        
    //     // Partie commentaire
    //     $comment = new Comments;

    //     // On génère le formulaire
    //     $commentForm = $this->createForm(CommentsType::class, $comment);

    //     //  $commentForm->handleRequest($request);

    //     // Traitement du formulaire
    //     if($commentForm->isSubmitted() && $commentForm->isValid()){
    //         $comment->setCreatedAt(new DateTimeImmutable());
    //         $comment->setProducts($products);

    //         // On récupère le contenu du champ parentid
    //         $parentid = $commentForm["comment"]->get("parentid")->getData();

    //         // $username = $form["username"]->getData();
    //         // $username = $form["user"]["username"]->getData();

    //         // On va chercher le commentaire correspondant
    //         $entityManager = $this->doctrine->getManager();

    //         if($parentid != null){
    //             $parent = $entityManager->getRepository(Comments::class)->find($parentid);
    //         }

    //         // On définit le parent
    //         $comment->setParent($parent ?? null);

    //         $entityManager->persist($comment);
    //         $entityManager->flush();

    //         $this->addFlash('message', 'Votre commentaire a bien été envoyé');
    //         return $this->redirectToRoute('annonces_details', ['slug' => $products->getSlug()]);
    //     }

    //     return $this->render('products/show.html.twig', [
    //         'products' => $products,
    //         // 'form' => $form->createView(),
    //         'commentForm' => $commentForm->createView()
    //     ]);
    // }

    

}}




    