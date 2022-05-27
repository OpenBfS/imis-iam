<template>
  <v-container>
    <v-row class="ma-5 pa-5">
      <Main />
    </v-row>
    <v-row justify="space-between">
      <v-col>
        <Notifications />
        <Applications />
      </v-col>
      <v-col>
        <v-btn
          link
          @click="
            $route.path == '/institutions' ? '' : $router.push('/institutions')
          "
        >
          Institutions
        </v-btn>
        <v-btn
          link
          @click="$route.path == '/users' ? '' : $router.push('/users')"
        >
          Users
        </v-btn>
        <Archive />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
import { defineComponent } from "vue";

// Components
import Main from "../components/Main.vue";
import Applications from "../components/ApplicationComponent.vue";
import Archive from "../components/ArchiveComponent.vue";
import Notifications from "../components/NotificationComponent.vue";
export default defineComponent({
  name: "HomeView",

  components: {
    Main,
    Applications,
    Archive,
    Notifications,
  },
});
</script>
