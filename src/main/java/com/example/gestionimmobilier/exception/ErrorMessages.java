package com.example.gestionimmobilier.exception;


public final class ErrorMessages {

    private ErrorMessages() {
    }

    // --- Validation (400) ---
    public static final String VALIDATION_ECHEC = "Échec de la validation";
    public static final String DONNEES_INVALIDES = "Les données fournies sont invalides. Vérifiez les champs indiqués.";
    public static final String REQUETE_INVALIDE = "Requête invalide";

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

    // --- Token expiré / invalide ---
    public static final String TOKEN_EXPIRE = "Token expiré ou invalide";
    public static final String MESSAGE_TOKEN_EXPIRE = "Votre session a expiré. Veuillez vous reconnecter pour obtenir un nouveau token.";
}
