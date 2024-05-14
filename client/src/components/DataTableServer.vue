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
        ? userStore.foundUsers
        : institutionStore.foundInstitutions
    "
    class="ma-2 pa-2"
    :headers="props.headers"
    :items="props.items"
    :items-length="totalNumberOfItems"
    :items-per-page="props.itemsPerPage"
    :items-per-page-text="$t('label.items_per_page')"
    :loading="applicationStore.isLoading"
    :no-data-text="props.noDataText"
    :page-text="`${offset + 1}-${
      offset + props.itemsPerPage > props.items.length
        ? offset + props.items.length
        : offset + props.itemsPerPage
    } ${$t('label.of')} ${props.totalNumberOfItems}`"
    @update:options="updateTable"
  >
    <template v-for="(_, slot) of $slots" v-slot:[slot]="scope"
      ><slot :name="slot" v-bind="scope"
    /></template>
  </v-data-table-server>
</template>

<script setup>
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
  "itemsPerPage",
  "noDataText",
  "offset",
  "totalNumberOfItems",

  // Custom props
  "type",
]);

const updateTable = (event) => {
  const offset = (event.page - 1) * event.itemsPerPage;
  const store = props.type === "users" ? userStore : institutionStore;
  store.offset = offset;
  store.itemsPerPage = event.itemsPerPage;
  store.sortBy = event.sortBy.length ? event.sortBy[0] : null;
  applicationStore.searchRequest([props.type]);
};
</script>
