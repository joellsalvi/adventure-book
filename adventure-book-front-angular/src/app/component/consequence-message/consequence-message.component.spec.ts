import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ConsequenceEvent} from '../../game/game-engine';
import {ConsequenceMessageComponent} from './consequence-message.component';

describe('ConsequenceMessageComponent', () => {
  let fixture: ComponentFixture<ConsequenceMessageComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({declarations: [ConsequenceMessageComponent]}).compileComponents();
    fixture = TestBed.createComponent(ConsequenceMessageComponent);
  });

  function render(type: string, healthChange: number): HTMLElement {
    const event: ConsequenceEvent = {consequence: {type, value: '3', text: 'Something happened.'}, healthChange};
    fixture.componentRef.setInput('event', event);
    fixture.detectChanges();
    return fixture.nativeElement.querySelector('[role="status"]');
  }

  it('shows a health gain as positive', () => {
    const message = render('GAIN_HEALTH', 3);

    expect(message.textContent).toContain('+3');
    expect(message.classList).toContain('consequence--positive');
  });

  it('shows a health loss as negative', () => {
    const message = render('LOSE_HEALTH', -3);

    expect(message.textContent).toContain('-3');
    expect(message.classList).not.toContain('consequence--positive');
  });

  it('shows only the text when health did not change', () => {
    const message = render('POISON', 0);

    expect(message.textContent?.trim()).toBe('Something happened.');
    expect(message.classList).not.toContain('consequence--positive');
  });
});
