import { Consequence } from '../model/book';

export interface PlayerStats {
  health: number;
  maxHealth: number;
}

/**
 * Receives the player's current stats and the raw consequence value, and
 * returns the updated stats. Must not mutate its input.
 */
export type ConsequenceHandler = (player: PlayerStats, value: string) => PlayerStats;

export type ConsequenceHandlers = Readonly<Record<string, ConsequenceHandler>>;

const toAmount = (value: string): number => {
  const amount = Number.parseInt(String(value), 10);
  return Number.isFinite(amount) && amount > 0 ? amount : 0;
};

/** POISON never lowers the maximum health below this value. */
export const MIN_MAX_HEALTH = 1;

/**
 * Registry of supported consequence types. To support a new consequence, add
 * a handler here and its name to ConsequenceTypeEnum in the API: the game
 * engine picks it up.
 */
export const CONSEQUENCE_HANDLERS: ConsequenceHandlers = {
  /** Removes `value` health points, never below 0. */
  LOSE_HEALTH: (player, value) => ({
    ...player,
    health: Math.max(0, player.health - toAmount(value)),
  }),

  /** Restores `value` health points, never above the maximum. */
  GAIN_HEALTH: (player, value) => ({
    ...player,
    health: Math.min(player.maxHealth, player.health + toAmount(value)),
  }),

  /** Raises the maximum health and the current health by `value`. */
  GAIN_MAX_HEALTH: (player, value) => ({
    ...player,
    health: player.health + toAmount(value),
    maxHealth: player.maxHealth + toAmount(value),
  }),

  /** Lowers the maximum health by `value`; health is capped to the new maximum. */
  POISON: (player, value) => {
    const maxHealth = Math.max(MIN_MAX_HEALTH, player.maxHealth - toAmount(value));
    return { ...player, maxHealth, health: Math.min(player.health, maxHealth) };
  },

  /** Kills the player whatever their health. `value` is ignored. */
  INSTANT_DEATH: (player) => ({ ...player, health: 0 }),
};

export function applyConsequence(
  player: PlayerStats,
  consequence: Consequence,
  handlers: ConsequenceHandlers = CONSEQUENCE_HANDLERS,
): PlayerStats {
  const handler = handlers[consequence.type];
  if (!handler) {
    console.warn(`Unknown consequence type "${consequence.type}" ignored.`);
    return player;
  }
  return handler(player, consequence.value);
}
