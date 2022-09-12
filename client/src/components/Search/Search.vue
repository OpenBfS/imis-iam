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
        prepend-inner-icon="mdi-magnify"
        class="v-col v-col-5"
        v-model="searchString"
        clearable
        density="comfortable"
      >
      </v-text-field>
    </v-row>
    <v-row>
      <Results class="v-col v-col-11" />
    </v-row>
  </v-container>
</template>

<script>
import { ref, defineAsyncComponent, watch } from "vue";
import { debounce } from "debounce";
import { useStore } from "vuex";

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
    const store = new useStore();
    const searchString = ref("");
    const searchRequest = () => {
      // Simulate the search request
      // ToDo: Replace this with the right request for search
      // once this gets implemnted in backend
      Promise.all([
        store.dispatch("user/loadUsers"),
        store.dispatch("institution/loadInstitutions"),
      ]).then(() => {
        store.commit("user/setFoundUsers", store.state.user.users.slice(0, 3));
        store.commit(
          "institution/setFoundInstitutions",
          store.state.institution.institutions.slice(0, 3)
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
    return { searchString };
  },
};
</script>
