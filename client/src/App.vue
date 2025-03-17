<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-app>
    <div
      class="d-flex flex-column flex-grow-1 h-100 overflow-y-hidden"
      style="max-height: 100vh"
    >
      <Appbar />
      <div class="h-100 pb-8 pa-3 overflow-y-hidden flex-grow-1">
        <v-main class="d-flex flex-column pt-0 h-100 overflow-y-hidden">
          <router-view />
          <UIAlert
            v-if="hasLoadingError"
            v-bind:message="applicationStore.httpErrorMessage"
          />
          <v-dialog v-model="applicationStore.showExportDialog">
            <ExportDialog />
          </v-dialog>
          <v-dialog v-model="applicationStore.showManageUserDialog">
            <ManageUser />
          </v-dialog>
          <v-dialog v-model="applicationStore.showManageInstitutionDialog">
            <ManageInstitution />
          </v-dialog>
          <v-dialog v-model="applicationStore.showManageEventDialog">
            <ManageEvent />
          </v-dialog>
          <v-dialog v-model="applicationStore.showSessionExpiredDialog">
            <SessionExpiredDialog />
          </v-dialog>
        </v-main>
      </div>
      <Appfooter />
    </div>
  </v-app>
</template>

<style lang="scss" scoped>
::v-deep(.v-card) {
  align-self: center;
}
</style>

<script setup>
import { onMounted } from "vue";
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { useNotification } from "./lib/use-notification.js";

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();

const { hasLoadingError } = useNotification();

onMounted(() => {
  profileStore.loadUserProfileMetadata().catch(() => {
    hasLoadingError.value = true;
  });
  profileStore.loadProfile().catch(() => {
    hasLoadingError.value = true;
  });
});
</script>
