import { Component, inject, signal } from '@angular/core';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { Allenamento as DatiAllenamento, FocusAllenamento } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-allenamento',
  templateUrl: './allenamento.html',
  styleUrl: './allenamento.scss',
})
export class Allenamento {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly dati = signal<DatiAllenamento | null>(null);
  protected readonly errore = signal<string | null>(null);
  protected readonly salvato = signal(false);

  constructor() {
    this.gioco.allenamento(this.stato.id).subscribe({
      next: (risposta) => this.dati.set(risposta),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected scegli(focus: FocusAllenamento): void {
    if (this.dati()?.focus === focus) {
      return;
    }
    this.errore.set(null);
    this.gioco.impostaFocus(this.stato.id, focus).subscribe({
      next: (risposta) => {
        this.dati.set(risposta);
        this.salvato.set(true);
      },
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }
}
