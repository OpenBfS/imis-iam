<template>
  <v-form ref="form" v-model="valid" lazy-validation>
    <v-container>
      <v-col cols="6">
        <div class="mb-4 text-h6">
          {{ $t("label.username") }}: {{ userData.username }}
        </div>
        <v-text-field
          v-model="userData.firstName"
          :label="$t('label.firstname')"
          variant="underlined"
          :rules="nameRules"
          required
        ></v-text-field>
        <v-text-field
          v-model="userData.lastName"
          :label="$t('label.lastname')"
          variant="underlined"
          :rules="nameRules"
          required
        ></v-text-field>
        <v-text-field
          v-model="userData.email"
          :label="$t('label.email')"
          variant="underlined"
          :rules="emailRules"
          required
        ></v-text-field>
        <v-row>
          <v-btn @click="save" class="ma-2 pa-2">{{ $t("label.save") }}</v-btn>
          <v-btn @click="reset" class="ma-2 pa-2">{{
            $t("label.reset")
          }}</v-btn>
        </v-row>
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
