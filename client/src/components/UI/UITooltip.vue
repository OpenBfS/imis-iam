<!--
 Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-tooltip :location="props.location ?? 'top'">
    <template v-slot:activator="{ props }">
      <span v-bind="props">
        <slot></slot>
      </span>
    </template>
    <span>{{ props.tooltipText }}</span>
  </v-tooltip>
</template>

<script setup>
const props = defineProps([
  // Vuetify props
  "location",

  // Custom props
  "tooltipText",
]);
</script>
