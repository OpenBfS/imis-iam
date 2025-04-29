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
      <span class="text-h5">{{ $t("user.createTitle") }}</span>
    </v-card-title>
    <v-card-title v-if="processType === 'edit'">
      <span class="text-h5">{{
        $t("user.editTitle", { name: user.attributes.username[0] })
      }}</span>
    </v-card-title>
    <v-divider></v-divider>
    <v-container class="pa-1 mt-4">
      <v-row justify="center">
        <v-col cols="11">
          <v-form v-model="valid" ref="form" :readonly="isReadOnly">
            <v-row>
              <v-col v-if="processType !== 'edit'">
                <TextField
                  attribute="username"
                  density="compact"
                  :ref="'username'"
                  :variant="
                    ['add', 'copy'].indexOf(processType) !== -1
                      ? 'underlined'
                      : 'plain'
                  "
                  :label="$t('user.username')"
                  :model-value="user.attributes.username"
                  required
                  @update:model-value="
                    clearValidationError('username');
                    setUserAttribute('username', $event);
                  "
                ></TextField>
              </v-col>
              <v-col>
                <Checkbox
                  attribute="enabled"
                  :label="$t('user.enabled')"
                  v-model="user.enabled"
                  :disabled="profileStore.userData.role !== 'chief_editor'"
                ></Checkbox>
              </v-col>
            </v-row>
            <template
              v-for="group in profileStore.attributeGroups"
              :key="group.name"
            >
              <v-row>
                <v-label>{{ $t(`user.${group.name}`) }}</v-label>
              </v-row>
              <v-row>
                <template
                  v-for="attribute in profileStore.attributesOfGroup(
                    group.name,
                  )"
                  :key="attribute.name"
                >
                  <v-col cols="6">
                    <GenericUserFormField
                      :attribute="attribute"
                      :clearValidationError="clearValidationError"
                      :setUserAttribute="setUserAttribute"
                      :user="user"
                    />
                  </v-col>
                </template>
              </v-row>
            </template>

            <v-row>
              <v-label>{{ $t("user.misc") }}</v-label>
            </v-row>
            <v-row>
              <template
                v-for="attribute in profileStore.attributesWithoutGroup"
                :key="attribute.name"
              >
                <v-col v-if="attribute.name !== 'username'" cols="6">
                  <GenericUserFormField
                    :attribute="attribute"
                    :clearValidationError="clearValidationError"
                    :setUserAttribute="setUserAttribute"
                    :user="user"
                    :disabled="
                      !(
                        profileStore.userData.role === 'chief_editor' ||
                        attribute.name !== 'tags'
                      )
                    "
                  />
                  <p
                    :id="`${attribute.name}-validation-error`"
                    class="validation-error"
                  ></p>
                </v-col>
              </template>
            </v-row>

            <v-row>
              <v-col>
                <Select
                  attribute="institutions"
                  :clearable="profileStore.isAllowedToManage"
                  :no-data-text="$t('label.noDataText')"
                  :label="$t('user.institutions')"
                  :items="institutionStore.institutions"
                  v-model="user.institutions"
                  item-title="name"
                  item-value="name"
                  persistent-hint
                  multiple
                  required
                ></Select>
              </v-col>
              <v-col>
                <Combobox
                  :items="applicationStore.networks"
                  item-title="name"
                  item-value="name"
                  :label="$t('user.network')"
                  :attribute="'network'"
                  :disabled="profileStore.userData.role !== 'chief_editor'"
                  :model-value="user.network"
                  @update:model-value="user.network = $event"
                  required
                ></Combobox>
              </v-col>
            </v-row>
            <v-row>
              <Select
                attribute="role"
                :disabled="profileStore.userData.role !== 'chief_editor'"
                :label="$t('user.role')"
                :items="userRoles"
                item-value="name"
                name="role"
                required
                v-model="user.role"
                persistent-hint
              ></Select>
            </v-row>
          </v-form>
          <v-label>* {{ $t("hints.requiredFields") }}</v-label>
          <UIAlert
            v-if="hasLoadingError || hasRequestError"
            v-bind:message="applicationStore.httpErrorMessage"
          />
        </v-col>
      </v-row>
    </v-container>
    <v-divider></v-divider>
    <v-card-actions>
      <v-spacer></v-spacer>
      <v-btn
        v-if="!isReadOnly"
        color="accent"
        :disabled="!valid || hasNoChange"
        @click="
          ['add', 'copy'].indexOf(processType) !== -1
            ? createUser()
            : saveUser()
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
        @click="resetForm(cloneObject(originalUser), user, resetNotification)"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          onCancel(() => {
            applicationStore.setOwnAccount(false);
            applicationStore.setShowManageUserDialog(false);
          })
        "
      >
        {{ $t("button.cancel") }}
      </v-btn>
    </v-card-actions>
  </v-card>
  <ConfirmCancelDialog
    :isActive="showConfirmCancelDialog"
    :onConfirm="
      () => {
        applicationStore.setOwnAccount(false);
        applicationStore.setShowManageUserDialog(false);
      }
    "
    :onCancel="() => closeConfirmCancelDialog()"
  ></ConfirmCancelDialog>
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
import { computed, provide, onBeforeMount, onMounted, onUnmounted } from "vue";
import { useNotification } from "@/lib/use-notification.js";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http.js";
import { useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import { trimSpacesInObject, useForm } from "@/lib/use-form.js";
import {
  finishUserDialog,
  handleDisplayName,
  updateUser,
} from "@/components/User/user.js";
import Checkbox from "@/components/Form/Checkbox.vue";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";
import GenericUserFormField from "../Form/GenericUserFormField.vue";

const { t } = useI18n();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

provide("translationCategory", "user");

function setUserAttribute(name, value) {
  const attrs = user.value.attributes;
  if (
    typeof value !== "string" &&
    (value?.length === 0 || value?.[0]?.length)
  ) {
    attrs[name] = value;
  } else if (value) {
    // Keycloak User Profile attributes are arrays expected to
    // contain a single value
    attrs[name] = [value];
  } else {
    delete attrs[name];
  }
}

// Creating rules for the validation of form components.
const getUserAttributeRules = (userAttribute) => {
  const tmpRules = [];
  // Rules for text field components
  if (!userAttribute.validations?.options) {
    if (userAttribute.validations?.pattern) {
      const pattern = userAttribute.validations.pattern.pattern;
      const errorMessage = userAttribute.validations.pattern["error-message"];
      tmpRules.push(...validRegex(pattern, t(errorMessage)));
    }

    if (userAttribute.validations?.length) {
      const length = userAttribute.validations.length;
      let message;
      const displayName = handleDisplayName(userAttribute.displayName);
      if (length.min && length.max) {
        message = t("error.invalidLength", [
          displayName,
          length.min,
          length.max,
        ]);
      } else if (length.min) {
        message = t("error.invalidLengthTooShort", [displayName, length.min]);
      } else {
        message = t("error.invalidLengthTooLong", {
          attribute: displayName,
          max: length.max,
        });
      }
      tmpRules.push(...validLength(length.min, length.max, message));
    }
  } else {
    // Rules for select components
  }
  return tmpRules;
};

onBeforeMount(() => {
  applicationStore.setForm(form);
});

onMounted(() => {
  profileStore.attributes.forEach((userAttribute) => {
    applicationStore.clientRules[userAttribute.name] =
      getUserAttributeRules(userAttribute);
  });
  Object.keys(applicationStore.clientRules).forEach((key) => {
    applicationStore.clientAndServerRules[key] =
      applicationStore.clientRules[key];
  });
});
onUnmounted(() => {
  applicationStore.removeAllResetEventListeners();
});
const user = computed(() => {
  return applicationStore.managedItem;
});
const originalUser = computed(() => {
  return applicationStore.savedItem;
});
const processType = computed(() => {
  return applicationStore.processType;
});
const userRoles = computed(() => {
  return userStore.roles?.map((item) => {
    const newItem = structuredClone(item)
    newItem.props = {};
    // Only allow 'chief_editor' to grant roles other than 'user'
    newItem.props.disabled =
      item.name !== "user" && profileStore.userData.role !== "chief_editor";
    // If available, use description field for localization
    newItem.title = item.description ? t(item.description) : item.name;
    return newItem;
  }) ?? [];
});
// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const createUser = () => {
  HTTP.post("/iam/user", trimSpacesInObject(user.value))
    .then((response) => {
      const newUser = response.data;
      userStore.addUser(newUser);
      finishUserDialog(newUser);
      applicationStore.searchRequest(["users"]);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};

// Cannot call updateUser directly in the <template> because then hasRequestError is no ref anymore
// but a ref is necessary so we can detect any change of it in this component.
const saveUser = () => {
  updateUser(
    { ...user.value },
    resetNotification,
    isServerValidationError,
    handleValidationErrorFromServer,
    hasRequestError,
  );
};

// Form
const {
  form,
  valid,
  hasNoChange,
  validRegex,
  validLength,
  resetForm,
  watchChange,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  handleValidationErrorFromServer,
  clearValidationError,
  isServerValidationError,
} = useForm();
watchChange(originalUser.value, user.value);

const isReadOnly = computed(() => {
  if (applicationStore.ownAccount) {
    return false;
  }
  return !profileStore.isAllowedToManage ||
  (profileStore.userData.role !== 'chief_editor' &&
    user.value.network !== profileStore.userData.network) ;
});

// Necessary so tests are able to access exactly these instances used in this component.
defineExpose({
  form,
  resetForm,
});
</script>
