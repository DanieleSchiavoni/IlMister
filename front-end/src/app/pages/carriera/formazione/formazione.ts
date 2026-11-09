import { Component, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';

import { messaggioErrore } from '../../../core/errori';
import { GiocoService } from '../../../core/gioco.service';
import { Giocatore, LineaDifensiva, Mentalita, Modulo, Pressing, Ritmo } from '../../../core/modelli';
import { LINEA_DIFENSIVA, MENTALITA, MODULI, PRESSING, RITMO } from '../../../core/tattica';
import { StatoCarriera } from '../stato-carriera';

const MAX_PANCHINA = 7;

@Component({
  selector: 'app-formazione',
  templateUrl: './formazione.html',
  styleUrl: './formazione.scss',
})
export class Formazione {
  private readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly moduli = MODULI;
  protected readonly mentalita = MENTALITA;
  protected readonly pressing = PRESSING;
  protected readonly ritmo = RITMO;
  protected readonly lineaDifensiva = LINEA_DIFENSIVA;
  protected readonly maxPanchina = MAX_PANCHINA;

  protected readonly rosa = signal<Giocatore[]>([]);
  protected readonly caricata = signal(false);
  protected readonly errore = signal<string | null>(null);
  protected readonly salvata = signal(false);
  protected readonly inCorso = signal(false);

  protected readonly modulo = signal<Modulo>('M_4_3_3');
  protected readonly mentalitaScelta = signal<Mentalita>('EQUILIBRATA');
  protected readonly pressingScelto = signal<Pressing>('MEDIO');
  protected readonly ritmoScelto = signal<Ritmo>('NORMALE');
  protected readonly lineaScelta = signal<LineaDifensiva>('MEDIA');
  /** Un id per ognuno degli 11 posti del modulo (null = posto vuoto) */
  protected readonly titolari = signal<(number | null)[]>(Array(11).fill(null));
  protected readonly panchina = signal<number[]>([]);

  protected readonly posti = computed(() => MODULI.find((m) => m.valore === this.modulo())?.posti ?? []);
  private readonly titolariScelti = computed(() => this.titolari().filter((id): id is number => id !== null));
  protected readonly disponibiliPerPanchina = computed(() =>
    this.rosa().filter((g) => !this.titolariScelti().includes(g.id)),
  );

  /** Si può salvare con 11 titolari diversi; il resto lo controlla il server */
  protected readonly valida = computed(
    () => this.titolariScelti().length === 11 && new Set(this.titolariScelti()).size === 11,
  );

  constructor() {
    forkJoin({ rosa: this.gioco.rosa(this.stato.id), formazione: this.gioco.formazione(this.stato.id) }).subscribe({
      next: ({ rosa, formazione }) => {
        this.rosa.set([...rosa].sort((a, b) => a.cognome.localeCompare(b.cognome)));
        this.modulo.set(formazione.modulo);
        this.mentalitaScelta.set(formazione.mentalita);
        this.pressingScelto.set(formazione.pressing);
        this.ritmoScelto.set(formazione.ritmo);
        this.lineaScelta.set(formazione.lineaDifensiva);
        this.titolari.set(formazione.titolari.map((g) => g.id));
        this.panchina.set(formazione.panchina.map((g) => g.id));
        this.salvata.set(formazione.salvata);
        this.caricata.set(true);
      },
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected etichetta(g: Giocatore): string {
    const nota = g.infortunato ? ' (infortunato)' : g.squalificaGiornate > 0 ? ' (squalificato)' : '';
    return `${g.siglaPosizione} ${g.cognome} ${g.nome.charAt(0)}. · ${g.valutazione}${nota}`;
  }

  /** Un giocatore non si può scegliere se manca o se è già in un altro posto */
  protected nonSceglibile(g: Giocatore, posto: number): boolean {
    return !g.disponibile || this.titolari().some((id, i) => id === g.id && i !== posto);
  }

  protected cambiaModulo(evento: Event): void {
    this.modulo.set((evento.target as HTMLSelectElement).value as Modulo);
    this.modificato();
  }

  protected cambiaTitolare(posto: number, evento: Event): void {
    const testo = (evento.target as HTMLSelectElement).value;
    const id = testo ? Number(testo) : null;
    this.titolari.update((elenco) => elenco.map((x, i) => (i === posto ? id : x)));
    // Chi diventa titolare lascia la panchina
    if (id !== null) {
      this.panchina.update((elenco) => elenco.filter((x) => x !== id));
    }
    this.modificato();
  }

  protected inPanchina(id: number): boolean {
    return this.panchina().includes(id);
  }

  protected cambiaPanchina(g: Giocatore, evento: Event): void {
    const scelto = (evento.target as HTMLInputElement).checked;
    this.panchina.update((elenco) => (scelto ? [...elenco, g.id] : elenco.filter((x) => x !== g.id)));
    this.modificato();
  }

  protected cambiaIstruzione(campo: 'mentalita' | 'pressing' | 'ritmo' | 'linea', evento: Event): void {
    const valore = (evento.target as HTMLSelectElement).value;
    switch (campo) {
      case 'mentalita': this.mentalitaScelta.set(valore as Mentalita); break;
      case 'pressing': this.pressingScelto.set(valore as Pressing); break;
      case 'ritmo': this.ritmoScelto.set(valore as Ritmo); break;
      case 'linea': this.lineaScelta.set(valore as LineaDifensiva); break;
    }
    this.modificato();
  }

  private modificato(): void {
    this.salvata.set(false);
    this.errore.set(null);
  }

  protected salva(): void {
    if (!this.valida() || this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    this.gioco
      .salvaFormazione(this.stato.id, {
        modulo: this.modulo(),
        mentalita: this.mentalitaScelta(),
        pressing: this.pressingScelto(),
        ritmo: this.ritmoScelto(),
        lineaDifensiva: this.lineaScelta(),
        titolari: this.titolariScelti(),
        panchina: this.panchina(),
      })
      .subscribe({
        next: () => {
          this.salvata.set(true);
          this.inCorso.set(false);
        },
        error: (e: unknown) => {
          this.errore.set(messaggioErrore(e));
          this.inCorso.set(false);
        },
      });
  }
}
