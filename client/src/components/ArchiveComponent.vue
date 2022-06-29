<template>
  <v-card class="ma-2 pa-2">
    <v-container>
      <v-row>
        <v-col cols="3">
          <v-card-title>{{ $t("archive.title") }}</v-card-title>
          <li v-for="date in dates" :key="date.year">
            {{ date.year }}
          </li>
        </v-col>
        <v-col align="left" justify="left">
          <v-btn :rounded="0" color="accent">{{ $t("archive.search") }}</v-btn>
        </v-col>
      </v-row>
    </v-container>
  </v-card>
</template>
<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

export default {
  setup() {
    const dates = [
      {
        year: 2022,
      },
      {
        year: 2021,
      },
      {
        year: 2020,
      },
    ];
    return {
      dates,
    };
  },
};
</script>
