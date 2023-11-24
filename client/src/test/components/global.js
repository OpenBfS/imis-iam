/* Copyright (C) 2023 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { createVuetify } from "vuetify";
import * as components from "vuetify/components";
import * as directives from "vuetify/directives";
import i18n from "@/i18n";
import store from "@/store";
import UIAlert from "@/components/UI/UIAlert.vue";

const vuetify = createVuetify({
  components,
  directives,
});

const global = {
  plugins: [vuetify, store, i18n],
  components: { UIAlert },
};

export default global;
