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
    return [(v) => !!v || reqMsg, (v) => regExprPhone.test(v) || validMsg];
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
    validPostalcode,
    reqValidPostalcode,
    reqMultipleSelect,
  };
}
