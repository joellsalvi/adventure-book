import { BOOK_SUMMARIES, thePrisoner } from '../testing/fixtures';
import { bookApi } from './bookApi';

const jsonResponse = (status: number, body: unknown) =>
  ({ ok: status >= 200 && status < 300, status, json: async () => body }) as Response;

describe('bookApi', () => {
  const fetchMock = jest.fn();

  beforeEach(() => {
    fetchMock.mockReset();
    global.fetch = fetchMock;
  });

  it('lists books from GET /api/books', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, BOOK_SUMMARIES));

    await expect(bookApi.getBooks()).resolves.toEqual(BOOK_SUMMARIES);
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/books', expect.anything());
  });

  it('loads a book from GET /api/books/{id}', async () => {
    fetchMock.mockResolvedValue(jsonResponse(200, thePrisoner()));

    const book = await bookApi.getBook(1);

    expect(book.title).toBe('The Prisoner');
    expect(fetchMock).toHaveBeenCalledWith('http://localhost:8080/api/books/1', expect.anything());
  });

  it('uses the API error message', async () => {
    fetchMock.mockResolvedValue(jsonResponse(404, { code: '404 NOT_FOUND', message: 'Book not found! ID: 42' }));

    await expect(bookApi.getBook(42)).rejects.toThrow('Book not found! ID: 42');
  });

  it('reports an unreachable API', async () => {
    fetchMock.mockRejectedValue(new TypeError('Failed to fetch'));

    await expect(bookApi.getBooks()).rejects.toThrow(/unavailable/);
  });

  it('reports a failing server', async () => {
    fetchMock.mockResolvedValue({ ok: false, status: 500, json: async () => Promise.reject(new SyntaxError()) });

    await expect(bookApi.getBooks()).rejects.toThrow(/unavailable \(HTTP 500\)/);
  });
});
