<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader v-bind:title="$t('label.search')" />
    <v-row justify="start" class="mt-6">
      <v-text-field
        class="v-col v-col-10"
        v-model="searchString"
        density="comfortable"
      >
      </v-text-field>
    </v-row>
    <v-row>
      <Results />
    </v-row>
  </v-container>
</template>

<script>
import { ref, defineAsyncComponent } from "vue";
export default {
  components: {
    UIHeader: defineAsyncComponent(() =>
      import("@/components/UI/UIHeader.vue")
    ),
    Results: defineAsyncComponent(() =>
      import("@/components/Search/Results.vue")
    ),
  },
  setup() {
    const searchString = ref("");
    const userTab = ref("");
    const institutionTab = ref();
    return { searchString, userTab, institutionTab };
  },
};
</script>
