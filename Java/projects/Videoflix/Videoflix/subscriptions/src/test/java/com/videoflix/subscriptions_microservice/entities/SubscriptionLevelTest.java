package com.videoflix.subscriptions_microservice.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubscriptionLevelTest {

    // Test pour vérifier la création et la récupération des attributs d'un niveau
    // d'abonnement
    @Test
    void subscriptionLevel_shouldSetAndGetValues() {
        // GIVEN : Création des valeurs pour les attributs du niveau d'abonnement
        SubscriptionLevel.Level level = SubscriptionLevel.Level.PREMIUM;
        double price = 19.99;
        String features = "Accès à tous les contenus, qualité HD";
        SubscriptionLevel.BillingFrequency billingFrequency = SubscriptionLevel.BillingFrequency.MONTHLY;
        String stripePriceId = "price_premium_monthly";

        // WHEN : Création d'une instance de SubscriptionLevel et définition de ses
        // attributs
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
        subscriptionLevel.setLevel(level);
        subscriptionLevel.setPrice(price);
        subscriptionLevel.setFeatures(features);
        subscriptionLevel.setBillingFrequency(billingFrequency);
        subscriptionLevel.setStripePriceId(stripePriceId);

        // THEN : Vérification que les valeurs ont été correctement définies et peuvent
        // être récupérées
        assertNull(subscriptionLevel.getId(), "L'ID devrait être null avant la persistance.");
        assertEquals(level, subscriptionLevel.getLevel(), "Le niveau d'abonnement doit correspondre.");
        assertEquals(price, subscriptionLevel.getPrice(), "Le prix doit correspondre.");
        assertEquals(features, subscriptionLevel.getFeatures(), "Les caractéristiques doivent correspondre.");
        assertEquals(billingFrequency, subscriptionLevel.getBillingFrequency(),
                "La fréquence de facturation doit correspondre.");
        assertEquals(stripePriceId, subscriptionLevel.getStripePriceId(), "L'ID de prix Stripe doit correspondre.");
    }

    // Test pour vérifier que les attributs d'un niveau d'abonnement peuvent être
    // modifiés
    @Test
    void subscriptionLevel_shouldAllowAttributeModification() {
        // GIVEN : Création d'une instance de SubscriptionLevel et définition de valeurs
        // initiales
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
        subscriptionLevel.setLevel(SubscriptionLevel.Level.BASIC);
        subscriptionLevel.setPrice(9.99);
        subscriptionLevel.setBillingFrequency(SubscriptionLevel.BillingFrequency.YEARLY);

        // WHEN : Modification des attributs du niveau d'abonnement
        SubscriptionLevel.Level newLevel = SubscriptionLevel.Level.ULTRA;
        double newPrice = 29.99;
        SubscriptionLevel.BillingFrequency newBillingFrequency = SubscriptionLevel.BillingFrequency.QUARTERLY;
        subscriptionLevel.setLevel(newLevel);
        subscriptionLevel.setPrice(newPrice);
        subscriptionLevel.setBillingFrequency(newBillingFrequency);

        // THEN : Vérification que les attributs ont été correctement modifiés
        assertEquals(newLevel, subscriptionLevel.getLevel(), "Le niveau d'abonnement devrait avoir été modifié.");
        assertEquals(newPrice, subscriptionLevel.getPrice(), "Le prix devrait avoir été modifié.");
        assertEquals(newBillingFrequency, subscriptionLevel.getBillingFrequency(),
                "La fréquence de facturation devrait avoir été modifiée.");
    }

    // Test pour vérifier que l'ID est initialement null (avant la persistance)
    @Test
    void subscriptionLevel_idShouldBeNullByDefault() {
        // GIVEN : Création d'une instance de SubscriptionLevel
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();

        // THEN : Vérification que l'ID est null
        assertNull(subscriptionLevel.getId(), "L'ID devrait être null par défaut avant la persistance.");
    }

    // Test pour vérifier le fonctionnement de la méthode fromString de
    // l'énumération Level (cas valide)
    @Test
    void level_fromString_shouldReturnCorrectEnumValueForValidString() {
        // GIVEN : Une chaîne de caractères représentant un niveau d'abonnement valide
        String basic = "BASIC";
        String premium = "PREMIUM";
        String ultra = "ULTRA";

        // WHEN : Appel de la méthode fromString
        SubscriptionLevel.Level basicLevel = SubscriptionLevel.Level.fromString(basic);
        SubscriptionLevel.Level premiumLevel = SubscriptionLevel.Level.fromString(premium);
        SubscriptionLevel.Level ultraLevel = SubscriptionLevel.Level.fromString(ultra);

        // THEN : Vérification que la méthode retourne l'énumération correcte
        assertEquals(SubscriptionLevel.Level.BASIC, basicLevel,
                "fromString devrait retourner BASIC pour la chaîne 'BASIC'.");
        assertEquals(SubscriptionLevel.Level.PREMIUM, premiumLevel,
                "fromString devrait retourner PREMIUM pour la chaîne 'PREMIUM'.");
        assertEquals(SubscriptionLevel.Level.ULTRA, ultraLevel,
                "fromString devrait retourner ULTRA pour la chaîne 'ULTRA'.");
    }

    // Test pour vérifier le fonctionnement de la méthode fromString de
    // l'énumération Level (cas insensible à la casse)
    @Test
    void level_fromString_shouldBeCaseInsensitive() {
        // GIVEN : Des chaînes de caractères représentant des niveaux d'abonnement
        // valides mais avec des casses différentes
        String basicLower = "basic";
        String premiumMixed = "PrEmIuM";
        String ultraUpper = "ULTRA";

        // WHEN : Appel de la méthode fromString
        SubscriptionLevel.Level basicLevel = SubscriptionLevel.Level.fromString(basicLower);
        SubscriptionLevel.Level premiumLevel = SubscriptionLevel.Level.fromString(premiumMixed);
        SubscriptionLevel.Level ultraLevel = SubscriptionLevel.Level.fromString(ultraUpper);

        // THEN : Vérification que la méthode est insensible à la casse
        assertEquals(SubscriptionLevel.Level.BASIC, basicLevel,
                "fromString devrait être insensible à la casse pour 'basic'.");
        assertEquals(SubscriptionLevel.Level.PREMIUM, premiumLevel,
                "fromString devrait être insensible à la casse pour 'PrEmIuM'.");
        assertEquals(SubscriptionLevel.Level.ULTRA, ultraLevel,
                "fromString devrait être insensible à la casse pour 'ULTRA'.");
    }

    // Test pour vérifier le fonctionnement de la méthode fromString de
    // l'énumération Level (cas invalide)
    @Test
    void level_fromString_shouldThrowIllegalArgumentExceptionForUnknownLevel() {
        // GIVEN : Une chaîne de caractères représentant un niveau d'abonnement inconnu
        String unknownLevel = "GOLD";

        // THEN : Vérification qu'une IllegalArgumentException est lancée avec le
        // message correct
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            SubscriptionLevel.Level.fromString(unknownLevel);
        });
        assertEquals("Niveau d'abonnement inconnu : GOLD", exception.getMessage(),
                "Le message de l'exception doit être correct.");
    }

    // Test pour vérifier le fonctionnement de la méthode toString()
    @Test
    void toString_shouldReturnLevelName() {
        // GIVEN : Création d'une instance de SubscriptionLevel avec un niveau défini
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();
        subscriptionLevel.setLevel(SubscriptionLevel.Level.PREMIUM);

        // WHEN : Appel de la méthode toString()
        String toStringResult = subscriptionLevel.toString();

        // THEN : Vérification que la méthode retourne le nom du niveau
        assertEquals("PREMIUM", toStringResult, "toString() devrait retourner le nom du niveau.");
    }

    // Test pour vérifier le fonctionnement de la méthode toString() lorsque le
    // niveau est null
    @Test
    void toString_shouldReturnNullWhenLevelIsNull() {
        // GIVEN : Création d'une instance de SubscriptionLevel sans définir de niveau
        SubscriptionLevel subscriptionLevel = new SubscriptionLevel();

        // WHEN : Appel de la méthode toString()
        String toStringResult = subscriptionLevel.toString();

        // THEN : Vérification que la méthode retourne null
        assertNull(toStringResult, "toString() devrait retourner null lorsque le niveau est null.");
    }

    // Test pour vérifier les valeurs de l'énumération BillingFrequency
    @Test
    void billingFrequency_enumValuesAreCorrect() {
        // THEN : Vérification que les valeurs de l'énumération sont correctement
        // définies
        assertEquals("MONTHLY", SubscriptionLevel.BillingFrequency.MONTHLY.toString(),
                "La fréquence MONTHLY doit être correcte.");
        assertEquals("QUARTERLY", SubscriptionLevel.BillingFrequency.QUARTERLY.toString(),
                "La fréquence QUARTERLY doit être correcte.");
        assertEquals("YEARLY", SubscriptionLevel.BillingFrequency.YEARLY.toString(),
                "La fréquence YEARLY doit être correcte.");
    }
}