<template>
  <v-app>
    <Appbar />
    <v-main>
      <router-view />
    </v-main>
  </v-app>
</template>

<script>
import Appbar from "./components/AppbarComponent.vue";

export default {
  name: "App",
  components: {
    Appbar,
  },
  data: () => ({
    //
  }),
};
</script>
