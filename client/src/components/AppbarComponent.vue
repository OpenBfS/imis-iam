<template>
  <v-app-bar dark id="theme" app clipped-left>
    <v-toolbar-title @click="$route.path == '/' ? '' : $router.push('/')">
      {{ $t("appbar.title") }}
    </v-toolbar-title>
    <v-spacer></v-spacer>
    <div>
      {{ $t("appbar.text_login") }} {{ userData.firstName }}
      {{ userData.lastName }} ({{ userData.username }})
    </div>
    <v-menu right :offset-x="true">
      <template v-slot:activator="{ props }">
        <v-btn icon v-bind="props">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list dense>
        <v-list-item
          link
          @click="$route.path == '/profile' ? '' : $router.push('/profile')"
        >
          {{ $t("appbar.button_profile") }}
        </v-list-item>
        <v-list-item link @click="logout">
          {{ $t("appbar.button_logout") }}</v-list-item
        >
      </v-list>
    </v-menu>
  </v-app-bar>
</template>
<style scoped></style>
<script>
import { computed, onMounted } from "vue";
import { ShibHTTP } from "../lib/http";
import profileStore from "../store";

export default {
  setup() {
    const userData = computed(() => {
      return profileStore.state.profile.userData;
    });
    onMounted(() => {
      profileStore.dispatch("profile/loadProfile");
    });
    const logout = () => {
      console.log("logout");
      ShibHTTP.get("Shibboleth.sso/Logout").then((response) => {
        console.log(response);
        window.location.reload();
      });
    };
    return {
      userData,
      logout,
    };
  },
};
</script>
