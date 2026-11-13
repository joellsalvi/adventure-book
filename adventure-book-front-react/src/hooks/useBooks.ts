import { useEffect, useState } from 'react';
import { BookApi, toErrorMessage } from '../api/bookApi';
import { BookSummary } from '../model/book';

interface BooksState {
  books: BookSummary[];
  loading: boolean;
  error: string | null;
}

export function useBooks(api: BookApi): BooksState {
  const [state, setState] = useState<BooksState>({ books: [], loading: true, error: null });

  useEffect(() => {
    const controller = new AbortController();
    setState((current) => ({ ...current, loading: true, error: null }));

    api
      .getBooks(controller.signal)
      .then((books) => {
        if (!controller.signal.aborted) {
          setState({ books, loading: false, error: null });
        }
      })
      .catch((error: unknown) => {
        if (!controller.signal.aborted) {
          setState({ books: [], loading: false, error: toErrorMessage(error) });
        }
      });

    return () => controller.abort();
  }, [api]);

  return state;
}
