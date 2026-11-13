import {ComponentFixture, TestBed} from '@angular/core/testing';
import {provideHttpClient} from '@angular/common/http';
import {HttpTestingController, provideHttpClientTesting} from '@angular/common/http/testing';
import {NoopAnimationsModule} from '@angular/platform-browser/animations';
import {GAME_COMPONENTS, MATERIAL_MODULES} from '../../app.module';
import {BOOK_SUMMARIES, thePrisoner} from '../../testing/book.fixtures';
import {AppComponent} from './app.component';

describe('AppComponent', () => {
  let fixture: ComponentFixture<AppComponent>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: GAME_COMPONENTS,
      imports: [NoopAnimationsModule, ...MATERIAL_MODULES],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(AppComponent);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges(); // ngOnInit loads the books
  });

  afterEach(() => http.verify());

  const text = (): string => fixture.nativeElement.textContent;
  const health = (): string | null =>
    fixture.nativeElement.querySelector('[role="meter"]')?.getAttribute('aria-valuenow') ?? null;
  const buttonLabels = (): string[] =>
    Array.from<HTMLButtonElement>(fixture.nativeElement.querySelectorAll('button'))
      .map(button => button.textContent?.trim() ?? '');

  function click(label: string): void {
    const button = Array.from<HTMLButtonElement>(fixture.nativeElement.querySelectorAll('button'))
      .find(candidate => candidate.textContent?.trim() === label);
    if (!button) {
      throw new Error(`No button "${label}", found: ${buttonLabels().join(' | ')}`);
    }
    button.click();
    fixture.detectChanges();
  }

  function loadBooksAndSelectThePrisoner(): void {
    http.expectOne('http://localhost:8080/api/books').flush(BOOK_SUMMARIES);
    fixture.detectChanges();

    fixture.nativeElement.querySelector('.mat-mdc-select-trigger').click();
    fixture.detectChanges();
    const option = Array.from<HTMLElement>(document.querySelectorAll('mat-option'))
      .find(candidate => candidate.textContent?.includes('The Prisoner'));
    option!.click();
    fixture.detectChanges();

    http.expectOne('http://localhost:8080/api/books/1').flush(thePrisoner());
    fixture.detectChanges();
  }

  it('asks to select a book first', () => {
    http.expectOne('http://localhost:8080/api/books').flush(BOOK_SUMMARIES);
    fixture.detectChanges();

    expect(text()).toContain('Please select a book to start your adventure.');
  });

  it('shows an error when the API is unreachable', () => {
    http.expectOne('http://localhost:8080/api/books').error(new ProgressEvent('error'), {status: 0});
    fixture.detectChanges();

    expect(fixture.nativeElement.querySelector('[role="alert"]').textContent).toContain('unavailable');
  });

  it('plays the book until an ending is reached', () => {
    loadBooksAndSelectThePrisoner();

    expect(text()).toContain('You wake up in what seems to be a dark prison cell');
    expect(health()).toBe('10');

    click('You look under the bed');
    click('Try to scan the area with your hands');

    expect(text()).toContain('you cut yourself on a rusty nail');
    expect(text()).toContain('-6');
    expect(health()).toBe('4');

    click('Try to open the door with the key');

    expect(text()).toContain('The door opens. You are now free...');
    expect(text()).toContain('The End');

    click('Play again');

    expect(health()).toBe('10');
    expect(text()).toContain('You wake up in what seems to be a dark prison cell');
  });

  it('ends the game when health reaches zero and starts a new one', () => {
    loadBooksAndSelectThePrisoner();

    for (let loop = 0; loop < 4; loop++) {
      click('You try to open the door');
      click('Gather your thoughts');
    }

    expect(health()).toBe('0');
    expect(text()).toContain('You died');
    expect(buttonLabels()).not.toContain('Gather your thoughts');

    click('Start a new game');

    expect(health()).toBe('10');
    expect(text()).not.toContain('You died');
    expect(buttonLabels()).toContain('You try to open the door');
  });
});
