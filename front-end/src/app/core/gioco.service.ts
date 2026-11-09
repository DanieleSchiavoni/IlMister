import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import {
  Carriera,
  DecisioniIntervallo,
  Formazione,
  FormazioneRichiesta,
  Giocatore,
  Giornata,
  GiornataGiocata,
  PartitaDettaglio,
  PrimoTempo,
  RigaClassifica,
} from './modelli';

/** Le chiamate alle API di una carriera: tutte passano dall'id della carriera */
@Injectable({ providedIn: 'root' })
export class GiocoService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/carriere`;

  carriera(id: number): Observable<Carriera> {
    return this.http.get<Carriera>(`${this.base}/${id}`);
  }

  classifica(id: number): Observable<RigaClassifica[]> {
    return this.http.get<RigaClassifica[]>(`${this.base}/${id}/classifica`);
  }

  calendario(id: number): Observable<Giornata[]> {
    return this.http.get<Giornata[]>(`${this.base}/${id}/calendario`);
  }

  rosa(id: number, squadraId?: number): Observable<Giocatore[]> {
    let params = new HttpParams();
    if (squadraId !== undefined) {
      params = params.set('squadraId', squadraId);
    }
    return this.http.get<Giocatore[]>(`${this.base}/${id}/rosa`, { params });
  }

  avanza(id: number): Observable<GiornataGiocata> {
    return this.http.post<GiornataGiocata>(`${this.base}/${id}/giornata/avanza`, {});
  }

  partita(id: number, partitaId: number): Observable<PartitaDettaglio> {
    return this.http.get<PartitaDettaglio>(`${this.base}/${id}/partite/${partitaId}`);
  }

  formazione(id: number): Observable<Formazione> {
    return this.http.get<Formazione>(`${this.base}/${id}/formazione`);
  }

  salvaFormazione(id: number, richiesta: FormazioneRichiesta): Observable<Formazione> {
    return this.http.put<Formazione>(`${this.base}/${id}/formazione`, richiesta);
  }

  /** Gioca il primo tempo della partita dell'utente (se è già all'intervallo lo ripropone) */
  primoTempo(id: number): Observable<PrimoTempo> {
    return this.http.post<PrimoTempo>(`${this.base}/${id}/partita/primo-tempo`, {});
  }

  /** Gioca il secondo tempo con le decisioni dell'intervallo e poi il resto della giornata */
  concludi(id: number, decisioni: DecisioniIntervallo): Observable<GiornataGiocata> {
    return this.http.post<GiornataGiocata>(`${this.base}/${id}/partita/conclusione`, decisioni);
  }
}
