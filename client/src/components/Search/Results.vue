<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card flat>
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
        >{{ $t("main.institutions") }} ({{
          institutionStore.foundInstitutions.length
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

<script setup>
import { ref, defineAsyncComponent } from "vue";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";

const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const ResultTable = defineAsyncComponent(() =>
  import("@/components/Search/ResultTable.vue")
);
const tab = ref(null);

const emit = defineEmits(["onSelectedTab"]);
</script>
