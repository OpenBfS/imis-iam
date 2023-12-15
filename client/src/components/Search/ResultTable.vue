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
            props.type === "users"
              ? $t("search.found_users")
              : $t("search.found_institution")
          }`
        }}
      </v-list-item-subtitle>
      <InstitutionTable
        v-if="props.type != 'users'"
        v-bind:institutions="institutionStore.foundInstitutions"
      />
      <UserTable v-else v-bind:users="userStore.foundUsers" />
    </v-list>
  </v-container>
</template>

<script setup>
import { defineAsyncComponent } from "vue";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user";

const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const InstitutionTable = defineAsyncComponent(() =>
  import("@/components/Institution/InstitutionTable.vue")
);
const UserTable = defineAsyncComponent(() =>
  import("@/components/User/UserTable.vue")
);

const props = defineProps({
  type: {
    type: String,
    default: () => {
      return "users";
    },
  },
});
</script>
