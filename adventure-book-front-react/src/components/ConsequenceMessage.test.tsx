import React from 'react';
import { render, screen } from '@testing-library/react';
import { ConsequenceEvent } from '../game/gameEngine';
import { ConsequenceMessage } from './ConsequenceMessage';

const event = (type: string, healthChange: number): ConsequenceEvent => ({
  consequence: { type, value: '3', text: 'Something happened.' },
  healthChange,
});

describe('ConsequenceMessage', () => {
  it('shows a health gain as positive', () => {
    render(<ConsequenceMessage event={event('GAIN_HEALTH', 3)} />);

    const message = screen.getByRole('status');
    expect(message).toHaveTextContent('+3');
    expect(message).toHaveClass('consequence--positive');
  });

  it('shows a health loss as negative', () => {
    render(<ConsequenceMessage event={event('LOSE_HEALTH', -3)} />);

    const message = screen.getByRole('status');
    expect(message).toHaveTextContent('-3');
    expect(message).not.toHaveClass('consequence--positive');
  });

  it('shows only the text when health did not change', () => {
    render(<ConsequenceMessage event={event('POISON', 0)} />);

    const message = screen.getByRole('status');
    expect(message).toHaveTextContent(/^Something happened\.$/);
    expect(message).not.toHaveClass('consequence--positive');
  });
});
