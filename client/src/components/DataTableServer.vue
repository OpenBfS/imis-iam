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
        <td
          class="pe-2 position-sticky"
          style="top: 30pt; z-index: 3; background-color: white"
        ></td>
        <td
          class="pe-2 position-sticky"
          style="top: 30pt; z-index: 3; background-color: white"
          v-for="header in allHeaders"
          :key="header"
        >
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

const { t } = useI18n();
const { hasLoadingError } = useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

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
</script>
