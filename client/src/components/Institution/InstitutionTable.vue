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
              profileStore.isAllowedToManage &&
              (item.network === profileStore.userData.network ||
                profileStore.userData.role === 'chief_editor')
                ? 'mdi-pencil'
                : 'mdi-information-outline'
            }`"
            size="small"
            v-bind="props"
            @click="editInstitution(item)"
          ></v-btn>
        </template>
        <span>{{
          profileStore.isAllowedToManage &&
          (item.network === profileStore.userData.network ||
            profileStore.userData.role === "chief_editor")
            ? $t("label.edit")
            : $t("label.showInfo")
        }}</span>
      </v-tooltip>
    </template>
  </DataTableServer>
</template>

<script setup>
import { PROCESS_TYPE, useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import DataTableServer from "@/components/DataTableServer.vue";
import { getExpInstitution } from "@/components/Institution/institution.js";
import { onMounted } from "vue";
import {
  createHeaders,
  initSelectedColumns,
} from "@/components/Search/searchTable.js";

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

const editInstitution = (institution) => {
  applicationStore.openInstitutionEditForm(
    structuredClone(institution),
    PROCESS_TYPE.EDIT
  );
};

onMounted(() => {
  const columns = Object.keys(getExpInstitution()).map((key) => {
    return {
      name: key,
      default: defaultHeaders.includes(key),
    };
  });
  institutionStore.tableHeaders = createHeaders(columns, "institutions");
  initSelectedColumns("institutions");
});
</script>
