<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-app>
    <Appbar />
    <Sidebar />
    <v-main>
      <router-view />
      <UIAlert
        v-if="hasLoadingError"
        v-bind:message="$store.state.application.httpErrorMessage"
      />
      <v-dialog v-model="$store.state.application.showExportDialog">
        <ExportDialog />
      </v-dialog>
      <v-dialog v-model="$store.state.application.showManageUserDialog">
        <ManageUser />
      </v-dialog>
      <v-dialog v-model="$store.state.application.showManageInstitutionDialog">
        <ManageInstitution />
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

<script>
import { defineAsyncComponent, onMounted } from "vue";
import { useStore } from "vuex";
import { useNotification } from "./lib/use-notification";

export default {
  components: {
    ManageUser: defineAsyncComponent(() =>
      import("@/components/User/ManageUser.vue")
    ),
    ManageInstitution: defineAsyncComponent(() =>
      import("@/components/Institution/ManageInstitution.vue")
    ),
    Sidebar: defineAsyncComponent(() => import("@/components/UI/Sidebar.vue")),
    Appbar: defineAsyncComponent(() => import("@/components/UI/Appbar.vue")),
    Appfooter: defineAsyncComponent(() =>
      import("@/components/UI/Appfooter.vue")
    ),
    ExportDialog: defineAsyncComponent(() =>
      import("@/components/UI/ExportDialog.vue")
    ),
  },
  name: "App",
  setup() {
    const store = useStore();
    const { hasLoadingError } = useNotification();
    onMounted(() => {
      store
        .dispatch("profile/loadProfile")
        .then(() => {})
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    return {
      hasLoadingError,
    };
  },
};
</script>
