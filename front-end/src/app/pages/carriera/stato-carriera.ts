import { Injectable, inject, signal } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { GiocoService } from '../../core/gioco.service';
import { messaggioErrore } from '../../core/errori';
import { Carriera, GiornataGiocata } from '../../core/modelli';

/**
 * Lo stato condiviso tra la pagina della carriera e le sue schede (classifica, calendario, rosa...).
 * Si crea una volta per carriera aperta; `versione` cresce a ogni giornata giocata,
 * così le schede sanno quando ricaricare i propri dati.
 */
@Injectable()
export class StatoCarriera {
  private readonly gioco = inject(GiocoService);

  readonly id = Number(inject(ActivatedRoute).snapshot.paramMap.get('id'));
  readonly carriera = signal<Carriera | null>(null);
  readonly errore = signal<string | null>(null);
  readonly versione = signal(0);
  /** I risultati dell'ultima giornata giocata, mostrati in cima alla pagina finché non si chiudono */
  readonly ultima = signal<GiornataGiocata | null>(null);

  constructor() {
    this.ricarica();
  }

  /** Rilegge la carriera e avvisa le schede che i dati sono cambiati */
  ricarica(): void {
    this.gioco.carriera(this.id).subscribe({
      next: (carriera) => {
        this.carriera.set(carriera);
        this.versione.update((v) => v + 1);
      },
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }
}
