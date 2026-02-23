<!--
 Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->

<!-- eslint-disable vue/no-v-for-template-key -->
<template>
  <FloatingDialog :z-index="managedItem.zIndex" @raise="applicationStore.raiseManagedItem(props.index)">
    <template v-slot:header>
      <span v-if="[PROCESS_TYPE.ADD, PROCESS_TYPE.COPY].indexOf(processType) !== -1" class="text-h5">{{
        $t("user.createTitle") }}</span>
      <span class="text-h5" v-else-if="processType === PROCESS_TYPE.EDIT">{{ $t("user.editTitle", {
        name:
          user?.attributes.username[0] }) }}
      </span>
      <span v-else-if="processType === PROCESS_TYPE.EDIT_PROFILE" class="text-h5">{{ $t("user.editProfileTitle")
      }}</span>
    </template>
    <v-form v-model="valid" ref="form" :readonly="isReadOnly">
      <v-container class="pa-3">
        <v-row>
          <v-col v-if="processType !== PROCESS_TYPE.EDIT" cols="12">
            <TextField attribute="username" density="compact" :ref="'username'" :variant="[PROCESS_TYPE.ADD, PROCESS_TYPE.COPY].indexOf(processType) !== -1
              ? 'underlined'
              : 'plain'
              " :label="$t('user.username')" :model-value="user.attributes.username" required @update:model-value="
                clearValidationError('username');
              setUserAttribute('username', $event);
              "></TextField>
          </v-col>
        </v-row>
        <v-row>
          <v-col style="max-width: fit-content">
            <Checkbox
              @change="(event) => {
              if (event.target.checked) {
                user.retired = false;
              }
              }"
              attribute="enabled"
              :label="$t('user.enabled')"
              v-model="user.enabled"
              :disabled="profileStore.userData.role !== 'chief_editor'"></Checkbox>
          </v-col>
          <v-col v-if="profileStore.userData.role === 'chief_editor'" style="max-width: fit-content">
            <Checkbox
              @change="(event) => {
              if (event.target.checked) {
                user.enabled = false;
              }
              }"
              attribute="retired"
              :label="$t('user.retired')"
              v-model="user.retired"
            ></Checkbox>
          </v-col>
          <v-col style="max-width: fit-content">
            <Checkbox attribute="hiddenInAddressbook" :label="$t('user.hiddenInAddressbook')"
              v-model="user.hiddenInAddressbook"></Checkbox>
          </v-col>
        </v-row>
        <template v-for="group in filteredAttributeGroups" :key="group.name">
          <v-row>
            <v-col>
              <v-label>{{ $t(`user.${group.name}`) }}</v-label>
            </v-col>
          </v-row>
          <v-row>
            <template v-for="attribute in profileStore.filteredAttributesOfGroup(
              group.name,
              toRaw(user)
            )" :key="attribute.name">
              <v-col :cols="cols">
                <GenericUserFormField :attribute="attribute" :clearValidationError="clearValidationError"
                  :setUserAttribute="setUserAttribute" :user="user" :disabled="isDisabled(attribute)" />
              </v-col>
            </template>
          </v-row>
        </template>

        <v-row>
          <v-label>{{ $t("user.misc") }}</v-label>
        </v-row>
        <v-row>
          <template v-for="attribute in profileStore.attributesWithoutGroup" :key="attribute.name">
            <v-col v-if="attribute.name !== 'username'" cols="6">
              <GenericUserFormField :attribute="attribute" :clearValidationError="clearValidationError"
                :setUserAttribute="setUserAttribute" :user="user" :disabled="isDisabled(attribute)" />
              <p :id="`${attribute.name}-validation-error`" class="validation-error"></p>
            </v-col>
          </template>
        </v-row>

        <v-row>
          <v-col>
            <Select attribute="institutions" :clearable="profileStore.isAllowedToManage"
              :no-data-text="$t('label.noDataText')" :label="$t('user.institutions')"
              :items="institutionStore.sortedInstitutions" v-model="user.institutions" item-title="name"
              item-value="name" persistent-hint multiple required></Select>
          </v-col>
          <v-col>
            <Combobox :items="applicationStore.networks" item-title="name" item-value="name" :label="$t('user.network')"
              :attribute="'network'" :disabled="profileStore.userData.role !== 'chief_editor'"
              :model-value="user.network" @update:model-value="user.network = $event" required></Combobox>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <Select attribute="role" :disabled="profileStore.userData.role !== 'chief_editor'" :label="$t('user.role')"
              :items="userRoles" item-value="name" name="role" required v-model="user.role" persistent-hint></Select>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-label>* {{ $t("hints.requiredFields") }}</v-label>
          </v-col>
        </v-row>
      </v-container>
    </v-form>
    <UIAlert v-if="hasLoadingError || hasRequestError" v-bind:message="applicationStore.httpErrorMessage" />
    <template v-slot:actions>
      <v-btn v-if="!isReadOnly" color="accent" :disabled="!valid || hasNoChange" @click="
        [PROCESS_TYPE.ADD, PROCESS_TYPE.COPY].indexOf(processType) !== -1
          ? createUser()
          : saveUser()
        ">
        {{
          [PROCESS_TYPE.ADD, PROCESS_TYPE.COPY].indexOf(processType) !== -1
            ? $t("button.create")
            : $t("button.save")
        }}
      </v-btn>
      <v-btn v-if="[PROCESS_TYPE.EDIT, PROCESS_TYPE.EDIT_PROFILE].includes(processType) && !isReadOnly" color="accent" :disabled="hasNoChange"
        @click="resetForm(resetNotification)">
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn color="accent" @click="
        onCancel(() => {
          close();
        })
        ">
        {{ $t("button.cancel") }}
      </v-btn>
    </template>
  </FloatingDialog>
  <ConfirmCancelDialog :isActive="showConfirmCancelDialog" :onConfirm="close"
    :onCancel="() => closeConfirmCancelDialog()"></ConfirmCancelDialog>
</template>

<script setup>
import {
  computed,
  provide,
  onMounted,
  onUnmounted,
  toRaw,
  ref,
} from "vue";
import { useNotification } from "@/lib/use-notification.js";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http.js";
import { PROCESS_TYPE, useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import { useProfileStore } from "@/stores/profile.js";
import { useUserStore } from "@/stores/user.js";
import { trimSpacesInObject, validLength, validRegex } from "@/lib/form-helper.js";
import { useForm } from "@/lib/use-form.js";
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

const props = defineProps(["index"]);

provide("translationCategory", "user");
provide("managedItemIndex", props.index);

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

const close = () => {
  applicationStore.removeManagedItem(props.index);
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

const isDisabled = (attribute) => {
  return !(
    profileStore.userData.role === "chief_editor" ||
    profileStore.userData.role === "editor" ||
    attribute.name !== "tags"
  );
};

onMounted(() => {
  const rules = {};
  profileStore.attributes.forEach((userAttribute) => {
    rules[userAttribute.name] = getUserAttributeRules(userAttribute);
  });
  setClientRules(rules);
});
onUnmounted(() => {
  removeAllResetEventListeners();
});
const managedItem = applicationStore.managedItems[props.index];
const user = ref(managedItem.item);
const originalUser = ref(managedItem.originalItem);
const processType = managedItem.processType;

const userRoles = computed(() => {
  return (
    userStore.roles?.map((item) => {
      const newItem = structuredClone(item);
      newItem.props = {};
      // Only allow 'chief_editor' to grant roles other than 'user'
      newItem.props.disabled =
        item.name !== "user" && profileStore.userData.role !== "chief_editor";
      // If available, use description field for localization
      newItem.title = item.description ? t(item.description) : item.name;
      return newItem;
    }) ?? []
  );
});
const filteredAttributeGroups = computed(() => {
  return (
    profileStore.attributeGroups?.filter((group) => {
      // Filter out groups which don't contain any attributes that the user can see.
      return (
        (user.value.attributes?.username &&
          profileStore.getOwnUsername === user.value.attributes.username[0]) ||
        profileStore.filteredAttributesOfGroup(group.name, toRaw(user.value))
          ?.length > 0
      );
    }) ?? []
  );
});

const createUser = () => {
  HTTP.post("/iam/user", trimSpacesInObject(user.value))
    .then((response) => {
      const newUser = response.data;
      userStore.addUser(newUser);
      finishUserDialog(newUser, props.index);
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
    props.index
  );
};

// Form
const {
  form,
  valid,
  hasNoChange,
  resetForm,
  onCancel,
  showConfirmCancelDialog,
  closeConfirmCancelDialog,
  handleValidationErrorFromServer,
  clearValidationError,
  isServerValidationError,
  removeAllResetEventListeners,
  setClientRules,
  cols,
} = useForm({
  originalObject: originalUser,
  changedObject: user
});

const isReadOnly = computed(() => {
  if (
    user.value.attributes?.username &&
    user.value.attributes.username[0] ===
    profileStore.userData.attributes.username[0]
  ) {
    return false;
  }
  return (
    !profileStore.isAllowedToManage ||
    (profileStore.userData.role !== "chief_editor" &&
      user.value.network !== profileStore.userData.network)
  );
});

// Necessary so tests are able to access exactly these instances used in this component.
defineExpose({
  form,
  resetForm,
});
</script>
