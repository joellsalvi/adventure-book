import {Component, input, output} from '@angular/core';
import {Option, Section} from '../../model/book.model';

@Component({
  selector: 'app-section-view',
  templateUrl: './section-view.component.html',
  styleUrl: './section-view.component.css'
})
export class SectionViewComponent {
  readonly section = input.required<Section>();

  readonly optionChosen = output<Option>();
}
