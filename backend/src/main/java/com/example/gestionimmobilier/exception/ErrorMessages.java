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
    public static final String IDENTIFIANTS_INVALIDES = "Identifiants invalides.";

    // --- Utilisateur ---
    public static final String UTILISATEUR_INTROUVABLE = "L'utilisateur demandé n'existe pas ou a été supprimé.";
    public static final String UTILISATEUR_EXISTE_DEJA = "Un utilisateur avec ce nom ou cet email existe déjà.";
    public static final String UTILISATEUR_INTROUVABLE_KEYCLOAK = "Utilisateur introuvable dans Keycloak.";
    public static final String USERNAME_INVALIDE = "Nom d'utilisateur invalide. Utilisez uniquement des lettres, chiffres, '_' ou '-' (sans espaces).";
    public static final String LISTE_ROLES_VIDE = "La liste des rôles ne peut pas être vide.";
    public static final String AUCUN_ROLE_VALIDE = "Au moins un rôle doit être attribué.";
    public static final String ROLE_PUBLIC_INTERDIT = "Inscription publique: ROLE_ADMIN et ROLE_AGENT ne sont pas autorisés.";
    public static final String ERREUR_ID_KEYCLOAK = "Impossible de récupérer l'ID du nouvel utilisateur Keycloak.";

    // --- Bien immobilier / Images ---
    public static final String REPERTOIRE_STOCKAGE_IMPOSSIBLE = "Impossible de créer le répertoire de stockage des fichiers.";
    public static final String REPERTOIRE_BIEN_IMAGES_IMPOSSIBLE = "Impossible de créer le répertoire du bien pour les images.";
    public static final String IMAGE_ENREGISTREMENT_ECHEC = "Erreur lors de l'enregistrement de l'image.";
    public static final String IMAGES_UPLOAD_ECHEC = "Erreur lors de l'enregistrement des images.";
    public static final String IMAGE_TAILLE_DEPASSEE = "La taille d'image dépasse la limite autorisée. Réduisez la taille ou envoyez moins d'images à la fois.";
    public static final String REFERENCE_UNIQUE_IMPOSSIBLE = "Impossible de générer une référence unique pour le bien.";
    public static final String BIEN_TYPE_INCOMPATIBLE = "Le type du bien (APPARTEMENT/MAISON) ne correspond pas au bien existant.";
    public static final String IDENTITE_PROPRIETAIRE_REQUISE = "L'identité du propriétaire est requise pour lister les biens.";

    // --- Contrat / Bail ---
    public static final String UTILISATEUR_N_EST_PAS_AGENT = "L'utilisateur choisi n'est pas un agent.";
    public static final String BIEN_N_APPARTIENT_PAS_PROPRIETAIRE = "Le bien n'appartient pas à ce propriétaire.";
    public static final String CONTRAT_NON_MODIFIABLE = "Un contrat résilié ou terminé ne peut pas être modifié.";
    public static final String CONTRAT_DEJA_RESILIE = "Ce contrat est déjà résilié ou terminé.";
    public static final String CONTRAT_DEMANDE_REFUSEE_NON_RESILIABLE = "Cette demande de location a été refusée ; la résiliation ne s'applique pas.";
    public static final String MANDAT_INTROUVABLE = "Le mandat demandé n'existe pas ou a été supprimé.";
    public static final String MANDAT_DEJA_RESILIE = "Ce mandat est déjà résilié ou terminé.";
    public static final String MANDAT_RESILIATION_DEJA_EN_COURS = "Une demande de résiliation est déjà en cours pour ce mandat.";
    public static final String MANDAT_RESILIATION_IMPOSSIBLE_STATUT = "La résiliation n'est possible que pour un mandat actif.";
    public static final String MANDAT_APPROBATION_RESILIATION_IMPOSSIBLE = "Seule une demande en attente de résiliation peut être approuvée ou rejetée.";
    public static final String MANDAT_SANS_DOCUMENT = "Aucun document n'est associé à ce mandat.";
    public static final String MANDAT_DOCUMENT_TELECHARGEMENT_ECHEC = "Impossible de récupérer le fichier du mandat.";
    public static final String MANDAT_DATE_DEBUT_PASSEE = "La date de début du mandat ne peut pas être dans le passé.";
    public static final String MANDAT_DATE_FIN_AVANT_DEBUT = "La date de fin du mandat doit être postérieure à la date de début.";
    public static final String MANDAT_DATE_SIGNATURE_FUTURE = "La date de signature du mandat ne peut pas être dans le futur.";
    public static final String MANDAT_DOCUMENT_PDF_REQUIS = "Le document du mandat doit être un fichier PDF valide.";
    public static final String BIEN_DEJA_SOUS_MANDAT_ACTIF = "Ce bien a déjà un mandat actif.";
    public static final String AGENT_NON_ACTIVE_POUR_MANDAT = "L'agent doit être activé (agence approuvée) pour pouvoir être assigné à un mandat.";
    public static final String COMPTE_NON_AGENT = "Ce compte n'est pas un compte agent.";
    public static final String AGENT_SANS_AGENCE = "Aucune agence n'est associée à ce compte agent.";
    public static final String AGENCE_MODIFICATION_NON_AUTORISEE = "La modification de cette agence n'est pas autorisée pour son statut actuel.";
    public static final String BIEN_DEJA_LIE_CONTRAT = "Ce bien est déjà lié à un contrat de location. Un seul contrat par bien est autorisé.";
    public static final String LOCATAIRE_DEJA_LIE_CONTRAT_ACTIF = "Ce locataire a déjà un contrat actif. Impossible d'activer un deuxième contrat.";
    public static final String BIEN_NON_DISPONIBLE_POUR_LOCATION = "Seul un bien disponible peut faire l'objet d'un nouveau contrat de location.";
    public static final String CONTRAT_GERE_PAR_MANDATAIRE = "Ce bien est sous mandat de gestion : seul l'agent mandaté peut modifier ce bail ou en accepter les conditions.";
    public static final String CONTRAT_VALIDATION_AGENT_REQUISE = "Cette demande de location est en attente de validation par l'agent mandaté.";
    public static final String MANDAT_ACTIF_SANS_AGENT = "Ce bien a un mandat actif sans agent assigné. Contactez l'administrateur.";
    public static final String CONTRAT_TRANSITION_STATUT_INTERDITE = "Transition de statut non autorisée pour ce bail.";
    public static final String CONTRAT_AGENT_HORS_MANDAT = "Seul l'agent titulaire du mandat actif sur ce bien peut traiter cette demande.";
    public static final String CONTRAINTE_VIOLATION = "Opération impossible : contrainte non respectée (ex. doublon).";

    // --- Agence ---
    public static final String AGENCE_INTROUVABLE = "L'agence demandée n'existe pas ou a été supprimée.";
    public static final String AGENCE_EMAIL_DEJA_UTILISE = "Une agence avec cet email est déjà inscrite.";
    public static final String AGENCE_DEJA_ACTIVE = "Cette agence est déjà active.";
    public static final String AGENCE_NON_ACTIVE = "Seules les agences actives peuvent effectuer cette action.";
    public static final String AGENCE_EMAIL_PERSONNEL_NON_ACCEPTE = "Un email professionnel (entreprise) est requis. Les adresses personnelles (gmail, yahoo, etc.) ne sont pas acceptées.";
    public static final String AGENCE_CONTACT_EMAIL_DEJA_UTILISE = "Un compte existe déjà avec cet email pour le contact de l'agence.";
    public static final String AGENCE_CONTACT_USERNAME_DEJA_UTILISE = "Un compte existe déjà avec ce nom d'utilisateur.";
    public static final String AGENCE_SUPPRESSION_IMPOSSIBLE = "Suppression impossible: cette agence possède des agents liés.";
    public static final String AGENCE_DEMANDE_INTROUVABLE = "La demande de modification n'existe pas ou a déjà été traitée.";
    public static final String AGENCE_DEMANDE_DEJA_TRAITEE = "Cette demande a déjà été approuvée ou rejetée.";
    public static final String AGENCE_DEMANDE_CHAMPS_VIDES = "Indiquez au moins un champ à modifier pour soumettre une demande.";

    // --- Versement / Quittance ---
    public static final String CONTRAT_ACTIF_REQUIS_VERSEMENT = "Seul un contrat actif peut recevoir un versement.";
    public static final String VERSEMENT_REFERENCE_DEJA_UTILISEE = "Cette référence de paiement est déjà utilisée.";
    public static final String VERSEMENT_INTROUVABLE = "Le versement demandé n'existe pas ou a été supprimé.";
    public static final String QUITTANCE_GENERATION_ECHEC = "Erreur lors de la génération de la quittance PDF.";
    public static final String QUITTANCE_DEJA_GENEREE = "Une quittance existe déjà pour ce versement.";
    public static final String PREUVE_PAIEMENT_ENREGISTREMENT_ECHEC = "Erreur lors de l'enregistrement de la preuve de paiement.";
}
