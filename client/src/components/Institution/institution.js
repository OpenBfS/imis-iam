/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

const serviceBuildingPlace = { SBStreet: "", SBPostalCode: "", SBLocation: "" };
const addressPlace = { AStreet: "", APostalCode: "", ALocation: "" };
const imis = { id: "", mail: "" };
const central = { CPhone: "", CMail: "", CFax: "" };
const expInstitution = {
  name: "",
  shortName: "",
  ...central,
  ...imis,
  ...serviceBuildingPlace,
  ...addressPlace,
  categories: [],
  active: false,
  xCoordinate: "",
  yCoordinate: "",
};

export { expInstitution };
