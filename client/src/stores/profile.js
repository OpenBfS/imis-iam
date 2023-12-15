/* Copyright (C) 2022 by Bundesamt fuer Strahlenschutz
 * Software engineering by Intevation GmbH
 *
 * This file is Free Software under the GNU GPL (v>=3)
 * and comes with ABSOLUTELY NO WARRANTY!
 */
import { defineStore } from "pinia";
import { HTTP } from "../lib/http";

export const useProfileStore = defineStore("profile", {
  namespaced: true,
  state: () => ({
    userData: {},
    userProfileMetadata: {},
    myMailingLists: [],
    isAllowedToManage: true,
  }),
  getters: {
    attributes: (state) => {
      return state.userProfileMetadata.attributes;
    },
    attributeGroups: (state) => {
      return state.userProfileMetadata.groups;
    },
    attributesWithoutGroup: (state) => {
      return state.userProfileMetadata.attributes?.filter(
        (attribute) => !attribute.group
      );
    },
  },
  actions: {
    setUserData(data) {
      this.$patch({
        userData: data,
      });
    },
    setUserProfileMetadata(data) {
      this.$patch({
        userProfileMetadata: data,
      });
    },
    setMyMailingLists(data) {
      this.$patch({
        myMailingLists: data,
      });
    },
    setIsAllowedToManage(data) {
      this.$patch({
        isAllowedToManage: data,
      });
    },
    loadProfile() {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/profile")
          .then((response) => {
            this.setUserData(response.data);
            const roles = response.data.roles;
            if (roles.length === 1 && ["user"].indexOf(roles[0]) !== -1) {
              this.setIsAllowedToManage(false);
            } else {
              this.setIsAllowedToManage(true);
            }
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    loadUserProfileMetadata() {
      return new Promise((resolve, reject) => {
        HTTP.get("/iamuser/userprofilemetadata")
          .then((response) => {
            this.setUserProfileMetadata(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
    getMyMailingLists() {
      return new Promise((resolve, reject) => {
        HTTP.get("mail/list?subscribed=true")
          .then((response) => {
            this.setMyMailingLists(response.data);
            resolve(response);
          })
          .catch((error) => reject(error));
      });
    },
  },
});
