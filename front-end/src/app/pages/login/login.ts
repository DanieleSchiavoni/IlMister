import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { messaggioErrore } from '../../core/errori';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: '../pagina-form.scss',
})
export class Login {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly form = inject(FormBuilder).nonNullable.group({
    identificativo: ['', Validators.required],
    password: ['', Validators.required],
  });
  protected readonly errore = signal<string | null>(null);
  protected readonly inCorso = signal(false);

  protected accedi(): void {
    if (this.form.invalid || this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    const { identificativo, password } = this.form.getRawValue();
    this.auth.login(identificativo.trim(), password).subscribe({
      next: () => this.router.navigate(['/carriere']),
      error: (e: unknown) => {
        this.errore.set(messaggioErrore(e));
        this.inCorso.set(false);
      },
    });
  }
}
