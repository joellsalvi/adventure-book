import {inject, Injectable, signal} from '@angular/core';
import {takeUntilDestroyed} from '@angular/core/rxjs-interop';
import {catchError, map, of, Subject, switchMap} from 'rxjs';
import {Book, BookSummary, Option} from '../model/book.model';
import {chooseOption, GameState, startGame} from '../game/game-engine';
import {toErrorMessage} from '../util/error-message';
import {BookApiService} from './book-api.service';

type BookResult = { book: Book } | { error: string };

/** Holds the game state for the UI. Game rules live in game-engine.ts. */
@Injectable({providedIn: 'root'})
export class GameService {

  private readonly api = inject(BookApiService);
  private readonly bookRequests = new Subject<number>();

  readonly books = signal<BookSummary[]>([]);
  readonly booksLoading = signal(false);
  readonly selectedBookId = signal<number | null>(null);
  readonly bookLoading = signal(false);
  readonly state = signal<GameState | null>(null);
  readonly error = signal<string | null>(null);

  constructor() {
    // switchMap drops the response of a book that is no longer selected.
    this.bookRequests.pipe(
      switchMap(id => this.api.getBook(id).pipe(
        map((book): BookResult => ({book})),
        catchError((error: unknown) => of<BookResult>({error: toErrorMessage(error)})),
      )),
      takeUntilDestroyed(),
    ).subscribe(result => {
      this.bookLoading.set(false);
      if ('error' in result) {
        this.error.set(result.error);
      } else {
        this.update(() => startGame(result.book));
      }
    });
  }

  loadBooks(): void {
    this.booksLoading.set(true);
    this.error.set(null);
    this.api.getBooks().subscribe({
      next: books => {
        this.books.set(books);
        this.booksLoading.set(false);
      },
      error: (error: unknown) => {
        this.error.set(toErrorMessage(error));
        this.booksLoading.set(false);
      },
    });
  }

  selectBook(id: number): void {
    this.selectedBookId.set(id);
    this.state.set(null);
    this.error.set(null);
    this.bookLoading.set(true);
    this.bookRequests.next(id);
  }

  choose(option: Option): void {
    const current = this.state();
    if (current) {
      this.update(() => chooseOption(current, option));
    }
  }

  /** Starts the current book again with full health. */
  restart(): void {
    const current = this.state();
    if (current) {
      this.update(() => startGame(current.book));
    }
  }

  private update(step: () => GameState): void {
    try {
      this.state.set(step());
    } catch (error) {
      this.error.set(toErrorMessage(error));
    }
  }
}
