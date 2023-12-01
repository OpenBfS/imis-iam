/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
const expUser = {
  attributes: {},
  groups: [],
  institutions: [],
  roles: [],
  // TODO: fetch from server
  userProfileMetadata: {
    attributes: [
      {
        name: "position",
        validations: {
          options: {
            options: [
              "Leiter",
              "Vertreter",
              "Radiologischer Ansprechpartner",
              "Mitarbeiter",
              "IT-Ansprechperson",
              "Störplatz",
            ],
          },
        },
      },
    ],
  },
};
export { expUser };
