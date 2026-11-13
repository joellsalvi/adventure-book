import { createTheme } from '@fluentui/react';

/** Dark Fluent UI theme matching the sample design (palette.white is the surface color). */
export const darkTheme = createTheme({
  isInverted: true,
  palette: {
    themePrimary: '#3a96dd',
    themeLighterAlt: '#020609',
    themeLighter: '#091823',
    themeLight: '#112d43',
    themeTertiary: '#235a85',
    themeSecondary: '#3385c3',
    themeDarkAlt: '#4ba0e1',
    themeDark: '#65aee6',
    themeDarker: '#8ac2ec',
    neutralLighterAlt: '#323232',
    neutralLighter: '#3a3a3a',
    neutralLight: '#474747',
    neutralQuaternaryAlt: '#4f4f4f',
    neutralQuaternary: '#565656',
    neutralTertiaryAlt: '#727272',
    neutralTertiary: '#c8c8c8',
    neutralSecondary: '#d0d0d0',
    neutralSecondaryAlt: '#d0d0d0',
    neutralPrimaryAlt: '#dadada',
    neutralPrimary: '#e8e8e8',
    neutralDark: '#f4f4f4',
    black: '#f8f8f8',
    white: '#2b2b2b',
  },
});
