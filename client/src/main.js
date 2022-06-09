import { createApp } from "vue";
import { createI18n, useI18n } from "vue-i18n";
import { createVueI18nAdapter } from "vuetify/locale/adapters/vue-i18n";
import { loadFonts } from "./plugins/webfontloader";

import vuetify from "./plugins/vuetify";

import App from "./App.vue";
import router from "./router";
import store from "./store";

import de from "./locales/de";

loadFonts();

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: "de",
  messages: {
    de: de,
  },
});

vuetify.locale = createVueI18nAdapter({ i18n, useI18n });

createApp(App).use(router).use(store).use(i18n).use(vuetify).mount("#app");
