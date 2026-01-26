/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { createSearchQueryString, HTTP } from "../lib/http.js";
import { PROCESS_TYPE, useApplicationStore } from "./application.js";

export const useUserStore = defineStore("user", {
  state: () => ({
    users: [],
    roles: null,
    foundUsers: [],
    selectedUsers: [],
    tableHeaders: [],
    selectedTableColumns: [],
    offset: 0,
    itemsPerPage: 25,
    filterBy: {},
    // Object with keys "key" and "order"
    sortBy: null,
    totalNumberOfUsers: 0,
  }),
  actions: {
    setFoundUsers(data) {
      this.foundUsers = data;
    },
    setUsers(data) {
      this.users = data;
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
    loadUsers(searchString, loadAll = false, abortController = undefined) {
      return new Promise((resolve, reject) => {
        const params = loadAll
          ? {}
          : {
              search: createSearchQueryString(searchString, this.filterBy),
              firstResult: this.offset,
              maxResults:
                this.itemsPerPage !== -1 ? this.itemsPerPage : undefined,
              sortBy: this.sortBy?.key,
              order: this.sortBy?.order,
            };
        HTTP.get("/iam/user", {
          params,
          signal: abortController?.signal,
        })
          .then((response) => {
            this.totalNumberOfUsers = response.data.size;
            if (loadAll) {
              this.setUsers(response.data.list);
            } else {
              this.setFoundUsers(response.data.list);
            }
            resolve(response);
          })
          .catch((error) => {
            // We cancel search requests for users and institutions if a new request is made on purpose.
            // This error is very likely not a problem.
            if (error.code === "ERR_CANCELED") {
              resolve();
            }
            reject(error);
          });
      });
    },
    loadRoles() {
      return new Promise((resolve, reject) => {
        HTTP.get("iam/user/roles")
          .then((response) => {
            this.setRoles(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    updateUser(user) {
      const applicationStore = useApplicationStore();
      return new Promise((resolve, reject) => {
        const endpoint =
          applicationStore.processType === PROCESS_TYPE.EDIT_PROFILE
            ? "/iam/user/profile"
            : "iam/user";
        HTTP.put(endpoint, user)
          .then((response) => {
            //Update the stored entity using response data
            this.updateUserEntity(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    addUser(data) {
      this.users.push(data);
      this.totalNumberOfUsers++;
    },
    removeUser(data) {
      const index = this.users.findIndex((user) => user.id === data.id);
      this.users.splice(index, 1);
      this.totalNumberOfUsers--;
    },
    updateFilter(key, term) {
      if (term == null || term.length === 0) {
        delete this.filterBy[key];
      } else if (!term.length || term.length > 0) {
        this.filterBy[key] = term;
      }
    },
  },
});
