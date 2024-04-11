/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
// Styles
import "@mdi/font/css/materialdesignicons.css";
import "vuetify/styles";

// Vuetify
import { createVuetify } from "vuetify";

const iamLight = {
  dark: false,
  colors: {
    background: "#FFFFFF",
    surface: "#ffffff",
    primary: "#6200EE",
    "primary-darken-1": "#3700B3",
    secondary: "#caddc1",
    "secondary-darken-1": "#018786",
    accent: "#5b8445",
    anchor: "#8c9eff",
    error: "#B00020",
    info: "#2196F3",
    success: "#4CAF50",
    warning: "#FB8C00",
  },
};

export default createVuetify({
  theme: {
    defaultTheme: "iamLight",
    themes: {
      iamLight,
    },
  },
  defaults: {
    VCheckbox: {
      density: "compact",
    },
    VDialog: {
      persistent: true,
    },
    VSelect: {
      density: "compact",
      variant: "filled",
    },
    VTextarea: {
      variant: "outlined",
    },
  },
});
