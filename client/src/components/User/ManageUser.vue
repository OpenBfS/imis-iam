<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
  <v-dialog v-model="show">
    <v-card width="80vw">
      <v-card-title v-if="['add', 'copy'].indexOf(processType) !== -1">
        <span class="text-h5">{{ $t("user.create_title") }}</span>
      </v-card-title>
      <v-card-title v-if="processType === 'edit'">
        <span class="text-h5">{{
          $t("user.edit_title", { name: user.username })
        }}</span>
      </v-card-title>
      <v-divider></v-divider>
      <v-container class="pa-1 mt-4">
        <v-row justify="center">
          <v-col jsutify="start" cols="11">
            <v-form v-model="valid" ref="form">
              <div class="two_group_class">
                <v-text-field
                  :variant="
                    ['add', 'copy'].indexOf(processType) !== -1
                      ? 'underlined'
                      : 'plain'
                  "
                  :label="'* ' + $t('user.username')"
                  v-model="user.username"
                  :rules="reqField($t('user.required_username'))"
                  :readonly="processType === 'edit'"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.title')"
                  v-model="user.attributes.title"
                ></v-text-field>
              </div>
              <div class="two_group_class">
                <v-text-field
                  variant="underlined"
                  :label="'* ' + $t('user.firstname')"
                  v-model="user.firstName"
                  :rules="reqField($t('user.required_firstname'))"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="'* ' + $t('user.lastname')"
                  :rules="reqField($t('user.required_lastname'))"
                  v-model="user.lastName"
                ></v-text-field>
              </div>
              <div class="one_group_class">
                <v-text-field
                  variant="underlined"
                  :label="'* ' + $t('label.email')"
                  v-model="user.email"
                  :rules="
                    reqValidmail(
                      $t('form.required_email'),
                      $t('form.valid_email')
                    )
                  "
                ></v-text-field>
              </div>

              <div class="three_group_class">
                <v-text-field
                  variant="underlined"
                  :label="'* ' + $t('user.phone')"
                  :rules="
                    reqValidPhone(
                      $t('form.required_phone'),
                      $t('form.valid_phone')
                    )
                  "
                  v-model="user.attributes.phone"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.mobile')"
                  v-model="user.attributes.mobile"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.fax')"
                  v-model="user.attributes.fax"
                ></v-text-field>
              </div>
              <div class="two_group_class">
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  clearable
                  :label="$t('user.oe')"
                  :items="[]"
                  v-model="user.attributes.oe"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                >
                </v-select>
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  clearable
                  :label="$t('user.bfslocation')"
                  :items="[]"
                  v-model="user.attributes.bfslocation"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                >
                </v-select>
              </div>
              <div class="three_group_class">
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  clearable
                  :label="'* ' + $t('user.label_institutions')"
                  :items="institutions"
                  v-model="user.institutions"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                  multiple
                  :rules="reqMultipleSelect($t('user.required_institution'))"
                >
                </v-select>
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  clearable
                  :label="'* ' + $t('user.label_memberships')"
                  :items="memeberships"
                  v-model="user.groups"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                  multiple
                  :rules="[
                    (v) => !!(v && v.length) || $t('user.required_membership'),
                  ]"
                >
                </v-select>
                <v-select
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  clearable
                  :label="'* ' + $t('user.label_positions')"
                  :items="positions"
                  v-model="user.attributes.position"
                  item-title="position"
                  item-value="id"
                  persistent-hint
                  :rules="reqField($t('user.required_position'))"
                >
                </v-select>
              </div>
            </v-form>
            <UIAlert
              v-if="hasLoadingError || hasRequestError"
              v-bind:message="$store.state.application.httpErrorMessage"
            />
          </v-col>
        </v-row>
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
              user = cloneObject(originalUser);
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
<style lang="scss" scoped>
form > div {
  display: flex;
  padding-top: 8px;
}
.three_group_class > div {
  max-width: 33.3333333333%;
  width: 100%;
}
.two_group_class > div {
  max-width: 50%;
  width: 100%;
}
.one_group_class > div {
  max-width: 100%;
  width: 100%;
}
.three_group_class > div:nth-child(2),
.two_group_class > div:nth-child(2) {
  padding: 0 10px;
}
</style>
<script>
import { computed, onMounted, defineAsyncComponent, ref } from "vue";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";
import { useStore } from "vuex";
import { useForm } from "@/lib/use-form";

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

    const getUserMemberships = () => {
      HTTP.get("iamuser/membership")
        .then((response) => {
          store.commit("user/setMemberships", response.data);
        })
        .catch(() => {
          hasLoadingError.value = false;
        });
    };
    const getUserPsositions = () => {
      HTTP.get("iamuser/position")
        .then((response) => {
          store.commit("user/setPositions", response.data);
        })
        .catch(() => {
          hasLoadingError.value = false;
        });
    };
    const getInstitutions = () => {
      store
        .dispatch("institution/loadInstitutions")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    onMounted(() => {
      getUserMemberships();
      getUserPsositions();
      getInstitutions();
    });
    const positions = computed(() => {
      return store.state.user.positions;
    });
    const memeberships = computed(() => {
      return store.state.user.memberships;
    });
    const institutions = computed(() => {
      return store.state.institution.institutions;
    });
    // Deep Copy for objects
    const cloneObject = (obj) => {
      return JSON.parse(JSON.stringify(obj));
    };
    const createUser = () => {
      HTTP.post("/iamuser", user.value)
        .then(() => {
          emit("child-object", { closeDialog: true, hasChanges: true });
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const updateUser = () => {
      HTTP.put("/iamuser", user.value)
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
    onMounted(() => {
      // This is necessary as the form value is not change to true with valid inputs
      // for the first load by filling the fields (copy, edit).
      // TODO: Check if this gets fixed by upstream with the next release.
      if (["edit", "copy"].indexOf(props.processType) !== -1) {
        setTimeout(() => {
          form.value.validate();
        }, 100);
      }
    });
    // Form
    const {
      form,
      valid,
      reqField,
      reqValidPhone,
      reqValidmail,
      reqMultipleSelect,
    } = useForm();
    // Activate button only if some values are changed for "edit"
    // and username and email are changed for "copy"
    // to avoid useless requests
    const hasNoChanges = computed(() => {
      return (
        (props.processType === "edit" &&
          JSON.stringify(originalUser.value) === JSON.stringify(user.value)) ||
        (props.processType === "copy" &&
          (user.value.username === originalUser.value.username ||
            user.value.email === originalUser.value.email))
      );
    });

    return {
      reqMultipleSelect,
      reqField,
      reqValidPhone,
      reqValidmail,
      cloneObject,
      positions,
      memeberships,
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
