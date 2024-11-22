/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { useApplicationStore } from "@/stores/application";
import { useInstitutionStore } from "@/stores/institution";

const serviceBuildingPlace = {
  serviceBuildingLocation: "",
  serviceBuildingPostalCode: "",
  serviceBuildingStreet: "",
  serviceBuildingState: "",
};
const addressPlace = {
  addressStreet: "",
  addressPostalCode: "",
  addressLocation: "",
};
const imis = { measFacilId: null };
const central = { centralMail: null, centralPhone: null, centralFax: null };
const alarm = { centralAlarmMailAddresses: [], centralAlarmPhoneNumbers: [] };
const expInstitution = {
  name: "",
  measFacilName: "",
  ...central,
  ...alarm,
  ...imis,
  ...serviceBuildingPlace,
  ...addressPlace,
  tags: [],
  active: false,
  /* TODO: Geocoding feature delayed to a subsequent date
  xCoordinate: "",
  yCoordinate: "",
  */
};

function getExpInstitution() {
  return structuredClone(expInstitution);
}

function updateInstitution(
  institution,
  showPostalAddress,
  isServerValidationError,
  handleValidationErrorFromServer,
  hasRequestError
) {
  const applicationStore = useApplicationStore();
  const institutionStore = useInstitutionStore();
  if (!showPostalAddress) {
    delete institution.addressLocation;
    delete institution.addressPostalCode;
    delete institution.addressStreet;
  }
  return new Promise((resolve) => {
    institutionStore
      .updateInstitution(institution)
      .then(() => {
        applicationStore.searchRequest(["institutions"]);
        applicationStore.setShowManageInstitutionDialog(false);
        resolve({ status: 200 });
      })
      .catch((error) => {
        isServerValidationError(error)
          ? handleValidationErrorFromServer(error.response.data)
          : (hasRequestError.value = true);
        console.error(error.response);
        resolve(error);
      });
  });
}

export { getExpInstitution, updateInstitution };
