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
            <v-row v-if="processType !== 'edit'">
              <v-col>
                <v-text-field
                  density="compact"
                  :variant="
                    ['add', 'copy'].indexOf(processType) !== -1
                      ? 'underlined'
                      : 'plain'
                  "
                  :label="$t('user.username')"
                  :model-value="user.attributes.username"
                  :rules="
                    reqField(
                      $t('error.user_attribute_required', {
                        attr: t('user.username'),
                      })
                    )
                  "
                  @update:model-value="setUserAttribute('username', $event)"
                ></v-text-field>
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
                    <v-text-field
                      v-if="isTextField(attribute.annotations?.inputType)"
                      density="compact"
                      variant="underlined"
                      :label="handleDisplayName(attribute.displayName)"
                      :name="attribute.name"
                      :model-value="user.attributes[attribute.name]"
                      @update:model-value="
                        setUserAttribute(attribute.name, $event)
                      "
                      :type="getTextFieldType(attribute.name)"
                      :rules="getRules(attribute.name)"
                    ></v-text-field>
                    <v-select
                      v-else-if="isSelection(attribute.annotations?.inputType)"
                      density="compact"
                      :label="handleDisplayName(attribute.displayName)"
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
                      :rules="getRules(attribute.name)"
                    ></v-select>
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
                <v-col cols="6">
                  <v-text-field
                    v-if="isTextField(attribute.annotations?.inputType)"
                    density="compact"
                    variant="underlined"
                    :label="handleDisplayName(attribute.displayName)"
                    :name="attribute.name"
                    :model-value="user.attributes[attribute.name]"
                    @update:model-value="
                      setUserAttribute(attribute.name, $event)
                    "
                    :type="getTextFieldType(attribute.name)"
                    :rules="getRules(attribute.name)"
                  ></v-text-field>
                  <v-select
                    v-else-if="
                      ['select', 'multiselect'].includes(
                        attribute.annotations.inputType
                      )
                    "
                    density="compact"
                    :label="handleDisplayName(attribute.displayName)"
                    item-title="name"
                    item-value="id"
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
                    :rules="getRules(attribute.name)"
                  ></v-select>
                </v-col>
              </template>
            </v-row>

            <div class="two_group_class">
              <v-select
                :clearable="profileStore.isAllowedToManage"
                :no-data-text="$t('label.no_data_text')"
                dense
                :label="$t('user.label_institutions')"
                :items="institutionStore.institutions"
                v-model="user.institutions"
                item-title="name"
                item-value="id"
                persistent-hint
                multiple
                :rules="reqMultipleSelect($t('user.required_institution'))"
              >
              </v-select>
              <v-select
                :clearable="profileStore.isAllowedToManage"
                :no-data-text="$t('label.no_data_text')"
                dense
                :label="$t('user.label_memberships')"
                :items="userStore.memberships"
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
                :clearable="profileStore.isAllowedToManage"
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
        @click="applicationStore.setManagedItem(cloneObject(originalUser))"
      >
        {{ $t("button.reset") }}
      </v-btn>
      <v-btn
        color="accent"
        @click="
          applicationStore.setOwnAccount(false);
          applicationStore.setShowManageUserDialog(false);
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
import { computed, onMounted, nextTick } from "vue";
import { useNotification } from "@/lib/use-notification";
import { useI18n } from "vue-i18n";
import { HTTP } from "@/lib/http";
import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";
import { useProfileStore } from "@/stores/profile";
import { useUserStore } from "@/stores/user";
import { useForm } from "@/lib/use-form";
import { expUser } from "@/components/User/user";

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
const getRules = (nameOfAttribute) => {
  const attribute = getMetaDataAttribute(nameOfAttribute);
  const rules = [];
  // Rules for text field components
  if (!attribute.validations?.options) {
    if (
      // In Keycloak it is not possible to choose if email
      // is a required attribute so it won't appear in the
      // UserProfileMetadata. That's why we can't handle it the "generic"
      // way.
      attribute.name === "email" ||
      attribute.required?.roles?.includes("user")
    ) {
      rules.push(
        ...reqField(
          t("error.user_attribute_required", {
            attr: handleDisplayName(attribute.displayName),
          })
        )
      );
    }

    if (attribute.validations?.pattern) {
      const pattern = attribute.validations.pattern.pattern;
      const errorMessage = attribute.validations.pattern["error-message"];
      rules.push(...validRegex(pattern, t(errorMessage)));
    }

    if (attribute.validations?.length) {
      const length = attribute.validations.length;
      let message;
      const displayName = handleDisplayName(attribute.displayName);
      if (length.min && length.max) {
        message = t("error.invalid_length", {
          attr: displayName,
          min: length.min,
          max: length.max,
        });
      } else if (length.min) {
        message = t("error.invalid_length_too_short", {
          attr: displayName,
          min: length.min,
        });
      } else {
        message = t("error.invalid_length_too_long", {
          attr: displayName,
          max: length.max,
        });
      }
      rules.push(...validLength(length.min, length.max, message));
    }
  }
  // Rules for select components
  else {
    rules.push(
      ...reqField(t("error.user_attribute_required", { attr: attribute.name }))
    );
  }
  return rules;
};
const getUserMemberships = () => {
  userStore.loadMemberships().catch(() => {
    hasLoadingError.value = false;
  });
};
const getInstitutions = () => {
  institutionStore
    .loadInstitutions()
    .then()
    .catch(() => {
      hasLoadingError.value = true;
    });
};
onMounted(() => {
  getUserMemberships();
  getInstitutions();
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
  return roles;
});
// Deep Copy for objects
const cloneObject = (obj) => {
  return JSON.parse(JSON.stringify(obj));
};
const getUsers = () => {
  userStore
    .loadUsers()
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
        applicationStore.setOwnAccount(true);
        applicationStore.setShowManageUserDialog(false);
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
  userStore
    .updateUser(user.value)
    .then(() => {
      // Update current user Profile and thus the data in App bar.
      if (profileStore.userData.id === user.value.id) {
        profileStore.loadProfile();
      }
      applicationStore.setOwnAccount(false);
      applicationStore.setShowManageUserDialog(false);
    })
    .catch((error) => {
      if (error.response.status === 400 && error.response.data[0]?.message) {
        let allMessages = "";
        for (let i = 0; i < error.response.data.length; i++) {
          const errorObject = error.response.data[i];
          const message = errorObject.message;
          const stringToTranslate = message.startsWith("error-")
            ? message.replace("error-", "error.").replaceAll("-", "_")
            : message;
          errorObject.messageParameters[0] = t(
            `user.${errorObject.messageParameters[0].toLowerCase()}`
          );
          allMessages = allMessages.concat(
            t(stringToTranslate, errorObject.messageParameters)
          );
          if (i < error.response.data.length - 1) {
            allMessages = allMessages.concat(" ");
          }
        }
        applicationStore.setHttpErrorMessage(allMessages);
      }
      console.error(error);
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
const { form, valid, reqField, reqMultipleSelect, validRegex, validLength } =
  useForm();
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
  if (applicationStore.ownAccount) {
    return false;
  }
  return !profileStore.isAllowedToManage;
});
</script>
