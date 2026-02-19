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

export { areArraysDifferent, trimSpacesInObject };
