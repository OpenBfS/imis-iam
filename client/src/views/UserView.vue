<template>
  <User />
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { defineComponent } from "vue";
import User from "../components/UserListComponent.vue";
export default defineComponent({
  name: "ProfileView",
  components: {
    User,
  },
});
</script>
