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
import { computed } from 'vue'
import { useStore } from 'vuex'
export default {
  setup() {
    const store = useStore();
    store.dispatch('loadProfile');
  },
  computed: {
    username: {
      get () {
        return this.$store.state.profile.username;
      }
    },
    firstname: {
      get () {
        return this.$store.state.profile.firstname;
      }
    },
    lastname: {
      get () {
        return this.$store.state.profile.lastname;
      }
    },
    email: {
      get () {
        return this.$store.state.profile.email;
      }
    }
  }
};
</script>
