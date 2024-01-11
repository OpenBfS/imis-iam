/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { useApplicationStore } from "@/stores/application";
import i18n from "@/i18n";
const { t } = i18n.global;
import axios from "axios";

const HTTP = axios.create({
  baseURL: "/backend/realms/imis3",
});

HTTP.interceptors.response.use(
  (response) => response,
  (error) => {
    const applicationStore = useApplicationStore();
    if (error.response) {
      if (error.response.status === 400 && error.response.data[0]?.message) {
        let allMessages = "";
        for (let i = 0; i < error.response.data.length; i++) {
          const errorObject = error.response.data[i];
          const message = errorObject.message;
          const stringToTranslate = message.includes("-")
            ? message.replaceAll("-", "_")
            : message;
          allMessages = allMessages.concat(t(stringToTranslate));
          if (i < error.response.data.length - 1) {
            allMessages = allMessages.concat("; ");
          }
        }
        applicationStore.setHttpErrorMessage(allMessages);
      } else if (error.response.data) {
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
