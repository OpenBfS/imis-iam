/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createI18n } from "vue-i18n";

import de from "@/locales/de.js";

const datetimeFormats = {
  de: {
    short: {
      year: "numeric",
      month: "numeric",
      day: "numeric",
    },
    long: {
      year: "numeric",
      month: "short",
      day: "numeric",
      weekday: "short",
      hour: "numeric",
      minute: "numeric",
      hour12: true,
    },
  },
};

const i18n = createI18n({
  datetimeFormats: datetimeFormats,
  legacy: false,
  globalInjection: true,
  locale: "de",
  messages: {
    de: de,
  },
});

export default i18n;
