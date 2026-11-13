import React from 'react';
import { ConsequenceEvent } from '../game/gameEngine';

interface ConsequenceMessageProps {
  event: ConsequenceEvent;
}

/** "-6", "+2", or null when health did not change. */
const formatChange = (change: number): string | null => {
  if (change === 0) {
    return null;
  }
  return change > 0 ? `+${change}` : `${change}`;
};

export const ConsequenceMessage: React.FC<ConsequenceMessageProps> = ({ event }) => {
  const change = formatChange(event.healthChange);
  // Health went up (GAIN_HEALTH, GAIN_MAX_HEALTH): shown in green.
  const className = event.healthChange > 0 ? 'consequence consequence--positive' : 'consequence';

  return (
    <div className={className} role="status">
      {change && <span className="consequence__change">{change} &#9829;</span>}
      <span>{event.consequence.text}</span>
    </div>
  );
};
