<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("user.title") }}
      </v-col>
    </v-row>
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
              processType = 'add';
              showManageUserDialog = true;
            "
          >
          </v-btn>
        </template>
        <span>{{ $t("user.add_user") }}</span>
      </v-tooltip>
    </v-row>
    <v-row>
      <v-col cols="12" class="mt-6">
        <v-table class="ma-2 pa-2">
          <thead>
            <th class="text-left">{{ $t("label.id") }}</th>
            <th class="text-left">{{ $t("label.username") }}</th>
            <th class="text-left">{{ $t("label.firstname") }}</th>
            <th class="text-left">{{ $t("label.lastname") }}</th>
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
    <ManageUser
      v-if="showManageUserDialog"
      v-bind:processType="processType"
      v-bind:copiedItem="savedUser"
      v-bind:item="{ ...user }"
      @child-object="checkChildObject"
    />
  </v-container>
</template>
<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("./UI/UIAlert.vue")),
    ManageUser: defineAsyncComponent(() =>
      import("@/components/User/ManageUser.vue")
    ),
  },
  setup() {
    const store = useStore();
    const { hasLoadingError, resetNotification } = useNotification();
    const users = computed(() => {
      return store.state.user.users;
    });
    onMounted(() => {
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
      store
        .dispatch("institution/loadInstitutions")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    const processType = ref("");
    const onCopyClicked = (id) => {
      savedUser.value = users.value.filter((u) => id === u.id)[0];
      user.value = { ...savedUser.value };
      delete user.value["id"];
      processType.value = "copy";
      savedUser.value = { ...user.value };
      showManageUserDialog.value = true;
    };
    const onEditClicked = (id) => {
      user.value = {
        ...users.value.filter((u) => id === u.id)[0],
      };
      // Save original user data for "reset" button
      savedUser.value = { ...user.value };
      processType.value = "edit";
      showManageUserDialog.value = true;
    };
    const user = ref({
      id: "",
      username: "",
      firstName: "",
      lastName: "",
      email: "",
      groups: [],
    });
    const resetUser = () => {
      user.value.id = "";
      user.value.username = "";
      user.value.firstName = "";
      user.value.lastName = "";
      user.value.email = "";
      user.value.groups = [];
    };
    const savedUser = ref();
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showManageUserDialog.value = false;
      }
      if (e.hasChanges) {
        resetUser();
        store.dispatch("user/loadUsers");
      }
    };
    const showManageUserDialog = ref(false);
    return {
      checkChildObject,
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
