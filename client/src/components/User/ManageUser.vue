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
                  density="compact"
                  :ref="'username'"
                  :variant="
                    ['add', 'copy'].indexOf(processType) !== -1
                      ? 'underlined'
                      : 'plain'
                  "
                  :label="$t('user.username')"
                  :model-value="user.attributes.username"
                  :rules="applicationStore.clientAndServerRules['username']"
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
                    <TextField
                      v-if="isTextField(attribute.annotations?.inputType)"
                      density="compact"
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
                      v-else-if="isSelection(attribute.annotations?.inputType)"
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
                  <TextField
                    v-if="isTextField(attribute.annotations?.inputType)"
                    density="compact"
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
                      ['select', 'multiselect'].includes(
                        attribute.annotations.inputType
                      )
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
                :label="$t('user.label_institutions')"
                :items="institutionStore.institutions"
                v-model="user.institutions"
                item-title="name"
                item-value="name"
                persistent-hint
                multiple
              ></Select>
            </div>
            <div class="one_group_class">
              <Select
                attribute="role"
                :clearable="profileStore.isAllowedToManage"
                :disabled="!profileStore.isAllowedToManage"
                :label="$t('user.label_roles')"
                :items="userRoles"
                item-value="name"
                v-model="user.role"
                persistent-hint
              ></Select>
            </div>
          </v-form>
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
import { computed, onBeforeMount, onMounted, nextTick } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { useForm } from "@/lib/use-form";
import { getExpUser } from "@/components/User/user";
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
const isTextField = (inputType) => {
  if (
    !inputType ||
    ["text", "html5-email", "html5-tel", "html5-url", "html5-number"].includes(
      inputType
    )
  ) {
    return true;
  } else {
    return false;
  }
};
const isSelection = (inputType) => {
  if (inputType && ["select", "multiselect"].includes(inputType)) {
    return true;
  } else {
    return false;
  }
};

// Convert the input type that Keycloak uses to a type that can be used by Vuetify.
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
const handleDisplayName = (displayName) => {
  if (displayName.startsWith("${") && displayName.endsWith("}")) {
    return t(`user.${displayName.replace("${", "").slice(0, -1)}`);
  }
  return displayName;
};

// Creating rules for the validation of form components.
const getUserAttributeRules = (userAttribute) => {
  const tmpRules = [];
  // Rules for text field components
  if (!userAttribute.validations?.options) {
    if (
      // In Keycloak it is not possible to choose if email
      // is a required attribute so it won't appear in the
      // UserProfileMetadata. That's why we can't handle it the "generic"
      // way.
      // TODO: Add rules for pattern and length as optional even when
      // attribute is not required (when optional rules are supported
      // by Vuetify)
      userAttribute.name === "email" ||
      userAttribute.required?.roles?.includes("user")
    ) {
      tmpRules.push(
        ...reqField(
          t("error.user_attribute_required", [
            handleDisplayName(userAttribute.displayName),
          ])
        )
      );

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
          message = t("error.invalid_length_too_long", [
            displayName,
            length.max,
          ]);
        }
        tmpRules.push(...validLength(length.min, length.max, message));
      }
    }
  }
  // Rules for select components
  else {
    tmpRules.push(
      ...reqField(t("error.user_attribute_required", [userAttribute.name]))
    );
  }
  return tmpRules;
};

onBeforeMount(() => {
  applicationStore.setForm(form);
  applicationStore.initClientRules({
    institutions: reqMultipleSelect(t("user.required_institution")),
    role: reqField(t("user.required_roles")),
    username: reqField(
      t("error.user_attribute_required", [t("user.username")])
    ),
  });
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
  var roles = userStore.roles;
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
const createUser = (shouldClose) => {
  HTTP.post("/iamuser", user.value)
    .then((response) => {
      userStore.addUser(response.data);
      if (shouldClose) {
        applicationStore.setOwnAccount(true);
        applicationStore.setShowManageUserDialog(false);
      } else {
        // Use the same roles of the last created user.
        const usedRole = user.value.role;
        user.value = getExpUser();
        nextTick(() => {
          form.value.resetValidation();
          user.value.role = usedRole;
        });
      }
      applicationStore.searchRequest(["users"]);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
    });
};

const updateUser = () => {
  resetNotification();
  userStore
    .updateUser(user.value)
    .then(() => {
      // Update current user Profile and thus the data in App bar.
      if (profileStore.userData.id === user.value.id) {
        profileStore.loadProfile();
      }
      applicationStore.setOwnAccount(false);
      applicationStore.setShowManageUserDialog(false);
      applicationStore.searchRequest(["users"]);
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
      console.error(error);
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
  hasNoChange,
  reqField,
  reqMultipleSelect,
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
</script>
