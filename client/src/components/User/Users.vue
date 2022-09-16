<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("user.user_title") }}
    </UIHeader>
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            v-if="$store.state.profile.isAllowedToManage"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-account-plus"
            @click="
              resetUser();
              resetNotification();
              $store.commit('application/setManagedItem', user);
              $store.commit('application/setProcessType', 'add');
              $store.commit('application/setShowManageUserDialog', true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("user.add_user") }}</span>
      </v-tooltip>
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-import"
            @click="
              $store.commit('application/setlistToExport', 'users');
              $store.commit('application/setShowExportDialog', true);
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("user.export") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="mt-6">
        <UserTable v-bind:users="users" />
        <UIAlert
          v-if="hasLoadingError"
          v-bind:isSuccessful="!hasLoadingError"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
</template>
<script>
import { onMounted, ref, defineAsyncComponent, computed } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useStore } from "vuex";
import { expUser } from "@/components/User/user";

export default {
  components: {
    UserTable: defineAsyncComponent(() =>
      import("@/components/User/UserTable.vue")
    ),
  },
  setup() {
    const store = useStore();
    const { hasLoadingError, resetNotification } = useNotification();
    // User
    // Deep Copy for objects
    const cloneObject = (obj) => {
      return JSON.parse(JSON.stringify(obj));
    };
    const users = computed(() => {
      return store.state.user.users;
    });
    const user = ref(cloneObject(expUser));
    const resetUser = () => {
      user.value = cloneObject(expUser);
    };

    onMounted(() => {
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
      store
        .dispatch("user/loadRoles")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    // Handle requests
    const showManageUserDialog = ref(false);

    return {
      showManageUserDialog,
      resetNotification,
      hasLoadingError,
      resetUser,
      user,
      users,
    };
  },
};
</script>
