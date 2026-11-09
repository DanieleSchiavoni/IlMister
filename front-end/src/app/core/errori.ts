import { HttpErrorResponse } from '@angular/common/http';

import { ErroreApi } from './modelli';

/** Trasforma un errore HTTP in una frase da mostrare all'utente */
export function messaggioErrore(errore: unknown): string {
  if (errore instanceof HttpErrorResponse) {
    if (errore.status === 0) {
      return 'Impossibile contattare il server. Riprova tra poco.';
    }
    const corpo = errore.error as Partial<ErroreApi> | null;
    if (corpo?.campi?.length) {
      return corpo.campi.map((c) => c.messaggio).join(' ');
    }
    if (corpo?.messaggio) {
      return corpo.messaggio;
    }
  }
  return 'Qualcosa è andato storto. Riprova.';
}
