<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("main.title") }}
    </UIHeader>
    <v-row class="mt-4">
      <v-col cols="4">
        <Notification />
      </v-col>
      <v-col cols="8">
        <!-- TODO: Calendar feature delayed to a subsequent date
        <Calendar />
        -->
      </v-col>
    </v-row>
    <v-row justify="space-between" class="my-2">
      <v-col>
        <ApplicationComponent />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup></script>
