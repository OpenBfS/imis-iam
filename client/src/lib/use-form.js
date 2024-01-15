/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { ref } from "vue";
import { useI18n } from "vue-i18n";

export function useForm() {
  const { t } = useI18n();
  const form = ref(null);
  const valid = ref(false);
  const regExprPhone = /(\(?([\d \-)–+/(]+){6,}\)?([ .\-–/]?)([\d]+))/;
  const regExprEmail = /[a-z0-9]+@[a-z]+\.[a-z]{2,3}/;
  const germanDateRegex = /[\d]{1,2}\.[\d]{1,2}\.[\d]{4}/;
  // Validation rules
  const reqValidmail = (reqMsg, validMsg) => {
    return [(v) => !!v || reqMsg, (v) => regExprEmail.test(v) || validMsg];
  };
  const validMail = (validMsg) => {
    return [(v) => regExprEmail.test(v) || v == "" || validMsg];
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
    (v) => regExprPhone.test(v) || validMsg;
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
        t("form.valid_date"),
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
  return {
    form,
    valid,
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
  };
}
