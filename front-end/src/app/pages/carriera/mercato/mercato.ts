import { CurrencyPipe } from '@angular/common';
import { Component, effect, inject, signal, untracked } from '@angular/core';
import { Observable } from 'rxjs';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { EsitoTrattativa, Giocatore, GiocatoreMercato, RuoloGiocatore } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

type Vista = 'cerca' | 'svincolati' | 'miei';

@Component({
  selector: 'app-mercato',
  imports: [CurrencyPipe],
  templateUrl: './mercato.html',
  styleUrl: './mercato.scss',
})
export class Mercato {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly ruoli: { valore: RuoloGiocatore | ''; nome: string }[] = [
    { valore: '', nome: 'Tutti i ruoli' },
    { valore: 'PORTIERE', nome: 'Portieri' },
    { valore: 'DIFENSORE', nome: 'Difensori' },
    { valore: 'CENTROCAMPISTA', nome: 'Centrocampisti' },
    { valore: 'ATTACCANTE', nome: 'Attaccanti' },
  ];

  protected readonly vista = signal<Vista>('cerca');
  protected readonly ruolo = signal<RuoloGiocatore | ''>('');
  protected readonly nome = signal('');
  protected readonly valoreMassimo = signal<number | null>(null);

  protected readonly trovati = signal<GiocatoreMercato[] | null>(null);
  protected readonly miei = signal<Giocatore[] | null>(null);
  protected readonly budget = signal<number | null>(null);
  protected readonly esito = signal<EsitoTrattativa | null>(null);
  protected readonly errore = signal<string | null>(null);
  protected readonly inCorso = signal(false);
  /** Le offerte che l'utente sta scrivendo, per giocatore; se manca si propone il valore di mercato */
  private readonly offerte = signal<Record<number, number>>({});

  constructor() {
    effect(() => {
      this.stato.versione();
      untracked(() => this.carica());
    });
  }

  protected scegliVista(vista: Vista): void {
    this.vista.set(vista);
    this.esito.set(null);
    this.errore.set(null);
    this.carica();
  }

  protected cerca(): void {
    this.carica();
  }

  protected cambiaRuolo(evento: Event): void {
    this.ruolo.set((evento.target as HTMLSelectElement).value as RuoloGiocatore | '');
    this.carica();
  }

  protected cambiaNome(evento: Event): void {
    this.nome.set((evento.target as HTMLInputElement).value);
  }

  protected cambiaValoreMassimo(evento: Event): void {
    const testo = (evento.target as HTMLInputElement).value;
    this.valoreMassimo.set(testo === '' ? null : Number(testo));
  }

  /** Rilegge l'elenco della vista attuale e il budget */
  private carica(): void {
    this.errore.set(null);
    this.gioco.finanze(this.stato.id).subscribe({
      next: (finanze) => this.budget.set(finanze.budget),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
    switch (this.vista()) {
      case 'cerca':
        this.gioco.cercaGiocatori(this.stato.id, this.ruolo(), this.nome(), this.valoreMassimo()).subscribe({
          next: (elenco) => this.trovati.set(elenco),
          error: (e: unknown) => this.errore.set(messaggioErrore(e)),
        });
        break;
      case 'svincolati':
        this.gioco.svincolati(this.stato.id).subscribe({
          next: (elenco) => this.trovati.set(elenco),
          error: (e: unknown) => this.errore.set(messaggioErrore(e)),
        });
        break;
      case 'miei':
        this.gioco.rosa(this.stato.id).subscribe({
          next: (rosa) => this.miei.set([...rosa].sort((a, b) => b.valutazione - a.valutazione)),
          error: (e: unknown) => this.errore.set(messaggioErrore(e)),
        });
        break;
    }
  }

  protected offerta(g: GiocatoreMercato): number {
    return this.offerte()[g.id] ?? g.valoreMercato;
  }

  protected cambiaOfferta(g: GiocatoreMercato, evento: Event): void {
    const valore = Number((evento.target as HTMLInputElement).value);
    this.offerte.update((o) => ({ ...o, [g.id]: valore }));
  }

  protected offri(g: GiocatoreMercato): void {
    this.esegui(this.gioco.offri(this.stato.id, g.id, this.offerta(g)));
  }

  protected ingaggia(g: GiocatoreMercato): void {
    this.esegui(this.gioco.ingaggia(this.stato.id, g.id));
  }

  protected vendi(g: Giocatore): void {
    if (window.confirm(`Vendere ${g.nome} ${g.cognome}? Incasserai circa l'85% del suo valore di mercato.`)) {
      this.esegui(this.gioco.vendi(this.stato.id, g.id));
    }
  }

  protected svincola(g: Giocatore): void {
    if (window.confirm(`Svincolare ${g.nome} ${g.cognome}? Non incasserai nulla.`)) {
      this.esegui(this.gioco.svincola(this.stato.id, g.id));
    }
  }

  private esegui(chiamata: Observable<EsitoTrattativa>): void {
    if (this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    chiamata.subscribe({
      next: (esito) => {
        this.esito.set(esito);
        this.inCorso.set(false);
        // Se la trattativa è andata a buon fine cambiano rosa, budget e finanze: si avvisano le schede
        if (esito.conclusa) {
          this.stato.ricarica();
        }
      },
      error: (e: unknown) => {
        this.errore.set(messaggioErrore(e));
        this.inCorso.set(false);
      },
    });
  }
}
