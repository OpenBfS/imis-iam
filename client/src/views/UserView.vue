<template>
  <User />
</template>

<script>
import { defineComponent } from "vue";
import User from "../components/UserListComponent.vue";
export default defineComponent({
  name: "ProfileView",

  components: {
    User,
  },
});
</script>
