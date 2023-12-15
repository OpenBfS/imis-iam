<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("institution.title") }}
    </UIHeader>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            v-if="isAllowedToAdd"
            class="mr-4"
            v-bind="props"
            @click="
              resetInstitution();
              applicationStore.setManagedItem(institution);
              applicationStore.setProcessType('add');
              applicationStore.setShowManageInstitutionDialog(true);
            "
            icon="mdi-plus"
          >
          </v-btn>
        </template>
        <span>{{ $t("institution.add_institution") }}</span>
      </v-tooltip>
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-import"
            @click="
              applicationStore.setlistToExport('institutions');
              applicationStore.setShowExportDialog(true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("institution.export") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="t-6">
        <InstitutionTable v-bind:institutions="institutions" />
        <UIAlert
          v-if="hasLoadingError"
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup>
import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useNotification } from "@/lib/use-notification";
import { expInstitution } from "@/components/Institution/institution";

const InstitutionTable = defineAsyncComponent(() =>
  import("@/components/Institution/InstitutionTable.vue")
);
const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const { hasLoadingError } = useNotification();
const isAllowedToAdd = computed(() => {
  return profileStore.isAllowedToManage;
});
// Institutions
const institutions = computed(() => {
  return institutionStore.institutions;
});
const institution = ref({ ...expInstitution });

const getInstitutions = () => {
  institutionStore
    .loadInstitutions()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  getInstitutions();
});

const resetInstitution = () => {
  institution.value = { ...expInstitution };
};
</script>
