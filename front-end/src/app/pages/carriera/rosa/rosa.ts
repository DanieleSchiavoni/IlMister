import { Component, effect, inject, signal, untracked } from '@angular/core';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { Giocatore } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-rosa',
  templateUrl: './rosa.html',
  styleUrl: './rosa.scss',
})
export class Rosa {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly giocatori = signal<Giocatore[] | null>(null);
  protected readonly errore = signal<string | null>(null);

  constructor() {
    // Dopo ogni giornata cambiano energia, forma, morale e infortuni
    effect(() => {
      this.stato.versione();
      untracked(() => this.carica());
    });
  }

  private carica(): void {
    this.gioco.rosa(this.stato.id).subscribe({
      // I più forti per primi
      next: (elenco) => this.giocatori.set([...elenco].sort((a, b) => b.valutazione - a.valutazione)),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  /** Una frase breve per chi non è disponibile */
  protected descrizioneStato(g: Giocatore): string {
    if (g.infortunato) {
      return g.tipoInfortunio ? `Infortunio: ${g.tipoInfortunio}` : 'Infortunato';
    }
    if (g.squalificaGiornate > 0) {
      return `Squalificato (${g.squalificaGiornate})`;
    }
    return 'Disponibile';
  }
}
