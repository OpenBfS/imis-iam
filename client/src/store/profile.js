/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */

import { HTTP } from "../lib/http";
export const profile = {
  namespaced: true,
  state: () => ({
    userData: {},
    myMailingLists: [],
  }),
  mutations: {
    setUserData: (state, data) => {
      state.userData = data;
    },
    setMyMailingLists: (state, data) => {
      state.myMailingLists = data;
    },
  },
  actions: {
    loadProfile({ commit }) {
      HTTP.get("/iamuser/profile").then((response) => {
        if (response.status == 200) {
          commit("setUserData", response.data);
        }
      });
    },
    storeProfile(context) {
      HTTP.put("/iamuser/profile", context.state.userData);
    },
    getMyMailingLists({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("mail/list?subscribed=true")
          .then((response) => {
            commit("setMyMailingLists", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
