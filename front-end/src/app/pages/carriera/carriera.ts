import { DatePipe } from '@angular/common';
import { Component, inject, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';

import { messaggioErrore } from '../../core/errori';
import { GiocoService } from '../../core/gioco.service';
import { StatoCarriera } from './stato-carriera';

@Component({
  selector: 'app-carriera',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, DatePipe],
  providers: [StatoCarriera],
  templateUrl: './carriera.html',
  styleUrl: './carriera.scss',
})
export class CarrieraPagina {
  protected readonly stato = inject(StatoCarriera);
  private readonly gioco = inject(GiocoService);

  protected readonly inCorso = signal(false);
  protected readonly erroreGiocata = signal<string | null>(null);

  protected gioca(): void {
    if (this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.erroreGiocata.set(null);
    this.gioco.avanza(this.stato.id).subscribe({
      next: (giornata) => {
        this.stato.ultima.set(giornata);
        this.inCorso.set(false);
        this.stato.ricarica();
      },
      error: (e: unknown) => {
        this.erroreGiocata.set(messaggioErrore(e));
        this.inCorso.set(false);
      },
    });
  }

  protected chiudiRisultati(): void {
    this.stato.ultima.set(null);
  }
}
