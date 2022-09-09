<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader v-bind:title="$t('institution.title')" />
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            @click="
              resetInstitution();
              $store.commit('application/setManagedItem', institution);
              $store.commit('application/setProcessType', 'add');
              $store.commit('application/setShowManageInstitutionDialog', true);
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
              $store.commit('application/setlistToExport', 'institutions');
              $store.commit('application/setShowExportDialog', true);
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
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";
import { expInstitution } from "@/components/Institution/institution";

export default {
  components: {
    UIHeader: defineAsyncComponent(() =>
      import("@/components/UI/UIHeader.vue")
    ),
    InstitutionTable: defineAsyncComponent(() =>
      import("@/components/Institution/InstitutionTable.vue")
    ),
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue"))
  },
  setup() {
    const store = useStore();
    const { hasLoadingError } = useNotification();
    const showManageDialog = ref(false);

    // Institutions
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });
    const institution = ref({ ...expInstitution });

    const getInstitutions = () => {
      store
        .dispatch("institution/loadInstitutions")
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
    return {
      resetInstitution,
      hasLoadingError,
      showManageDialog,
      institution,
      institutions
    };
  }
};
</script>
