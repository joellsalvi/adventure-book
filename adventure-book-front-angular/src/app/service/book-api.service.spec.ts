import {TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {BOOK_SUMMARIES, thePrisoner} from '../testing/book.fixtures';
import {BookApiService} from './book-api.service';

describe('BookApiService', () => {
  let service: BookApiService;
  let http: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(BookApiService);
    http = TestBed.inject(HttpTestingController);
  });

  afterEach(() => http.verify());

  it('lists books from GET /api/books', () => {
    let books: unknown;
    service.getBooks().subscribe(result => (books = result));

    const request = http.expectOne('http://localhost:8080/api/books');
    expect(request.request.method).toBe('GET');
    request.flush(BOOK_SUMMARIES);

    expect(books).toEqual(BOOK_SUMMARIES);
  });

  it('loads a book from GET /api/books/{id}', () => {
    let title: string | undefined;
    service.getBook(1).subscribe(book => (title = book.title));

    http.expectOne('http://localhost:8080/api/books/1').flush(thePrisoner());

    expect(title).toBe('The Prisoner');
  });
});
