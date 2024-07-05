/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

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

export { getExpInstitution };
