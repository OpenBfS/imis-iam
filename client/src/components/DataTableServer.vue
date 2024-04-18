<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-data-table-server
    :model-value="
      props.type === 'users'
        ? userStore.itemsLength
        : institutionStore.itemsLength
    "
    class="ma-2 pa-2"
    :headers="props.headers"
    :items="props.items"
    :items-length="itemsLength"
    :items-per-page-text="$t('label.items_per_page')"
    :loading="applicationStore.isLoading"
    :no-data-text="props.noDataText"
    @update:options="updateTable"
  >
    <template v-for="(_, slot) of $slots" v-slot:[slot]="scope"
      ><slot :name="slot" v-bind="scope"
    /></template>
  </v-data-table-server>
</template>

<script setup>
import { ref } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const props = defineProps([
  // Vuetify props
  "headers",
  "items",
  "noDataText",

  // Custom props
  "type",
]);
const itemsLength = ref(
  // TODO: Get total length of items from server
  /*props.type === "users"
    ? userStore.itemsLength
    : institutionStore.itemsLength*/
  100
);

const updateTable = (event) => {
  const offset = (event.page - 1) * event.itemsPerPage;
  if (props.type === "users") {
    userStore.offset = offset;
    userStore.itemsPerPage = event.itemsPerPage;
    applicationStore.searchRequest(["users"], true);
  } else {
    institutionStore.offset = offset;
    institutionStore.itemsPerPage = event.itemsPerPage;
    applicationStore.searchRequest(["institutions"], true);
  }
};
</script>
