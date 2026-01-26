<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <div class="d-flex justify-end my-1">
    <UITooltip :tooltipText="$t('search.editTags')">
      <v-btn
        color="accent"
        class="ms-4"
        icon="mdi-tag-edit"
        :disabled="
          (selectedTab === 'users' && userStore.selectedUsers.length === 0) ||
          (selectedTab === 'institutions' &&
            institutionStore.selectedInstitutions.length === 0)
        "
        @click="toggleEditTags"
      >
      </v-btn>
    </UITooltip>
    <UITooltip v-if="selectedTab === 'users'" :tooltipText="$t('user.addUser')">
      <v-btn
        v-if="profileStore.isAllowedToManage"
        color="accent"
        class="ms-4"
        icon="mdi-account-plus"
        @click="
          applicationStore.setManagedItem(getExpUser());
          applicationStore.setProcessType(PROCESS_TYPE.ADD);
          applicationStore.setShowManageUserDialog(true);
        "
      >
      </v-btn>
    </UITooltip>
    <UITooltip
      v-if="selectedTab === 'institutions'"
      :tooltipText="$t('institution.addInstitution')"
    >
      <v-btn
        color="accent"
        v-if="isAllowedToAdd"
        class="ms-4"
        @click="
          applicationStore.setManagedItem(getExpInstitution());
          applicationStore.setProcessType(PROCESS_TYPE.ADD);
          applicationStore.setShowManageInstitutionDialog(true);
        "
        icon="mdi-office-building-plus"
      >
      </v-btn>
    </UITooltip>
    <UITooltip :tooltipText="$t('user.export')">
      <v-btn
        color="accent"
        class="ms-4"
        icon="mdi-download"
        @click="
          applicationStore.setlistToExport(selectedTab);
          applicationStore.setShowExportDialog(true);
        "
      >
      </v-btn>
    </UITooltip>
    <ColumnSelection :type="selectedTab"></ColumnSelection>
  </div>
  <Results @onSelectedTab="onSelectedTab" />
  <UIAlert
    v-if="hasLoadingError || hasRequestError"
    v-bind:message="applicationStore.httpErrorMessage"
  />
  <EditTags
    :close="() => toggleEditTags()"
    :isActive="showEditTags"
    :type="selectedTab"
  ></EditTags>
</template>

<script setup>
import { computed, ref, onMounted } from "vue";
import { useNotification } from "@/lib/use-notification.js";
import { PROCESS_TYPE, useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";
import { useProfileStore } from "@/stores/profile.js";
import { getExpInstitution } from "@/components/Institution/institution.js";
import { getExpUser } from "@/components/User/user.js";
import EditTags from "@/components/UI/EditTags.vue";
import UITooltip from "@/components/UI/UITooltip.vue";

const { hasLoadingError, hasRequestError } = useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const userStore = useUserStore();
const profileStore = useProfileStore();
const isAllowedToAdd = computed(() => {
  return profileStore.isAllowedToManage;
});
const selectedTab = ref("users");
const showEditTags = ref(false);

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
