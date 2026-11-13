import {Book, Consequence, Option, Section} from '../model/book.model';
import {applyConsequence, ConsequenceHandlers, CONSEQUENCE_HANDLERS, PlayerStats} from './consequence-handlers';

export const INITIAL_HEALTH = 10;

export type GameStatus = 'PLAYING' | 'WON' | 'DEAD';

/** Feedback about the consequence triggered by the last choice. */
export interface ConsequenceEvent {
  consequence: Consequence;
  healthChange: number;
}

export interface GameState {
  book: Book;
  section: Section;
  player: PlayerStats;
  status: GameStatus;
  lastEvent: ConsequenceEvent | null;
}

export function findSection(book: Book, id: number): Section {
  // The API sends numeric ids, but raw book files sometimes use strings ("500").
  const section = book.sections.find(candidate => Number(candidate.id) === Number(id));
  if (!section) {
    throw new Error(`Section ${id} does not exist in "${book.title}".`);
  }
  return section;
}

export function startGame(book: Book): GameState {
  const begin = book.sections.find(section => section.type === 'BEGIN');
  if (!begin) {
    throw new Error(`"${book.title}" has no beginning section.`);
  }
  return {
    book,
    section: begin,
    player: {health: INITIAL_HEALTH, maxHealth: INITIAL_HEALTH},
    status: 'PLAYING',
    lastEvent: null,
  };
}

/**
 * Applies the option's consequence first, then moves to the next section.
 * The player dies as soon as health reaches zero, whatever the destination.
 */
export function chooseOption(
  state: GameState,
  option: Option,
  handlers: ConsequenceHandlers = CONSEQUENCE_HANDLERS,
): GameState {
  if (state.status !== 'PLAYING') {
    return state;
  }

  let player = state.player;
  let lastEvent: ConsequenceEvent | null = null;

  if (option.consequence) {
    const updated = applyConsequence(player, option.consequence, handlers);
    lastEvent = {consequence: option.consequence, healthChange: updated.health - player.health};
    player = updated;
  }

  if (player.health <= 0) {
    return {...state, player: {...player, health: 0}, status: 'DEAD', lastEvent};
  }

  const next = findSection(state.book, option.gotoId);
  return {
    ...state,
    section: next,
    player,
    status: next.type === 'END' ? 'WON' : 'PLAYING',
    lastEvent,
  };
}
