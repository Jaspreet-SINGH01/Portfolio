<?php

namespace App\Controller\Admin;

use App\Controller\ProductsController;
use App\Entity\Categories;
use App\Entity\Comments;
use App\Entity\Products;
use EasyCorp\Bundle\EasyAdminBundle\Config\Crud;
use EasyCorp\Bundle\EasyAdminBundle\Config\Dashboard;
use EasyCorp\Bundle\EasyAdminBundle\Config\MenuItem;
use EasyCorp\Bundle\EasyAdminBundle\Controller\AbstractDashboardController;
use Symfony\Component\HttpFoundation\Response;
use Symfony\Component\Routing\Annotation\Route;
use EasyCorp\Bundle\EasyAdminBundle\Router\AdminUrlGenerator;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\Security;
use Sensio\Bundle\FrameworkExtraBundle\Configuration\IsGranted;

/**
 * Will throw an HttpException with a 404 status code:
 *
 * @IsGranted("ROLE_ADMIN", statusCode=404, message="Page not found")
 */

class DashboardController extends AbstractDashboardController
{
    public function __construct(
        private AdminUrlGenerator $adminUrlGenerator
    ) {
    }

    #[Route('/admin', name: 'admin')]
    public function index(): Response
    {
        $url = $this->adminUrlGenerator
            ->setController(ProductsCrudController::class)
            ->generateUrl();

            return $this->redirect($url);
        //return parent::index();

        //return $this->redirect($adminUrlGenerator->setController(OneOfYourCrudController::class)->generateUrl());

    }

    public function configureDashboard(): Dashboard
    {
        return Dashboard::new()
            ->setTitle('Jaspreet SINGH');
    }

    public function configureMenuItems(): iterable
    {
        yield MenuItem::linkToRoute('Retourner sur le site', 'fas fa-home', routeName:'app_home');

        yield MenuItem::section('E-commerce');
        
        yield MenuItem::section('Products');
        
        yield MenuItem::subMenu('Actions', 'fas fa-bars')->setSubItems([
            MenuItem::linkToCrud('Create product', 'fas fa-plus', Products::class)->setAction(Crud::PAGE_NEW),
            MenuItem::linkToCrud('Show products', 'fas fa-eye', Products::class)
        ]);
        
        yield MenuItem::subMenu('Categories', 'fas fa-bars')->setSubItems([
            MenuItem::linkToCrud('Create Category', 'fas fa-plus', Categories::class)->setAction(Crud::PAGE_NEW),
            MenuItem::linkToCrud('Show Categories', 'fas fa-eye', Categories::class)
        ]);

        yield MenuItem::linkToCrud('Commentaires', 'fas fa-comment', Comments::class);
    }
}
