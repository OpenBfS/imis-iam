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
    memberships: [],
    roles: [],
    foundUsers: [],
  }),
  mutations: {
    setFoundUsers: (state, data) => {
      state.foundUsers = data;
    },
    setUsers: (state, data) => {
      state.users = data;
    },
    setMemberships: (state, data) => {
      state.memberships = data;
    },
    setRoles: (state, data) => {
      state.roles = data;
    },
    updateUserEntity: (state, data) => {
      state.users.forEach((element, index) => {
        if (element.id === data.id) {
          state.users[index] = data;
        }
      });
    },
  },
  actions: {
    loadMemberships({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("iamuser/membership")
          .then((response) => {
            commit("setMemberships", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadUsers({ commit }, searchString) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser", {
          params: { search: searchString },
        })
          .then((response) => {
            commit("setUsers", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadRoles({ commit }) {
      return new Promise((resolve, reject) => {
        HTTP.get("iamuser/roles")
          .then((response) => {
            commit("setRoles", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateUser({ commit }, user) {
      return new Promise((resolve, reject) => {
        HTTP.put("iamuser", user)
          .then((response) => {
            //Update the stored entity using repsonse data
            commit("updateUserEntity", response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
};
