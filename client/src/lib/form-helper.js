/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

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

export { areArraysDifferent, areObjectsDifferent, trimSpacesInObject };
