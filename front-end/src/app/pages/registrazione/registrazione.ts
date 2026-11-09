import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../core/auth.service';
import { messaggioErrore } from '../../core/errori';

@Component({
  selector: 'app-registrazione',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './registrazione.html',
  styleUrl: '../pagina-form.scss',
})
export class Registrazione {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  // Le stesse regole del back-end: così gli errori più comuni si vedono prima di inviare
  protected readonly form = inject(FormBuilder).nonNullable.group({
    username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50),
      Validators.pattern(/^[A-Za-z0-9_.-]*$/)]],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(8), Validators.maxLength(72)]],
  });
  protected readonly errore = signal<string | null>(null);
  protected readonly inCorso = signal(false);

  protected registra(): void {
    if (this.form.invalid || this.inCorso()) {
      return;
    }
    this.inCorso.set(true);
    this.errore.set(null);
    const { username, email, password } = this.form.getRawValue();
    this.auth.registra(username.trim(), email.trim(), password).subscribe({
      // Dopo la registrazione si entra subito
      next: () =>
        this.auth.login(username.trim(), password).subscribe({
          next: () => this.router.navigate(['/carriere']),
          error: () => this.router.navigate(['/login']),
        }),
      error: (e: unknown) => {
        this.errore.set(messaggioErrore(e));
        this.inCorso.set(false);
      },
    });
  }
}
