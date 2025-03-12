<!--
 Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-tooltip activator="#column-selection" location="top">
    <span>{{ $t("label.columnsSelection") }}</span>
  </v-tooltip>
  <v-menu
    v-model="areTableSettingsOpen"
    :close-on-content-click="false"
    class="overflow-visible"
    location="start"
  >
    <template v-slot:activator="{ props }">
      <div class="position-relative">
        <v-btn
          class="ms-4"
          color="accent"
          icon="mdi-view-column"
          v-bind="props"
        ></v-btn>
        <v-badge
          inline
          class="position-absolute left-0 top-0"
          color="light-green"
          id="column-selection"
          :model-value="areFiltersForHiddenColumnsAvailable"
        >
          <template v-slot:badge>
            <div>
              <v-icon color="white" icon="mdi-filter"></v-icon>
            </div>
          </template>
        </v-badge>
      </div>
    </template>
    <v-card min-width="380">
      <v-list>
        <v-list-item
          :title="`${$t('label.columns')} (${$t('label.filters')})`"
        ></v-list-item>
        <v-list-item>
          <v-checkbox
            v-for="item in tableHeaders"
            @update:modelValue="
              () => {
                updateSelectedColumns();
              }
            "
            v-model="item.visible"
            v-bind:key="item.key"
            density="compact"
            hide-details
          >
            <template v-slot:label>
              <div class="d-flex gap-2">
                <span class="me-2">{{ `${item.title}` }}</span>
                <div v-if="getColumnCheckboxLabel(item.key)?.length > 0">
                  (
                  <v-chip
                    class="me-2 px-1"
                    color="light-green"
                    size="x-small"
                    variant="flat"
                  >
                    <v-icon color="white" icon="mdi-filter"></v-icon>
                  </v-chip>
                  <span>{{ getColumnCheckboxLabel(item.key) }}</span
                  >)
                </div>
              </div>
            </template>
          </v-checkbox>
        </v-list-item>
      </v-list>
    </v-card>
  </v-menu>
</template>

<script setup>
import { computed, nextTick, ref } from "vue";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";

const props = defineProps(["type"]);

const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const areTableSettingsOpen = ref(false);

const tableHeaders = computed(() => {
  if (props.type === "users") {
    return userStore.tableHeaders;
  } else {
    return institutionStore.tableHeaders;
  }
});

const getFilterValue = (key) => {
  return getFilters()?.[key];
};

const getColumnCheckboxLabel = (key) => {
  const value = getFilterValue(key);
  if (value === undefined) {
    return "";
  } else {
    return value;
  }
};

const getFilters = () => {
  return props.type === "users"
    ? userStore.filterBy
    : institutionStore.filterBy;
};

const areFiltersForHiddenColumnsAvailable = computed(() => {
  const filters = Object.keys(getFilters());
  return tableHeaders.value.some(
    (header) => header.visible === false && filters.includes(header.key)
  );
});

const updateSelectedColumns = async () => {
  await nextTick();
  let headers =
    props.type === "users"
      ? userStore.tableHeaders
      : institutionStore.tableHeaders;
  const selectedTableColumns = headers
    .filter((header) => header.visible === true)
    .map((header) => {
      return header.key;
    });
  if (props.type === "users") {
    userStore.selectedTableColumns = selectedTableColumns;
  } else {
    institutionStore.selectedTableColumns = selectedTableColumns;
  }
};
</script>
