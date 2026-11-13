import {Component, input, output} from '@angular/core';
import {BookSummary} from '../../model/book.model';

@Component({
  selector: 'app-book-selector',
  templateUrl: './book-selector.component.html',
  styleUrl: './book-selector.component.css'
})
export class BookSelectorComponent {
  readonly books = input.required<BookSummary[]>();
  readonly selectedId = input<number | null>(null);
  readonly disabled = input(false);

  readonly bookSelected = output<number>();
}
