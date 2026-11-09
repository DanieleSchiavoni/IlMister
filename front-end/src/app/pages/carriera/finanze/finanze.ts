import { CurrencyPipe, DatePipe } from '@angular/common';
import { Component, effect, inject, signal, untracked } from '@angular/core';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { Finanze as DatiFinanze } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-finanze',
  imports: [CurrencyPipe, DatePipe],
  templateUrl: './finanze.html',
  styleUrl: './finanze.scss',
})
export class Finanze {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly finanze = signal<DatiFinanze | null>(null);
  protected readonly errore = signal<string | null>(null);

  constructor() {
    // Dopo ogni giornata o trattativa i conti cambiano
    effect(() => {
      this.stato.versione();
      untracked(() => this.carica());
    });
  }

  private carica(): void {
    this.gioco.finanze(this.stato.id).subscribe({
      next: (finanze) => this.finanze.set(finanze),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected etichetta(tipo: string): string {
    switch (tipo) {
      case 'BIGLIETTI': return 'Biglietti';
      case 'STIPENDI': return 'Stipendi';
      case 'ACQUISTO': return 'Acquisto';
      case 'VENDITA': return 'Cessione';
      default: return tipo;
    }
  }
}
