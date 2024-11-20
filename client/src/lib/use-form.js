/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { nextTick, ref, watch } from "vue";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application";

export function useForm(i18n) {
  const { t } = i18n?.global ?? useI18n();
  const form = ref(null);
  const valid = ref(false);
  const hasNoChange = ref(true);
  const showConfirmCancelDialog = ref(false);
  const regExprPhone = /(\(?([\d \-)–+/(]+){6,}\)?([ .\-–/]?)([\d]+))/;
  const regExprEmail = /^\S+@\S+\.\S+$/;
  const germanDateRegex = /[\d]{1,2}\.[\d]{1,2}\.[\d]{4}/;
  // Validation rules
  const reqValidmail = (reqMsg, validMsg) => {
    return [
      (v) => !!v || reqMsg,
      (v) => doesRegexMatchWholeString(regExprEmail, v) || validMsg,
    ];
  };
  const validMail = (validMsg) => {
    return [
      (v) => !v || doesRegexMatchWholeString(regExprEmail, v) || validMsg,
    ];
  };
  const reqValidPhone = (reqMsg, validMsg) => {
    return [
      (v) => !!v || reqMsg,
      (v) =>
        (v &&
          v.toString().match(regExprPhone)?.[0] === v.toString() &&
          v.toString().match(regExprPhone)?.[0].length ===
            v.toString().length) ||
        validMsg,
    ];
  };
  const validPhone = (validMsg) => {
    return [
      (v) => !v || doesRegexMatchWholeString(regExprPhone, v) || validMsg,
    ];
  };
  const validPostalcode = (validMsg) => {
    return [(v) => /^\d{5}$/.test(v) || v == "" || validMsg];
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
        t("error.valid_date"),
    ];
  };
  const reqValidPostalcode = (reqMsg, validMsg) => {
    return [
      (v) => !!v || reqMsg,
      (v) => /^\d{5}$/.test(v) || v == "" || validMsg,
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
        (v &&
          v.toString().match(regex)?.[0] === v.toString() &&
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
    applicationStore.$subscribe((mutation) => {
      if (mutation.events.key === "attributesOfFieldsThatChanged") {
        updateHasNoChange(originalObject, changedObject);
      }
    });
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

  const translateError = (message, parameters) => {
    let translatedMessage;
    // Keycloak error is not translated
    if (message.startsWith("error-")) {
      const stringToTranslate = message.startsWith("error-")
        ? message.replace("error-", "error.").replaceAll("-", "_")
        : message;
      parameters[0] = t(`user.${parameters[0].toLowerCase()}`);
      translatedMessage = t(stringToTranslate, parameters);
    } else if (message.startsWith("error.")) {
      translatedMessage = t(message, parameters[0]);
    }
    // Keycloak error is already translated
    else {
      translatedMessage = message;
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
      const attribute = errorObject.messageParameters[0];
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
    Object.keys(applicationStore.clientRules).forEach((key) => {
      applicationStore.clientAndServerRules[key].push(
        ...applicationStore.clientRules[key]
      );
    });
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

  return {
    form,
    valid,
    hasNoChange,
    reqValidmail,
    validMail,
    reqField,
    reqValidPhone,
    validPhone,
    validPostalcode,
    validGermanDate,
    reqValidPostalcode,
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
  };
}
