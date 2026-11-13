import { Book, Option } from '../model/book';
import { toErrorMessage } from '../api/bookApi';
import { chooseOption, GameState, startGame } from './gameEngine';

export interface GameStore {
  selectedBookId: number | null;
  /** Incremented on every book selection so the same book can be loaded again. */
  requestId: number;
  loading: boolean;
  game: GameState | null;
  error: string | null;
}

export type GameAction =
  | { type: 'bookRequested'; bookId: number }
  | { type: 'bookLoaded'; book: Book }
  | { type: 'bookFailed'; error: string }
  | { type: 'optionChosen'; option: Option }
  | { type: 'restarted' };

export const initialGameStore: GameStore = {
  selectedBookId: null,
  requestId: 0,
  loading: false,
  game: null,
  error: null,
};

function play(store: GameStore, step: () => GameState): GameStore {
  try {
    return { ...store, loading: false, game: step() };
  } catch (error) {
    return { ...store, loading: false, error: toErrorMessage(error) };
  }
}

/** UI state around the game. Game rules live in gameEngine.ts. */
export function gameReducer(store: GameStore, action: GameAction): GameStore {
  switch (action.type) {
    case 'bookRequested':
      return {
        selectedBookId: action.bookId,
        requestId: store.requestId + 1,
        loading: true,
        game: null,
        error: null,
      };
    case 'bookLoaded':
      return play(store, () => startGame(action.book));
    case 'bookFailed':
      return { ...store, loading: false, error: action.error };
    case 'optionChosen': {
      const { game } = store;
      return game ? play(store, () => chooseOption(game, action.option)) : store;
    }
    case 'restarted': {
      const { game } = store;
      return game ? play(store, () => startGame(game.book)) : store;
    }
    default:
      return store;
  }
}
