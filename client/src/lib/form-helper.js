/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import i18n from "@/i18n";

const { t } = i18n.global;

const regExprPhone = /^\+[1-9][0-9]{7,16}$/;
const regExprEmail = /^\S+@\S+\.\S+$/;
const regExprPostalCode = /^\d{5}$/;
const noLeadingTrailingSpaces = /\S.*\S/;
const germanDateRegex = /[\d]{1,2}\.[\d]{1,2}\.[\d]{4}/;

// Validation rules
const validMail = (validMsg) => {
  return [(v) => !v || doesRegexMatchWholeString(regExprEmail, v) || validMsg];
};
const validPhone = (validMsg) => {
  return [(v) => !v || doesRegexMatchWholeString(regExprPhone, v) || validMsg];
};
const validPostalcode = (validMsg) => {
  return [(v) => regExprPostalCode.test(v) || v == "" || validMsg];
};

const doesRegexMatchWholeString = (regex, text) => {
  const matches = regex.exec(text);
  return matches && matches[0] === text;
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

const areArraysDifferent = (a, b, sameOrder = true) => {
  let sameElementsCount = 0;
  if (a.length !== b.length) return true;
  for (var i = 0; i < a.length; ++i) {
    if (sameOrder && a[i] !== b[i]) {
      return true;
    } else if (!sameOrder) {
      const index = b.findIndex((element) => element === a[i]);
      if (index != -1) sameElementsCount++;
    }
  }
  if (!sameOrder && a.length !== sameElementsCount) {
    return true;
  }
  return false;
};

const trimSpacesInObject = (obj) => {
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

const areObjectsDifferent = (originalA, originalB) => {
  if (originalA === undefined && originalB === undefined) return false;
  const a = structuredClone(originalA);
  const b = structuredClone(originalB);
  if (Array.isArray(a) && Array.isArray(b)) return areArraysDifferent(a, b);
  const allKeys = [];
  const addKeys = (obj) => {
    for (let i = 0; i < Object.keys(obj).length; i++) {
      const key = Object.keys(obj)[i];
      if (!allKeys.includes(key)) allKeys.push(key);
    }
  };
  addKeys(a);
  addKeys(b);
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

export {
  areArraysDifferent,
  areObjectsDifferent,
  trimSpacesInObject,
  dateStringToDate,
  validMail,
  reqField,
  validPhone,
  validPostalcode,
  validGermanDate,
  validLength,
  validRegex,
  doesRegexMatchWholeString,
  noLeadingTrailingSpaces,
  createRequiredRule,
  germanDateRegex,
};
