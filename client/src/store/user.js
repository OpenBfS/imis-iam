/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { HTTP } from "../lib/http";
export const user = {
  namespaced: true,
  state: () => ({
    users: [],
  }),
  mutations: {
    setUserList: (state, data) => {
      state.users = data;
    },
  },
  actions: {
    loadUsers({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser")
          .then((response) => {
            commit("setUserList", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
