/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { useApplicationStore } from "@/stores/application";
import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

HTTP.interceptors.response.use(
  (response) => response,
  (error) => {
    const applicationStore = useApplicationStore();
    if (error.response) {
      // If the error has status 400 and contains at least one message it is probably
      // a validation error from keycloak which is handled in ManageUser.vue. In that
      // case we don't want to set the httpErrorMessage because we show the error right
      // below the text field in the form.
      if (
        error.response.data &&
        !(error.response.status === 400 && error.response.data[0]?.message)
      ) {
        applicationStore.setHttpErrorMessage(error.response.data);
      } else {
        applicationStore.setHttpErrorMessage(error.response.statusText);
      }
      // Handle other type of errors.
    } else if (error.request) {
      applicationStore.setHttpErrorMessage(error.request);
    } else {
      applicationStore.setHttpErrorMessage("Error" + error.message);
    }
    return Promise.reject(error);
  }
);

// Base URL for photon geocoder API
const PhotonHTTP = axios.create({
  baseURL: "/photon",
});

export { HTTP, PhotonHTTP };
