<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog :model-value="props.isActive">
    <v-card width="50vw" class="mx-auto" title="Wirklich abbrechen?">
      <v-card-text> Alle Änderungen werden dabei verworfen. </v-card-text>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <v-btn color="accent" @click="props.onConfirm()"> Ja </v-btn>
        <v-btn color="accent" @click="props.onCancel()"> Nein </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup>
const props = defineProps(["onConfirm", "onCancel", "isActive"]);
</script>
