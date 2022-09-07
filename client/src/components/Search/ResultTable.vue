<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-card>
    <v-list density="compact" nav lines="">
      <v-list-item-subtitle
        >{{ type === "users" ? "Found users:" : "Found institutions:" }}
      </v-list-item-subtitle>
      <v-list-item
        v-for="(item, i) in items"
        :key="i"
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
import { computed, onMounted, ref } from "@vue/runtime-core";
import { useStore } from "vuex";
export default {
  props: {
    type: String,
  },
  setup(props) {
    const store = useStore();
    const users = computed(() => {
      return store.state.user.users;
    });
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });
    const items = ref([]);
    onMounted(() => {
      if (props.type === "users") {
        items.value = users.value;
      } else {
        items.value = institutions.value;
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
      console.log(item);
    };
    return {
      items,
      selectItem,
      users,
    };
  },
};
</script>

<style></style>
