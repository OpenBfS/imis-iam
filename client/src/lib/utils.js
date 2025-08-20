/* Copyright (C) 2025 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import i18n from "@/i18n";
import { useInstitutionStore } from "@/stores/institution";
import { useUserStore } from "@/stores/user.js";

const { t } = i18n.global;

// Creates headers that can be inserted into a Vuetify table.
const createHeaders = (columns, type) => {
  const userStore = useUserStore();
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
      if (type === "institutions" && headerName === "serviceBuildingState") {
        const state = structuredClone(item)[headerName];
        const capitalizedState = state[0].toUpperCase() + state.slice(1);
        return t(`institution.state${capitalizedState}`);
      } else if (
        type === "users" &&
        headerName === "role" &&
        structuredClone(item)?.[headerName]
      ) {
        const roleName = structuredClone(item)[headerName];
        const roles = userStore.roles;
        const role = roles.find((r) => r.name === roleName);
        if (role) {
          return t(role.description);
        } else {
          return structuredClone(item)?.[headerName];
        }
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

const initSelectedColumns = (type) => {
  const store = type === "users" ? useUserStore() : useInstitutionStore();
  store.selectedTableColumns = store.tableHeaders
    .filter((header) => header.visible === true)
    .map((header) => {
      return header.key;
    });
};

const deselectAllColumns = (type) => {
  const store = type === "users" ? useUserStore() : useInstitutionStore();
  store.selectedTableColumns = [];
  store.tableHeaders.forEach((header) => {
    header.visible = false;
  });
};

const selectAllColumns = (type) => {
  const store = type === "users" ? useUserStore() : useInstitutionStore();
  store.selectedTableColumns = store.tableHeaders.map((header) => {
    header.visible = true;
    return header.key;
  });
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

export {
  createHeaders,
  initSelectedColumns,
  deselectAllColumns,
  selectAllColumns,
};
