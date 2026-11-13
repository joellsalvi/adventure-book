import React from 'react';
import { fireEvent, render, screen } from '@testing-library/react';
import App from './App';
import { BookApi } from './api/bookApi';
import { BOOK_SUMMARIES, thePrisoner } from './testing/fixtures';

function fakeApi(overrides: Partial<BookApi> = {}): BookApi {
  return {
    getBooks: jest.fn().mockResolvedValue(BOOK_SUMMARIES),
    getBook: jest.fn().mockImplementation(async () => thePrisoner()),
    ...overrides,
  };
}

const health = () => screen.getByLabelText('Health points').getAttribute('aria-valuenow');
const click = (name: string) => fireEvent.click(screen.getByRole('button', { name }));

async function selectThePrisoner() {
  const dropdown = await screen.findByRole('combobox');
  await screen.findByText('Select an Adventure Book');
  // Wait for the book list: the dropdown stays disabled while it loads.
  await screen.findByText('Please select a book to start your adventure.');
  fireEvent.click(dropdown);
  fireEvent.click(await screen.findByRole('option', { name: 'The Prisoner' }));
  await screen.findByText(/You wake up in what seems to be a dark prison cell/);
}

describe('App', () => {
  it('asks to select a book first', async () => {
    render(<App api={fakeApi()} />);

    expect(await screen.findByText('Please select a book to start your adventure.')).toBeInTheDocument();
  });

  it('shows an error when the API is unreachable', async () => {
    const api = fakeApi({ getBooks: jest.fn().mockRejectedValue(new Error('The Adventure Book API is unavailable')) });

    render(<App api={api} />);

    expect(await screen.findByText('The Adventure Book API is unavailable')).toBeInTheDocument();
  });

  it('plays the book until an ending is reached', async () => {
    const api = fakeApi();
    render(<App api={api} />);

    await selectThePrisoner();

    expect(api.getBook).toHaveBeenCalledWith(1, expect.anything());
    expect(screen.getByText('The Prisoner', { selector: 'h2' })).toBeInTheDocument();
    expect(health()).toBe('10');

    click('You look under the bed');
    click('Try to scan the area with your hands');

    expect(screen.getByText(/you cut yourself on a rusty nail/)).toBeInTheDocument();
    expect(screen.getByText(/-6/)).toBeInTheDocument();
    expect(health()).toBe('4');

    click('Try to open the door with the key');

    expect(screen.getByText('The door opens. You are now free...')).toBeInTheDocument();
    expect(screen.getByText('The End')).toBeInTheDocument();

    click('Play again');

    expect(health()).toBe('10');
    expect(screen.getByText(/You wake up in what seems to be a dark prison cell/)).toBeInTheDocument();
  });

  it('ends the game when health reaches zero and starts a new one', async () => {
    render(<App api={fakeApi()} />);
    await selectThePrisoner();

    for (let loop = 0; loop < 4; loop++) {
      click('You try to open the door');
      click('Gather your thoughts');
    }

    expect(health()).toBe('0');
    expect(screen.getByText('You died')).toBeInTheDocument();
    expect(screen.queryByRole('button', { name: 'Gather your thoughts' })).not.toBeInTheDocument();

    click('Start a new game');

    expect(health()).toBe('10');
    expect(screen.queryByText('You died')).not.toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'You try to open the door' })).toBeInTheDocument();
  });

  it('shows the API error when the book cannot be loaded', async () => {
    const api = fakeApi({ getBook: jest.fn().mockRejectedValue(new Error('Book not found! ID: 1')) });
    render(<App api={api} />);

    await screen.findByText('Please select a book to start your adventure.');
    fireEvent.click(screen.getByRole('combobox'));
    fireEvent.click(await screen.findByRole('option', { name: 'The Prisoner' }));

    expect(await screen.findByText('Book not found! ID: 1')).toBeInTheDocument();
  });
});
