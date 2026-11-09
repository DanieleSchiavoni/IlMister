/** I tipi che il back-end restituisce (rispecchiano i DTO Java) */

export type RuoloUtente = 'UTENTE' | 'ADMIN';

export interface Utente {
  id: number;
  username: string;
  email: string;
  ruolo: RuoloUtente;
  abilitato: boolean;
  dataCreazione: string;
}

export interface Autenticazione {
  accessToken: string;
  refreshToken: string;
  tipo: string;
  scadenzaAccessInSecondi: number;
  utente: Utente;
}

export interface ErroreCampo {
  campo: string;
  messaggio: string;
}

export interface ErroreApi {
  status: number;
  errore: string;
  messaggio: string;
  campi?: ErroreCampo[];
}

export interface Carriera {
  id: number;
  nome: string;
  nomeAllenatore: string;
  squadra: string | null;
  campionato: string | null;
  stagione: string | null;
  dataCorrente: string;
  fiduciaPresidente: number;
  stato: string;
  prossimaGiornata: number | null;
  giornateTotali: number;
}

export interface NuovaCarriera {
  nome: string;
  nomeAllenatore: string;
  nomeSquadra: string;
  seme: number;
}

export interface AnteprimaSquadra {
  indice: number;
  nome: string;
  citta: string;
  stadio: string;
  capienza: number;
  reputazione: number;
  forzaMedia: number;
}

export interface AnteprimaCampionato {
  nome: string;
  seme: number;
  squadre: AnteprimaSquadra[];
}
