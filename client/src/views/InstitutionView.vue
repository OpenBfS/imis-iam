<template>
  <Institution />
</template>

<script>
import { defineComponent } from "vue";
import Institution from "../components/InstitutionListComponent.vue";
export default defineComponent({
  name: "ProfileView",

  components: {
    Institution,
  },
});
</script>
