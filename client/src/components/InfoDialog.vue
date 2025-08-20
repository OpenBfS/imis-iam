<!--
 Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card min-width="460px">
    <v-container class="pa-1 mt-4 mx-2">
      <div>Software-Version 1.0.2</div>
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" size="small" @click="close">
        {{ $t("button.close") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application.js";

const applicationStore = useApplicationStore();

const close = () => {
  applicationStore.showInfoDialog = false;
};
</script>
