import { LineaDifensiva, Mentalita, Modulo, Pressing, Ritmo } from './modelli';

/** I moduli con le sigle dei loro 11 posti, nello stesso ordine del back-end (il primo è il portiere) */
export const MODULI: { valore: Modulo; nome: string; posti: string[] }[] = [
  { valore: 'M_4_3_3', nome: '4-3-3', posti: ['POR', 'TD', 'DC', 'DC', 'TS', 'MED', 'CEN', 'CEN', 'AD', 'ATT', 'AS'] },
  { valore: 'M_4_4_2', nome: '4-4-2', posti: ['POR', 'TD', 'DC', 'DC', 'TS', 'ED', 'CEN', 'CEN', 'ES', 'ATT', 'ATT'] },
  { valore: 'M_4_2_3_1', nome: '4-2-3-1', posti: ['POR', 'TD', 'DC', 'DC', 'TS', 'MED', 'MED', 'AD', 'TRQ', 'AS', 'ATT'] },
  { valore: 'M_3_5_2', nome: '3-5-2', posti: ['POR', 'DC', 'DC', 'DC', 'ED', 'CEN', 'MED', 'CEN', 'ES', 'ATT', 'ATT'] },
  { valore: 'M_3_4_3', nome: '3-4-3', posti: ['POR', 'DC', 'DC', 'DC', 'ED', 'CEN', 'CEN', 'ES', 'AD', 'ATT', 'AS'] },
  { valore: 'M_5_3_2', nome: '5-3-2', posti: ['POR', 'TD', 'DC', 'DC', 'DC', 'TS', 'CEN', 'MED', 'CEN', 'ATT', 'ATT'] },
];

export const MENTALITA: { valore: Mentalita; nome: string }[] = [
  { valore: 'MOLTO_DIFENSIVA', nome: 'Molto difensiva' },
  { valore: 'DIFENSIVA', nome: 'Difensiva' },
  { valore: 'EQUILIBRATA', nome: 'Equilibrata' },
  { valore: 'OFFENSIVA', nome: 'Offensiva' },
  { valore: 'MOLTO_OFFENSIVA', nome: 'Molto offensiva' },
];

export const PRESSING: { valore: Pressing; nome: string }[] = [
  { valore: 'BASSO', nome: 'Basso' },
  { valore: 'MEDIO', nome: 'Medio' },
  { valore: 'ALTO', nome: 'Alto' },
];

export const RITMO: { valore: Ritmo; nome: string }[] = [
  { valore: 'LENTO', nome: 'Lento' },
  { valore: 'NORMALE', nome: 'Normale' },
  { valore: 'VELOCE', nome: 'Veloce' },
];

export const LINEA_DIFENSIVA: { valore: LineaDifensiva; nome: string }[] = [
  { valore: 'BASSA', nome: 'Bassa' },
  { valore: 'MEDIA', nome: 'Media' },
  { valore: 'ALTA', nome: 'Alta' },
];
