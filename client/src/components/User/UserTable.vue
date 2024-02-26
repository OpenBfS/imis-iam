<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-table class="ma-2 pa-2">
    <thead>
      <th class="text-left">{{ $t("user.username") }}</th>
      <th class="text-left">{{ $t("user.firstname") }}</th>
      <th class="text-left">{{ $t("user.lastname") }}</th>
      <th class="text-left">{{ $t("label.email") }}</th>
      <th class="text-left">{{ $t("user.phone") }}</th>
      <th class="text-left">{{ $t("user.label_memberships") }}</th>
      <th class="text-left">{{ $t("label.actions") }}</th>
    </thead>
    <tbody>
      <tr v-for="user in props.users" :key="user.id">
        <td>{{ getUserAttribute(user, "username") }}</td>
        <td>{{ getUserAttribute(user, "firstName") }}</td>
        <td>{{ getUserAttribute(user, "lastName") }}</td>
        <td>{{ getUserAttribute(user, "email") }}</td>
        <td>{{ getUserAttribute(user, "phone") }}</td>
        <td>{{ user.groups?.join(", ") }}</td>
        <td class="d-flex">
          <v-tooltip location="top">
            <template v-slot:activator="{ props }">
              <v-btn
                variant="plain"
                :icon="`${
                  profileStore.isAllowedToManage
                    ? 'mdi-account-edit-outline'
                    : 'mdi-information-outline'
                }`"
                size="small"
                v-bind="props"
                @click="onEditClicked(user.id)"
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
                @click="onCopyClicked(user.id)"
              ></v-btn>
            </template>
            <span>{{ $t("label.copy") }}</span>
          </v-tooltip>
        </td>
      </tr>
    </tbody>
  </v-table>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { ref } from "vue";
import { expUser } from "@/components/User/user";
import { onMounted } from "vue";
const props = defineProps({
  users: Array,
});

function getUserAttribute(user, attributeName) {
  // Keycloak User Profile attributes are either missing (if no value is given)
  // or an array expected to contain a single value
  return user.attributes[attributeName]
    ? user.attributes[attributeName][0]
    : "";
}

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const userStore = useUserStore();
const savedUser = ref();

onMounted(() => {
  userStore.loadMemberships();
});

// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const getUserById = (id) => {
  return props.users.filter((u) => id === u.id)[0];
};
const user = ref(cloneObject(expUser));
const onCopyClicked = (id) => {
  user.value = cloneObject(getUserById(id));
  savedUser.value = cloneObject(user.value);
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
