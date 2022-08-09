/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import Vuex from "vuex";
import { profile } from "./profile";
import { institution } from "./institution";
import { application } from "./application";
import { mail } from "./mail";
import { user } from "./user";

export default new Vuex.Store({
  modules: {
    institution,
    profile,
    user,
    application,
    mail,
  },
});
