<template>
  <v-container>
    <v-row class="bg-secondary my-2" style="position: relative">
      <v-col cols="12" class="text-h4">
        {{ $t("main.title") }}
      </v-col>
    </v-row>
    <v-row class="mt-4">
      <v-col cols="8">
        <NOTIFICATION />
      </v-col>
    </v-row>
    <v-row justify="space-between" class="my-2">
      <v-col>
        <Applications />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import Applications from "../components/ApplicationComponent.vue";
import { defineAsyncComponent } from "vue";
export default {
  name: "Home",
  components: {
    Applications,
    NOTIFICATION: defineAsyncComponent(() =>
      import("../components/Notification.vue")
    ),
  },
  setup() {},
};
</script>
