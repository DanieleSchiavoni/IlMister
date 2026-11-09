# Il Mister - front-end (Angular)

## Avvio
```
npm install
npm start          # http://localhost:4200
```
Il back-end deve girare su `http://localhost:8082` (vedi `src/environments/environment.ts`).
Il CORS del back-end consente già `http://localhost:4200`.

## Struttura
- `src/styles.scss`: tema scuro globale (variabili CSS, pulsanti, schede, tabelle)
- `src/app/core`: sessione (JWT + refresh), interceptor, guard, servizi API, modelli
- `src/app/pages`: login, registrazione, elenco carriere, nuova carriera
