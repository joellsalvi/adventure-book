import {Component, input, output} from '@angular/core';
import {Section} from '../../model/book.model';
import {GameStatus} from '../../game/game-engine';

/** Shown once the game is over: the player died or reached an ending. */
@Component({
  selector: 'app-game-status',
  templateUrl: './game-status.component.html',
  styleUrl: './game-status.component.css'
})
export class GameStatusComponent {
  readonly status = input.required<GameStatus>();
  readonly section = input.required<Section>();

  readonly restart = output<void>();
}
