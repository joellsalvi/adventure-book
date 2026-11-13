import React from 'react';
import { PrimaryButton } from '@fluentui/react';
import { Section } from '../model/book';
import { GameStatus as Status } from '../game/gameEngine';

interface GameStatusProps {
  status: Status;
  section: Section;
  onRestart(): void;
}

/** Shown once the game is over: the player died or reached an ending. */
export const GameStatus: React.FC<GameStatusProps> = ({ status, section, onRestart }) => (
  <div className="game-status">
    {status === 'DEAD' ? (
      <>
        <div className="status status--dead" role="alert">
          <h3>You died</h3>
          <p>Your health dropped to zero. The adventure is over.</p>
        </div>
        <PrimaryButton onClick={onRestart}>Start a new game</PrimaryButton>
      </>
    ) : (
      <>
        <p className="section-text">{section.text}</p>
        <div className="status status--won" role="status">
          <h3>The End</h3>
          <p>You reached an ending of this adventure.</p>
        </div>
        <PrimaryButton onClick={onRestart}>Play again</PrimaryButton>
      </>
    )}
  </div>
);
