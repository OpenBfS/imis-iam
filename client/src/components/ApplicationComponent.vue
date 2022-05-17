<template>
  <v-card class="ma-2 pa-2">
    <v-card-title>Applications</v-card-title>
    <v-container>
      <v-row>
        <v-btn @click="openInTab(urls.exampleapp1)" class="ma-2 pa-2">
          <v-icon>mdi-application</v-icon>
          Example Application 1
        </v-btn>
        <v-btn @click="openInTab(urls.exampleapp2)" class="ma-2 pa-2">
          <v-icon>mdi-application</v-icon>
          Example Application 2
        </v-btn>
      </v-row>
    </v-container>
  </v-card>
</template>
<script>
export default {
  setup() {
    const openInTab = (url) => {
      window.open(url, "_blank").focus();
    };
    const urls = {
      exampleapp1: "https://exampleapp1.test",
      exampleapp2: "https://exampleapp2.test",
    };
    return {
      urls,
      openInTab,
    };
  },
};
</script>
