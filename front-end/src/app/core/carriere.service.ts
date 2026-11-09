import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

import { environment } from '../../environments/environment';
import { AnteprimaCampionato, Carriera, NuovaCarriera } from './modelli';

@Injectable({ providedIn: 'root' })
export class CarriereService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api`;

  elenco(): Observable<Carriera[]> {
    return this.http.get<Carriera[]>(`${this.base}/carriere`);
  }

  crea(richiesta: NuovaCarriera): Observable<Carriera> {
    return this.http.post<Carriera>(`${this.base}/carriere`, richiesta);
  }

  elimina(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/carriere/${id}`);
  }

  anteprima(seme: number): Observable<AnteprimaCampionato> {
    return this.http.get<AnteprimaCampionato>(`${this.base}/anteprima/campionato`, {
      params: new HttpParams().set('seme', seme),
    });
  }
}
