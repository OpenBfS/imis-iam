<template>
  <v-container>
    <v-row class="bg-secondary my-2">
      <v-col cols="12" class="text-h4">
        {{ $t("main.title") }}
      </v-col>
    </v-row>
    <v-row class="mt-4" v-if="notifications.length">
      <v-col
        v-for="(notification, index) in notifications"
        cols="8"
        :key="index"
      >
        <NOTIFICATION
          v-bind:message="notification.text"
          v-bind:isWarning="notification.type === 'warn'"
        />
      </v-col>
    </v-row>
    <v-row justify="space-between" class="my-2">
      <v-col>
        <Applications />
      </v-col>
      <v-col>
        <v-btn
          link
          @click="
            $route.path == '/institutions' ? '' : $router.push('/institutions')
          "
        >
          {{ $t("main.institutions") }}
        </v-btn>
        <v-btn
          link
          @click="$route.path == '/users' ? '' : $router.push('/users')"
        >
          {{ $t("main.users") }}
        </v-btn>
        <Archive />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
// Components
import Applications from "../components/ApplicationComponent.vue";
import Archive from "../components/ArchiveComponent.vue";
import { defineAsyncComponent } from "vue";
export default {
  name: "Home",
  components: {
    Applications,
    Archive,
    NOTIFICATION: defineAsyncComponent(() =>
      import("../components/Notification.vue")
    ),
  },
  setup() {
    // TODO: Replace following fake data with comming data from HTTP request
    const notifications = [
      {
        type: "warn",
        text: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        date: new Date(),
      },
      {
        type: "message",
        text: "(2)Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.",
        date: new Date("1995-12-17T03:24:00"),
      },
    ];
    return {
      notifications,
    };
  },
};
</script>
