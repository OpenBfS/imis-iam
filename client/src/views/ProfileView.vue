<template>
  <Profile />
</template>

<script>
import { defineComponent } from "vue";

// Components
import Profile from "../components/ProfileComponent.vue";

export default defineComponent({
  name: "ProfileView",

  components: {
    Profile,
  },
});
</script>
