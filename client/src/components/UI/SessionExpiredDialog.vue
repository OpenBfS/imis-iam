<!--
 Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->

<template>
  <v-card width="50vw" class="mx-auto" :title="$t('label.sessionExpired')">
    <v-card-text> {{ $t("label.sessionExpiredExplanation") }} </v-card-text>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn color="accent" @click="window.location.reload()">
        {{ $t("button.reloadPage") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="applicationStore.setShowSessionExpiredDialog(false)"
      >
        {{ $t("button.close") }}
      </v-btn>
    </v-card-actions>
  </v-card>
</template>

<script setup>
import { useApplicationStore } from "@/stores/application.js";

const applicationStore = useApplicationStore();
</script>
