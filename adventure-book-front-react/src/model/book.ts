export type SectionType = 'BEGIN' | 'NODE' | 'END';

/**
 * Known consequence types. The type stays open (any string) so a book using a
 * consequence the frontend does not know yet does not break the game.
 */
export type ConsequenceType =
  | 'LOSE_HEALTH'
  | 'GAIN_HEALTH'
  | 'GAIN_MAX_HEALTH'
  | 'POISON'
  | 'INSTANT_DEATH'
  | (string & {});

export interface BookSummary {
  id: number;
  title: string;
}

export interface Consequence {
  type: ConsequenceType;
  value: string;
  text?: string | null;
}

export interface Option {
  description: string;
  gotoId: number;
  consequence?: Consequence | null;
}

export interface Section {
  id: number;
  text: string;
  type: SectionType;
  options?: Option[] | null;
}

export interface Book {
  id: number;
  title: string;
  author: string;
  sections: Section[];
}

/** Error body returned by the API (see RestControllerHandler). */
export interface ApiErrorBody {
  code: string;
  message: string;
}
