<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <DataTableServer
    :headers="tableHeaders"
    :items="props.users"
    :items-per-page="userStore.itemsPerPage"
    :no-data-text="$t('user.no_users_available')"
    :offset="userStore.offset"
    :total-number-of-items="userStore.totalNumberOfUsers"
    type="users"
  >
    <template v-slot:[`item.actions`]="{ item }">
      <v-tooltip location="top">
        <template v-slot:activator="{ props }">
          <v-btn
            variant="plain"
            :icon="`${
              profileStore.isAllowedToManage
                ? 'mdi-pencil'
                : 'mdi-information-outline'
            }`"
            size="small"
            v-bind="props"
            @click="onEditClicked(item.id)"
          ></v-btn>
        </template>
        <span>{{
          profileStore.isAllowedToManage
            ? $t("label.edit")
            : $t("label.show_info")
        }}</span>
      </v-tooltip>
      <v-tooltip>
        <template
          v-slot:activator="{ props }"
          v-if="profileStore.isAllowedToManage"
        >
          <v-btn
            variant="plain"
            icon="mdi-content-copy"
            size="small"
            v-bind="props"
            @click="onCopyClicked(item.id)"
          ></v-btn>
        </template>
        <span>{{ $t("label.copy") }}</span>
      </v-tooltip>
    </template>
  </DataTableServer>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { ref, toRaw } from "vue";
import { getExpUser } from "@/components/User/user";
import DataTableServer from "@/components/DataTableServer.vue";

const props = defineProps({
  users: Array,
});

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();
const savedUser = ref();
const tableHeaders = [
  ...profileStore.attributes.map((attr) => toRaw(attr).name),
  "institutions",
  "roles",
  "enabled",
];

// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const getUserById = (id) => {
  return props.users.filter((u) => id === u.id)[0];
};
const user = ref(getExpUser());
const onCopyClicked = (id) => {
  user.value = cloneObject(getUserById(id));
  savedUser.value = cloneObject(user.value);
  user.value.attributes.title = "";
  user.value.attributes.username = "";
  user.value.attributes.firstName = "";
  user.value.attributes.lastName = "";
  user.value.attributes.email = "";
  user.value.enabled = false;
  delete user.value["id"];
  applicationStore.setManagedItem(user.value);
  applicationStore.setSavedItem(savedUser.value);
  applicationStore.setProcessType("copy");
  applicationStore.setShowManageUserDialog(true);
};
const onEditClicked = (id) => {
  user.value = cloneObject(getUserById(id));
  // Save original user data for "reset" button
  savedUser.value = cloneObject(user.value);
  applicationStore.setManagedItem(user.value);
  applicationStore.setSavedItem(savedUser.value);
  applicationStore.setProcessType("edit");
  applicationStore.setShowManageUserDialog(true);
};
</script>
