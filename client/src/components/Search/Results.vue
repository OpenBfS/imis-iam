<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card>
    <v-tabs density="compact" v-model="tab" grow>
      <v-tab value="users"
        >{{ $t("main.users") }} ({{
          $store.state.user.foundUsers.length
        }})</v-tab
      >
      <v-tab value="institutions"
        >{{ $t("main.institutions") }} ({{
          $store.state.institution.foundInstitutions.length
        }})</v-tab
      >
    </v-tabs>
    <v-card-text>
      <v-window v-model="tab">
        <v-window-item value="users">
          <ResultTable v-bind:type="'users'" />
        </v-window-item>
        <v-window-item value="institutions">
          <ResultTable v-bind:type="'institutions'" />
        </v-window-item>
      </v-window>
    </v-card-text>
  </v-card>
</template>

<script>
import { ref, defineAsyncComponent } from "vue";

export default {
  components: {
    ResultTable: defineAsyncComponent(() =>
      import("@/components/Search/ResultTable.vue")
    ),
  },
  setup() {
    const tab = ref(null);
    const userTab = ref("");
    const institutionTab = ref();
    return { tab, userTab, institutionTab };
  },
};
</script>
