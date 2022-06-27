<template>
  <v-container>
    <v-row class="bg-secondary my-2" style="position: relative">
      <v-col cols="12" class="text-h4">
        {{ $t("main.title") }}
      </v-col>
      <div style="position: absolute; top: 100px; right: 2px">
        <v-col>
          <v-btn
            class="text-none"
            variant="plain"
            @click="
              $route.path == '/mailinglist' ? '' : $router.push('/mailinglist')
            "
          >
            {{ $t("mailinglist.title") }}
          </v-btn>
        </v-col>
      </div>
    </v-row>
    <v-row class="mt-4">
      <v-col cols="8">
        <NOTIFICATION />
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
  setup() {},
};
</script>
