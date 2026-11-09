import { DatePipe } from '@angular/common';
import { Component, computed, effect, inject, signal, untracked } from '@angular/core';
import { RouterLink } from '@angular/router';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { Giornata } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-calendario',
  imports: [DatePipe, RouterLink],
  templateUrl: './calendario.html',
  styleUrl: './calendario.scss',
})
export class Calendario {
  protected readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly giornate = signal<Giornata[] | null>(null);
  protected readonly errore = signal<string | null>(null);
  /** Il numero della giornata mostrata; se l'utente non ha scelto, è la prossima da giocare */
  private readonly scelta = signal<number | null>(null);

  protected readonly numero = computed(() => {
    const elenco = this.giornate();
    if (!elenco?.length) {
      return null;
    }
    return this.scelta() ?? this.stato.carriera()?.prossimaGiornata ?? elenco[elenco.length - 1].numero;
  });
  protected readonly giornata = computed(() => this.giornate()?.find((g) => g.numero === this.numero()) ?? null);
  protected readonly totale = computed(() => this.giornate()?.length ?? 0);

  constructor() {
    effect(() => {
      this.stato.versione();
      untracked(() => {
        this.scelta.set(null);
        this.carica();
      });
    });
  }

  private carica(): void {
    this.gioco.calendario(this.stato.id).subscribe({
      next: (elenco) => this.giornate.set(elenco),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected vai(delta: number): void {
    const attuale = this.numero();
    if (attuale === null) {
      return;
    }
    this.scelta.set(Math.min(this.totale(), Math.max(1, attuale + delta)));
  }
}
