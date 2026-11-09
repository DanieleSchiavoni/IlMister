import { Routes } from '@angular/router';

import { guardAutenticato, guardOspite } from './core/auth.guard';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guardOspite],
    loadComponent: () => import('./pages/login/login').then((m) => m.Login),
  },
  {
    path: 'registrazione',
    canActivate: [guardOspite],
    loadComponent: () => import('./pages/registrazione/registrazione').then((m) => m.Registrazione),
  },
  {
    path: 'carriere',
    canActivate: [guardAutenticato],
    loadComponent: () => import('./pages/carriere/carriere').then((m) => m.Carriere),
  },
  {
    path: 'carriere/nuova',
    canActivate: [guardAutenticato],
    loadComponent: () => import('./pages/nuova-carriera/nuova-carriera').then((m) => m.NuovaCarriera),
  },
  { path: '', pathMatch: 'full', redirectTo: 'carriere' },
  { path: '**', redirectTo: 'carriere' },
];
