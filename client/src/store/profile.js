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
    userProfileMetadata: {},
    myMailingLists: [],
    isAllowedToManage: true,
  }),
  mutations: {
    setUserData: (state, data) => {
      state.userData = data;
    },
    setUserProfileMetadata: (state, data) => {
      state.userProfileMetadata = data;
    },
    setMyMailingLists: (state, data) => {
      state.myMailingLists = data;
    },
    setIsAllowedToManage: (state, data) => {
      state.isAllowedToManage = data;
    },
  },
  getters: {
    attributes: (state) => {
      return state.userProfileMetadata.attributes;
    },
  },
  actions: {
    loadProfile({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/profile")
          .then((response) => {
            commit("setUserData", response.data);
            const roles = response.data.roles;
            if (roles.length === 1 && ["user"].indexOf(roles[0]) !== -1) {
              commit("setIsAllowedToManage", false);
            } else {
              commit("setIsAllowedToManage", true);
            }
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadUserProfileMetadata({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/userprofilemetadata")
          .then((response) => {
            commit("setUserProfileMetadata", response.data);
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
