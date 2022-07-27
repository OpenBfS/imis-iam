<template>
  <v-container>
    <v-row>
      <v-col cols="12" class="mt-10 pa-2 text-h6 bg-secondary">
        {{ $t("user.edit_my_profile") }}
      </v-col>
    </v-row>
    <v-row class="mt-6" justify="start">
      <v-col cols="10">
        <v-form v-model="valid" ref="from" class="pa-2">
          <v-text-field
            v-model="user.firstName"
            :label="$t('label.firstname')"
            variant="underlined"
            :rules="[(v) => !!v || $t('form.required_firstname')]"
            required
          ></v-text-field>
          <v-text-field
            v-model="user.lastName"
            :label="$t('label.lastname')"
            variant="underlined"
            :rules="[(v) => !!v || $t('form.required_lastname')]"
            required
          ></v-text-field>
          <v-text-field
            v-model="user.email"
            :label="$t('label.email')"
            variant="underlined"
            :rules="[
              (v) => !!v || $t('form.required_email'),
              (v) => /.+@.+/.test(v) || $t('form.valid_email'),
            ]"
            required
          ></v-text-field>
        </v-form>
        <div class="d-flex">
          <v-btn
            @click="save"
            :disabled="
              !valid ||
              JSON.stringify(orgiginalUserData) === JSON.stringify(user)
            "
            class="ma-2 pa-2"
            >{{ $t("button.save") }}</v-btn
          >
          <v-btn @click="user = { ...orgiginalUserData }" class="ma-2 pa-2">{{
            $t("button.reset")
          }}</v-btn>
        </div>
        <UIAlert
          v-if="hasLoadingError || hasRequestError"
          v-bind:isSuccessful="false"
          v-bind:message="$store.state.application.httpErrorMessage"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, onMounted, ref, defineAsyncComponent } from "vue";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";
export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("./UI/UIAlert.vue")),
  },
  setup() {
    const store = useStore();
    const { hasLoadingError, hasRequestError } = useNotification();
    const valid = ref(false);

    const orgiginalUserData = computed(() => {
      return store.state.profile.userData;
    });
    const user = ref({});
    onMounted(() => {
      store
        .dispatch("profile/loadProfile")
        .then(() => {
          user.value = { ...orgiginalUserData.value };
        })
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    const save = () => {
      HTTP.put("/iamuser/profile", user.value)
        .then((response) => {
          store.commit("profile/setUserData", response.data);
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };

    return {
      user,
      valid,
      save,
      orgiginalUserData,
      hasLoadingError,
      hasRequestError,
    };
  },
};
</script>
