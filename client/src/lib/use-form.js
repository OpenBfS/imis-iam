/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application.js";

export function useForm(i18n) {
  const { t } = i18n?.global ?? useI18n();
  const form = ref(null);
  const valid = ref(false);
  const hasNoChange = ref(true);
  const showConfirmCancelDialog = ref(false);
  const regExprPhone = /^\+[1-9][0-9]{7,16}$/;
  const regExprEmail = /^\S+@\S+\.\S+$/;
  const regExprPostalCode = /^\d{5}$/;
  const noLeadingTrailingSpaces = /\S.*\S/;
  const germanDateRegex = /[\d]{1,2}\.[\d]{1,2}\.[\d]{4}/;
  // Validation rules
  const validMail = (validMsg) => {
    return [
      (v) => !v || doesRegexMatchWholeString(regExprEmail, v) || validMsg,
    ];
  };
  const validPhone = (validMsg) => {
    return [
      (v) => !v || doesRegexMatchWholeString(regExprPhone, v) || validMsg,
    ];
  };
  const validPostalcode = (validMsg) => {
    return [(v) => regExprPostalCode.test(v) || v == "" || validMsg];
  };
  const dateStringToDate = (dateString) => {
    const parts = dateString.split(".");
    const year = parts[2];
    const month = parts[1].length === 1 ? "0" + parts[1] : parts[1];
    const day = parts[0].length === 1 ? "0" + parts[0] : parts[0];
    const date = new Date(`${year}-${month}-${day}T23:59:59`);
    date.setMilliseconds(999);
    return date.toDateString() !== "Invalid Date" ? date : undefined;
  };
  const doesRegexMatchWholeString = (regex, text) => {
    const matches = regex.exec(text);
    return matches && matches[0] === text;
  };
  const validGermanDate = () => {
    return [
      (v) =>
        !v ||
        v.length === 0 ||
        (doesRegexMatchWholeString(germanDateRegex, v) &&
          dateStringToDate(v) !== undefined) ||
        t("error.validDate"),
    ];
  };
  const reqField = (reqMsg) => {
    return [(v) => (v && Boolean(v.toString())) || reqMsg];
  };
  const reqMultipleSelect = (reqMsg) => {
    return [(v) => !!(v && v.length) || reqMsg];
  };

  const validRegex = (regex, validMsg) => {
    return [
      (v) =>
        // Make sure that the whole string has to be a match and that this
        // match is the only one.
        // Otherwise a string could be valid even if it had two or more
        // matches.
        !v ||
        (v.toString().match(regex)?.[0] === v.toString() &&
          v.toString().match(regex)?.[0].length === v.toString().length) ||
        validMsg,
    ];
  };
  const validLength = (minLength, maxLength, validMsg) => {
    return [
      (v) => {
        return (
          !v ||
          (v &&
            (!minLength || (minLength && v.toString().length >= minLength)) &&
            (!maxLength || (maxLength && v.toString().length <= maxLength))) ||
          validMsg
        );
      },
    ];
  };
  const resetForm = async (
    originalObject,
    changedObject,
    resetNotificationCallback
  ) => {
    const applicationStore = useApplicationStore();
    if (resetNotificationCallback) resetNotificationCallback();
    const changedKeys = Object.keys(changedObject);
    changedKeys.forEach((key) => {
      if (!originalObject[key]) {
        delete changedObject[key];
      }
    });
    Object.assign(changedObject, originalObject);
    await nextTick();
    applicationStore.attributesOfFieldsThatChanged = [];
    applicationStore.serverValidationRules = {};
    aggregateRules();
    form.value?.validate();
    applicationStore.callResetEventListener();
    hasNoChange.value = true;
  };

  const areObjectsDifferent = (a, b) => {
    const allKeys = [...Object.keys(a), ...Object.keys(b)];
    for (let i = 0; i < allKeys.length; i++) {
      const key = allKeys[i];
      if (
        (a[key] === undefined && b[key]?.length === 0) ||
        (a[key]?.length === 0 && b[key] === undefined)
      ) {
        // Treat empty array as if it was undefined because in the end both mean that there is no value for an attribute.
        continue;
      }
      if (a[key] === null && b[key] === null) {
        continue;
      } else if (
        (a[key] === null || b[key] === null) &&
        ((a[key] === null && b[key] !== null) ||
          (a[key] !== null && b[key] === null))
      ) {
        return true;
      } else if (typeof a[key] === "object" && typeof b[key] === "object") {
        // Compare arrays
        if (a[key].length && b[key].length && areArraysDifferent(a, b)) {
          return true;
        } else {
          // Compare nested objects
          if (areObjectsDifferent(a[key], b[key])) {
            return true;
          }
        }
      } else if (a[key] !== b[key]) {
        return true;
      }
    }
    return false;
  };

  const areArraysDifferent = (a, b) => {
    if (a.length !== b.length) return true;
    for (var i = 0; i < a.length; ++i) {
      if (a[i] !== b[i]) return true;
    }
    return false;
  };

  const watchChange = (originalObject, changedObject) => {
    const applicationStore = useApplicationStore();
    watch(
      () => applicationStore.attributesOfFieldsThatChanged,
      () => {
        updateHasNoChange(originalObject, changedObject);
      }
    );
    watch(changedObject, () => {
      updateHasNoChange(originalObject, changedObject);
    });
  };

  const updateHasNoChange = (originalObject, changedObject) => {
    const applicationStore = useApplicationStore();
    hasNoChange.value =
      !areObjectsDifferent(originalObject, changedObject) &&
      applicationStore.attributesOfFieldsThatChanged.length === 0;
  };

  const onCancel = (cancelCallback) => {
    showConfirmCancelDialog.value = !hasNoChange.value;
    if (!showConfirmCancelDialog.value) cancelCallback();
  };
  const closeConfirmCancelDialog = () => {
    showConfirmCancelDialog.value = false;
  };

  const initClientRules = (rules) => {
    const applicationStore = useApplicationStore();
    applicationStore.clientRules = rules;
    Object.keys(applicationStore.clientRules).forEach((key) => {
      applicationStore.clientAndServerRules[key] =
        applicationStore.clientRules[key];
    });
  };

  /**
   * Uses "message" and "messageParameters" returned from Keycloak to convert them to a translated
   * error message.
   *
   * There are three cases which need different handling:
   * 1. Keycloak returns a string which is already translated.
   * 2. Keycloak returns a string that we can control and that can directly be used as
   *    a translation key (starts with "error.").
   * 3. Keycloak returns a string that we do not control and which isn't translated
   *    (starts with "error-").
   */
  const translateError = (message, parameters) => {
    let translatedMessage = message;
    // Keycloak error is not translated
    if (message.startsWith("error-")) {
      // Need to convert the key from Keycloak because the translation keys use camelCase.
      let translationKey = message.replace("error-", "error.");
      let index = translationKey.indexOf("-");
      while (index !== -1) {
        if (index < translationKey.length - 1) {
          const charAfterDash = translationKey.charAt(index + 1);
          translationKey = translationKey.replace(
            `-${charAfterDash}`,
            charAfterDash.toUpperCase()
          );
        }
        index = translationKey.indexOf("-");
      }
      parameters[0] = t(`user.${parameters[0]}`);
      translatedMessage = t(translationKey, parameters);
    } else if (message.startsWith("error.")) {
      translatedMessage = t(message, parameters[0]);
    }
    return translatedMessage;
  };

  /**
   * @param {object} error:
   * {
   *   "message": "<error message>",
   *   "messageParameters": [
   *      "<attribute of the validated input field>"
   *   ]
   * }
   */
  const handleValidationErrorFromServer = async (error) => {
    const applicationStore = useApplicationStore();
    for (let i = 0; i < error.length; i++) {
      const errorObject = error[i];
      // If error is a list element, ignore element position.
      const attribute = errorObject.messageParameters[0].split("[")[0];
      const message = errorObject.message;
      const translatedMessage = translateError(
        message,
        errorObject.messageParameters
      );
      // Create rules that can be used by the validation mechanism of Vuetify.
      applicationStore.serverValidationRules[attribute] = () => {
        return translatedMessage;
      };
      aggregateRules();
      // Need to wait for the DOM. Otherwise the error messages are not automatically shown.
      await nextTick();
      form.value?.validate();
    }
  };

  const aggregateRules = () => {
    const applicationStore = useApplicationStore();
    Object.keys(applicationStore.clientAndServerRules).forEach((key) => {
      delete applicationStore.clientAndServerRules[key];
      applicationStore.clientAndServerRules[key] = [];
    });
    // First add the server rules and then the client rules because Vuetify shows only one
    // error message at a time and if the user saved an item already they obviously passed
    // the client rules and the server rules are more important so we decide to show them.
    Object.keys(applicationStore.serverValidationRules).forEach((key) => {
      if (!applicationStore.clientAndServerRules[key]) {
        // Necessary because it could be that the backend returns errors
        // for text fields which have no rules on the client side.
        // Therefore, rules and their keys of client and server might differ.
        applicationStore.clientAndServerRules[key] = [];
      }
      applicationStore.clientAndServerRules[key].push(
        applicationStore.serverValidationRules[key]
      );
    });
    Object.keys(applicationStore.clientRules).forEach((key) => {
      applicationStore.clientAndServerRules[key].push(
        ...applicationStore.clientRules[key]
      );
    });
  };

  const aggregateRulesForSingleAttribute = (attribute) => {
    const applicationStore = useApplicationStore();
    delete applicationStore.clientAndServerRules[attribute];
    applicationStore.clientAndServerRules[attribute] = [];
    if (applicationStore.clientRules[attribute]) {
      applicationStore.clientAndServerRules[attribute].push(
        ...applicationStore.clientRules[attribute]
      );
    }
    if (applicationStore.serverValidationRules[attribute]) {
      applicationStore.clientAndServerRules[attribute].push(
        applicationStore.serverValidationRules[attribute]
      );
    }
  };

  const clearValidationError = async (attribute) => {
    const applicationStore = useApplicationStore();
    if (applicationStore.serverValidationRules[attribute]) {
      delete applicationStore.serverValidationRules[attribute];
      aggregateRulesForSingleAttribute(attribute);
      await nextTick();
      form.value.validate();
    }
  };

  const isServerValidationError = (error) => {
    return error.response?.status === 400 && error.response?.data?.[0]?.message;
  };

  const onUpdateModelValue = (event, emit, attribute) => {
    const applicationStore = useApplicationStore();
    if (applicationStore.clientAndServerRules) {
      applicationStore.clearValidationError(attribute);
    }
    emit("update:modelValue", event);
  };

  const showFormError = (error, hasRequestError) => {
    const applicationStore = useApplicationStore();
    hasRequestError.value = true;
    const statusText = error.response?.statusText
      ? `: ${error.response.statusText}`
      : "";
    applicationStore.setHttpErrorMessage(`${error.message}${statusText}`);
  };

  // Creates a rule for required fields with a specific message if possible.
  // "translationCategory" is the top-level property in the locale file, e.g. "institution".
  const createRequiredRule = (required, attribute, translationCategory) => {
    const newRules = [];
    if (required === true) {
      if (attribute && translationCategory) {
        const translatedAttribute = t(`${translationCategory}.${attribute}`);
        const fullTranslation = t("error.requiredField", [translatedAttribute]);
        if (
          translationCategory !== undefined &&
          translatedAttribute?.length > 0 &&
          fullTranslation?.length > 0
        ) {
          newRules.push(...reqField(fullTranslation));
        } else {
          newRules.push(...reqField(t("error.requiredFieldGeneric")));
        }
      } else {
        newRules.push(...reqField(t("error.requiredFieldGeneric")));
      }
    }
    return newRules;
  };

  return {
    form,
    valid,
    hasNoChange,
    validMail,
    reqField,
    validPhone,
    validPostalcode,
    validGermanDate,
    reqMultipleSelect,
    dateStringToDate,
    doesRegexMatchWholeString,
    germanDateRegex,
    validRegex,
    validLength,
    resetForm,
    watchChange,
    onCancel,
    showConfirmCancelDialog,
    closeConfirmCancelDialog,
    initClientRules,
    handleValidationErrorFromServer,
    clearValidationError,
    isServerValidationError,
    onUpdateModelValue,
    translateError,
    areArraysDifferent,
    showFormError,
    createRequiredRule,
    noLeadingTrailingSpaces,
  };
}

export const trimSpacesInObject = (obj) => {
  const keys = Object.keys(obj);
  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    const value = obj[key];
    if (value === undefined || value === null) continue;
    if (typeof value === "string") {
      obj[key] = value.trim();
    } else if (typeof value === "object") {
      if (value.length) {
        obj[key] = value.map((v) => v.trim());
      } else {
        trimSpacesInObject(obj[key]);
      }
    }
  }
  return obj;
};
