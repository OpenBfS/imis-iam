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
};
const addressPlace = {
  addressStreet: "",
  addressPostalCode: "",
  addressLocation: "",
};
const imis = { imisId: "", imisMail: "" };
const central = { centralMail: "", centralPhone: "", centralFax: "" };
const expInstitution = {
  name: "",
  shortName: "",
  ...central,
  ...imis,
  ...serviceBuildingPlace,
  ...addressPlace,
  category: "",
  active: false,
  xCoordinate: "",
  yCoordinate: "",
};

export { expInstitution };
