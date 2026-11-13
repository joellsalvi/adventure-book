import { ApiErrorBody, Book, BookSummary } from '../model/book';

/**
 * Adventure Book API, called directly (no dev proxy). The API must allow this
 * app's origin in its CORS config (cors.allowed-origins in application.yaml).
 * Override at build time with REACT_APP_API_BASE_URL.
 */
const API_BASE_URL = process.env.REACT_APP_API_BASE_URL ?? 'http://localhost:8080/api';

const unavailable = (status: number) =>
  `The Adventure Book API is unavailable (HTTP ${status}). Make sure it is running on http://localhost:8080.`;

const isAbort = (error: unknown) => error instanceof DOMException && error.name === 'AbortError';

async function getJson<T>(path: string, signal?: AbortSignal): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${API_BASE_URL}${path}`, { signal, headers: { Accept: 'application/json' } });
  } catch (error) {
    if (isAbort(error)) {
      throw error;
    }
    throw new Error(unavailable(0));
  }

  if (!response.ok) {
    const body: Partial<ApiErrorBody> | null = await response.json().catch(() => null);
    if (body?.message) {
      throw new Error(body.message);
    }
    throw new Error(response.status >= 500 ? unavailable(response.status) : `Request failed (HTTP ${response.status}).`);
  }
  return (await response.json()) as T;
}

export interface BookApi {
  /** Lists the valid books only (the API filters out invalid ones). */
  getBooks(signal?: AbortSignal): Promise<BookSummary[]>;
  getBook(id: number, signal?: AbortSignal): Promise<Book>;
}

export const bookApi: BookApi = {
  getBooks: (signal) => getJson<BookSummary[]>('/books', signal),
  getBook: (id, signal) => getJson<Book>(`/books/${id}`, signal),
};

export function toErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : 'Unexpected error.';
}
