<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <DataTableServer
    :headers="institutionStore.tableHeaders"
    :items="props.institutions"
    :items-per-page="institutionStore.itemsPerPage"
    :no-data-text="$t('institution.noInstitutionsAvailable')"
    :offset="institutionStore.offset"
    :total-number-of-items="institutionStore.totalNumberOfInstitutions"
    type="institutions"
  >
    <template v-slot:[`item.actions`]="{ item }">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            variant="plain"
            :icon="`${
              profileStore.isAllowedToManage
                ? 'mdi-pencil'
                : 'mdi-information-outline'
            }`"
            size="small"
            v-bind="props"
            @click="
              applicationStore.setManagedItem({
                ...item,
              });
              applicationStore.setProcessType('edit');
              applicationStore.setShowManageInstitutionDialog(true);
            "
          ></v-btn>
        </template>
        <span>{{
          profileStore.isAllowedToManage
            ? $t("label.edit")
            : $t("label.showInfo")
        }}</span>
      </v-tooltip>
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-if="profileStore.isAllowedToManage"
            variant="plain"
            icon="mdi-delete"
            size="small"
            v-bind="props"
            @click="
              applicationStore.setManagedItem({
                ...item,
              });
              applicationStore.setProcessType('delet');
              applicationStore.setShowManageInstitutionDialog(true);
            "
          ></v-btn>
        </template>
        <span>{{ $t("label.delete") }}</span>
      </v-tooltip>
    </template>
  </DataTableServer>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import DataTableServer from "@/components/DataTableServer.vue";
import { getExpInstitution } from "@/components/Institution/institution.js";
import { onMounted } from "vue";
import { createHeaders } from "@/lib/utils";


const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const defaultHeaders = [
  "name",
  "measFacilName",
  "measFacilId",
  "serviceBuildingLocation",
  "tags",
  "active",
];

const props = defineProps({
  institutions: Array,
});

onMounted(() => {
  const columns = Object.keys(getExpInstitution()).map((key) => {
    return {
      name: key,
      default: defaultHeaders.includes(key),
    };
  });
  institutionStore.tableHeaders = createHeaders(columns, "institutions");
});
</script>
