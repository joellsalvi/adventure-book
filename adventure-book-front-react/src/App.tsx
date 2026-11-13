import React from 'react';
import { ActionButton, MessageBar, MessageBarType, ProgressIndicator, ThemeProvider } from '@fluentui/react';
import './App.css';
import { BookApi, bookApi } from './api/bookApi';
import { BookSelector } from './components/BookSelector';
import { GameView } from './components/GameView';
import { useBooks } from './hooks/useBooks';
import { useGame } from './hooks/useGame';
import { darkTheme } from './theme';

interface AppProps {
  /** Injectable for tests. */
  api?: BookApi;
}

const App: React.FC<AppProps> = ({ api = bookApi }) => {
  const { books, loading: booksLoading, error: booksError } = useBooks(api);
  const { game, loading: bookLoading, error: gameError, selectedBookId, selectBook, choose, restart } = useGame(api);
  const error = gameError ?? booksError;

  const renderContent = () => {
    if (booksLoading || bookLoading) {
      return <ProgressIndicator ariaValueText="Loading" />;
    }
    if (game) {
      return <GameView game={game} onChoose={choose} onRestart={restart} />;
    }
    return error ? null : <p className="hint">Please select a book to start your adventure.</p>;
  };

  return (
    <ThemeProvider theme={darkTheme} applyTo="none">
      <main className="page">
        <section className="card" aria-labelledby="app-title">
          <header className="card__header">
            <h1 id="app-title">Adventure Book</h1>
            {game?.status === 'PLAYING' && <ActionButton onClick={restart}>Restart</ActionButton>}
          </header>

          <BookSelector books={books} selectedId={selectedBookId} disabled={booksLoading} onSelect={selectBook} />

          {error && <MessageBar messageBarType={MessageBarType.error}>{error}</MessageBar>}

          {renderContent()}
        </section>
      </main>
    </ThemeProvider>
  );
};

export default App;
