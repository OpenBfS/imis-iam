<template>
  <v-app>
    <Appbar />
    <Sidebar />
    <v-main>
      <router-view />
      <UIAlert
        v-if="hasLoadingError"
        v-bind:isSuccessful="false"
        v-bind:message="$store.state.application.httpErrorMessage"
      />
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

import { defineAsyncComponent, onMounted } from "vue";
import { useStore } from "vuex";
import { useNotification } from "./lib/use-notification";

export default {
  components: {
    Sidebar: defineAsyncComponent(() => import("@/components/UI/Sidebar.vue")),
    Appbar: defineAsyncComponent(() => import("@/components/UI/Appbar.vue")),
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  name: "App",
  setup() {
    const store = useStore();
    const { hasLoadingError } = useNotification();
    onMounted(() => {
      store
        .dispatch("profile/loadProfile")
        .then(() => {})
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    return {
      hasLoadingError,
    };
  },
};
</script>
