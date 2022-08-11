/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
const attributes = {
  title: "",
  phone: "",
  mobile: "",
  fax: "",
  oe: "",
  bfsLocation: "",
  position: null,
};
const expUser = {
  firstName: "",
  lastName: "",
  email: "",
  username: "",
  groups: [],
  institutions: [],
  attributes: attributes,
};
export { expUser };
