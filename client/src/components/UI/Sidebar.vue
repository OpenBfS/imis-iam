<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
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
        <v-list-item-title> Home</v-list-item-title>
      </v-list-item>
      <v-list-item
        @click="$route.path == '/search' ? '' : $router.push('/search')"
      >
        <v-list-item-title> {{ $t("label.search") }}</v-list-item-title>
      </v-list-item>
      <v-list-item
        @click="
          $route.path == '/mailinglist' ? '' : $router.push('/mailinglists')
        "
      >
        <v-list-item-title> {{ $t("mailinglist.title") }}</v-list-item-title>
      </v-list-item>
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
        @click="
          $route.path == '/archive/all' ? '' : $router.push('/archive/all')
        "
      ></v-list-item>
      <v-list-item
        @click="
          $route.path == `/archive/${currentYear}`
            ? ''
            : $router.push(`/archive/${currentYear}`)
        "
        class="ml-2"
        :title="currentYear"
      ></v-list-item>
      <v-list-item
        @click="
          $route.path == `/archive/${currentYear - 1}`
            ? ''
            : $router.push(`/archive/${currentYear - 1}`)
        "
        class="ml-2"
        :title="currentYear - 1"
      ></v-list-item>
      <v-list-item
        @click="
          $route.path == `/archive/${currentYear - 2}`
            ? ''
            : $router.push(`/archive/${currentYear - 2}`)
        "
        class="ml-2 mb-16"
        :title="currentYear - 2"
      ></v-list-item>
      <v-list-item
        disabled
        class="ml-2 mt-16"
        :title="$t('label.faq')"
      ></v-list-item>
      <v-list-item
        link
        :href="`mailto:${reportMail}`"
        class="ml-2"
        :title="$t('label.report_problem')"
      ></v-list-item>
    </v-list>
  </v-navigation-drawer>
</template>

<script>
import { computed } from "vue";
import { useStore } from "vuex";
export default {
  setup() {
    const currentYear = new Date().getFullYear();
    const store = useStore();
    const reportMail = computed(() => {
      return store.state.application.reportMail;
    });
    return {
      reportMail,
      currentYear,
    };
  },
};
</script>

<style></style>
