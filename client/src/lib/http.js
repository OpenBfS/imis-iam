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
      if (error.response.data) {
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
