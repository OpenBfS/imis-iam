<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader v-bind:title="$t('user.user_title')" />
    <v-row justify="end" class="mt-6">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
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
        <v-table class="ma-2 pa-2">
          <thead>
            <th class="text-left">{{ $t("user.id") }}</th>
            <th class="text-left">{{ $t("user.username") }}</th>
            <th class="text-left">{{ $t("user.firstname") }}</th>
            <th class="text-left">{{ $t("user.lastname") }}</th>
            <th class="text-left">{{ $t("label.email") }}</th>
            <th class="text-left">{{ $t("label.actions") }}</th>
          </thead>
          <tbody>
            <tr v-for="user in users" :key="user.id">
              <td class="text-truncate" style="max-width: 150px">
                {{ user.id }}
              </td>
              <td>{{ user.username }}</td>
              <td>{{ user.firstName }}</td>
              <td>{{ user.lastName }}</td>
              <td>{{ user.email }}</td>
              <td class="d-flex">
                <v-tooltip location="top">
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-account-edit-outline"
                      size="small"
                      v-bind="props"
                      @click="
                        resetNotification();
                        onEditClicked(user.id);
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.edit") }}</span>
                </v-tooltip>
                <v-tooltip>
                  <template v-slot:activator="{ props }">
                    <v-btn
                      variant="plain"
                      icon="mdi-content-copy"
                      size="small"
                      v-bind="props"
                      @click="
                        resetNotification();
                        onCopyClicked(user.id);
                      "
                    ></v-btn>
                  </template>
                  <span>{{ $t("label.copy") }}</span>
                </v-tooltip>
              </td>
            </tr>
          </tbody>
        </v-table>
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
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
    UIHeader: defineAsyncComponent(() =>
      import("@/components/UI/UIHeader.vue")
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
    const savedUser = ref();
    const resetUser = () => {
      user.value = cloneObject(expUser);
    };

    onMounted(() => {
      resetUser();
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    // Handle requests
    const processType = ref("");
    const showManageUserDialog = ref(false);
    const onCopyClicked = (id) => {
      user.value = cloneObject(users.value.filter((u) => id === u.id)[0]);
      savedUser.value = cloneObject(users.value.filter((u) => id === u.id)[0]);
      user.value.email = "";
      user.value.username = "";
      delete user.value.attributes.id;
      delete user.value["id"];
      store.commit("application/setManagedItem", user.value);
      store.commit("application/setSavedItem", savedUser.value);
      store.commit("application/setProcessType", "copy");
      store.commit("application/setShowManageUserDialog", true);
    };
    const onEditClicked = (id) => {
      user.value = cloneObject(users.value.filter((u) => id === u.id)[0]);
      // Save original user data for "reset" button
      savedUser.value = cloneObject(user.value);
      store.commit("application/setManagedItem", user.value);
      store.commit("application/setSavedItem", savedUser.value);
      store.commit("application/setProcessType", "edit");
      store.commit("application/setShowManageUserDialog", true);
    };

    return {
      showManageUserDialog,
      resetNotification,
      hasLoadingError,
      onEditClicked,
      savedUser,
      user,
      resetUser,
      users,
      processType,
      onCopyClicked,
    };
  },
};
</script>
