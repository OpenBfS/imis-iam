<!--
 Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 Software engineering by Intevation GmbH

 This file is Free Software under the GNU GPL (v>=3)
 and comes with ABSOLUTELY NO WARRANTY!
 -->

<template>
  <ChipTextField
    v-if="
      getFormFieldType(
        attribute.annotations?.inputType,
        attribute.multivalued
      ) === FORM_FIELD_TYPE_CHIPTEXTFIELD
    "
    v-bind="commonProps"
  ></ChipTextField>
  <TextField
    v-else-if="
      getFormFieldType(attribute.annotations?.inputType) ===
      FORM_FIELD_TYPE_TEXTFIELD
    "
    v-bind="commonProps"
    :type="getTextFieldType(attribute.name)"
  ></TextField>
  <Select
    v-else-if="
      getFormFieldType(attribute.annotations?.inputType) ===
      FORM_FIELD_TYPE_SELECTION
    "
    v-bind="commonProps"
    item-title="name"
    item-value="id"
    :items="attribute.validations.options.options"
    :clearable="attribute.annotations.inputType === 'multiselect'"
    :multiple="attribute.annotations.inputType === 'multiselect'"
  ></Select>
</template>

<script setup>
import { computed } from "vue";
import { useProfileStore } from "@/stores/profile.js";
import { handleDisplayName } from "@/components/User/user.js";

const profileStore = useProfileStore();

const props = defineProps({
  attribute: Object,
  clearValidationError: Function,
  setUserAttribute: Function,
  user: Object,
});

// Creating these variables so we don't have to write "props." everywhere.
const attribute = props.attribute;
const clearValidationError = props.clearValidationError;
const setUserAttribute = props.setUserAttribute;
const user = props.user;

const commonProps = computed(() => {
  return {
    attribute: attribute.name,
    label: handleDisplayName(attribute.displayName),
    modelValue: user.attributes[attribute.name],
    name: attribute.name,
    required: isUserAttributeRequired(attribute),
    "onUpdate:modelValue": (event) => {
      setUserAttribute(attribute.name, event);
      clearValidationError(attribute.name);
    },
  };
});

const FORM_FIELD_TYPE_TEXTFIELD = "textfield";
const FORM_FIELD_TYPE_SELECTION = "selection";
const FORM_FIELD_TYPE_CHIPTEXTFIELD = "chiptextfield";

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
</script>
