export interface NombreBiensDisponiblesResponse {
  nombreBiensDisponibles: number;
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

/** Aligné sur `LocataireResponse` backend (sous-classe Client → table `clients` + `utilisateurs`). */
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

/** Aligné sur `ProprietaireResponse` backend (sous-classe → table `proprietaires` + `utilisateurs`). */
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
