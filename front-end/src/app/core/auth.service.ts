import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Observable, catchError, finalize, of, shareReplay, tap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { Autenticazione, Utente } from './modelli';

const CHIAVE = 'il-mister.sessione';

interface Sessione {
  accessToken: string;
  refreshToken: string;
  utente: Utente;
}

/**
 * Gestisce la sessione: login, registrazione, rinnovo dei token e logout.
 * I token stanno nel localStorage per sopravvivere al ricaricamento della pagina:
 * è una scelta comoda per un progetto di portfolio; in produzione vera si preferirebbe un cookie HttpOnly.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly base = `${environment.apiUrl}/api/auth`;

  private readonly sessione = signal<Sessione | null>(this.leggiSalvata());
  private rinnovoInCorso$: Observable<Autenticazione> | null = null;

  readonly utente = computed(() => this.sessione()?.utente ?? null);
  readonly autenticato = computed(() => this.sessione() !== null);

  get accessToken(): string | null {
    return this.sessione()?.accessToken ?? null;
  }

  registra(username: string, email: string, password: string): Observable<Utente> {
    return this.http.post<Utente>(`${this.base}/register`, { username, email, password });
  }

  login(identificativo: string, password: string): Observable<Autenticazione> {
    return this.http
      .post<Autenticazione>(`${this.base}/login`, { identificativo, password })
      .pipe(tap((risposta) => this.salva(risposta)));
  }

  /** Cambia il refresh token con una nuova coppia. Più richieste insieme condividono un solo rinnovo (il token è monouso). */
  rinnova(): Observable<Autenticazione> {
    const refreshToken = this.sessione()?.refreshToken;
    if (!refreshToken) {
      return throwError(() => new Error('Nessuna sessione'));
    }
    if (!this.rinnovoInCorso$) {
      this.rinnovoInCorso$ = this.http.post<Autenticazione>(`${this.base}/refresh`, { refreshToken }).pipe(
        tap((risposta) => this.salva(risposta)),
        catchError((errore) => {
          this.pulisci();
          return throwError(() => errore);
        }),
        finalize(() => (this.rinnovoInCorso$ = null)),
        shareReplay(1),
      );
    }
    return this.rinnovoInCorso$;
  }

  logout(): Observable<void> {
    const refreshToken = this.sessione()?.refreshToken;
    this.pulisci();
    if (!refreshToken) {
      return of(undefined);
    }
    // Anche se il server non risponde, la sessione locale è già chiusa
    return this.http.post<void>(`${this.base}/logout`, { refreshToken }).pipe(catchError(() => of(undefined)));
  }

  private salva(risposta: Autenticazione): void {
    const sessione: Sessione = {
      accessToken: risposta.accessToken,
      refreshToken: risposta.refreshToken,
      utente: risposta.utente,
    };
    this.sessione.set(sessione);
    try {
      localStorage.setItem(CHIAVE, JSON.stringify(sessione));
    } catch {
      // storage non disponibile: la sessione resta valida finché la pagina è aperta
    }
  }

  private pulisci(): void {
    this.sessione.set(null);
    try {
      localStorage.removeItem(CHIAVE);
    } catch {
      // niente da fare
    }
  }

  private leggiSalvata(): Sessione | null {
    try {
      const testo = localStorage.getItem(CHIAVE);
      return testo ? (JSON.parse(testo) as Sessione) : null;
    } catch {
      return null;
    }
  }
}
