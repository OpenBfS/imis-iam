<template>
  <v-form>
    <v-container>
      <v-text-field v-model="userData.username" label="Username" readonly>
      </v-text-field>
      <v-text-field
        v-model="userData.firstName"
        label="First name"
        required
      ></v-text-field>
      <v-text-field
        v-model="userData.lastName"
        label="Last name"
        required
      ></v-text-field>
      <v-text-field
        v-model="userData.email"
        label="Email"
        required
      ></v-text-field>
      <v-container>
        <v-row>
          <v-btn>Save</v-btn>
          <v-btn>Reset</v-btn>
        </v-row>
      </v-container>
    </v-container>
  </v-form>
</template>
<script>
import { computed, onMounted } from "vue";
import { useStore } from "vuex";
export default {
  setup() {
    const store = useStore();
    const userData = computed(() => {
      return store.state.profile.userData;
    });
    onMounted(() => {
      store.dispatch("profile/loadProfile");
    });
    return {
      userData,
    };
  },
};
</script>
