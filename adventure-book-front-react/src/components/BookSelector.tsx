import React, { useMemo } from 'react';
import { Dropdown, IDropdownOption } from '@fluentui/react';
import { BookSummary } from '../model/book';

interface BookSelectorProps {
  books: BookSummary[];
  selectedId: number | null;
  disabled?: boolean;
  onSelect(bookId: number): void;
}

export const BookSelector: React.FC<BookSelectorProps> = ({ books, selectedId, disabled, onSelect }) => {
  const options = useMemo<IDropdownOption[]>(
    () => books.map((book) => ({ key: book.id, text: book.title })),
    [books],
  );

  return (
    <Dropdown
      ariaLabel="Adventure book"
      placeholder="Select an Adventure Book"
      options={options}
      selectedKey={selectedId}
      disabled={disabled || books.length === 0}
      onChange={(_, option) => option && onSelect(Number(option.key))}
    />
  );
};
