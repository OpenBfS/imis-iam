/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import store from "@/store";
import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

HTTP.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      if (error.response.data) {
        store.commit("application/setHttpErrorMessage", error.response.data);
      } else {
        store.commit(
          "application/setHttpErrorMessage",
          error.response.statusText
        );
      }
      // Handle other type of errors.
    } else if (error.request) {
      store.commit("application/setHttpErrorMessage", error.request);
    } else {
      store.commit("application/setHttpErrorMessage", "Error" + error.message);
    }
    return Promise.reject(error);
  }
);

const OSMHTTP = axios.create({
  baseURL: "/",
});

export { HTTP, OSMHTTP };
