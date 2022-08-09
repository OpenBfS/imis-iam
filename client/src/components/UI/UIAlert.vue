<!--
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
 -->
<template>
  <div>
    <v-alert v-if="isSuccessful" variant="contained-text" type="success" text>
      {{ message }}
    </v-alert>
    <v-alert v-else variant="contained-text" type="error" text>
      {{ message }}
    </v-alert>
  </div>
</template>

<script>
export default {
  props: {
    isSuccessful: Boolean,
    message: String,
  },
};
</script>
