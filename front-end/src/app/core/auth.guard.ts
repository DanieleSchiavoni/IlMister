import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';

import { AuthService } from './auth.service';

/** Le pagine di gioco sono solo per chi ha fatto il login */
export const guardAutenticato: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticato() ? true : inject(Router).createUrlTree(['/login']);
};

/** Login e registrazione hanno senso solo per chi non è già dentro */
export const guardOspite: CanActivateFn = () => {
  const auth = inject(AuthService);
  return auth.autenticato() ? inject(Router).createUrlTree(['/carriere']) : true;
};
