import { Book, Option } from '../model/book';
import { thePrisoner } from '../testing/fixtures';
import { applyConsequence, ConsequenceHandlers, MIN_MAX_HEALTH } from './consequenceHandlers';
import { chooseOption, GameState, INITIAL_HEALTH, startGame } from './gameEngine';

function choose(state: GameState, description: string): GameState {
  const option = state.section.options?.find((candidate) => candidate.description === description);
  if (!option) {
    throw new Error(`No option "${description}" in section ${state.section.id}`);
  }
  return chooseOption(state, option);
}

describe('game engine', () => {
  let book: Book;

  beforeEach(() => {
    book = thePrisoner();
  });

  describe('startGame', () => {
    it('starts at the BEGIN section with full health', () => {
      const state = startGame(book);

      expect(state.section.id).toBe(1);
      expect(state.player).toEqual({ health: INITIAL_HEALTH, maxHealth: INITIAL_HEALTH });
      expect(state.status).toBe('PLAYING');
      expect(state.lastEvent).toBeNull();
    });

    it('fails when the book has no BEGIN section', () => {
      book.sections = book.sections.filter((section) => section.type !== 'BEGIN');

      expect(() => startGame(book)).toThrow(/no beginning section/);
    });
  });

  describe('chooseOption', () => {
    it('moves to the next section when the option has no consequence', () => {
      const state = choose(startGame(book), 'You look under the bed');

      expect(state.section.id).toBe(20);
      expect(state.player.health).toBe(10);
      expect(state.lastEvent).toBeNull();
    });

    it('applies LOSE_HEALTH and keeps playing while health remains', () => {
      let state = choose(startGame(book), 'You look under the bed');
      state = choose(state, 'Try to scan the area with your hands');

      expect(state.section.id).toBe(30);
      expect(state.player.health).toBe(4);
      expect(state.status).toBe('PLAYING');
      expect(state.lastEvent?.healthChange).toBe(-6);
      expect(state.lastEvent?.consequence.text).toContain('rusty nail');
    });

    it('clears the consequence message on the next choice without consequence', () => {
      let state = choose(startGame(book), 'You look under the bed');
      state = choose(state, 'Try to scan the area with your hands');
      state = choose(state, 'Try to open the door with the key');

      expect(state.lastEvent).toBeNull();
    });

    it('wins when an END section is reached', () => {
      let state = choose(startGame(book), 'You look under the bed');
      state = choose(state, 'Try to scan the area with your hands');
      state = choose(state, 'Try to open the door with the key');

      expect(state.section.id).toBe(1000);
      expect(state.status).toBe('WON');
      expect(state.player.health).toBe(4);
    });

    it('dies when health reaches zero', () => {
      let state = startGame(book);
      const healthAfterEachLoop: number[] = [];

      while (state.status === 'PLAYING') {
        state = choose(state, 'You try to open the door');
        state = choose(state, 'Gather your thoughts');
        healthAfterEachLoop.push(state.player.health);
      }

      expect(healthAfterEachLoop).toEqual([7, 4, 1, 0]);
      expect(state.status).toBe('DEAD');
      expect(state.section.id).toBe(500);
      expect(state.lastEvent?.healthChange).toBe(-1);
    });

    it('dies even when the deadly option leads to an END section', () => {
      const deadly: Option = {
        description: 'Jump',
        gotoId: 1000,
        consequence: { type: 'LOSE_HEALTH', value: '10', text: 'You fall.' },
      };

      const state = chooseOption(startGame(book), deadly);

      expect(state.status).toBe('DEAD');
      expect(state.player.health).toBe(0);
    });

    it('ignores choices once the game is over', () => {
      const won: GameState = { ...startGame(book), status: 'WON' };

      expect(chooseOption(won, book.sections[0].options![0])).toBe(won);
    });

    it('ignores unknown consequence types', () => {
      const warn = jest.spyOn(console, 'warn').mockImplementation(() => undefined);
      const option: Option = { description: 'Pray', gotoId: 20, consequence: { type: 'GAIN_LUCK', value: '1' } };

      const state = chooseOption(startGame(book), option);

      expect(state.section.id).toBe(20);
      expect(state.player.health).toBe(10);
      expect(state.lastEvent?.healthChange).toBe(0);
      expect(warn).toHaveBeenCalled();
      warn.mockRestore();
    });

    it('supports additional consequence types through custom handlers', () => {
      const handlers: ConsequenceHandlers = {
        GAIN_HEALTH: (player, value) => ({
          ...player,
          health: Math.min(player.maxHealth, player.health + Number(value)),
        }),
      };
      const hurt: GameState = { ...startGame(book), player: { health: 5, maxHealth: 10 } };
      const option: Option = { description: 'Drink', gotoId: 20, consequence: { type: 'GAIN_HEALTH', value: '3' } };

      const state = chooseOption(hurt, option, handlers);

      expect(state.player.health).toBe(8);
      expect(state.lastEvent?.healthChange).toBe(3);
    });

    it('matches section ids sent as strings', () => {
      book.sections[3].id = '500' as unknown as number;

      const state = choose(startGame(book), 'You try to open the door');

      expect(state.section.text).toBe('The door is locked.');
    });

    it('fails when the next section does not exist', () => {
      const option: Option = { description: 'Nowhere', gotoId: 999 };

      expect(() => chooseOption(startGame(book), option)).toThrow(/Section 999 does not exist/);
    });
  });

  describe('LOSE_HEALTH', () => {
    it('never drops health below zero', () => {
      const player = applyConsequence({ health: 2, maxHealth: 10 }, { type: 'LOSE_HEALTH', value: '6' });

      expect(player.health).toBe(0);
    });

    it('ignores invalid amounts', () => {
      const player = applyConsequence({ health: 5, maxHealth: 10 }, { type: 'LOSE_HEALTH', value: 'abc' });

      expect(player.health).toBe(5);
    });
  });

  describe('GAIN_HEALTH', () => {
    it('restores health points', () => {
      const player = applyConsequence({ health: 5, maxHealth: 10 }, { type: 'GAIN_HEALTH', value: '3' });

      expect(player).toEqual({ health: 8, maxHealth: 10 });
    });

    it('never raises health above the maximum', () => {
      const player = applyConsequence({ health: 9, maxHealth: 10 }, { type: 'GAIN_HEALTH', value: '5' });

      expect(player).toEqual({ health: 10, maxHealth: 10 });
    });

    it('ignores negative amounts', () => {
      const player = applyConsequence({ health: 5, maxHealth: 10 }, { type: 'GAIN_HEALTH', value: '-3' });

      expect(player.health).toBe(5);
    });

    it('reports the health actually gained', () => {
      const hurt: GameState = { ...startGame(book), player: { health: 8, maxHealth: 10 } };
      const option: Option = {
        description: 'Eat',
        gotoId: 20,
        consequence: { type: 'GAIN_HEALTH', value: '10' },
      };

      const state = chooseOption(hurt, option);

      expect(state.player.health).toBe(10);
      expect(state.lastEvent?.healthChange).toBe(2);
    });
  });

  describe('GAIN_MAX_HEALTH', () => {
    it('raises the maximum and the current health', () => {
      const player = applyConsequence({ health: 6, maxHealth: 10 }, { type: 'GAIN_MAX_HEALTH', value: '2' });

      expect(player).toEqual({ health: 8, maxHealth: 12 });
    });
  });

  describe('POISON', () => {
    it('lowers the maximum health and keeps health below it', () => {
      const player = applyConsequence({ health: 5, maxHealth: 10 }, { type: 'POISON', value: '2' });

      expect(player).toEqual({ health: 5, maxHealth: 8 });
    });

    it('caps health to the new maximum', () => {
      const player = applyConsequence({ health: 10, maxHealth: 10 }, { type: 'POISON', value: '3' });

      expect(player).toEqual({ health: 7, maxHealth: 7 });
    });

    it('never lowers the maximum below the minimum, so it cannot kill on its own', () => {
      const player = applyConsequence({ health: 4, maxHealth: 5 }, { type: 'POISON', value: '9' });

      expect(player).toEqual({ health: MIN_MAX_HEALTH, maxHealth: MIN_MAX_HEALTH });
    });
  });

  describe('INSTANT_DEATH', () => {
    it('kills the player whatever their health, even on the way to an END section', () => {
      const option: Option = {
        description: 'Dive',
        gotoId: 1000,
        consequence: { type: 'INSTANT_DEATH', value: '0' },
      };

      const state = chooseOption(startGame(book), option);

      expect(state.status).toBe('DEAD');
      expect(state.player.health).toBe(0);
      expect(state.lastEvent?.healthChange).toBe(-INITIAL_HEALTH);
    });
  });
});
