<template>
  <v-dialog v-model="show">
    <v-card min-width="600">
      <v-card-title v-if="['add', 'copy'].indexOf(processType) !== -1">
        <span class="text-h5">{{ $t("user.create_title") }}</span>
      </v-card-title>
      <v-card-title v-if="processType === 'edit'">
        <span class="text-h5">{{
          $t("user.edit_title", { name: user.username })
        }}</span>
      </v-card-title>
      <v-divider></v-divider>
      <v-container>
        <v-col jsutify="start" cols="10">
          <v-form v-model="valid" ref="form">
            <v-col>
              <v-text-field
                v-if="processType === 'edit'"
                variant="plain"
                :label="$t('label.id')"
                readonly
                v-model="user.id"
              ></v-text-field>
              <v-text-field
                :variant="
                  ['add', 'copy'].indexOf(processType) !== -1
                    ? 'underlined'
                    : 'plain'
                "
                :label="$t('label.username')"
                v-model="user.username"
                :readonly="processType === 'edit'"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                :label="$t('label.firstname')"
                v-model="user.firstName"
                :rules="[(v) => !!v || $t('form.required_firstname')]"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                :label="$t('label.lastname')"
                :rules="[(v) => !!v || $t('form.required_lastname')]"
                v-model="user.lastName"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                :label="$t('label.email')"
                v-model="user.email"
                :rules="[
                  (v) => !!v || $t('form.required_email'),
                  (v) => /.+@.+/.test(v) || $t('form.valid_email'),
                ]"
              ></v-text-field>
              <v-select
                return-object
                dense
                clearable
                :label="$t('user.label_institutions')"
                :items="institutions"
                v-model="user.groups"
                item-title="name"
                item-value="id"
                persistent-hint
                multiple
              >
              </v-select>
            </v-col>
          </v-form>
          <UIAlert
            v-if="hasLoadingError || hasRequestError"
            v-bind:isSuccessful="false"
            v-bind:message="$store.state.application.httpErrorMessage"
          />
        </v-col>
      </v-container>
      <v-divider></v-divider>
      <v-card-actions>
        <v-spacer></v-spacer>
        <!-- ":color" It is necessary to handle color like this as disabled
        does not have style effect yet. TODO: Check this if fixed by upstream -->
        <v-btn
          :color="`${valid && !hasNoChanges ? 'accent' : 'grey'}`"
          :disabled="!valid || hasNoChanges"
          @click="
            ['add', 'copy'].indexOf(processType) !== -1
              ? createUser()
              : updateUser()
          "
        >
          {{
            ["add", "copy"].indexOf(processType) !== -1
              ? $t("button.create")
              : $t("button.save")
          }}
        </v-btn>
        <v-btn
          v-if="processType === 'edit'"
          color="accent"
          @click="
            () => {
              user = { ...originalUser };
            }
          "
        >
          {{ $t("button.reset") }}
        </v-btn>
        <v-btn
          color="accent"
          @click="
            $emit('child-object', {
              closeDialog: true,
            })
          "
        >
          {{ $t("button.cancel") }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script>
/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, onMounted, defineAsyncComponent, ref } from "vue";
import { useStore } from "vuex";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";

export default {
  components: {
    UIAlert: defineAsyncComponent(() => import("@/components/UI/UIAlert.vue")),
  },
  props: {
    item: Object,
    copiedItem: Object,
    processType: String,
  },
  setup(props, { emit }) {
    const show = true;
    const store = useStore();
    const user = ref(props.item);
    const originalUser = ref(props.copiedItem);
    const { hasLoadingError, hasRequestError } = useNotification();
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });
    onMounted(() => {
      store
        .dispatch("institution/loadInstitutions")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    });
    const getInstitutionIds = (institution) => {
      return institution.map((i) => i.id);
    };
    const createUser = () => {
      const payload = { ...user.value };
      payload["groups"] = user.value.groups.length
        ? getInstitutionIds(user.value.groups)
        : [];
      HTTP.post("/iamuser", payload)
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const updateUser = () => {
      const payload = { ...user.value };
      payload["groups"] = user.value.length
        ? getInstitutionIds(user.value)
        : [];
      HTTP.put("/iamuser", payload)
        .then(() => {
          // Update current user Profile and thus the data in App bar.
          if (store.state.profile.userData.id === user.value.id) {
            store.dispatch("profile/loadProfile");
          }
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const form = ref(false);
    onMounted(() => {
      // This is necessary as the form value is not change to true with valid inputs.
      // TODO: Check if this is fixed by upstream with the next release.
      if (props.processType === "edit") {
        setTimeout(() => {
          form.value.validate();
        }, 100);
      }
    });
    // Form
    const valid = ref(false);
    // Activate button only if some values are changed for "edit"
    // to avoid useless requests
    const hasNoChanges = computed(() => {
      return (
        props.processType === "edit" &&
        JSON.stringify(originalUser.value) === JSON.stringify(user.value)
      );
    });

    return {
      hasNoChanges,
      hasRequestError,
      hasLoadingError,
      valid,
      institutions,
      show,
      createUser,
      updateUser,
      user,
      originalUser,
      form,
    };
  },
};
</script>
