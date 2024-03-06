/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { ref, watch } from "vue";
import { useI18n } from "vue-i18n";

export function useForm() {
  const { t } = useI18n();
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
      (v) => doesRegexMatchWholeString(regExprEmail, v) || v == "" || validMsg,
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
    (v) => doesRegexMatchWholeString(regExprPhone, v) || validMsg;
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
    return matches && matches.length === 1 && matches[0] === text;
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
  const resetForm = (
    originalObject,
    changedObject,
    resetNotificationCallback
  ) => {
    if (resetNotificationCallback) resetNotificationCallback();
    const changedKeys = Object.keys(changedObject);
    changedKeys.forEach((key) => {
      if (!originalObject[key]) {
        delete changedObject[key];
      }
    });
    Object.assign(changedObject, originalObject);
  };

  const areObjectsDifferent = (a, b) => {
    const allKeys = [...Object.keys(a), ...Object.keys(b)];
    for (let i = 0; i < allKeys.length; i++) {
      const key = allKeys[i];
      if (
        a[key] === null ||
        (b[key] === null &&
          ((a[key] === null && b[key] !== null) ||
            (a[key] !== null && b[key] === null)))
      ) {
        return true;
      } else if (typeof a[key] === "object" && typeof b[key] === "object") {
        // Compare arrays
        if (a[key].length && b[key].length && a[key].length === b[key].length) {
          for (let j = 0; j < a[key].length; j++) {
            if (!b[key].includes(a[key][j])) {
              return true;
            }
          }
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

  const watchChange = (originalObject, changedObject) => {
    watch(changedObject, () => {
      hasNoChange.value = !areObjectsDifferent(originalObject, changedObject);
    });
  };

  const onCancel = (cancelCallback) => {
    showConfirmCancelDialog.value = !hasNoChange.value;
    if (!showConfirmCancelDialog.value) cancelCallback();
  };
  const closeConfirmCancelDialog = () => {
    showConfirmCancelDialog.value = false;
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
  };
}
