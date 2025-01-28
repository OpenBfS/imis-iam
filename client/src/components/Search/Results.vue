<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div
    class="mx-0 px-0 pb-0 h-100 overflow-y-hidden flex-grow-1 d-flex flex-column"
  >
    <!-- div is necessary because of the parent flexbox so the line indicating the active tab is always visible -->
    <div>
      <v-tabs
        density="compact"
        v-model="tab"
        grow
        @update:modelValue="emit('onSelectedTab', $event)"
      >
        <v-tab value="users"
          >{{ $t("main.users") }} ({{ userStore.foundUsers.length }})</v-tab
        >
        <v-tab value="institutions"
          >{{ $t("institutions") }} ({{
            institutionStore.foundInstitutions.length
          }})</v-tab
        >
      </v-tabs>
    </div>
    <v-window
      v-model="tab"
      class="h-100 flex-grow-1 overflow-y-hidden"
      style="min-height: 0"
    >
      <v-window-item value="users" style="min-height: 0">
        <ResultTable v-bind:type="'users'" />
      </v-window-item>
      <v-window-item value="institutions" style="min-height: 0">
        <ResultTable v-bind:type="'institutions'" />
      </v-window-item>
    </v-window>
  </div>
</template>

<script setup>
import { ref } from "vue";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";

const institutionStore = useInstitutionStore();
const userStore = useUserStore();
const tab = ref(null);

const emit = defineEmits(["onSelectedTab"]);
</script>
