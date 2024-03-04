/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http";

export const useUserStore = defineStore("user", {
  namespaced: true,
  state: () => ({
    users: [],
    memberships: [],
    roles: [],
    foundUsers: [],
  }),
  actions: {
    setFoundUsers(data) {
      this.foundUsers = data;
    },
    setUsers(data) {
      this.users = data;
    },
    setMemberships(data) {
      this.memberships = data;
    },
    setRoles(data) {
      this.roles = data;
    },
    updateUserEntity(data) {
      this.users.forEach((element, index) => {
        if (element.id === data.id) {
          this.users[index] = data;
        }
      });
    },
    loadMemberships() {
      return new Promise((resolve, reject) => {
        HTTP.get("iamuser/membership")
          .then((response) => {
            this.setMemberships(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadUsers(searchString) {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser", {
          params: { search: searchString },
        })
          .then((response) => {
            this.setUsers(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadRoles() {
      return new Promise((resolve, reject) => {
        HTTP.get("iamuser/roles")
          .then((response) => {
            this.setRoles(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateUser(user) {
      return new Promise((resolve, reject) => {
        HTTP.put("iamuser", user)
          .then((response) => {
            //Update the stored entity using repsonse data
            this.updateUserEntity(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    addUser(data) {
      this.users.push(data);
    },
    removeUser(data) {
      const index = this.users.findIndex((user) => user.id === data.id);
      this.users.splice(index, 1);
    },
  },
});
