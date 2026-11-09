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

export type StatoPartita = 'DA_GIOCARE' | 'INTERVALLO' | 'CONCLUSA';

export interface RigaClassifica {
  posizione: number;
  squadraId: number;
  squadra: string;
  giocate: number;
  vittorie: number;
  pareggi: number;
  sconfitte: number;
  golFatti: number;
  golSubiti: number;
  differenzaReti: number;
  punti: number;
  dellUtente: boolean;
}

export interface PartitaInCalendario {
  id: number;
  squadraCasaId: number;
  squadraCasa: string;
  squadraTrasfertaId: number;
  squadraTrasferta: string;
  stato: StatoPartita;
  golCasa: number | null;
  golTrasferta: number | null;
  dellUtente: boolean;
}

export interface Giornata {
  numero: number;
  data: string;
  partite: PartitaInCalendario[];
}

export interface RisultatoPartita {
  partitaId: number;
  squadraCasa: string;
  squadraTrasferta: string;
  golCasa: number;
  golTrasferta: number;
  dellUtente: boolean;
}

export interface GiornataGiocata {
  numero: number;
  data: string;
  risultati: RisultatoPartita[];
  stagioneConclusa: boolean;
}

export interface Giocatore {
  id: number;
  nome: string;
  cognome: string;
  posizione: string;
  siglaPosizione: string;
  ruolo: string;
  numeroMaglia: number | null;
  eta: number;
  piede: string;
  valutazione: number;
  potenziale: number;
  forma: number;
  morale: number;
  energia: number;
  infortunato: boolean;
  tipoInfortunio: string | null;
  infortunatoFinoAl: string | null;
  squalificaGiornate: number;
  disponibile: boolean;
}

export interface Statistica {
  squadraId: number;
  squadra: string;
  possesso: number;
  tiri: number;
  tiriInPorta: number;
  xg: number;
  falli: number;
  calciDAngolo: number;
  ammonizioni: number;
  espulsioni: number;
}

export interface Evento {
  minuto: number;
  minutoRecupero: number;
  tipo: string;
  squadraId: number | null;
  testo: string;
}

export interface Prestazione {
  giocatoreId: number;
  giocatore: string;
  siglaPosizione: string;
  squadraId: number;
  titolare: boolean;
  minutiGiocati: number;
  gol: number;
  assist: number;
  tiri: number;
  ammonito: boolean;
  espulso: boolean;
  voto: number | null;
}

export interface PartitaDettaglio {
  id: number;
  giornata: number;
  data: string;
  stadio: string;
  spettatori: number | null;
  stato: StatoPartita;
  squadraCasaId: number;
  squadraCasa: string;
  squadraTrasfertaId: number;
  squadraTrasferta: string;
  golCasa: number | null;
  golTrasferta: number | null;
  statistiche: Statistica[];
  cronaca: Evento[];
  prestazioni: Prestazione[];
}

export type Modulo = 'M_4_3_3' | 'M_4_4_2' | 'M_4_2_3_1' | 'M_3_5_2' | 'M_3_4_3' | 'M_5_3_2';
export type Mentalita = 'MOLTO_DIFENSIVA' | 'DIFENSIVA' | 'EQUILIBRATA' | 'OFFENSIVA' | 'MOLTO_OFFENSIVA';
export type Pressing = 'BASSO' | 'MEDIO' | 'ALTO';
export type Ritmo = 'LENTO' | 'NORMALE' | 'VELOCE';
export type LineaDifensiva = 'BASSA' | 'MEDIA' | 'ALTA';

/** Le istruzioni tattiche: si scelgono in formazione e si possono cambiare all'intervallo */
export interface Istruzioni {
  mentalita: Mentalita;
  pressing: Pressing;
  ritmo: Ritmo;
  lineaDifensiva: LineaDifensiva;
}

export interface Formazione extends Istruzioni {
  salvata: boolean;
  modulo: Modulo;
  titolari: Giocatore[];
  panchina: Giocatore[];
}

export interface FormazioneRichiesta extends Istruzioni {
  modulo: Modulo;
  titolari: number[];
  panchina: number[];
}

export interface Sostituzione {
  esceId: number;
  entraId: number;
}

export interface DecisioniIntervallo {
  istruzioni: Istruzioni;
  sostituzioni: Sostituzione[];
}

export interface StatisticheSquadra {
  possesso: number;
  tiri: number;
  tiriInPorta: number;
  xg: number;
  falli: number;
  calciDAngolo: number;
  ammonizioni: number;
  espulsioni: number;
}

export interface SituazioneGiocatore {
  giocatoreId: number;
  cognome: string;
  siglaPosizione: string;
  inCampo: boolean;
  espulso: boolean;
  ammonito: boolean;
  infortunato: boolean;
  energia: number;
  gol: number;
  assist: number;
  tiri: number;
}

export interface PrimoTempo {
  partitaId: number;
  squadraCasa: string;
  squadraTrasferta: string;
  utenteInCasa: boolean;
  golCasa: number;
  golTrasferta: number;
  statisticheCasa: StatisticheSquadra;
  statisticheTrasferta: StatisticheSquadra;
  cronaca: string[];
  giocatoriUtente: SituazioneGiocatore[];
}
