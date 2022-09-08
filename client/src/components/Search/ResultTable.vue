<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card>
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
      <v-list-item
        v-for="item in items"
        :key="item.id"
        :value="item"
        active-color="accent"
        @click="selectItem(item)"
      >
        <v-list-item-title>
          {{ type == "users" ? item.username : item.name }}
        </v-list-item-title>
      </v-list-item>
    </v-list>
  </v-card>
</template>

<script>
import { computed } from "@vue/runtime-core";
import { useStore } from "vuex";
export default {
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
