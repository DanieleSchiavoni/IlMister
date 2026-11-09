import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { CarriereService } from '../../core/carriere.service';
import { messaggioErrore } from '../../core/errori';
import { AnteprimaCampionato } from '../../core/modelli';

@Component({
  selector: 'app-nuova-carriera',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './nuova-carriera.html',
  styleUrl: './nuova-carriera.scss',
})
export class NuovaCarriera {
  private readonly servizio = inject(CarriereService);
  private readonly router = inject(Router);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    nome: ['', [Validators.required, Validators.maxLength(80)]],
    nomeAllenatore: ['', [Validators.required, Validators.maxLength(80)]],
  });

  protected readonly seme = signal(Math.floor(Math.random() * 1_000_000) + 1);
  protected readonly anteprima = signal<AnteprimaCampionato | null>(null);
  protected readonly squadraScelta = signal<string | null>(null);
  protected readonly errore = signal<string | null>(null);
  protected readonly inCorso = signal(false);

  constructor() {
    this.caricaAnteprima();
  }

  /** Un altro seme = un altro campionato, con squadre e giocatori diversi */
  protected rigenera(): void {
    this.seme.set(Math.floor(Math.random() * 1_000_000) + 1);
    this.squadraScelta.set(null);
    this.caricaAnteprima();
  }

  private caricaAnteprima(): void {
    this.anteprima.set(null);
    this.servizio.anteprima(this.seme()).subscribe({
      next: (risposta) => this.anteprima.set(risposta),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected inizia(): void {
    const squadra = this.squadraScelta();
    if (this.form.invalid || !squadra || this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    const { nome, nomeAllenatore } = this.form.getRawValue();
    this.servizio
      .crea({ nome: nome.trim(), nomeAllenatore: nomeAllenatore.trim(), nomeSquadra: squadra, seme: this.seme() })
      .subscribe({
        next: (creata) => this.router.navigate(['/carriere', creata.id]),
        error: (e: unknown) => {
          this.errore.set(messaggioErrore(e));
          this.inCorso.set(false);
        },
      });
  }
}
