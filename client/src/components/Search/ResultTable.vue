<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-container>
    <v-list density="compact" nav lines="">
      <InstitutionTable
        v-if="props.type != 'users'"
        v-bind:institutions="institutionStore.foundInstitutions"
      />
      <UserTable v-else v-bind:users="userStore.foundUsers" />
    </v-list>
  </v-container>
</template>

<script setup>
import { useInstitutionStore } from "@/stores/institution.js";
import { useUserStore } from "@/stores/user.js";

const institutionStore = useInstitutionStore();
const userStore = useUserStore();

const props = defineProps({
  type: {
    type: String,
    default: () => {
      return "users";
    },
  },
});
</script>
