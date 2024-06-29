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
};
const addressPlace = {
  addressStreet: "",
  addressPostalCode: "",
  addressLocation: "",
};
const imis = { imisId: null };
const central = { centralMail: "", centralPhone: "", centralFax: "" };
const alarm = { centralAlarmEmailAddresses: [], centralAlarmPhoneNumbers: [] };
const expInstitution = {
  name: "",
  shortName: "",
  ...central,
  ...alarm,
  ...imis,
  ...serviceBuildingPlace,
  ...addressPlace,
  categoryNames: [],
  active: false,
  xCoordinate: "",
  yCoordinate: "",
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
  institutionStore
    .updateInstitution(institution)
    .then(() => {
      applicationStore.searchRequest(["institutions"]);
      applicationStore.setShowManageInstitutionDialog(false);
      return true;
    })
    .catch((error) => {
      isServerValidationError(error)
        ? handleValidationErrorFromServer(error.response.data)
        : (hasRequestError.value = true);
      return false;
    });
}

export { getExpInstitution, updateInstitution };
