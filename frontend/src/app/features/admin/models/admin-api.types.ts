export interface NombreBiensDisponiblesResponse {
  nombreBiensDisponibles: number;
}

/** Inscriptions publiques (locataire / propriétaire) non activées dans Keycloak. */
export interface NombreComptesEnAttenteActivationResponse {
  nombreComptesEnAttenteActivation: number;
}

export interface BiensLouesVsLibresResponse {
  disponibles: number;
  loues: number;
  vendus: number;
  sousCompromis: number;
}

export interface RevenusMensuelsResponse {
  annee: number;
  mois: number;
  revenusMensuels: number;
}

export interface LocataireEnRetardLigneResponse {
  contratId: string;
  numContrat: string;
  locataireNom: string;
  bienReference: string;
  annee: number;
  mois: number;
  resteAPayer: number;
}

export interface LocatairesEnRetardResponse {
  locataires: LocataireEnRetardLigneResponse[];
}

/** Synthèse tableau de bord agent (mandats, baux, revenus du mois, retards). */
export interface AgentDashboardOverviewResponse {
  mandatsActifs: number;
  mandatsTotal: number;
  biensDistinctsSousMandat: number;
  bauxActifs: number;
  demandesEnAttenteValidationAgent: number;
  locatairesActifsDistincts: number;
  revenusMensuels: number;
  anneePeriode: number;
  moisPeriode: number;
  locatairesEnRetard: LocataireEnRetardLigneResponse[];
  /** Répartition des biens liés à un mandat actif (périmètre agent). */
  biensDisponiblesSousMandat: number;
  biensLouesSousMandat: number;
  biensVendusSousMandat: number;
  biensSousCompromisSousMandat: number;
  demandeModificationAgenceEnAttente: boolean;
  typeDemandeModificationAgenceEnAttente: string | null;
  resumeDemandeModificationAgenceEnAttente: string | null;
  derniereNoteAdminDemandeAgence: string | null;
  derniereNoteAdminDemandeAgenceLe: string | null;
}

export type TypeDemandeModificationAgence = 'MISE_AJOUR' | 'SUPPRESSION';
export type StatutDemandeModificationAgence = 'EN_ATTENTE' | 'APPROUVE' | 'REJETE';

export interface AgenceDemandeEnAttenteResponse {
  id: string;
  type: TypeDemandeModificationAgence;
  nomPropose: string | null;
  emailPropose: string | null;
  telephonePropose: string | null;
  adressePropose: string | null;
  villePropose: string | null;
  demandeLe: string;
}

export interface AgenceEspaceAgentResponse {
  agence: AgenceResponse;
  demandeEnAttente: AgenceDemandeEnAttenteResponse | null;
}

export interface AgenceModificationDemandeAdminResponse {
  id: string;
  agenceId: string;
  agenceNom: string;
  type: TypeDemandeModificationAgence;
  statut: StatutDemandeModificationAgence;
  nomPropose: string | null;
  emailPropose: string | null;
  telephonePropose: string | null;
  adressePropose: string | null;
  villePropose: string | null;
  createdAt: string;
  demandeurKeycloakId: string | null;
}

export interface MandatsGestionStatistiqueResponse {
  agenceId: string;
  nomAgence: string;
  totalMandats: number;
  mandatsActifs: number;
  mandatsResilies: number;
  mandatsTermines: number;
  mandatsEnAttente: number;
}

export type StatutAgence = 'PENDING' | 'ACTIVE' | 'REJECTED' | 'SUSPENDED';

export interface AgenceResponse {
  id: string;
  nom: string;
  email: string;
  telephone: string;
  adresse: string;
  ville: string;
  statut: StatutAgence;
  createdAt: string;
}

export type StatutBien = 'DISPONIBLE' | 'LOUE' | 'VENDU' | 'SOUS_COMPROMIS';
export type StatutBail =
  | 'ACTIF'
  | 'RESILIE'
  | 'TERMINE'
  | 'EN_ATTENTE'
  | 'EN_ATTENTE_VALIDATION_AGENT'
  | 'REFUSE';
export type StatutMandat =
  | 'ACTIF'
  | 'RESILIE'
  | 'TERMINE'
  | 'EN_ATTENTE'
  | 'EN_ATTENTE_RESILIATION';

export interface BienResponse {
  id: string;
  reference: string;
  titre: string;
  surface: number;
  prixBase: number;
  statut: StatutBien;
  images: string[];
  proprietaireId: string;
  proprietaireNom: string;
  /** Présent quand le backend renvoie le polymorphisme Appartement / Maison */
  type?: 'APPARTEMENT' | 'MAISON';
  etage?: number;
  ascenseur?: boolean;
  surfaceTerrain?: number;
  garage?: boolean;
  adresse?: {
    rue?: string;
    ville?: string;
    codePostal?: string;
    pays?: string;
  };
}

export interface ContratResponse {
  id: string;
  numContrat: string;
  dateSignature: string;
  dateDebut: string;
  dateFin: string;
  loyerHC: number;
  charges: number;
  documentUrl: string | null;
  statut: StatutBail;
  bien: BienResponse;
  proprietaire: ProprietaireResponse;
  locataire: LocataireResponse;
  agentId: string | null;
  agentNomComplet: string | null;
}

export interface MandatResponse {
  id: string;
  numMandat: string;
  proprietaireId: string;
  proprietaireNom: string;
  agentId: string;
  agentNom: string;
  bienId: string;
  bienReference: string;
  dateDebut: string;
  dateFin: string;
  commissionPct: number;
  statut: StatutMandat;
  dateSignature: string;
  documentUrl: string | null;
}

export type RoleEnum =
  | 'ROLE_CLIENT'
  | 'ROLE_PROPRIETAIRE'
  | 'ROLE_AGENT'
  | 'ROLE_ADMIN';

export interface UtilisateurResponse {
  id: string;
  keycloakId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: RoleEnum[];
  type: string;
  emailVerified: boolean;
  enabled: boolean;
}

export type StatutDossier = 'EN_ATTENTE' | 'VALIDE' | 'REFUSE';

export interface LocataireResponse {
  id: string;
  keycloakId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: RoleEnum[];
  type: string;
  emailVerified: boolean;
  enabled: boolean;
  budgetMax: number | null;
  statutDossier: StatutDossier | null;
}

export interface ProprietaireResponse {
  id: string;
  keycloakId: string;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: RoleEnum[];
  type: string;
  emailVerified: boolean;
  enabled: boolean;
  rib: string | null;
  adresseContact: string | null;
}
