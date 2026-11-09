import { Component, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { forkJoin } from 'rxjs';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import {
  LineaDifensiva, Mentalita, Pressing, PrimoTempo, Ritmo, SituazioneGiocatore, Sostituzione,
} from '../../../core/modelli';
import { LINEA_DIFENSIVA, MENTALITA, PRESSING, RITMO } from '../../../core/tattica';
import { StatoCarriera } from '../stato-carriera';

const MAX_CAMBI = 5;

/** L'intervallo della partita dell'utente: si vede il primo tempo e si decide come giocare il secondo */
@Component({
  selector: 'app-intervallo',
  templateUrl: './intervallo.html',
  styleUrl: './intervallo.scss',
})
export class Intervallo {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);
  private readonly router = inject(Router);

  protected readonly mentalita = MENTALITA;
  protected readonly pressing = PRESSING;
  protected readonly ritmo = RITMO;
  protected readonly lineaDifensiva = LINEA_DIFENSIVA;
  protected readonly maxCambi = MAX_CAMBI;

  protected readonly tempo = signal<PrimoTempo | null>(null);
  protected readonly errore = signal<string | null>(null);
  protected readonly inCorso = signal(false);

  protected readonly mentalitaScelta = signal<Mentalita>('EQUILIBRATA');
  protected readonly pressingScelto = signal<Pressing>('MEDIO');
  protected readonly ritmoScelto = signal<Ritmo>('NORMALE');
  protected readonly lineaScelta = signal<LineaDifensiva>('MEDIA');
  /** I cambi in preparazione; un id a 0 significa "non ancora scelto" */
  protected readonly cambi = signal<{ esce: number; entra: number }[]>([]);

  protected readonly giocatori = computed(() => this.tempo()?.giocatoriUtente ?? []);
  protected readonly inCampo = computed(() => this.giocatori().filter((g) => g.inCampo && !g.espulso));
  protected readonly inPanchina = computed(() =>
    this.giocatori().filter((g) => !g.inCampo && !g.espulso && !g.infortunato),
  );
  protected readonly cambiCompleti = computed(() => this.cambi().filter((c) => c.esce && c.entra));
  protected readonly cambiValidi = computed(() => this.cambi().every((c) => c.esce && c.entra));
  protected readonly puoAggiungere = computed(
    () => this.cambi().length < MAX_CAMBI && this.cambi().length < Math.min(this.inCampo().length, this.inPanchina().length),
  );

  protected readonly utenteInCasa = computed(() => this.tempo()?.utenteInCasa ?? true);

  constructor() {
    // Le istruzioni di partenza sono quelle della formazione dell'utente
    forkJoin({ tempo: this.gioco.primoTempo(this.stato.id), formazione: this.gioco.formazione(this.stato.id) }).subscribe({
      next: ({ tempo, formazione }) => {
        this.tempo.set(tempo);
        this.mentalitaScelta.set(formazione.mentalita);
        this.pressingScelto.set(formazione.pressing);
        this.ritmoScelto.set(formazione.ritmo);
        this.lineaScelta.set(formazione.lineaDifensiva);
      },
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected etichetta(g: SituazioneGiocatore): string {
    const note = [g.ammonito ? 'ammonito' : '', g.infortunato ? 'infortunato' : '', `energia ${g.energia}`]
      .filter((x) => x)
      .join(', ');
    return `${g.siglaPosizione} ${g.cognome} (${note})`;
  }

  protected giaScelto(id: number, indice: number, campo: 'esce' | 'entra'): boolean {
    return this.cambi().some((c, i) => i !== indice && c[campo] === id);
  }

  protected aggiungiCambio(): void {
    this.cambi.update((elenco) => [...elenco, { esce: 0, entra: 0 }]);
  }

  protected rimuoviCambio(indice: number): void {
    this.cambi.update((elenco) => elenco.filter((_, i) => i !== indice));
  }

  protected scegliCambio(indice: number, campo: 'esce' | 'entra', evento: Event): void {
    const id = Number((evento.target as HTMLSelectElement).value);
    this.cambi.update((elenco) => elenco.map((c, i) => (i === indice ? { ...c, [campo]: id } : c)));
  }

  protected cambiaIstruzione(campo: 'mentalita' | 'pressing' | 'ritmo' | 'linea', evento: Event): void {
    const valore = (evento.target as HTMLSelectElement).value;
    switch (campo) {
      case 'mentalita': this.mentalitaScelta.set(valore as Mentalita); break;
      case 'pressing': this.pressingScelto.set(valore as Pressing); break;
      case 'ritmo': this.ritmoScelto.set(valore as Ritmo); break;
      case 'linea': this.lineaScelta.set(valore as LineaDifensiva); break;
    }
  }

  protected gioca(): void {
    if (this.inCorso() || !this.cambiValidi()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    const sostituzioni: Sostituzione[] = this.cambiCompleti().map((c) => ({ esceId: c.esce, entraId: c.entra }));
    this.gioco
      .concludi(this.stato.id, {
        istruzioni: {
          mentalita: this.mentalitaScelta(),
          pressing: this.pressingScelto(),
          ritmo: this.ritmoScelto(),
          lineaDifensiva: this.lineaScelta(),
        },
        sostituzioni,
      })
      .subscribe({
        next: (giornata) => {
          this.stato.ultima.set(giornata);
          this.stato.ricarica();
          this.router.navigate(['/carriere', this.stato.id, 'classifica']);
        },
        error: (e: unknown) => {
          this.errore.set(messaggioErrore(e));
          this.inCorso.set(false);
        },
      });
  }
}
