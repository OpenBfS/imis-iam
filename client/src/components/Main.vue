<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader v-bind:title="$t('main.title')" />
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
import Applications from "../components/ApplicationComponent.vue";
import { defineAsyncComponent } from "vue";
export default {
  name: "Home",
  components: {
    Applications,
    UIHeader: defineAsyncComponent(() =>
      import("@/components/UI/UIHeader.vue")
    ),
    NOTIFICATION: defineAsyncComponent(() =>
      import("../components/Notification.vue")
    ),
  },
  setup() {},
};
</script>
