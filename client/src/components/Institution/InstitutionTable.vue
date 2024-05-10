<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <DataTableServer
    :headers="tableHeaders"
    :items="props.institutions"
    :no-data-text="$t('institution.no_institutions_available')"
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
            : $t("label.show_info")
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
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useI18n } from "vue-i18n";
import DataTableServer from "@/components/DataTableServer.vue";

const { t } = useI18n();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();

const tableHeaders = [
  { title: t("label.name"), key: "name", sortable: true },
  { title: t("institution.shortname"), key: "shortName", sortable: true },
  { title: t("institution.imis_id"), key: "imisId", sortable: true },
  { title: t("label.actions"), key: "actions", sortable: false },
];

const props = defineProps({
  institutions: Array,
});
</script>
