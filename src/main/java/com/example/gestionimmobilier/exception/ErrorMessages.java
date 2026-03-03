package com.example.gestionimmobilier.exception;


public final class ErrorMessages {

    private ErrorMessages() {
    }

    // --- Validation (400) ---
    public static final String VALIDATION_ECHEC = "Échec de la validation";
    public static final String DONNEES_INVALIDES = "Les données fournies sont invalides. Vérifiez les champs indiqués.";
    public static final String REQUETE_INVALIDE = "Requête invalide";
    public static final String JSON_INVALIDE = "Le format JSON du corps de la requête est invalide. Utilisez des guillemets doubles pour les clés et les chaînes.";

    // --- Authentification / Autorisation ---
    public static final String NON_AUTORISE = "Non autorisé";
    public static final String MESSAGE_UNAUTHORIZED = "Vous devez vous connecter pour accéder à cette ressource.";
    public static final String ACCES_REFUSE = "Accès refusé";
    public static final String MESSAGE_FORBIDDEN = "Vous n'avez pas les droits pour effectuer cette action.";

    // --- Ressource non trouvée (404) ---
    public static final String RESSOURCE_INTROUVABLE = "Ressource introuvable";
    public static final String MESSAGE_NOT_FOUND = "La ressource demandée n'existe pas ou a été supprimée.";
    public static final String BIEN_INTROUVABLE = "Le bien immobilier demandé n'existe pas ou a été supprimé.";
    public static final String CLIENT_INTROUVABLE = "Le client demandé n'existe pas ou a été supprimé.";
    public static final String PROPRIETAIRE_INTROUVABLE = "Le propriétaire demandé n'existe pas ou a été supprimé.";
    public static final String CONTRAT_INTROUVABLE = "Le contrat demandé n'existe pas ou a été supprimé.";
    public static final String BAIL_INTROUVABLE = "Le bail demandé n'existe pas ou a été supprimé.";
    public static final String INTERVENTION_INTROUVABLE = "L'intervention demandée n'existe pas ou a été supprimée.";

    // --- Règle métier (422) ---
    public static final String VIOLATION_REGLE_METIER = "Violation d'une règle métier";
    public static final String MESSAGE_BUSINESS_RULE = "L'opération demandée n'est pas autorisée dans l'état actuel des données.";

    // --- Erreur serveur (500) ---
    public static final String ERREUR_SERVEUR = "Erreur interne du serveur";
    public static final String MESSAGE_ERREUR_INTERNE = "Une erreur technique s'est produite. Veuillez réessayer plus tard.";

    // --- Méthode HTTP (405) ---
    public static final String METHODE_NON_AUTORISEE = "Méthode non autorisée";
    public static final String MESSAGE_METHODE_NON_AUTORISEE = "La méthode HTTP utilisée n'est pas supportée pour cette ressource.";

    // --- Token expiré / invalide ---
    public static final String TOKEN_EXPIRE = "Token expiré ou invalide";
    public static final String MESSAGE_TOKEN_EXPIRE = "Votre session a expiré. Veuillez vous reconnecter pour obtenir un nouveau token.";

    // --- Utilisateur ---
    public static final String UTILISATEUR_INTROUVABLE = "L'utilisateur demandé n'existe pas ou a été supprimé.";
    public static final String UTILISATEUR_EXISTE_DEJA = "Un utilisateur avec ce nom ou cet email existe déjà.";
    public static final String UTILISATEUR_INTROUVABLE_KEYCLOAK = "Utilisateur introuvable dans Keycloak.";
    public static final String LISTE_ROLES_VIDE = "La liste des rôles ne peut pas être vide.";
    public static final String AUCUN_ROLE_VALIDE = "Au moins un rôle doit être attribué.";
    public static final String ERREUR_ID_KEYCLOAK = "Impossible de récupérer l'ID du nouvel utilisateur Keycloak.";

    // --- Bien immobilier / Images ---
    public static final String REPERTOIRE_STOCKAGE_IMPOSSIBLE = "Impossible de créer le répertoire de stockage des fichiers.";
    public static final String REPERTOIRE_BIEN_IMAGES_IMPOSSIBLE = "Impossible de créer le répertoire du bien pour les images.";
    public static final String IMAGE_ENREGISTREMENT_ECHEC = "Erreur lors de l'enregistrement de l'image.";
    public static final String IMAGES_UPLOAD_ECHEC = "Erreur lors de l'enregistrement des images.";
    public static final String REFERENCE_UNIQUE_IMPOSSIBLE = "Impossible de générer une référence unique pour le bien.";
    public static final String BIEN_TYPE_INCOMPATIBLE = "Le type du bien (APPARTEMENT/MAISON) ne correspond pas au bien existant.";
    public static final String IDENTITE_PROPRIETAIRE_REQUISE = "L'identité du propriétaire est requise pour lister les biens.";
}
