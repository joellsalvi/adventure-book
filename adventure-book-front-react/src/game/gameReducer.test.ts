import { thePrisoner } from '../testing/fixtures';
import { gameReducer, GameStore, initialGameStore } from './gameReducer';

describe('gameReducer', () => {
  const loaded = (): GameStore =>
    gameReducer(gameReducer(initialGameStore, { type: 'bookRequested', bookId: 1 }), {
      type: 'bookLoaded',
      book: thePrisoner(),
    });

  it('starts loading and clears the previous game when a book is requested', () => {
    const store = gameReducer(loaded(), { type: 'bookRequested', bookId: 2 });

    expect(store).toEqual({ selectedBookId: 2, requestId: 2, loading: true, game: null, error: null });
  });

  it('starts a new game when the book is loaded', () => {
    const store = loaded();

    expect(store.loading).toBe(false);
    expect(store.game?.section.type).toBe('BEGIN');
    expect(store.game?.player.health).toBe(10);
  });

  it('keeps the error when the book cannot be loaded', () => {
    const requested = gameReducer(initialGameStore, { type: 'bookRequested', bookId: 42 });

    const store = gameReducer(requested, { type: 'bookFailed', error: 'Book not found! ID: 42' });

    expect(store.loading).toBe(false);
    expect(store.game).toBeNull();
    expect(store.error).toBe('Book not found! ID: 42');
  });

  it('reports a broken book instead of crashing', () => {
    const book = thePrisoner();
    book.sections = book.sections.filter((section) => section.type !== 'BEGIN');

    const store = gameReducer(initialGameStore, { type: 'bookLoaded', book });

    expect(store.game).toBeNull();
    expect(store.error).toMatch(/no beginning section/);
  });

  it('restarts the current book with full health', () => {
    let store = loaded();
    store = gameReducer(store, { type: 'optionChosen', option: store.game!.section.options![0] });
    store = gameReducer(store, { type: 'optionChosen', option: store.game!.section.options![0] });
    expect(store.game?.player.health).toBe(7);

    store = gameReducer(store, { type: 'restarted' });

    expect(store.game?.player.health).toBe(10);
    expect(store.game?.section.id).toBe(1);
    expect(store.game?.lastEvent).toBeNull();
  });

  it('ignores choices when no game is running', () => {
    const option = thePrisoner().sections[0].options![0];

    expect(gameReducer(initialGameStore, { type: 'optionChosen', option })).toBe(initialGameStore);
    expect(gameReducer(initialGameStore, { type: 'restarted' })).toBe(initialGameStore);
  });
});
