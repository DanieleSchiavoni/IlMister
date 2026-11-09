import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { PartitaDettaglio, Prestazione } from '../../../core/modelli';
import { StatoCarriera } from '../stato-carriera';

@Component({
  selector: 'app-partita',
  imports: [DatePipe, RouterLink],
  templateUrl: './partita.html',
  styleUrl: './partita.scss',
})
export class Partita {
  protected readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);
  private readonly partitaId = Number(inject(ActivatedRoute).snapshot.paramMap.get('partitaId'));

  protected readonly partita = signal<PartitaDettaglio | null>(null);
  protected readonly errore = signal<string | null>(null);

  constructor() {
    this.gioco.partita(this.stato.id, this.partitaId).subscribe({
      next: (dettaglio) => this.partita.set(dettaglio),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  /** Le prestazioni di una squadra, dal voto più alto */
  protected prestazioni(p: PartitaDettaglio, squadraId: number): Prestazione[] {
    return p.prestazioni
      .filter((x) => x.squadraId === squadraId && x.minutiGiocati > 0)
      .sort((a, b) => (b.voto ?? 0) - (a.voto ?? 0));
  }

  protected squadre(p: PartitaDettaglio): { id: number; nome: string }[] {
    return [
      { id: p.squadraCasaId, nome: p.squadraCasa },
      { id: p.squadraTrasfertaId, nome: p.squadraTrasferta },
    ];
  }

  protected minuto(minuto: number, recupero: number): string {
    return recupero > 0 ? `${minuto}+${recupero}'` : `${minuto}'`;
  }

  protected eGol(tipo: string): boolean {
    return tipo === 'GOL' || tipo === 'RIGORE';
  }
}
