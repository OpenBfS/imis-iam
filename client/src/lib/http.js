/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { useApplicationStore } from "@/stores/application.js";
import axios from "axios";
import qs from "qs";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

function handleError(error) {
  // If the error has status 400 and contains at least one message it is probably
  // a validation error from keycloak which is handled in ManageUser.vue. In that
  // case we don't want to set the httpErrorMessage because we show the error right
  // below the text field in the form.
  if (error.response?.status === 400 && error.response.data?.[0]?.message) {
    return;
  }

  const applicationStore = useApplicationStore();

  if (error.response?.status === 401) {
    applicationStore.setShowSessionExpiredDialog(true);
  }

  let msg;
  if (error.response) {
    const errData = error.response.data;
    if (errData) {
      msg = errData.errorMessage ?? JSON.stringify(errData);
    } else {
      msg = error.response.statusText;
    }
    // Handle other type of errors.
  } else if (error.request) {
    msg = error.request;
  } else {
    msg = "Error" + error.message;
  }
  applicationStore.setHttpErrorMessage(msg);
}

HTTP.interceptors.response.use(
  (response) => response,
  (error) => {
    handleError(error);
    return Promise.reject(error);
  },
);

// Base URL for photon geocoder API
const PhotonHTTP = axios.create({
  baseURL: "/photon",
});

const paramsSerializer = {
  serialize: (params) => {
    return qs.stringify(params, { indices: false });
  },
};

const escapeQuotes = (text) => {
  const regex = /["]{1}/g;
  const result = text.replaceAll(regex, (match) => {
    return `\\${match}`;
  });
  return result;
};

/**
 * Creates a query parameter like this:
 * "search=search:searchTerm firstName:Jane lastName:Doe"
 * @param {string} searchTerm
 * @param {object} filters
 */
function createSearchQueryString(searchTerm, filters) {
  let searchQueryString = "";
  if (searchTerm?.length > 0) {
    searchQueryString = searchQueryString.concat(`search:${searchTerm}`);
  }
  const keys = Object.keys(filters);
  if (keys.length > 0 && searchQueryString.length > 0)
    searchQueryString = searchQueryString.concat(" ");
  for (let i = 0; i < keys.length; i++) {
    const key = keys[i];
    if (filters[key]) {
      searchQueryString = searchQueryString.concat(
        `${key}:"${escapeQuotes(filters[key])}"`
      );
      if (i < keys.length - 1) {
        searchQueryString = searchQueryString.concat(" ");
      }
    }
  }
  return searchQueryString;
}

export {
  createSearchQueryString,
  escapeQuotes,
  handleError,
  HTTP,
  PhotonHTTP,
  paramsSerializer,
};
