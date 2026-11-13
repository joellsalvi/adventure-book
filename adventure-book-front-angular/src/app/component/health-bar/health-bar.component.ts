import {Component, computed, input} from '@angular/core';

@Component({
  selector: 'app-health-bar',
  templateUrl: './health-bar.component.html',
  styleUrl: './health-bar.component.css'
})
export class HealthBarComponent {
  readonly health = input.required<number>();
  readonly maxHealth = input.required<number>();

  protected readonly percent = computed(() =>
    Math.max(0, Math.min(100, (this.health() / this.maxHealth()) * 100)));

  protected readonly level = computed(() => {
    const percent = this.percent();
    return percent > 60 ? 'high' : percent > 30 ? 'medium' : 'low';
  });
}
