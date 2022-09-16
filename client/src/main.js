/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createApp, defineAsyncComponent } from "vue";
import { createI18n, useI18n } from "vue-i18n";
import { createVueI18nAdapter } from "vuetify/locale/adapters/vue-i18n";
import UIHeader from "@/components/UI/UIHeader.vue";

import vuetify from "./plugins/vuetify";

import App from "./App.vue";
import router from "./router";
import store from "./store";

const UIAlert = defineAsyncComponent(() =>
  import("@/components/UI/UIAlert.vue")
);

import de from "./locales/de";

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: "de",
  messages: {
    de: de,
  },
});

vuetify.locale = createVueI18nAdapter({ i18n, useI18n });

createApp(App)
  .use(router)
  .use(store)
  .use(i18n)
  .use(vuetify)
  .component("UIHeader", UIHeader)
  .component("UIAlert", UIAlert)
  .mount("#app");
