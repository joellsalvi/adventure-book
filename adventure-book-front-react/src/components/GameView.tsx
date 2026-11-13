import React from 'react';
import { Option } from '../model/book';
import { GameState } from '../game/gameEngine';
import { ConsequenceMessage } from './ConsequenceMessage';
import { GameStatus } from './GameStatus';
import { HealthBar } from './HealthBar';
import { SectionView } from './SectionView';

interface GameViewProps {
  game: GameState;
  onChoose(option: Option): void;
  onRestart(): void;
}

export const GameView: React.FC<GameViewProps> = ({ game, onChoose, onRestart }) => (
  <>
    <div className="game-header">
      <div>
        <h2 className="book-title">{game.book.title}</h2>
        <span className="book-author">by {game.book.author}</span>
      </div>
      <HealthBar health={game.player.health} maxHealth={game.player.maxHealth} />
    </div>

    {game.lastEvent && <ConsequenceMessage event={game.lastEvent} />}

    {game.status === 'PLAYING' ? (
      <SectionView section={game.section} onChoose={onChoose} />
    ) : (
      <GameStatus status={game.status} section={game.section} onRestart={onRestart} />
    )}
  </>
);
