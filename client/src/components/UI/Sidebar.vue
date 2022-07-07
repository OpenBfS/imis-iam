<template>
  <v-navigation-drawer
    permanent
    floating
    width="150"
    location="right"
    :clipped="true"
  >
    <v-list style="margin-top: 90px" nav>
      <v-list-item @click="$route.path == '/' ? '' : $router.push('/')">
        <v-list-item-title> Home</v-list-item-title></v-list-item
      >
      <v-list-item
        @click="
          $route.path == '/mailinglist' ? '' : $router.push('/mailinglist')
        "
      >
        <v-list-item-title>
          {{ $t("mailinglist.title") }}</v-list-item-title
        ></v-list-item
      >
      <v-list-item
        @click="$route.path == '/users' ? '' : $router.push('/users')"
        :title="$t('main.users')"
      ></v-list-item>
      <v-list-item
        @click="
          $route.path == '/institutions' ? '' : $router.push('/institutions')
        "
        :title="$t('main.institutions')"
      ></v-list-item>
      <v-list-item
        :title="$t('archive.title')"
        @click="$route.path == '/archive' ? '' : $router.push('/archive')"
      ></v-list-item>
      <v-list-item disabled class="ml-2" title="2022"></v-list-item>
      <v-list-item disabled class="ml-2" title="2021"></v-list-item>
      <v-list-item disabled class="ml-2" title="2020"></v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

export default {};
</script>

<style></style>
