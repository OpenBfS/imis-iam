<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("label.search") }}
    </UIHeader>
    <v-row justify="space-between" class="mt-6">
      <v-col col="8" sm="7" md="4" lg="4" xl="3" xxl="2">
        <v-text-field
          prepend-inner-icon="mdi-magnify"
          v-model="applicationStore.searchString"
          clearable
          density="compact"
        >
        </v-text-field>
      </v-col>
      <v-spacer></v-spacer>
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-tag-edit"
            :disabled="
              (selectedTab === 'users' &&
                userStore.selectedUsers.length === 0) ||
              (selectedTab === 'institutions' &&
                institutionStore.selectedInstitutions.length === 0)
            "
            @click="toggleEditTags"
          >
          </v-btn>
        </template>
        <span>{{ $t("search.edit_tags") }}</span>
      </v-tooltip>
      <v-tooltip v-if="selectedTab === 'users'" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-if="profileStore.isAllowedToManage"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-account-plus"
            @click="
              applicationStore.setManagedItem(getExpUser());
              applicationStore.setProcessType('add');
              applicationStore.setShowManageUserDialog(true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("user.add_user") }}</span>
      </v-tooltip>
      <v-tooltip v-if="selectedTab === 'institutions'" location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            v-if="isAllowedToAdd"
            class="mr-4"
            v-bind="props"
            @click="
              applicationStore.setManagedItem(getExpInstitution());
              applicationStore.setProcessType('add');
              applicationStore.setShowManageInstitutionDialog(true);
            "
            icon="mdi-office-building-plus"
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
              applicationStore.setlistToExport(selectedTab);
              applicationStore.setShowExportDialog(true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("user.export") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <Results @onSelectedTab="onSelectedTab" class="v-col v-col-11" />
    </v-row>
    <UIAlert
      v-if="hasLoadingError || hasRequestError"
      v-bind:message="applicationStore.httpErrorMessage"
    />
  </v-container>
  <EditTags
    :close="() => toggleEditTags()"
    :isActive="showEditTags"
    :type="selectedTab"
  ></EditTags>
</template>

<script setup>
import { computed, ref, defineAsyncComponent, onMounted, watch } from "vue";
import debounce from "debounce";
import { useNotification } from "@/lib/use-notification.js";
import { useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";
import { useProfileStore } from "@/stores/profile.js";
import { getExpInstitution } from "@/components/Institution/institution.js";
import { getExpUser } from "@/components/User/user.js";
import EditTags from "@/components/UI/EditTags.vue";

const { hasLoadingError, hasRequestError } = useNotification();

const Results = defineAsyncComponent(() =>
  import("@/components/Search/Results.vue")
);
const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();
const profileStore = useProfileStore();
const isAllowedToAdd = computed(() => {
  return profileStore.isAllowedToManage;
});
const selectedTab = ref("users");
const showEditTags = ref(false);
const triggerSearch = debounce(() => {
  applicationStore.searchRequest(["users", "institutions"], true);
}, 500);
watch(
  () => applicationStore.searchString,
  (oldV, newV) => {
    if (oldV !== newV) {
      triggerSearch();
    }
  }
);
onMounted(() => {
  if (!userStore.roles) {
    userStore.loadRoles();
  }
  Promise.all([
    userStore.loadUsers(),
    institutionStore.loadInstitutions(),
  ]).catch(() => {
    hasLoadingError.value = true;
  });
});
function onSelectedTab(tab) {
  selectedTab.value = tab;
}

const toggleEditTags = () => {
  showEditTags.value = !showEditTags.value;
};
</script>
