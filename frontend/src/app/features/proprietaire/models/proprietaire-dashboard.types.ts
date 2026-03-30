export interface MoisMontantResponse {
  annee: number;
  mois: number;
  montant: number;
}

export interface ProprietaireDashboardOverviewResponse {
  biensTotal: number;
  biensDisponibles: number;
  biensLoues: number;
  biensVendus: number;
  biensSousCompromis: number;
  patrimoineEstimeAnnonce: number;
  contratsTotal: number;
  contratsActifs: number;
  contratsEnAttenteCandidatures: number;
  mandatsTotal: number;
  mandatsActifs: number;
  mandatsEnAttente: number;
  loyerMensuelTheoriqueContratsActifs: number;
  revenusEncaissesMois: number;
  tauxOccupationContratsSurBiens: number;
  anneePeriode: number;
  moisPeriode: number;
  historiqueRevenus6Mois: MoisMontantResponse[];
}
