<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <DataTableServer
    :headers="userStore.tableHeaders"
    :items="props.users"
    :items-per-page="userStore.itemsPerPage"
    :no-data-text="$t('user.noUsersAvailable')"
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
              profileStore.isAllowedToManage &&
              (item.network === profileStore.userData.network ||
                profileStore.userData.role === 'chief_editor')
                ? 'mdi-pencil'
                : 'mdi-information-outline'
            }`"
            size="small"
            v-bind="props"
            @click="onEditClicked(item.id)"
          ></v-btn>
        </template>
        <span>{{
          profileStore.isAllowedToManage &&
          (item.network === profileStore.userData.network ||
            profileStore.userData.role === "chief_editor")
            ? $t("label.edit")
            : $t("label.showInfo")
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
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import { onMounted, ref, toRaw } from "vue";
import { getExpUser } from "@/components/User/user.js";
import DataTableServer from "@/components/DataTableServer.vue";
import { createHeaders, initSelectedColumns } from "@/lib/utils";

const props = defineProps({
  // columns only used to insert different columns for testing
  columns: Array,
  users: Array,
});

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();
const savedUser = ref();

onMounted(async () => {
  if (!profileStore.attributes) {
    await profileStore.loadUserProfileMetadata();
  }
  const columns = props.columns
    ? props.columns
    : profileStore.attributes
      ? [
          ...profileStore.attributes.map((attr) => {
            const rawAttr = toRaw(attr);
            return {
              name: rawAttr.name,
              default:
                attr.annotations?.defaultField &&
                Boolean(attr.annotations?.defaultField) === true,
            };
          }),
          { name: "institutions", default: true },
          { name: "role", default: false },
          { name: "network", default: false },
          { name: "enabled", default: false },
        ]
      : [];
  userStore.tableHeaders = createHeaders(columns, "users");
  initSelectedColumns("users");
});

// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const getUserById = (id) => {
  return props.users.filter((u) => id === u.id)[0];
};
const user = ref(getExpUser());
const onCopyClicked = (id) => {
  const network = structuredClone(user.value).network;
  user.value = cloneObject(getUserById(id));
  // Keep network of example user because other roles than chief redakteur are not allowed
  // to set other network values.
  user.value.network = network;
  savedUser.value = cloneObject(user.value);
  user.value.attributes.title = undefined;
  user.value.attributes.username = undefined;
  user.value.attributes.firstName = undefined;
  user.value.attributes.lastName = undefined;
  user.value.attributes.email = undefined;
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
