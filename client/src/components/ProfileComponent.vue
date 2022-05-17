<template>
  <v-form ref="form" v-model="valid" lazy-validation>
    <v-container>
      <v-col>
        <v-text-field
          v-model="userData.username"
          label="Username"
          variant="plain"
          readonly
        >
        </v-text-field>
        <v-text-field
          v-model="userData.firstName"
          label="First name"
          variant="underlined"
          :rules="nameRules"
          required
        ></v-text-field>
        <v-text-field
          v-model="userData.lastName"
          label="Last name"
          variant="underlined"
          :rules="nameRules"
          required
        ></v-text-field>
        <v-text-field
          v-model="userData.email"
          label="Email"
          variant="underlined"
          :rules="emailRules"
          required
        ></v-text-field>
        <v-container>
          <v-row>
            <v-btn @click="save" class="ma-2 pa-2">Save</v-btn>
            <v-btn @click="reset" class="ma-2 pa-2">Reset</v-btn>
          </v-row>
        </v-container>
      </v-col>
    </v-container>
  </v-form>
</template>

<script>
import { computed, onMounted } from "vue";
import profileStore from "../store";
export default {
  setup() {
    //Validation rules
    const nameRules = [(v) => !!v || "Name is required"];
    const emailRules = [(v) => !!v || "Email is required"];
    const valid = true;

    //Form data
    const userData = computed(() => {
      return profileStore.state.profile.userData;
    });
    onMounted(() => {
      profileStore.dispatch("profile/loadProfile");
    });

    //Functions
    const save = () => {
      if (valid == true) {
        profileStore.dispatch("profile/storeProfile");
      }
    };
    const reset = () => {
      profileStore.dispatch("profile/loadProfile");
    };
    return {
      emailRules,
      nameRules,
      userData,
      valid,
      save,
      reset,
    };
  },
};
</script>
