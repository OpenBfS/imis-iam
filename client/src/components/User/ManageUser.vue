<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->
<template>
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
          <v-form v-model="valid" ref="form" :readonly="isReadOnly">
            <div class="two_group_class">
              <v-text-field
                density="compact"
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
              <v-checkbox
                :label="$t('user.enabled')"
                v-model="user.enabled"
                :disabled="
                  !$store.state.profile.userData.roles.includes('chief_editor')
                "
              ></v-checkbox>
            </div>
            <div class="three_group_class">
              <v-text-field
                density="compact"
                variant="underlined"
                :label="$t('user.title')"
                v-model="user.title"
              ></v-text-field>
              <v-text-field
                density="compact"
                variant="underlined"
                name="firstname"
                :label="$t('user.firstname')"
                v-model="user.firstName"
                :rules="reqField($t('user.required_firstname'))"
              ></v-text-field>
              <v-text-field
                variant="underlined"
                density="compact"
                name="lastname"
                :label="$t('user.lastname')"
                :rules="reqField($t('user.required_lastname'))"
                v-model="user.lastName"
              ></v-text-field>
            </div>
            <div class="one_group_class">
              <v-text-field
                density="compact"
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
                density="compact"
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
              <!--TODO: Add this rule once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPhone($t('form.valid_fax'))" -->
              <v-text-field
                density="compact"
                variant="underlined"
                :label="$t('user.mobile')"
                v-model="user.mobile"
              ></v-text-field>
              <v-text-field
                density="compact"
                variant="underlined"
                :label="$t('user.fax')"
                v-model="user.fax"
              ></v-text-field>
            </div>
            <div class="two_group_class">
              <v-select
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
              <v-select
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
                :clearable="$store.state.profile.isAllowedToManage"
                dense
                :label="$t('user.label_roles')"
                :items="userRoles"
                item-value="name"
                v-model="user.roles"
                multiple
                persistent-hint
                :rules="reqMultipleSelect($t('user.required_roles'))"
              >
              </v-select>
              <!-- TODO: Add this rule once the validation for
                    optional fields gets implemented by upstream.
                    :rules="validPostalcode($t('form.valid_postalcode'))" -->
              <v-select
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
        v-if="!isReadOnly"
        color="accent"
        :disabled="!valid || hasNoChange"
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
        v-if="processType === 'edit' && !isReadOnly"
        color="accent"
        :disabled="hasNoChange"
        @click="resetForm(cloneObject(originalUser), user)"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          $store.commit('application/setOwnAccount', false);
          $store.commit('application/setShowManageUserDialog', false);
        "
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
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
<script setup>
import { computed, onMounted, ref, nextTick } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http";
import { useStore } from "vuex";
import { useForm } from "@/lib/use-form";
import { expUser } from "@/components/User/user";

const { t } = useI18n();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();
const store = useStore();
const user = ref(store.state.application.managedItem);
const originalUser = ref(store.state.application.savedItem);
const processType = ref(store.state.application.processType);

const getUserMemberships = () => {
  store.dispatch("user/loadMemberships").catch(() => {
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
  var roles = store.state.user.roles;
  // If available, use description field for localization
  roles.forEach(
    (item) => (item.title = item.description ? t(item.description) : item.name)
  );
  return roles;
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
        store.commit("application/setOwnAccount", true);
        store.commit("application/setShowManageUserDialog", false);
      } else {
        // Use the same roles of the last created user.
        const usedRoles = user.value.roles;
        user.value = cloneObject(expUser);
        nextTick(() => {
          form.value.resetValidation();
          user.value.roles = usedRoles;
        });
      }
    })
    .catch(() => {
      hasRequestError.value = true;
    });
};

const updateUser = () => {
  resetNotification();
  store
    .dispatch("user/updateUser", user.value)
    .then(() => {
      // Update current user Profile and thus the data in App bar.
      if (store.state.profile.userData.id === user.value.id) {
        store.dispatch("profile/loadProfile");
      }
      store.commit("application/setOwnAccount", false);
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
// Form
const {
  form,
  valid,
  reqField,
  reqValidPhone,
  reqValidmail,
  reqMultipleSelect,
  resetForm,
  hasNoChangeWrapper,
} = useForm();
const hasNoChange = hasNoChangeWrapper(originalUser.value, user.value);
const isReadOnly = computed(() => {
  if (store.state.application.ownAccount) {
    return false;
  }
  return !store.state.profile.isAllowedToManage;
});
</script>
