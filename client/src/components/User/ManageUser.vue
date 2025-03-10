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
                    group.name
                  )"
                  :key="attribute.name"
                >
                  <v-col cols="6">
                    <ChipTextField
                      v-if="
                        getFormFieldType(
                          attribute.annotations?.inputType,
                          attribute.multivalued
                        ) === FORM_FIELD_TYPE_CHIPTEXTFIELD
                      "
                      :attribute="attribute.name"
                      :required="isUserAttributeRequired(attribute)"
                      :label="handleDisplayName(attribute.displayName)"
                      :name="attribute.name"
                      :model-value="user.attributes[attribute.name]"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event);
                        clearValidationError(attribute.name);
                      "
                      :rules="
                        applicationStore.clientAndServerRules[attribute.name]
                      "
                    ></ChipTextField>
                    <TextField
                      v-else-if="
                        getFormFieldType(attribute.annotations?.inputType) ===
                        FORM_FIELD_TYPE_TEXTFIELD
                      "
                      :attribute="attribute.name"
                      density="compact"
                      :required="isUserAttributeRequired(attribute)"
                      variant="underlined"
                      :label="handleDisplayName(attribute.displayName)"
                      :name="attribute.name"
                      :model-value="user.attributes[attribute.name]"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event);
                        clearValidationError(attribute.name);
                      "
                      :type="getTextFieldType(attribute.name)"
                    ></TextField>
                    <Select
                      v-else-if="
                        getFormFieldType(attribute.annotations?.inputType) ===
                        FORM_FIELD_TYPE_SELECTION
                      "
                      :label="handleDisplayName(attribute.displayName)"
                      :attribute="attribute.name"
                      item-title="name"
                      item-value="id"
                      :name="attribute.name"
                      :items="attribute.validations.options.options"
                      :model-value="user.attributes[attribute.name]"
                      :clearable="
                        attribute.annotations.inputType === 'multiselect'
                      "
                      :multiple="
                        attribute.annotations.inputType === 'multiselect'
                      "
                      :required="isUserAttributeRequired(attribute)"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event)
                      "
                    ></Select>
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
                  <ChipTextField
                    v-if="
                      getFormFieldType(
                        attribute.annotations?.inputType,
                        attribute.multivalued
                      ) === FORM_FIELD_TYPE_CHIPTEXTFIELD
                    "
                    :attribute="attribute.name"
                    :required="isUserAttributeRequired(attribute)"
                    :label="handleDisplayName(attribute.displayName)"
                    :name="attribute.name"
                    :model-value="user.attributes[attribute.name]"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event);
                      clearValidationError(attribute.name);
                    "
                    :rules="
                      applicationStore.clientAndServerRules[attribute.name]
                    "
                  ></ChipTextField>
                  <TextField
                    v-else-if="
                      getFormFieldType(attribute.annotations?.inputType) ===
                      FORM_FIELD_TYPE_TEXTFIELD
                    "
                    :attribute="attribute.name"
                    density="compact"
                    :required="isUserAttributeRequired(attribute)"
                    variant="underlined"
                    :label="handleDisplayName(attribute.displayName)"
                    :name="attribute.name"
                    :model-value="user.attributes[attribute.name]"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event);
                      clearValidationError(attribute.name);
                    "
                    :type="getTextFieldType(attribute.name)"
                    :rules="
                      applicationStore.clientAndServerRules[attribute.name]
                    "
                  ></TextField>
                  <Select
                    v-else-if="
                      getFormFieldType(attribute.annotations?.inputType) ===
                      FORM_FIELD_TYPE_SELECTION
                    "
                    :label="handleDisplayName(attribute.displayName)"
                    item-title="name"
                    item-value="id"
                    :attribute="attribute.name"
                    :items="attribute.validations.options.options"
                    :name="attribute.name"
                    :model-value="user.attributes[attribute.name]"
                    :clearable="
                      attribute.annotations.inputType === 'multiselect'
                    "
                    :multiple="
                      attribute.annotations.inputType === 'multiselect'
                    "
                    :required="isUserAttributeRequired(attribute)"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event)
                    "
                  ></Select>
                  <p
                    :id="`${attribute.name}-validation-error`"
                    class="validation-error"
                  ></p>
                </v-col>
              </template>
            </v-row>

            <div class="two_group_class">
              <Select
                attribute="institutions"
                :clearable="profileStore.isAllowedToManage"
                :no-data-text="$t('label.no_data_text')"
                :label="$t('user.institutions')"
                :items="institutionStore.institutions"
                v-model="user.institutions"
                item-title="name"
                item-value="name"
                persistent-hint
                multiple
                required
              ></Select>
            </div>
            <div class="one_group_class">
              <Select
                attribute="role"
                :clearable="profileStore.isAllowedToManage"
                :readonly="!profileStore.isAllowedToManage"
                :label="$t('user.role')"
                :items="userRoles"
                item-value="name"
                name="role"
                required
                v-model="user.role"
                persistent-hint
              ></Select>
            </div>
          </v-form>
          <v-label>* {{ $t("hints.required_fields") }}</v-label>
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
import { useForm } from "@/lib/use-form.js";
import { handleDisplayName, updateUser } from "@/components/User/user.js";
import Checkbox from "@/components/Form/Checkbox.vue";
import TextField from "@/components/Form/TextField.vue";
import Select from "@/components/Form/Select.vue";
import ConfirmCancelDialog from "@/components/ConfirmCancelDialog.vue";

const { t } = useI18n();
const { hasLoadingError, hasRequestError, resetNotification } =
  useNotification();

const applicationStore = useApplicationStore();
const institutionStore = useInstitutionStore();
const profileStore = useProfileStore();
const userStore = useUserStore();

provide("translationCategory", "user");

const FORM_FIELD_TYPE_TEXTFIELD = "textfield";
const FORM_FIELD_TYPE_SELECTION = "selection";
const FORM_FIELD_TYPE_CHIPTEXTFIELD = "chiptextfield";

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

const getMetaDataAttribute = (nameOfAttribute) => {
  return profileStore.attributes.find(
    (attribute) => attribute.name === nameOfAttribute
  );
};
const getFormFieldType = (inputType, multivalued) => {
  // textfield is the fallback if an attribute from user-profile.json contains combinations of values
  // for which we don't have a special field yet.
  if (!inputType) return FORM_FIELD_TYPE_TEXTFIELD;
  if (
    ["text", "html5-email", "html5-tel", "html5-url", "html5-number"].includes(
      inputType
    )
  ) {
    if (multivalued) {
      return FORM_FIELD_TYPE_CHIPTEXTFIELD;
    } else {
      return FORM_FIELD_TYPE_TEXTFIELD;
    }
  } else if (["select", "multiselect"].includes(inputType)) {
    return FORM_FIELD_TYPE_SELECTION;
  } else return FORM_FIELD_TYPE_TEXTFIELD;
};

// Get the input type that Keycloak uses to a type that can be used by Vuetify's v-text-field.
const getTextFieldType = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  if (attribute.annotations?.inputType === "html5-email") {
    return "email";
  } else if (attribute.annotations?.inputType === "html5-tel") {
    return "tel";
  } else {
    return "text";
  }
};

const isUserAttributeRequired = (userAttribute) => {
  // In Keycloak it is not possible to choose if email
  // is a required attribute so it won't appear in the
  // UserProfileMetadata. That's why we can't handle it the "generic"
  // way.
  if (userAttribute.name === "email") return true;
  return userAttribute.required?.roles?.includes("user");
};

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
        message = t("error.invalid_length", [
          displayName,
          length.min,
          length.max,
        ]);
      } else if (length.min) {
        message = t("error.invalid_length_too_short", [
          displayName,
          length.min,
        ]);
      } else {
        message = t("error.invalid_length_too_long", {
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
  if (!userStore.roles) {
    userStore.loadRoles();
  }
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
  var roles = userStore.roles ?? [];
  // If available, use description field for localization
  roles.forEach(
    (item) => (item.title = item.description ? t(item.description) : item.name)
  );
  return roles.filter(
    // Only allow 'chief_editor' to grant this role
    (item) =>
      item.name !== "chief_editor" ||
      user.value.role === "chief_editor" ||
      profileStore.userData.role === "chief_editor"
  );
});
// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const createUser = () => {
  HTTP.post("/iamuser", user.value)
    .then((response) => {
      userStore.addUser(response.data);
      applicationStore.setOwnAccount(true);
      applicationStore.setShowManageUserDialog(false);
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
    hasRequestError
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
  return !profileStore.isAllowedToManage;
});

// Necessary so tests are able to access exactly these instances used in this component.
defineExpose({
  form,
  resetForm,
});
</script>
