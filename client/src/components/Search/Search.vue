<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <UIHeader>
      {{ $t("label.search") }}
    </UIHeader>
    <v-row justify="start" class="mt-6">
      <v-text-field
        prepend-inner-icon="mdi-magnify"
        class="v-col v-col-5"
        v-model="searchString"
        clearable
        density="compact"
      >
      </v-text-field>
    </v-row>
    <v-row>
      <Results class="v-col v-col-11" />
    </v-row>
  </v-container>
</template>

<script setup>
import { ref, defineAsyncComponent, onMounted, watch } from "vue";
import { debounce } from "debounce";
import { useStore } from "vuex";

const Results = defineAsyncComponent(() =>
  import("@/components/Search/Results.vue")
);
const store = new useStore();
const searchString = ref("");
const searchRequest = () => {
  Promise.all([
    store.dispatch("user/loadUsers", searchString.value),
    store.dispatch("institution/loadInstitutions", searchString.value),
  ]).then(() => {
    store.commit("user/setFoundUsers", store.state.user.users);
    store.commit(
      "institution/setFoundInstitutions",
      store.state.institution.institutions
    );
  });
};
const triggerSearch = debounce(() => {
  searchRequest();
}, 500);
watch(
  () => searchString.value,
  (oldV, newV) => {
    if (oldV !== newV) {
      triggerSearch();
    }
  }
);
onMounted(() => {
  store.dispatch("user/loadMemberships").then(() => {
    searchRequest();
  });
});
</script>
