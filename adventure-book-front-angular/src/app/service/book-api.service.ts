import {inject, Injectable, InjectionToken} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {Book, BookSummary} from '../model/book.model';
import {environment} from '../../environments/environment';

/** Base URL of the Adventure Book API (see environments/environment.ts). */
export const API_BASE_URL = new InjectionToken<string>('API_BASE_URL', {
  providedIn: 'root',
  factory: () => environment.apiBaseUrl,
});

@Injectable({providedIn: 'root'})
export class BookApiService {

  private readonly http = inject(HttpClient);
  private readonly booksUrl = `${inject(API_BASE_URL)}/books`;

  /** Lists the valid books only (the API filters out invalid ones). */
  getBooks(): Observable<BookSummary[]> {
    return this.http.get<BookSummary[]>(this.booksUrl);
  }

  getBook(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.booksUrl}/${id}`);
  }
}
