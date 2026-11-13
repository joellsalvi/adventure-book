import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BOOK_SUMMARIES, thePrisoner} from '../testing/book.fixtures';
import {GameService} from './game.service';

describe('GameService', () => {
  let service: GameService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(GameService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('loads the list of books', () => {
    service.loadBooks();
    expect(service.booksLoading()).toBeTrue();

    http.expectOne('http://localhost:8080/api/books').flush(BOOK_SUMMARIES);

    expect(service.books()).toEqual(BOOK_SUMMARIES);
    expect(service.booksLoading()).toBeFalse();
  });

  it('reports an unreachable API', () => {
    service.loadBooks();

    http.expectOne('http://localhost:8080/api/books').error(new ProgressEvent('error'), {status: 0});

    expect(service.error()).toContain('unavailable');
    expect(service.booksLoading()).toBeFalse();
  });

  it('starts a new game when a book is selected', () => {
    service.selectBook(1);
    expect(service.bookLoading()).toBeTrue();

    http.expectOne('http://localhost:8080/api/books/1').flush(thePrisoner());

    expect(service.bookLoading()).toBeFalse();
    expect(service.selectedBookId()).toBe(1);
    expect(service.state()?.section.type).toBe('BEGIN');
    expect(service.state()?.player.health).toBe(10);
  });

  it('shows the API error message when a book is not found', () => {
    service.selectBook(42);

    http.expectOne('http://localhost:8080/api/books/42').flush(
      {code: '404 NOT_FOUND', message: 'Book not found! ID: 42'},
      {status: 404, statusText: 'Not Found'},
    );

    expect(service.state()).toBeNull();
    expect(service.error()).toBe('Book not found! ID: 42');
  });

  it('ignores the response of a book that is no longer selected', () => {
    service.selectBook(1);
    service.selectBook(2);

    const first = http.expectOne('http://localhost:8080/api/books/1');
    expect(first.cancelled).toBeTrue();
    http.expectOne('http://localhost:8080/api/books/2').flush({...thePrisoner(), id: 2, title: 'Second book'});

    expect(service.state()?.book.title).toBe('Second book');
  });

  it('restarts the current book with full health', () => {
    service.selectBook(1);
    http.expectOne('http://localhost:8080/api/books/1').flush(thePrisoner());
    service.choose(service.state()!.section.options![0]);
    service.choose(service.state()!.section.options![0]);
    expect(service.state()?.player.health).toBe(7);

    service.restart();

    expect(service.state()?.player.health).toBe(10);
    expect(service.state()?.section.id).toBe(1);
    expect(service.state()?.lastEvent).toBeNull();
  });
});
