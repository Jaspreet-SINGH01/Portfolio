<?php

namespace App\DataFixtures;

use App\Entity\Users;
use Doctrine\Bundle\FixturesBundle\Fixture;
use Doctrine\Persistence\ObjectManager;
use Symfony\Component\PasswordHasher\Hasher\UserPasswordHasherInterface;
use Faker;

class UsersFixtures extends Fixture
{
    public function __construct(private UserPasswordHasherInterface $passwordEncoder)
    {
        
    }

    public function load(ObjectManager $manager): void
    {
        // $product = new Product();
        // $manager->persist($product);
        $admin = new Users();
        $admin->setEmail('admin@demo.fr');
        $admin->setLastname('last');
        $admin->setFirstname('first');
        $admin->setAdress('12 rue des arbres');
        $admin->setZipcode('75001');
        $admin->setCity('Paris');
        $admin->setPassword(
            $this->passwordEncoder->hashPassword($admin, 'admin')
        );
        $admin->setRoles(['ROLE_ADMIN']);

        $manager->persist($admin);

        $faker = Faker\Factory::create('fr_FR');

        for ($i = 0; $i < 50; $i++) {
            $user = new Users();
            $user->setEmail($faker->email);
            $user->setLastname($faker->lastName());
            $user->setFirstname($faker->firstName());
            $user->setAdress($faker->address());
            $user->setZipcode($faker->postcode());
            $user->setCity($faker->city());
            //$user->setPassword('password');
            $user->setPassword(
                $this->passwordEncoder->hashPassword(
                $user,
                'the_new_password'
            ));
            $user->setRoles(['ROLE_USER']);
            //$user->setCreatedAt(new \DateTimeImmutable());
            $manager->persist($user);
            $users[] = $user;
        }

        $manager->flush();
    }
}
