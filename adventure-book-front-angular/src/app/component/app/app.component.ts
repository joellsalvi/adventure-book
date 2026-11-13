import {Component, inject, OnInit} from '@angular/core';
import {GameService} from '../../service/game.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit {
  title = 'Adventure Book';

  protected readonly game = inject(GameService);

  ngOnInit(): void {
    this.game.loadBooks();
  }
}
