import { HttpErrorResponse, HttpInterceptorFn, HttpRequest } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, switchMap, throwError } from 'rxjs';

import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

const èRottaAuth = (url: string) => url.includes('/api/auth/');
const èRottaApi = (url: string) => url.startsWith(`${environment.apiUrl}/api/`) || url.startsWith('/api/');

function conToken(richiesta: HttpRequest<unknown>, token: string | null): HttpRequest<unknown> {
  return token ? richiesta.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : richiesta;
}

/**
 * Aggiunge il token a ogni richiesta alle API. Se il server risponde 401 (token scaduto),
 * rinnova la sessione una volta e ripete la richiesta; se il rinnovo fallisce rimanda al login.
 */
export const interceptorAutenticazione: HttpInterceptorFn = (richiesta, avanti) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  if (!èRottaApi(richiesta.url) || èRottaAuth(richiesta.url)) {
    return avanti(richiesta);
  }

  return avanti(conToken(richiesta, auth.accessToken)).pipe(
    catchError((errore: unknown) => {
      if (!(errore instanceof HttpErrorResponse) || errore.status !== 401 || !auth.autenticato()) {
        return throwError(() => errore);
      }
      return auth.rinnova().pipe(
        switchMap((nuova) => avanti(conToken(richiesta, nuova.accessToken))),
        catchError((erroreRinnovo: unknown) => {
          router.navigate(['/login']);
          return throwError(() => erroreRinnovo);
        }),
      );
    }),
  );
};
