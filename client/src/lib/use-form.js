/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { ref } from "vue";

export function useForm() {
  const form = ref(null);
  const valid = ref(false);
  const regExprPhone = /(\(?([\d \-)–+/(]+){6,}\)?([ .\-–/]?)([\d]+))/;
  const regExprEmail = /[a-z0-9]+@[a-z]+\.[a-z]{2,3}/;
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
    reqValidPostalcode,
    reqMultipleSelect,
    validRegex,
    validLength,
  };
}
