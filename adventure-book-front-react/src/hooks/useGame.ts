import { useCallback, useEffect, useReducer } from 'react';
import { BookApi, toErrorMessage } from '../api/bookApi';
import { Option } from '../model/book';
import { gameReducer, GameStore, initialGameStore } from '../game/gameReducer';

export interface UseGame extends GameStore {
  selectBook(bookId: number): void;
  choose(option: Option): void;
  restart(): void;
}

export function useGame(api: BookApi): UseGame {
  const [store, dispatch] = useReducer(gameReducer, initialGameStore);
  const { selectedBookId, requestId } = store;

  useEffect(() => {
    if (selectedBookId === null) {
      return undefined;
    }
    // Aborting on cleanup drops the response of a book that is no longer selected.
    const controller = new AbortController();
    api
      .getBook(selectedBookId, controller.signal)
      .then((book) => {
        if (!controller.signal.aborted) {
          dispatch({ type: 'bookLoaded', book });
        }
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          dispatch({ type: 'bookFailed', error: toErrorMessage(error) });
        }
      });
    return () => controller.abort();
  }, [api, selectedBookId, requestId]);

  const selectBook = useCallback((bookId: number) => dispatch({ type: 'bookRequested', bookId }), []);
  const choose = useCallback((option: Option) => dispatch({ type: 'optionChosen', option }), []);
  const restart = useCallback(() => dispatch({ type: 'restarted' }), []);

  return { ...store, selectBook, choose, restart };
}
