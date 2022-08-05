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
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/profile")
          .then((response) => {
            commit("setUserData", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
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
