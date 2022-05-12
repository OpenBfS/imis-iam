<template>
  <v-form>
    <v-container>
      <v-text-field v-model="username" label="Username" readonly>
      </v-text-field>
      <v-text-field
        v-model="firstname"
        label="First name"
        required
      ></v-text-field>
      <v-text-field
        v-model="lastname"
        label="Last name"
        required
      ></v-text-field>
      <v-text-field v-model="email" label="Email" required></v-text-field>
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
import { computed, defineComponent } from "vue";
import { useStore } from "vuex";
export default defineComponent({
  setup() {
    const store = useStore();
    store.dispatch("loadProfile");
    const username = computed(() => {
      return store.state.username;
    });
    const firstname = computed(() => {
      return store.state.firstname;
    });
    const lastname = computed(() => {
      return store.state.lastname;
    });
    const email = computed(() => {
      return store.state.email;
    });
    return {
      username,
      firstname,
      lastname,
      email,
    };
  },
});
</script>
