/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createApp, defineAsyncComponent } from "vue";
import { useI18n } from "vue-i18n";
import { createVueI18nAdapter } from "vuetify/locale/adapters/vue-i18n";
import UIHeader from "@/components/UI/UIHeader.vue";
import DatePicker from "@/components/DatePicker.vue";

import vuetify from "./plugins/vuetify";

import App from "./App.vue";
import router from "./router";
import { createPinia } from "pinia";
import i18n from "./i18n";

const pinia = createPinia();

const UIAlert = defineAsyncComponent(() =>
  import("@/components/UI/UIAlert.vue")
);

vuetify.locale = createVueI18nAdapter({ i18n, useI18n });

createApp(App)
  .use(pinia)
  .use(router)
  .use(i18n)
  .use(vuetify)
  .component("UIHeader", UIHeader)
  .component("UIAlert", UIAlert)
  .component("DatePicker", DatePicker)
  .mount("#app");
