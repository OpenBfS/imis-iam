<template>
  <v-app>
    <Appbar />
    <Sidebar />
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { defineAsyncComponent } from "vue";

export default {
  components: {
    Sidebar: defineAsyncComponent(() => import("@/components/UI/Sidebar.vue")),
    Appbar: defineAsyncComponent(() => import("@/components/UI/Appbar.vue")),
  },
  name: "App",
};
</script>
