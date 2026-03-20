/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { computed, nextTick, provide, ref, useTemplateRef } from "vue";
import { useI18n } from "vue-i18n";
import { useApplicationStore } from "@/stores/application.js";
import { areArraysDifferent, areObjectsDifferent } from "@/lib/form-helper";

export function useForm({ originalObject, changedObject, rules, i18n }) {
  const { t } = i18n?.global ?? useI18n();
  const form = useTemplateRef("form");
  const valid = ref(false);
  const hasNoChange = computed(() => {
    if (!originalObject && !changedObject) return true;
    return (
      !areObjectsDifferent(originalObject.value, changedObject.value) &&
      changedAttributes.value.length === 0
    );
  });
  const showConfirmCancelDialog = ref(false);
  const changedAttributes = ref([]);
  const resetEventListeners = ref([]);
  const clientRules = ref(rules);
  // Object with fake rules. It contains maximal one rule per input field.
  // These rules always return a message so they always lead to an
  // error message for the attribute. This way we can use Vuetify's internal
  // mechanism to show error messages. We use it to show validation errors
  // coming from keycloak.
  const serverValidationRules = ref({});
  const clientAndServerRules = computed(() => {
    const tmpRules = {};
    // First add the server rules and then the client rules because Vuetify shows only one
    // error message at a time and if the user saved an item already they obviously passed
    // the client rules and the server rules are more important so we decide to show them.
    Object.keys(serverValidationRules.value).forEach((key) => {
      if (!tmpRules[key]) {
        tmpRules[key] = [];
      }
      tmpRules[key].push(serverValidationRules.value[key]);
    });
    if (clientRules.value) {
      Object.keys(clientRules.value).forEach((key) => {
        if (!tmpRules[key]) {
          tmpRules[key] = [];
        }
        tmpRules[key].push(...clientRules.value[key]);
      });
    }
    return tmpRules;
  });
  const dialogWidth = ref(600);
  const resetForm = async (resetNotificationCallback) => {
    if (resetNotificationCallback) resetNotificationCallback();
    const keysOfChangedItem = Object.keys(changedObject.value);
    keysOfChangedItem.forEach((key) => {
      if (!originalObject.value[key]) {
        delete changedObject.value[key];
      }
    });
    Object.assign(changedObject.value, structuredClone(originalObject.value));
    await nextTick();
    changedAttributes.value = [];
    form.value?.validate();
    callResetEventListener();
  };

  const addResetEventListener = (listener) => {
    resetEventListeners.value = [...resetEventListeners.value, listener];
  };
  const removeAllResetEventListeners = () => {
    resetEventListeners.value = [];
  };
  const callResetEventListener = () => {
    resetEventListeners.value.forEach((listener) => {
      listener();
    });
  };

  const submitChange = (attribute) => {
    changedAttributes.value = [...changedAttributes.value, attribute];
  };
  const removeChange = (attribute) => {
    const index = changedAttributes.value.findIndex((a) => a === attribute);
    if (index !== -1) {
      changedAttributes.value = changedAttributes.value.toSpliced(index, 1);
    }
  };

  const onCancel = (cancelCallback) => {
    showConfirmCancelDialog.value = !hasNoChange.value;
    if (!showConfirmCancelDialog.value) cancelCallback();
  };
  const closeConfirmCancelDialog = () => {
    showConfirmCancelDialog.value = false;
  };

  const setClientRules = (rules) => {
    clientRules.value = rules;
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
            charAfterDash.toUpperCase(),
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
    for (let i = 0; i < error.length; i++) {
      const errorObject = error[i];
      // If error is a list element, ignore element position.
      const attribute = errorObject.messageParameters[0].split("[")[0];
      const message = errorObject.message;
      const translatedMessage = translateError(
        message,
        errorObject.messageParameters,
      );
      // Create rules that can be used by the validation mechanism of Vuetify.
      serverValidationRules.value[attribute] = () => {
        return translatedMessage;
      };
      // Need to wait for the DOM. Otherwise the error messages are not automatically shown.
      await nextTick();
      form.value?.validate();
    }
  };

  const clearValidationError = async (attribute) => {
    if (serverValidationRules.value[attribute]) {
      delete serverValidationRules.value[attribute];
      await nextTick();
    }
    form.value.validate();
  };

  const isServerValidationError = (error) => {
    return error.response?.status === 400 && error.response?.data?.[0]?.message;
  };

  const onUpdateModelValue = (event, emit, attribute) => {
    clearValidationError(attribute);
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

  const setDialogWidth = (width) => {
    dialogWidth.value = width;
  };

  const cols = computed(() => {
    return dialogWidth.value > 1200 ? 4 : dialogWidth.value > 600 ? 6 : 12;
  });

  provide("useForm", {
    changedAttributes,
    submitChange,
    removeChange,
    addResetEventListener,
    removeAllResetEventListeners,
    onUpdateModelValue,
    clientAndServerRules,
    setDialogWidth,
    form,
  });

  return {
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
    onUpdateModelValue,
    translateError,
    areArraysDifferent,
    showFormError,
    removeAllResetEventListeners,
    clientAndServerRules,
    clientRules,
    setClientRules,
    cols,
  };
}
