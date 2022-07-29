<template>
  <v-app-bar dark id="theme" app clipped-left>
    <v-toolbar-title
      style="cursor: pointer"
      @click="$route.path == '/' ? '' : $router.push('/')"
    >
      {{ $t("appbar.title") }}
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <div>
      {{ $t("appbar.text_login") }} {{ userData.firstName }}
      {{ userData.lastName }} ({{ userData.username }})
    </div>
    <v-menu left>
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item link @click="showManageUserDialog = true">
          {{ $t("appbar.button_profile") }}
        </v-list-item>
        <v-list-item link @click="logout">
          {{ $t("appbar.button_logout") }}</v-list-item
        >
      </v-list>
    </v-menu>
  </v-app-bar>
  <ManageUser
    v-if="showManageUserDialog"
    v-bind:processType="'edit'"
    v-bind:copiedItem="{ ...userData }"
    v-bind:item="{ ...user }"
    @child-object="checkChildObject"
  />
</template>
<style scoped></style>
<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, defineAsyncComponent, ref } from "vue";
import { ShibHTTP } from "../../lib/http";
import { useStore } from "vuex";

export default {
  components: {
    ManageUser: defineAsyncComponent(() =>
      import("@/components/User/ManageUser.vue")
    ),
  },
  setup() {
    const store = useStore();
    const userData = computed(() => {
      return store.state.profile.userData;
    });
    const user = computed(() => {
      return { ...userData.value };
    });
    const checkChildObject = (e) => {
      if (e.closeDialog) {
        showManageUserDialog.value = false;
      }
      if (e.hasChanges) {
        store.dispatch("user/loadProfile");
      }
    };
    const showManageUserDialog = ref(false);
    const logout = () => {
      console.log("logout");
      ShibHTTP.get("Shibboleth.sso/Logout").then((response) => {
        console.log(response);
        window.location.reload();
      });
    };
    return {
      showManageUserDialog,
      user,
      userData,
      logout,
      checkChildObject,
    };
  },
};
</script>
