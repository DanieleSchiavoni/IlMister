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
  {
    path: 'carriere/:id',
    canActivate: [guardAutenticato],
    loadComponent: () => import('./pages/carriera/carriera').then((m) => m.CarrieraPagina),
    children: [
      { path: '', pathMatch: 'full', redirectTo: 'classifica' },
      {
        path: 'classifica',
        loadComponent: () => import('./pages/carriera/classifica/classifica').then((m) => m.Classifica),
      },
      {
        path: 'calendario',
        loadComponent: () => import('./pages/carriera/calendario/calendario').then((m) => m.Calendario),
      },
      {
        path: 'formazione',
        loadComponent: () => import('./pages/carriera/formazione/formazione').then((m) => m.Formazione),
      },
      {
        path: 'intervallo',
        loadComponent: () => import('./pages/carriera/intervallo/intervallo').then((m) => m.Intervallo),
      },
      { path: 'rosa', loadComponent: () => import('./pages/carriera/rosa/rosa').then((m) => m.Rosa) },
      {
        path: 'partite/:partitaId',
        loadComponent: () => import('./pages/carriera/partita/partita').then((m) => m.Partita),
      },
    ],
  },
  { path: '', pathMatch: 'full', redirectTo: 'carriere' },
  { path: '**', redirectTo: 'carriere' },
];
