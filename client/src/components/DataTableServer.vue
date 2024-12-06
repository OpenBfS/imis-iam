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
        <td></td>
        <td class="pe-2" v-for="header in allHeaders" :key="header">
          <template v-if="header.key === 'active' || header.key === 'enabled'">
            <Filter
              :filterKey="header.key"
              :items="[
                { title: t('true'), value: 'true' },
                { title: t('false'), value: 'false' },
              ]"
              :label="$t('user.enabled')"
              :type="props.type"
            ></Filter>
          </template>
          <template v-else-if="header.key === 'institutions'">
            <Filter
              :filterKey="header.key"
              :items="institutionStore.institutions"
              item-title="name"
              item-value="name"
              :label="$t('user.institutions')"
              :type="props.type"
            ></Filter>
          </template>
          <template
            v-else-if="props.type === 'institutions' && header.key === 'tags'"
          >
            <Filter
              :filterKey="header.key"
              :items="institutionTags"
              item-title="name"
              item-value="name"
              :label="$t('institution.tags')"
              :type="props.type"
            ></Filter>
          </template>
          <template
            v-else-if="
              props.type === 'institutions' &&
              header.key === 'serviceBuildingState'
            "
          >
            <Filter
              :filterKey="header.key"
              :items="states"
              item-title="label"
              item-value="value"
              :label="$t('institution.service_building_state')"
              :type="props.type"
            ></Filter>
          </template>
          <template v-else-if="header.key === 'role'">
            <Filter
              :filterKey="header.key"
              :items="roles"
              :label="$t(`user.${header.key}`)"
              :type="props.type"
            ></Filter>
          </template>
          <template
            v-else-if="
              props.type === 'users' &&
              profileStore.getAttribute(header.key) &&
              profileStore.isAttributeSelection(header.key)
            "
          >
            <Filter
              :filterKey="header.key"
              :items="profileStore.getSelectionItemsOfAttribute(header.key)"
              :label="$t(`user.${header.key}`)"
              :type="props.type"
            ></Filter>
          </template>
          <template v-else-if="header.key !== 'actions'">
            <Filter
              :filterKey="header.key"
              :placeholder="header.title"
              :type="props.type"
            ></Filter>
          </template>
        </td>
      </tr>
    </template>
  </v-data-table-server>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { computed, onMounted, ref, toRaw, watch } from "vue";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { states } from "./Institution/institution";
import Filter from "./Search/Filter.vue";

const { t } = useI18n();
const { hasLoadingError } = useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

const areTableSettingsOpen = ref(false);
const headers = ref([]);
const actionHeader = {
  title: t("label.actions"),
  key: "actions",
  sortable: false,
};
const allHeaders = computed(() => {
  return [...headers.value.filter((h) => h.visible), actionHeader];
});
const roles = computed(() => {
  return userStore.roles.map((role) => {
    return { title: t(role.description), value: role.name };
  });
});
const selected = ref([]);
const institutionTags = ref([]);
const getInstitutionTags = () => {
  HTTP.get("institution/tag")
    .then((response) => {
      institutionTags.value = response.data;
    })
    .catch(() => {
      hasLoadingError.value = true;
    });
};

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
  const newHeaders = [];
  props.headers.forEach((header) => {
    const headerName = header.name;
    const translationPrefix = props.type === "users" ? "user" : "institution";
    const translationKey =
      headerName === "name"
        ? "label.name"
        : `${translationPrefix}.${camelCaseToUnderscore(headerName)}`;

    const newHeader = {
      title: t(translationKey),
      key: headerName,
      sortable: props.type !== "users",
      visible: header.default,
    };
    // This function decides how the values inside an item are displayed in the UI
    newHeader.value = (item) => {
      if (
        props.type === "users" &&
        headerName === "role" &&
        toRaw(item)?.[headerName]
      ) {
        return t(`role_iam_${toRaw(item)[headerName]}`);
      }
      const value =
        props.type === "users"
          ? toRaw(item.attributes[headerName] ?? item[headerName])
          : item[headerName];
      return getDisplayName(toRaw(value));
    };
    newHeaders.push(newHeader);
  });
  headers.value = newHeaders;
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
  if (value === undefined) return;
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
  getInstitutionTags();
});
</script>
