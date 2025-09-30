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
    density="compact"
    style="min-height: 0; max-height: 100%"
    fixed-header
    :headers="allHeaders"
    :items="props.items"
    :items-length="totalNumberOfItems"
    :items-per-page="props.itemsPerPage"
    :items-per-page-text="$t('label.itemsPerPage')"
    :loading="applicationStore.isLoading"
    :no-data-text="props.noDataText"
    :page-text="`${offset + 1}-${getNumberForEndOfRange()} ${$t('label.of')} ${props.totalNumberOfItems}`"
    show-select
    v-model="selected"
    @update:options="updateTable"
  >
    <template v-for="(_, slot) of $slots" v-slot:[slot]="scope"
      ><slot :name="slot" v-bind="scope"
    /></template>

    <template
      v-slot:headers="{
        headers,
        columns,
        toggleSort,
        getSortIcon,
        isSorted,
        someSelected,
        allSelected,
        selectAll,
      }"
    >
      <tr>
        <th
          :style="header.width ? `width: ${header.width}px` : ''"
          :class="getTableHeaderClass(header.key, isSorted(columns[index]))"
          v-for="(header, index) in headers[0]"
          :key="header"
        >
          <template v-if="header.key === 'data-table-select'">
            <v-checkbox
              @click="
                () => {
                  selectAll(!allSelected);
                }
              "
              :model-value="allSelected"
              :indeterminate="someSelected && !allSelected"
              hide-details
            />
          </template>
          <div v-else class="d-flex align-baseline ga-1">
            <v-chip
              v-if="getFilterValue(props.type, columns[index].key)"
              class="me-2 px-1"
              color="light-green"
              size="x-small"
              variant="flat"
            >
              <v-icon color="white" icon="mdi-filter"></v-icon>
            </v-chip>
            <button
              v-if="columns[index].sortable"
              @click="toggleSort(columns[index])"
            >
              <span>{{ columns[index].title }}</span>
              <v-icon
                :icon="getSortIcon(columns[index])"
                class="v-data-table-header__sort-icon"
              />
            </button>
            <div v-else>
              <span>{{ columns[index].title }}</span>
            </div>
          </div>
        </th>
      </tr>
      <tr>
        <td
          class="px-2 position-sticky"
          style="top: 30pt; z-index: 3; background-color: white"
        ></td>
        <td
          class="ps-0 pe-2 position-sticky"
          style="top: 30pt; z-index: 3; background-color: white"
          v-for="header in allHeaders"
          :key="header"
        >
          <template v-if="header.key === 'active' || header.key === 'enabled'">
            <Filter
              :filterKey="header.key"
              :items="booleanFilterItems"
              :label="$t('user.enabled')"
              :type="props.type"
            ></Filter>
          </template>
          <template v-else-if="header.key === 'hiddenInAddressbook'">
            <Filter
              :filterKey="header.key"
              :items="booleanFilterItems"
              :label="$t('user.hiddenInAddressbook')"
              :type="props.type"
            ></Filter>
          </template>
          <template v-else-if="header.key === 'institutions'">
            <Filter
              :filterKey="header.key"
              :items="institutionStore.sortedInstitutions"
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
              :label="$t('institution.serviceBuildingState')"
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

<style>
.v-data-table-footer {
  padding: 2px 4px;
}
</style>

<script setup>
import { useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import { computed, onMounted, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http";
import { useNotification } from "@/lib/use-notification";
import { states } from "./Institution/institution";
import Filter from "./Search/Filter.vue";
import { getFilterValue } from "./Search/searchTable";

const { t } = useI18n();
const { hasLoadingError } = useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

const booleanFilterItems = [
  { title: t("true"), value: "true" },
  { title: t("false"), value: "false" },
];

const actionHeader = {
  title: t("label.actions"),
  key: "actions",
  sortable: false,
};
const allHeaders = computed(() => {
  const mainHeaders =
    props.type === "users"
      ? userStore.tableHeaders
      : institutionStore.tableHeaders;
  return [...mainHeaders.filter((h) => h.visible), actionHeader];
});
const roles = computed(() => {
  return userStore.roles.map((role) => {
    return { title: t(role.description), value: role.name };
  });
});
const selected = ref([]);
const institutionTags = ref([]);
const getInstitutionTags = () => {
  HTTP.get("iam/institution/tag")
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

const updateTable = (event) => {
  const offset = (event.page - 1) * event.itemsPerPage;
  const store = props.type === "users" ? userStore : institutionStore;
  store.offset = offset;
  store.itemsPerPage = event.itemsPerPage;
  store.sortBy = event.sortBy.length ? event.sortBy[0] : null;
  applicationStore.searchRequest([props.type]);
};

onMounted(() => {
  getInstitutionTags();
});

const getNumberForEndOfRange = () => {
  // -1 means user selected to show all items in the table. Without this case the UI would display
  // 1--1 of... instead of 1-1...
  if (props.itemsPerPage === -1) return 1;
  return props.offset + props.itemsPerPage > props.items.length
    ? props.offset + props.items.length
    : props.offset + props.itemsPerPage;
};

const getTableHeaderClass = (key, isSorted) => {
  const sortedClass = "v-data-table__th--sorted";
  if (key === "data-table-select") {
    return "v-data-table__td v-data-table-column--no-padding v-data-table-column--align-start v-data-table__td--select-row";
  } else {
    return `pe-2 position-sticky ${isSorted ? sortedClass : ""}`;
  }
};
</script>
