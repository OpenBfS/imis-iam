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
            <v-form
              v-model="valid"
              ref="form"
              :readonly="!$store.state.profile.isAllowedToManage"
            >
              <div class="two_group_class">
                <v-text-field
                  :variant="
                    ['add', 'copy'].indexOf(processType) !== -1
                      ? 'underlined'
                      : 'plain'
                  "
                  :label="$t('user.username')"
                  v-model="user.username"
                  :rules="reqField($t('user.required_username'))"
                  :readonly="processType === 'edit'"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.title')"
                  v-model="user.title"
                ></v-text-field>
              </div>
              <div class="two_group_class">
                <v-text-field
                  variant="underlined"
                  :label="$t('user.firstname')"
                  v-model="user.firstName"
                  :rules="reqField($t('user.required_firstname'))"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.lastname')"
                  :rules="reqField($t('user.required_lastname'))"
                  v-model="user.lastName"
                ></v-text-field>
              </div>
              <div class="one_group_class">
                <v-text-field
                  variant="underlined"
                  :label="$t('label.email')"
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
                  :label="$t('user.phone')"
                  :rules="
                    reqValidPhone(
                      $t('form.required_phone'),
                      $t('form.valid_phone')
                    )
                  "
                  v-model="user.phone"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.mobile')"
                  v-model="user.mobile"
                ></v-text-field>
                <v-text-field
                  variant="underlined"
                  :label="$t('user.fax')"
                  v-model="user.fax"
                ></v-text-field>
              </div>
              <div class="two_group_class">
                <v-select
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :clearable="$store.state.profile.isAllowedToManage"
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  :label="$t('user.oe')"
                  :items="[]"
                  v-model="user.oe"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                >
                </v-select>
                <v-select
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :clearable="$store.state.profile.isAllowedToManage"
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  :label="$t('user.bfslocation')"
                  :items="[]"
                  v-model="user.bfslocation"
                  item-title="name"
                  item-value="id"
                  persistent-hint
                >
                </v-select>
              </div>
              <div class="two_group_class">
                <!-- readonly attribute is set here explicitly, which
                 should inherit this value from the <v-form> element.
                TODO: Check if this gets fixed by upstream
                 -->
                <v-select
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :clearable="$store.state.profile.isAllowedToManage"
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  :label="$t('user.label_institutions')"
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
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :clearable="$store.state.profile.isAllowedToManage"
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  :label="$t('user.label_memberships')"
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
              </div>
              <div class="two_group_class">
                <v-select
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :clearable="$store.state.profile.isAllowedToManage"
                  dense
                  :label="$t('user.label_roles')"
                  :items="userRoles"
                  v-model="user.roles"
                  multiple
                  persistent-hint
                  :rules="reqMultipleSelect($t('user.required_roles'))"
                >
                </v-select>
                <v-select
                  :readonly="!$store.state.profile.isAllowedToManage"
                  :no-data-text="$t('label.no_data_text')"
                  dense
                  :label="$t('user.label_positions')"
                  :items="positions"
                  v-model="user.position"
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
        <v-btn
          v-if="processType == 'add'"
          @click="createAndPrepare"
          color="accent"
          :disabled="!valid"
        >
          {{ $t("label.create_and_prepare") }}
        </v-btn>
        <v-btn
          v-if="$store.state.profile.isAllowedToManage"
          color="accent"
          :disabled="!valid || hasNoChanges"
          @click="
            ['add', 'copy'].indexOf(processType) !== -1
              ? createUser(true)
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
          v-if="
            processType === 'edit' && $store.state.profile.isAllowedToManage
          "
          color="accent"
          @click="user = cloneObject(originalUser)"
          :disabled="!$store.state.profile.isAllowedToManage"
        >
          {{ $t("button.reset") }}
        </v-btn>
        <v-btn
          color="accent"
          @click="$store.commit('application/setShowManageUserDialog', false)"
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
::v-deep(.v-card) {
  align-self: center;
}
</style>
<script>
import { computed, onMounted, ref, nextTick } from "vue";
import { useNotification } from "@/lib/use-notification";
import { HTTP } from "@/lib/http";
import { useStore } from "vuex";
import { useForm } from "@/lib/use-form";
import { expUser } from "@/components/User/user";

export default {
  setup() {
    const show = true;
    const { hasLoadingError, hasRequestError, resetNotification } =
      useNotification();
    const store = useStore();
    const user = ref(store.state.application.managedItem);
    const originalUser = ref(store.state.application.savedItem);
    const processType = ref(store.state.application.processType);

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
    const userRoles = computed(() => {
      return store.state.user.roles;
    });
    // Deep Copy for objects
    const cloneObject = (obj) => {
      return JSON.parse(JSON.stringify(obj));
    };
    const getUsers = () => {
      store
        .dispatch("user/loadUsers")
        .then()
        .catch(() => {
          hasLoadingError.value = true;
        });
    };
    const createUser = (shouldClose) => {
      HTTP.post("/iamuser", user.value)
        .then(() => {
          getUsers();
          if (shouldClose) {
            store.commit("application/setShowManageUserDialog", false);
          } else {
            // Use the same roles of the last created user.
            const usedRoles = user.value.roles;
            user.value = cloneObject(expUser);
            user.value.roles = usedRoles;
            nextTick(() => {
              form.value.resetValidation();
            });
          }
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const updateUser = () => {
      resetNotification();
      HTTP.put("/iamuser", user.value)
        .then(() => {
          // Update current user Profile and thus the data in App bar.
          if (store.state.profile.userData.id === user.value.id) {
            store.dispatch("profile/loadProfile");
          }
          getUsers();
          store.commit("application/setShowManageUserDialog", false);
        })
        .catch(() => {
          hasRequestError.value = true;
        });
    };
    const createAndPrepare = () => {
      resetNotification();
      createUser(false);
    };
    onMounted(() => {
      // This is necessary as the form value is not change to true with valid inputs
      // for the first load by filling the fields (copy, edit).
      // TODO: Check if this gets fixed by upstream with the next release.
      if (["edit", "copy"].indexOf(processType.value) !== -1) {
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
        (processType.value === "edit" &&
          JSON.stringify(originalUser.value) === JSON.stringify(user.value)) ||
        (processType.value === "copy" &&
          (user.value.username === originalUser.value.username ||
            user.value.email === originalUser.value.email))
      );
    });
    return {
      createAndPrepare,
      userRoles,
      processType,
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
