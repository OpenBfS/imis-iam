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
  enabled: false,
};

function getExpUser() {
  return structuredClone(expUser);
}

export { getExpUser };
