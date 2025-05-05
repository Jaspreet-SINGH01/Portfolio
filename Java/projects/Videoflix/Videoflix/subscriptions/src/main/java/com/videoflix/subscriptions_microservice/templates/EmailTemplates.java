package com.videoflix.subscriptions_microservice.templates;

public final class EmailTemplates {

    private EmailTemplates() {
        // Classe utilitaire pour empêcher l'instanciation
    }

    // Email subjects
    public static final String SUBJECT_SUBSCRIPTION_EXPIRING = "Votre abonnement Videoflix expire bientôt !";
    public static final String SUBJECT_SUBSCRIPTION_EXPIRED = "Votre abonnement Videoflix a expiré";
    public static final String SUBJECT_TRIAL_ENDING = "Votre période d'essai Videoflix se termine bientôt !";
    public static final String SUBJECT_TRIAL_ENDED = "Votre période d'essai Videoflix est terminée";
    public static final String SUBJECT_WELCOME = "Bienvenue chez Videoflix !";
    public static final String SUBJECT_PAYMENT_REMINDER = "Rappel de paiement Videoflix à venir";

    public static final String SUBSCRIPTION_NOTIFICATION = """
            Cher %s,

            Ceci est un rappel amical concernant votre abonnement %s.
            Votre prochaine facturation aura lieu le %s (dans %d jours).

            Veuillez vous assurer que vos informations de paiement sont à jour pour éviter toute interruption de service.

            Si vous avez des questions, n'hésitez pas à contacter notre support.

            Merci, L'équipe Videoflix""";

    public static final String WELCOME_EMAIL = """
            Cher %s,

            Bienvenue chez Videoflix ! Vous avez souscrit à l'abonnement %s.
            Profitez de notre vaste bibliothèque de contenu !

            Voici quelques liens utiles :
            - [Lien vers votre compte]
            - [Lien vers notre catalogue]
            - [Lien vers la FAQ ou l'aide]

            Merci de nous rejoindre,
            L'équipe Videoflix""";

    public static final String TRIAL_PERIOD_ENDING_NOTIFICATION = """
            Cher %s,

            Votre période d'essai pour l'abonnement %s se termine le %s.
            Profitez-en au maximum jusqu'à la fin !

            Pour continuer à bénéficier de Videoflix sans interruption, vous pouvez souscrire à un plan payant dès maintenant.

            Merci,
            L'équipe Videoflix""";

    public static final String SUBSCRIPTION_EXPIRING_EMAIL = """
            Cher %s,

            Votre abonnement %s expire le %s.
            Pour continuer à profiter de Videoflix sans interruption, veuillez renouveler votre abonnement.

            Merci,
            L'équipe Videoflix""";

    public static final String SUBSCRIPTION_EXPIRED_EMAIL = """
            Cher %s,

            Votre abonnement %s a expiré le %s.
            Pour retrouver l'accès à tout le contenu de Videoflix, veuillez renouveler votre abonnement dès aujourd'hui.

            Merci,
            L'équipe Videoflix""";

    public static final String TRIAL_ENDED_EMAIL = """
            Cher %s,

            Votre période d'essai pour l'abonnement %s s'est terminée le %s.
            Nous espérons que vous avez apprécié votre essai !

            Pour continuer à accéder à tout notre contenu, veuillez souscrire à un plan payant dès aujourd'hui.

            Merci,
            L'équipe Videoflix""";
}