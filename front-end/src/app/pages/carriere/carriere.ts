import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';

import { CarriereService } from '../../core/carriere.service';
import { messaggioErrore } from '../../core/errori';
import { Carriera } from '../../core/modelli';

@Component({
  selector: 'app-carriere',
  imports: [RouterLink, DatePipe],
  templateUrl: './carriere.html',
  styleUrl: './carriere.scss',
})
export class Carriere {
  private readonly servizio = inject(CarriereService);

  protected readonly carriere = signal<Carriera[] | null>(null);
  protected readonly errore = signal<string | null>(null);

  constructor() {
    this.carica();
  }

  private carica(): void {
    this.servizio.elenco().subscribe({
      next: (elenco) => this.carriere.set(elenco),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }

  protected elimina(carriera: Carriera): void {
    // Cancella tutto (squadre, giocatori, partite): meglio chiedere conferma
    if (!window.confirm(`Eliminare la carriera "${carriera.nome}"? L'operazione non si può annullare.`)) {
      return;
    }
    this.servizio.elimina(carriera.id).subscribe({
      next: () => this.carriere.update((elenco) => (elenco ?? []).filter((c) => c.id !== carriera.id)),
      error: (e: unknown) => this.errore.set(messaggioErrore(e)),
    });
  }
}
