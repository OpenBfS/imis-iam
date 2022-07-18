/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { HTTP } from "../lib/http";
export const mail = {
  namespaced: true,
  state: () => ({
    mailTypes: [],
  }),
  mutations: {
    setMailTypes: (state, data) => {
      state.mailTypes = data;
    },
  },
  actions: {
    loadMailTypes({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("mail/type")
          .then((response) => {
            commit("setMailTypes", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
