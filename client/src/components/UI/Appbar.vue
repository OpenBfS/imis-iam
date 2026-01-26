<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-app-bar dark id="theme" app clipped-left class="position-static p-0 m-0">
    <v-toolbar-title
      style="cursor: pointer"
      @click="$route.path == '/' ? '' : $router.push('/')"
    >
      {{ $t("appbar.title") }}
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <v-list>
      <v-list-item
        base-color="accent"
        class="me-4"
        link
        :href="`mailto:${applicationStore.reportMail}`"
        :title="$t('label.reportProblem')"
      ></v-list-item>
    </v-list>
    <div>
      {{ $t("appbar.textLogin") }}
      {{
        savedUser.attributes
          ? savedUser.attributes.firstName[0] +
            " " +
            savedUser.attributes.lastName[0] +
            " (" +
            savedUser.attributes.username[0] +
            ")"
          : ""
      }}
    </div>
    <v-menu left>
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item link @click="editProfile">
          {{ $t("appbar.buttonProfile") }}
        </v-list-item>
        <v-list-item link @click="changePassword">
          {{ $t("appbar.buttonChangePassword") }}</v-list-item
        >
        <v-list-item link @click="logout">
          {{ $t("appbar.buttonLogout") }}</v-list-item
        >
      </v-list>
    </v-menu>
  </v-app-bar>
</template>
<style scoped></style>
<script setup>
import { useApplicationStore } from "@/stores/application.js";
import { useProfileStore } from "@/stores/profile.js";
import { computed } from "vue";

const applicationStore = useApplicationStore();
const profileStore = useProfileStore();
const savedUser = computed(() => {
  return profileStore.userData;
});

const logout = () => {
  window.location.assign(
    `${import.meta.env.BASE_URL}redirect_uri?logout=${window.location.origin}`,
  );
};

const changePassword = () => {
  let prefixXhr = new XMLHttpRequest();
  prefixXhr.responseType = 'json';
  prefixXhr.open('GET', '/backend/realms/imis3/');
  prefixXhr.onload = function() {
    let urlPrefix = prefixXhr.response['token-service'];
    window.location.assign(
      `${urlPrefix}/auth?client_id=iam-client&redirect_uri=${window.location.origin}${window.location.pathname}&response_type=code&kc_action=UPDATE_PASSWORD`
    );
  };
  prefixXhr.send();
};

const editProfile = () => {
  applicationStore.openUserEditForm(structuredClone(savedUser.value), true);
};
</script>
