<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->

<!-- eslint-disable vue/no-v-for-template-key -->
<template>
  <v-card width="80vw">
    <v-card-title v-if="['add', 'copy'].indexOf(processType) !== -1">
      <span class="text-h5">{{ $t("user.create_title") }}</span>
    </v-card-title>
    <v-card-title v-if="processType === 'edit'">
      <span class="text-h5">{{
        $t("user.edit_title", { name: user.attributes.username[0] })
      }}</span>
    </v-card-title>
    <v-divider></v-divider>
    <v-container class="pa-1 mt-4">
      <v-row justify="center">
        <v-col jsutify="start" cols="11">
          <v-form v-model="valid" ref="form" :readonly="isReadOnly">
            <template v-for="group in attributeGroups" :key="group.name">
              <v-row>
                <v-label>{{ $t(`user.${group.name}`) }}</v-label>
              </v-row>
              <v-row>
                <template
                  v-for="attribute in getAttributesForGroup(group.name)"
                  :key="attribute.name"
                >
                  <v-col cols="4">
                    <v-text-field
                      v-if="getElementType(attribute.name) === 'input'"
                      density="compact"
                      :variant="
                        attribute.name === 'username' && processType === 'edit'
                          ? 'plain'
                          : 'underlined'
                      "
                      :label="$t(`user.${attribute.name.toLowerCase()}`)"
                      :model-value="user.attributes[attribute.name]"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event)
                      "
                      :type="getInputTypeOfAttribute(attribute.name)"
                      :readonly="
                        attribute.name === 'username' && processType === 'edit'
                      "
                      :rules="getRules(attribute.name)"
                    ></v-text-field>
                    <v-select
                      v-else
                      density="compact"
                      :label="$t(`user.${attribute.name.toLowerCase()}`)"
                      :item-title="attribute.name"
                      :item-value="attribute.displayName"
                      :items="getSelectItems(attribute.name)"
                      :model-value="user.attributes[attribute.name]"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event)
                      "
                      :rules="getRules(attribute.name)"
                    ></v-select>
                  </v-col>
                </template>
              </v-row>
            </template>

            <v-row>
              <v-label>{{ $t("user.misc") }}</v-label>
            </v-row>
            <template
              v-for="attribute in attributesWithoutGroup"
              :key="attribute.name"
            >
              <v-row>
                <v-col>
                  <v-text-field
                    v-if="getElementType(attribute.name) === 'input'"
                    density="compact"
                    :variant="
                      attribute.name === 'username' && processType === 'edit'
                        ? 'plain'
                        : 'underlined'
                    "
                    :label="$t(`user.${attribute.name.toLowerCase()}`)"
                    :model-value="user.attributes[attribute.name]"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event)
                    "
                    :type="getInputTypeOfAttribute(attribute.name)"
                    :readonly="
                      attribute.name === 'username' && processType === 'edit'
                    "
                    :rules="getRules(attribute.name)"
                  ></v-text-field>
                  <v-select
                    v-else
                    density="compact"
                    :label="$t(`user.${attribute.name.toLowerCase()}`)"
                    :item-title="attribute.name"
                    :item-value="attribute.displayName"
                    :items="getSelectItems(attribute.name)"
                    :model-value="user.attributes[attribute.name]"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event)
                    "
                    :rules="getRules(attribute.name)"
                  ></v-select>
                </v-col>
              </v-row>
            </template>

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
            <div class="one_group_class">
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
        v-if="processType === 'edit' && !isReadOnly"
        color="accent"
        @click="user = cloneObject(originalUser)"
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

function setUserAttribute(name, value) {
  const attrs = user.value.attributes;
  if (value) {
    // Keycloak User Profile attributes are arrays expected to
    // contain a single value
    attrs[name] = [value];
  } else {
    delete attrs[name];
  }
}

const getAttributesForGroup = (groupName) => {
  return JSON.parse(JSON.stringify(store.getters["profile/attributes"])).filter(
    (attribute) => attribute.group === groupName
  );
};
const getMetaDataAttribute = (nameOfAttribute) => {
  return JSON.parse(JSON.stringify(store.getters["profile/attributes"])).find(
    (attribute) => attribute.name === nameOfAttribute
  );
};
const getInputTypeOfAttribute = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  if (attribute.name === "email") {
    return "email";
  } else if (["phone", "mobile", "fax"].includes(attribute.name)) {
    return "tel";
  } else {
    return "text";
  }
};
const getElementType = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  return attribute.annotations?.inputType ?? "input";
};
const getSelectItems = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  return attribute.validations.options.options;
};
const getRules = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  const rules = [];
  // Rules for text field components
  if (!attribute.validations.options) {
    if (attribute.name === "email") {
      rules.push(
        ...reqValidmail(t("form.required_email"), t("form.valid_email"))
      );
    } else if (attribute.name === "phone") {
      rules.push(
        ...reqValidPhone(t("form.required_phone"), t("form.valid_phone"))
      );
    } else if (
      ["lastname", "firstname"].includes(attribute.name.toLowerCase())
    ) {
      rules.push(
        ...reqField(
          t("user.is_required", {
            attr: t(`user.${nameOfAttribute.toLowerCase()}`),
          })
        )
      );
    }

    if (attribute.validations?.length) {
      const length = attribute.validations.length;
      let message;
      if (length.min && length.max) {
        message = t("user.min_and_max_characters_allowed", {
          min: length.min,
          max: length.max,
        });
      } else if (length.min) {
        message = t("user.min_characters_necessary", {
          min: length.min,
        });
      } else {
        message = t("user.max_characters_allowed", {
          max: length.max,
        });
      }
      rules.push(...validLength(length.min ?? 0, length.max ?? 0, message));
    }
  }
  // Rules for select components
  else {
    rules.push(...reqField(t(`user.required_${attribute.name}`)));
  }
  return rules;
};
const getUserMemberships = () => {
  store.dispatch("user/loadMemberships").catch(() => {
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
  getInstitutions();
});
const attributesWithoutGroup = computed(() => {
  return JSON.parse(
    JSON.stringify(store.getters["profile/attributesWithoutGroup"])
  );
});
const attributeGroups = computed(() => {
  return JSON.parse(JSON.stringify(store.getters["profile/attributeGroups"]));
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
  validLength,
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
const isReadOnly = computed(() => {
  if (store.state.application.ownAccount) {
    return false;
  }
  return !store.state.profile.isAllowedToManage;
});
</script>
