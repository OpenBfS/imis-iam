<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card class="ma-2 pa-2">
    <v-card-title>{{ $t("application.title") }}</v-card-title>
    <v-container>
      <v-row>
        <div v-for="app in applications" :key="app.url">
          <v-btn
            :rounded="0"
            color="accent"
            @click="app.openInTab ? openInTab(app.url) : $router.push(app.url)"
            class="ma-2 pa-2"
          >
            <v-icon>{{ app.icon }}</v-icon>
            {{ app.name }}
          </v-btn>
        </div>
      </v-row>
    </v-container>
  </v-card>
</template>
<script setup>
import { useI18n } from "vue-i18n";

const { t } = useI18n();

const openInTab = (url) => {
  window.open(url, "_blank").focus();
};
const applications = [
  {
    name: t("main.userAddressManagement"),
    icon: "mdi-application",
    url: "/search",
    openInTab: true,
  },
  {
    name: "Example Application 1",
    icon: "mdi-application",
    url: "https://exampleapp1.test",
    openInTab: true,
  },
  {
    name: "Example Application 2",
    icon: "mdi-application",
    url: "https://exampleapp2.test",
    openInTab: true,
  },
];
</script>
