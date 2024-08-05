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
    show-select
    v-model="selected"
    @update:options="updateTable"
  >
    <template v-for="(_, slot) of $slots" v-slot:[slot]="scope"
      ><slot :name="slot" v-bind="scope"
    /></template>

    <template v-slot:thead>
      <tr>
        <td class="pe-2" v-for="header in headers" :key="header">
          <v-text-field
            v-if="header.key !== 'active'"
            class="my-1"
            density="compact"
            :placeholder="header.title"
            hide-details
            variant="outlined"
            @update:modelValue="(event) => handleFilterInput(header.key, event)"
          ></v-text-field>
        </td>
      </tr>
    </template>
  </v-data-table-server>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";
import { computed, onMounted, ref, toRaw, watch } from "vue";
import { useI18n } from "vue-i18n";
import debounce from "debounce";

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
const selected = ref([]);

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

watch(selected, (value) => {
  if (props.type === "users") {
    userStore.selectedUsers = value;
  } else {
    institutionStore.selectedInstitutions = value;
  }
});

watch(
  () => props.headers,
  () => {
    adjustHeaders();
  }
);

const adjustHeaders = () => {
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
    newHeader.value = (item) => {
      if (
        props.type === "users" &&
        header === "role" &&
        toRaw(item)?.[header]
      ) {
        return t(`role_iam_${toRaw(item)[header]}`);
      }
      const value =
        props.type === "users"
          ? toRaw(item.attributes[header] ?? item[header])
          : item[header];
      return value ? getDisplayName(toRaw(value)) : undefined;
    };
    headers.value.push(newHeader);
  });
  selectedHeaders.value = headers.value;
};

const updateTable = (event) => {
  const offset = (event.page - 1) * event.itemsPerPage;
  const store = props.type === "users" ? userStore : institutionStore;
  store.offset = offset;
  store.itemsPerPage = event.itemsPerPage;
  store.sortBy = event.sortBy.length ? event.sortBy[0] : null;
  applicationStore.searchRequest([props.type]);
};

function getDisplayName(value) {
  if (!value) return;
  if (typeof value === "boolean") {
    return t(`${value}`);
  } else if (Array.isArray(value)) {
    return value.join(", ");
  }
  return value;
}

const camelCaseToUnderscore = (text) => {
  return text.replace(/([A-Z])/g, "_$1").toLowerCase();
};

onMounted(() => {
  adjustHeaders();
});

const triggerSearch = debounce(() => {
  applicationStore.searchRequest([props.type]);
}, 500);

const handleFilterInput = (key, value) => {
  if (props.type === "users") {
    userStore.updateFilter(key, value);
  } else if (props.type === "institutions") {
    institutionStore.updateFilter(key, value);
  }
  triggerSearch();
};
</script>
