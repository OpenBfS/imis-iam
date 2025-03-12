/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import i18n from "@/i18n";

const { t } = i18n.global;

// Creates headers that can be inserted into a Vuetify table.
const createHeaders = (columns, type) => {
  const newHeaders = [];
  columns.forEach((column) => {
    const headerName = column.name;
    const translationPrefix = type === "users" ? "user" : "institution";
    const translationKey =
      headerName === "name"
        ? "label.name"
        : `${translationPrefix}.${headerName}`;

    const header = {
      title: t(translationKey),
      key: headerName,
      sortable: type !== "users",
      visible: column.default,
    };
    // This function decides how the values inside an item are displayed in the v-data-server-table.
    header.value = (item) => {
      if (
        type === "users" &&
        headerName === "role" &&
        structuredClone(item)?.[headerName]
      ) {
        return t(`role_iam_${structuredClone(item)[headerName]}`);
      }
      const value =
        type === "users"
          ? structuredClone(item.attributes[headerName] ?? item[headerName])
          : item[headerName];
      return createLabelForTableCell(structuredClone(value), t);
    };
    newHeaders.push(header);
  });
  return newHeaders;
};

// Format and translate values where necessary.
function createLabelForTableCell(value) {
  if (value === undefined) return;

  // While other attributes of users and institutions are translated booleans are not. We translate them here
  // so all values in the table are translated.
  if (typeof value === "boolean") {
    return t(`${value}`);
  } else if (Array.isArray(value)) {
    return value.join(", ");
  }
  return value;
}

export { createHeaders };
