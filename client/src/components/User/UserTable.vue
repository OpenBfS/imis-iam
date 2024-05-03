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
    :no-data-text="$t('user.no_users_available')"
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
import { ref } from "vue";
import { getExpUser } from "@/components/User/user";
import { useI18n } from "vue-i18n";
import DataTableServer from "@/components/DataTableServer.vue";

const { t } = useI18n();

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
const savedUser = ref();

const tableHeaders = [
  {
    title: t("user.username"),
    key: "username",
    value: (item) => getUserAttribute(item, "username"),
    sortable: false,
  },
  {
    title: t("user.firstname"),
    key: "firstName",
    value: (item) => getUserAttribute(item, "firstName"),
    sortable: false,
  },
  {
    title: t("user.lastname"),
    key: "lastName",
    value: (item) => getUserAttribute(item, "lastName"),
    sortable: false,
  },
  {
    title: t("user.email"),
    key: "email",
    value: (item) => getUserAttribute(item, "email"),
    sortable: false,
  },
  {
    title: t("user.phone"),
    key: "phone",
    value: (item) => getUserAttribute(item, "phone"),
    sortable: false,
  },
  {
    title: t("user.label_memberships"),
    key: "label_memberships",
    value: (item) => item.groups?.join(", "),
    sortable: false,
  },
  { title: t("label.actions"), key: "actions", sortable: false },
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
