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
            v-if="profileStore.isAllowedToManage"
            color="accent"
            class="mr-4"
            v-bind="props"
            icon="mdi-account-plus"
            @click="
              resetUser();
              resetNotification();
              applicationStore.setManagedItem(user);
              applicationStore.setProcessType('add');
              applicationStore.setShowManageUserDialog(true);
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
              applicationStore.setlistToExport('users');
              applicationStore.setShowExportDialog(true);
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
          v-bind:message="applicationStore.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
</template>
<script setup>
import { onMounted, ref, defineAsyncComponent, computed } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { expUser } from "@/components/User/user";

const UserTable = defineAsyncComponent(() =>
  import("@/components/User/UserTable.vue")
);

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();
const { hasLoadingError, resetNotification } = useNotification();
// User
// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const users = computed(() => {
  return userStore.users;
});
const user = ref(cloneObject(expUser));
const resetUser = () => {
  user.value = cloneObject(expUser);
};

onMounted(() => {
  userStore
    .loadUsers()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
  userStore
    .loadRoles()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
  userStore.loadMemberships();
});
</script>
