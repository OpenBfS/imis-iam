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
  // Validation rules
  const reqValidmail = (reqMsg, validMsg) => {
    return [
      (v) => !!v || reqMsg,
      (v) => /[a-z0-9]+@[a-z]+\.[a-z]{2,3}/.test(v) || validMsg,
    ];
  };
  const validMail = (validMsg) => {
    return [
      (v) => /[a-z0-9]+@[a-z]+\.[a-z]{2,3}/.test(v) || v == "" || validMsg,
    ];
  };
  const reqValidPhone = (reqMsg, validMsg) => {
    return [
      (v) => !!v || reqMsg,
      (v) => /^[+]?\d{2,3}[-\s.]?\d{3,4}[-\s.]?\d{6,}/.test(v) || validMsg,
    ];
  };
  const validPhone = (validMsg) => {
    (v) => /^[+]?\d{2,3}[-\s.]?\d{3,4}[-\s.]?\d{6,}/.test(v) || validMsg;
  };
  const validFax = (validMsg) => {
    return [
      (v) =>
        /^(\+?\d{1,}(\s?|-?)\d*(\s?|-?)\(?\d{2,}\)?(\s?|-?)\d{3,}\s?\d{3,})$/.test(
          v
        ) ||
        v == "" ||
        validMsg,
    ];
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
    return [(v) => !!v || reqMsg];
  };
  const reqMultipleSelect = (reqMsg) => {
    return [(v) => !!(v && v.length) || reqMsg];
  };
  return {
    form,
    valid,
    reqValidmail,
    validMail,
    reqField,
    reqValidPhone,
    validPhone,
    validFax,
    validPostalcode,
    reqValidPostalcode,
    reqMultipleSelect,
  };
}
