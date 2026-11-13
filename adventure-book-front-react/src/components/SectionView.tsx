import React from 'react';
import { DefaultButton, PrimaryButton } from '@fluentui/react';
import { Option, Section } from '../model/book';

interface SectionViewProps {
  section: Section;
  onChoose(option: Option): void;
}

export const SectionView: React.FC<SectionViewProps> = ({ section, onChoose }) => (
  <div className="section-view">
    <p className="section-text">{section.text}</p>
    <div className="options">
      {(section.options ?? []).map((option, index) => {
        const Button = index === 0 ? PrimaryButton : DefaultButton;
        return (
          <Button key={index} className="option" onClick={() => onChoose(option)}>
            {option.description}
          </Button>
        );
      })}
    </div>
  </div>
);
