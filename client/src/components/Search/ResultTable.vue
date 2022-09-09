<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <v-list density="compact" nav lines="">
      <v-list-item-subtitle>
        {{
          `${
            type === "users"
              ? $t("search.found_users")
              : $t("search.found_institution")
          }`
        }}
      </v-list-item-subtitle>
      <InstitutionTable
        v-if="type != 'users'"
        v-bind:institutions="$store.state.institution.foundInstitutions"
      />
      <UserTable v-else v-bind:users="$store.state.user.foundUsers" />
    </v-list>
  </v-container>
</template>

<script>
import { computed, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
export default {
  components: {
    InstitutionTable: defineAsyncComponent(() =>
      import("@/components/Institution/InstitutionTable.vue")
    ),
    UserTable: defineAsyncComponent(() =>
      import("@/components/User/UserTable.vue")
    ),
  },
  props: {
    type: String,
    default: () => "users",
  },
  setup(props) {
    const store = useStore();
    const items = computed(() => {
      if (props.type === "users") {
        return store.state.user.foundUsers;
      } else {
        return store.state.institution.foundInstitutions;
      }
    });
    const cloneObject = (obj) => {
      return JSON.parse(JSON.stringify(obj));
    };
    const selectItem = (item) => {
      store.commit("application/setManagedItem", cloneObject(item));
      store.commit("application/setProcessType", "edit");
      if (props.type == "users") {
        store.commit("application/setShowManageUserDialog", true);
      } else {
        store.commit("application/setShowManageInstitutionDialog", true);
      }
    };
    return {
      items,
      selectItem,
    };
  },
};
</script>
