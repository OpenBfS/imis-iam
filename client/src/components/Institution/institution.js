/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { useApplicationStore } from "@/stores/application.js";
import { useInstitutionStore } from "@/stores/institution.js";
import i18n from "@/i18n";

const { t } = i18n.global;

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
const central = { mailAddresses: null, phoneNumbers: null, centralFax: null };
const operationModeChange = {
  operationModeChangeMailAddresses: [],
  operationModeChangePhoneNumbers: [],
  operationModeChangeSmsPhoneNumbers: [],
};
const expInstitution = {
  name: "",
  measFacilName: "",
  ...central,
  ...operationModeChange,
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
        resolve(error);
      });
  });
}

const states = [
  { value: "baden_wuerttemberg" },
  { value: "bavaria" },
  { value: "berlin" },
  { value: "brandenburg" },
  { value: "bremen" },
  { value: "hamburg" },
  { value: "hesse" },
  { value: "lower_saxony" },
  { value: "mecklenburg_vorpommern" },
  { value: "north_rhine_westphalia" },
  { value: "rhineland_palatinate" },
  { value: "saarland" },
  { value: "saxony" },
  { value: "saxony_anhalt" },
  { value: "schleswig_holstein" },
  { value: "thuringia" },
].map((state) => {
  return {
    label: t(`institution.state_${state.value}`),
    value: state.value,
  };
});

export { getExpInstitution, updateInstitution, states };
