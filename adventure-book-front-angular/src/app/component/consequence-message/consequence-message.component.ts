import {Component, computed, input} from '@angular/core';
import {ConsequenceEvent} from '../../game/game-engine';

@Component({
  selector: 'app-consequence-message',
  templateUrl: './consequence-message.component.html',
  styleUrl: './consequence-message.component.css'
})
export class ConsequenceMessageComponent {
  readonly event = input.required<ConsequenceEvent>();

  /** "-6", "+2", or null when health did not change. */
  protected readonly healthChange = computed(() => {
    const change = this.event().healthChange;
    if (change === 0) {
      return null;
    }
    return change > 0 ? `+${change}` : `${change}`;
  });

  /** Health went up (GAIN_HEALTH, GAIN_MAX_HEALTH): shown in green. */
  protected readonly positive = computed(() => this.event().healthChange > 0);
}
