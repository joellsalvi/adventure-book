import React from 'react';

interface HealthBarProps {
  health: number;
  maxHealth: number;
}

export const HealthBar: React.FC<HealthBarProps> = ({ health, maxHealth }) => {
  const percent = Math.max(0, Math.min(100, (health / maxHealth) * 100));
  const level = percent > 60 ? 'high' : percent > 30 ? 'medium' : 'low';

  return (
    <div
      className="health"
      role="meter"
      aria-label="Health points"
      aria-valuemin={0}
      aria-valuemax={maxHealth}
      aria-valuenow={health}
      data-level={level}
    >
      <span className="health__label">
        <span aria-hidden="true">&#9829;</span> {health} / {maxHealth}
      </span>
      <div className="health__track">
        <div className="health__fill" style={{ width: `${percent}%` }} />
      </div>
    </div>
  );
};
