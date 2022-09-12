<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-row>
    <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
      {{ title }}
    </v-col>
  </v-row>
</template>

<script>
export default {
  props: {
    title: String,
  },
};
</script>

<style></style>
