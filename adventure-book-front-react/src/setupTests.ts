import '@testing-library/jest-dom';
import { setIconOptions } from '@fluentui/react';

// Icons are registered by initializeIcons() in index.tsx, which tests do not load.
setIconOptions({ disableWarnings: true });
