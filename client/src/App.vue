<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-app>
    <Appbar />
    <v-main>
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
    </v-main>
    <Appfooter />
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
