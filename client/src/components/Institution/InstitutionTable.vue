<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-data-table-server
    v-model:items-per-page="itemsPerPage"
    class="ma-2 pa-2"
    :headers="tableHeaders"
    :items="props.institutions"
    :items-length="props.institutions.length"
    :loading="isLoading"
    :no-data-text="$t('institution.no_institutions_available')"
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
  </v-data-table-server>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useI18n } from "vue-i18n";
import { ref } from "vue";

const { t } = useI18n();

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const isLoading = ref(false);
const itemsPerPage = ref(25);

const tableHeaders = [
  { title: t("label.name"), key: "name", sortable: false },
  { title: t("institution.shortname"), key: "shortName", sortable: false },
  { title: t("institution.imis_id"), key: "imisId", sortable: false },
  { title: t("label.actions"), key: "actions", sortable: false },
];

const props = defineProps({
  institutions: Array,
});
</script>
