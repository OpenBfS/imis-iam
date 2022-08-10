<!--
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
 -->
<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("institution.title") }}
      </v-col>
    </v-row>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            @click="
              resetUser();
              processType = 'add';
              showManageDialog = true;
            "
            icon="mdi-plus"
          >
          </v-btn>
        </template>
        <span>{{ $t("institution.add_institution") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="t-6">
        <v-table class="ma-2 pa-2">
          <thead>
            <th class="text-left">{{ $t("label.name") }}</th>
            <th class="text-left">{{ $t("institution.shortname") }}</th>
            <th class="text-left">{{ $t("label.actions") }}</th>
          </thead>
          <tbody>
            <tr v-for="item in institutions" :key="item.id">
              <td>{{ item.name }}</td>
              <td>{{ item.shortName }}</td>
              <td class="d-flex">
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-account-edit-outline"
                      size="small"
                      v-bind="props"
                      @click="
                        institution = { ...item };
                        processType = 'edit';
                        showManageDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.edit") }}</span>
                </v-tooltip>
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-delete"
                      size="small"
                      v-bind="props"
                      @click="
                        institution = { ...item };
                        processType = 'delete';
                        showManageDialog = true;
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.delete") }}</span>
                </v-tooltip>
              </td>
            </tr>
            <tr v-if="institutions && !institutions.length">
              <td colspan="3">
                {{ $t("institution.no_institutions_available") }}
              </td>
            </tr>
          </tbody>
        </v-table>
        <UIAlert
          v-if="hasLoadingError"
          v-bind:isSuccessful="false"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-col>
    </v-row>
    <ManageInstitution
      v-if="showManageDialog"
      v-bind:item="institution"
      v-bind:processType="processType"
      @child-object="checkChildObject"
    />
  </v-container>
</template>

<script>
import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";
import { expInstitution } from "@/components/Institution/institution";

export default {
  components: {
    ManageInstitution: defineAsyncComponent(() =>
      import("@/components/Institution/ManageInstitution.vue")
    ),
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  setup() {
    const store = useStore();
    const { hasLoadingError } = useNotification();
    const processType = ref("");
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
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showManageDialog.value = false;
      }
      if (e.hasChanges) {
        getInstitutions();
      }
    };
    const resetUser = () => {
      institution.value = { ...expInstitution };
    };
    return {
      resetUser,
      hasLoadingError,
      showManageDialog,
      institution,
      checkChildObject,
      institutions,
      processType,
    };
  },
};
</script>
