<!--
 Copyright (C) 2024 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-row class="px-2" align-content="end">
    <v-spacer></v-spacer>
    <v-menu
      v-model="areTableSettingsOpen"
      :close-on-content-click="false"
      location="end"
    >
      <template v-slot:activator="{ props }">
        <v-btn icon="mdi-cog" v-bind="props"></v-btn>
      </template>
      <v-card min-width="380">
        <v-list>
          <v-list-item title="Spalten"></v-list-item>
          <v-list-item>
            <v-checkbox
              v-for="item in headers"
              v-model="item.visible"
              v-bind:key="item.key"
              density="compact"
              :label="item.title"
              hide-details
            >
            </v-checkbox>
          </v-list-item>
        </v-list>
      </v-card>
    </v-menu>
  </v-row>
  <v-data-table-server
    :model-value="
      props.type === 'users'
        ? userStore.foundUsers
        : institutionStore.foundInstitutions
    "
    class="ma-2 pa-2"
    :headers="allHeaders"
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
import { computed, onMounted, ref } from "vue";
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const areTableSettingsOpen = ref(false);
const headers = ref([]);
const selectedHeaders = ref([]);
const actionHeader = {
  title: t("label.actions"),
  key: "actions",
  sortable: false,
};
const allHeaders = computed(() => {
  return [...headers.value.filter((h) => h.visible), actionHeader];
});

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

function getUserAttribute(user, attributeName) {
  const attribute = user.attributes[attributeName] ?? user[attributeName];
  return attribute?.join(", ");
}

const camelCaseToUnderscore = (text) => {
  return text.replace(/([A-Z])/g, "_$1").toLowerCase();
};

onMounted(() => {
  props.headers.forEach((header) => {
    const translationPrefix = props.type === "users" ? "user" : "institution";
    const translationKey =
      header === "name"
        ? "label.name"
        : `${translationPrefix}.${camelCaseToUnderscore(header)}`;

    const newHeader = {
      title: t(translationKey),
      key: header,
      sortable: props.type !== "users",
      visible: true,
    };
    if (props.type === "users") {
      newHeader.value = (item) => getUserAttribute(item, header);
    }
    headers.value.push(newHeader);
  });
  selectedHeaders.value = headers.value;
});
</script>
