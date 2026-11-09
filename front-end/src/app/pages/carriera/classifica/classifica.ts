import { Component, effect, inject, signal, untracked } from '@angular/core';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { RigaClassifica } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-classifica',
  templateUrl: './classifica.html',
  styleUrl: './classifica.scss',
})
export class Classifica {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly righe = signal<RigaClassifica[] | null>(null);
  protected readonly errore = signal<string | null>(null);

  constructor() {
    // Si ricarica ogni volta che la carriera cambia (ad esempio dopo una giornata giocata)
    effect(() => {
      this.stato.versione();
      untracked(() => this.carica());
    });
  }

  private carica(): void {
    this.gioco.classifica(this.stato.id).subscribe({
      next: (righe) => this.righe.set(righe),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }
}
