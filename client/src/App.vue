<template>
  <v-app>
    <Appbar />
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

import Appbar from "./components/Appbar.vue";

export default {
  name: "App",
  components: {
    Appbar,
  },
  data: () => ({
    //
  }),
};
</script>
